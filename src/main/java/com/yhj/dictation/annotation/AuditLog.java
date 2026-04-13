package com.yhj.dictation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计日志注解
 * 标注在方法上，AOP切面会自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 操作描述
     */
    String operation();

    /**
     * 日志级别
     */
    LogLevel level() default LogLevel.NORMAL;

    /**
     * 是否记录参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录结果
     */
    boolean recordResult() default false;

    /**
     * 日志级别枚举
     */
    enum LogLevel {
        NORMAL,     // 普通操作
        IMPORTANT,  // 重要操作（如删除）
        SENSITIVE   // 敏感操作（如密码修改）
    }
}