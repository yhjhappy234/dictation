package com.yhj.dictation.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.service.AuditLogService;
import com.yhj.dictation.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 审计日志切面
 * 拦截带有 @AuditLog 注解的方法，异步记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    /**
     * 拦截所有带有 @AuditLog 注解的方法
     */
    @Around("@annotation(com.yhj.dictation.annotation.AuditLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AuditLog annotation = signature.getMethod().getAnnotation(AuditLog.class);

        // 获取用户信息（方法执行前，用于登出等操作）
        Long userIdBefore = UserContext.getCurrentUserId();
        String usernameBefore = UserContext.getCurrentUsername();

        // 获取IP地址
        String ipAddress = getIpAddress();

        // 获取方法名
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();

        // 获取参数
        String params = null;
        if (annotation.recordParams()) {
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    // 过滤掉 HttpServletRequest/Response 等对象
                    Object[] filteredArgs = java.util.Arrays.stream(args)
                            .filter(arg -> arg != null &&
                                    !(arg instanceof HttpServletRequest) &&
                                    !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                            .toArray();
                    if (filteredArgs.length > 0) {
                        params = objectMapper.writeValueAsString(filteredArgs);
                        // 限制参数长度
                        if (params.length() > 500) {
                            params = params.substring(0, 500) + "...";
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("序列化参数失败: {}", e.getMessage());
                params = "参数序列化失败";
            }
        }

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;
        Object result = null;

        try {
            // 执行原方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // 记录结束时间
            long endTime = System.currentTimeMillis();
            long durationMs = endTime - startTime;

            // 方法执行后重新获取用户信息（登录操作在执行后才设置用户信息）
            Long userIdAfter = UserContext.getCurrentUserId();
            String usernameAfter = UserContext.getCurrentUsername();

            // 优先使用执行后的用户信息（登录操作），如果为空则使用执行前的信息（登出操作）
            Long userId = userIdAfter != null ? userIdAfter : userIdBefore;
            String username = usernameAfter != null ? usernameAfter : usernameBefore;

            // 获取结果
            String resultStr = null;
            if (annotation.recordResult() && result != null) {
                try {
                    resultStr = objectMapper.writeValueAsString(result);
                    // 限制结果长度
                    if (resultStr.length() > 500) {
                        resultStr = resultStr.substring(0, 500) + "...";
                    }
                } catch (Exception e) {
                    log.warn("序列化结果失败: {}", e.getMessage());
                    resultStr = "结果序列化失败";
                }
            }

            // 构建审计日志对象
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setUserId(userId);
            auditLog.setUsername(username != null ? username : "anonymous");
            auditLog.setOperation(annotation.operation());
            auditLog.setMethod(methodName);
            auditLog.setParams(params);
            auditLog.setResult(resultStr);
            auditLog.setIpAddress(ipAddress);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setDurationMs(durationMs);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);

            // 异步保存日志（不阻塞主流程）
            auditLogService.saveLogAsync(auditLog);
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                // 处理多IP的情况（X-Forwarded-For可能包含多个IP）
                if (ip != null && ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                // 将IPv6格式的localhost转换为IPv4格式
                if (ip != null && "0:0:0:0:0:0:0:1".equals(ip)) {
                    ip = "127.0.0.1";
                }
                return ip;
            }
        } catch (Exception e) {
            log.warn("获取IP地址失败: {}", e.getMessage());
        }
        return null;
    }
}