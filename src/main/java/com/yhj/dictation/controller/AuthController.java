package com.yhj.dictation.controller;

import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.LoginRequest;
import com.yhj.dictation.dto.UserInfoDTO;
import com.yhj.dictation.entity.User;
import com.yhj.dictation.service.UserService;
import com.yhj.dictation.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、登出、状态检查等接口")
public class AuthController {

    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @AuditLog(operation = "用户登录", recordParams = true, recordResult = false)
    public ApiResponse<UserInfoDTO> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ApiResponse.error("密码不能为空");
        }

        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }

        // 保存用户信息到 Session
        UserContext.setCurrentUser(user.getId(), user.getUsername(), user.getRole().name(), user.getAvatar());

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setStatus(user.getStatus().name());
        userInfo.setRole(user.getRole().name());
        userInfo.setAvatar(user.getAvatar());

        log.info("用户登录成功: {}, 角色: {}", user.getUsername(), user.getRole());
        return ApiResponse.success("登录成功", userInfo);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录状态")
    @AuditLog(operation = "用户登出", recordParams = false)
    public ApiResponse<Void> logout(HttpSession session) {
        String username = UserContext.getCurrentUsername();
        UserContext.clearCurrentUser();
        log.info("用户登出: {}", username);
        return ApiResponse.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户的信息")
    public ApiResponse<UserInfoDTO> getCurrentUser() {
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();

        if (userId == null || username == null) {
            return ApiResponse.error("未登录");
        }

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(userId);
        userInfo.setUsername(username);
        userInfo.setRole(UserContext.getCurrentUserRole());
        userInfo.setAvatar(UserContext.getCurrentUserAvatar());

        // 获取完整用户信息
        userService.getUserById(userId).ifPresent(user -> {
            userInfo.setStatus(user.getStatus().name());
        });

        return ApiResponse.success(userInfo);
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查登录状态", description = "检查用户是否已登录")
    public ApiResponse<Boolean> checkLoginStatus() {
        boolean isLoggedIn = UserContext.isLoggedIn();
        return ApiResponse.success(isLoggedIn);
    }

    /**
     * 获取所有可用头像列表
     */
    @GetMapping("/avatars")
    @Operation(summary = "获取头像列表", description = "获取系统内置的所有头像文件列表")
    public ApiResponse<java.util.List<String>> getAllAvatars() {
        return ApiResponse.success(userService.getAllAvatars());
    }
}