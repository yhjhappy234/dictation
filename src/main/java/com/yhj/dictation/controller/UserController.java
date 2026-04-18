package com.yhj.dictation.controller;

import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.UserInfoDTO;
import com.yhj.dictation.dto.UserCreateRequest;
import com.yhj.dictation.dto.UserUpdateRequest;
import com.yhj.dictation.dto.PasswordUpdateRequest;
import com.yhj.dictation.entity.User;
import com.yhj.dictation.service.UserService;
import com.yhj.dictation.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户创建、查询、更新、删除等接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取所有用户（仅管理员）
     */
    @GetMapping
    @Operation(summary = "获取所有用户", description = "获取系统所有用户列表（仅管理员可访问）")
    public ApiResponse<List<UserInfoDTO>> getAllUsers() {
        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以查看用户列表");
        }

        List<User> users = userService.getAllUsers();
        List<UserInfoDTO> userDTOs = users.stream()
                .map(this::toUserInfoDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(userDTOs);
    }

    /**
     * 根据ID获取用户（仅管理员）
     */
    @GetMapping("/{id}")
    public ApiResponse<UserInfoDTO> getUserById(@PathVariable Long id) {
        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以查看用户详情");
        }

        return userService.getUserById(id)
                .map(this::toUserInfoDTO)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("用户不存在: " + id));
    }

    /**
     * 创建用户（仅管理员）
     */
    @PostMapping
    @AuditLog(operation = "创建用户", level = AuditLog.LogLevel.IMPORTANT, recordParams = true)
    public ApiResponse<UserInfoDTO> createUser(@RequestBody UserCreateRequest request) {
        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以创建用户");
        }

        try {
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ApiResponse.error("用户名不能为空");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ApiResponse.error("密码不能为空");
            }

            User.UserRole role = User.UserRole.USER;
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                try {
                    role = User.UserRole.valueOf(request.getRole().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ApiResponse.error("无效的角色: " + request.getRole());
                }
            }

            User user = userService.createUser(request.getUsername(), request.getPassword(), role);
            return ApiResponse.success("用户创建成功", toUserInfoDTO(user));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ApiResponse.error("创建用户失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户（仅管理员）
     */
    @PutMapping("/{id}")
    @AuditLog(operation = "更新用户信息", level = AuditLog.LogLevel.IMPORTANT, recordParams = true)
    public ApiResponse<UserInfoDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以更新用户");
        }

        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + id));

            // 更新角色
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                try {
                    User.UserRole role = User.UserRole.valueOf(request.getRole().toUpperCase());
                    userService.updateRole(id, role);
                } catch (IllegalArgumentException e) {
                    return ApiResponse.error("无效的角色: " + request.getRole());
                }
            }

            // 更新状态
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                try {
                    User.UserStatus status = User.UserStatus.valueOf(request.getStatus().toUpperCase());
                    if (status == User.UserStatus.ACTIVE) {
                        userService.enableUser(id);
                    } else {
                        userService.disableUser(id);
                    }
                } catch (IllegalArgumentException e) {
                    return ApiResponse.error("无效的状态: " + request.getStatus());
                }
            }

            // 更新头像
            if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
                userService.updateAvatar(id, request.getAvatar());
            }

            User updatedUser = userService.getUserById(id).orElse(user);
            return ApiResponse.success("用户更新成功", toUserInfoDTO(updatedUser));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ApiResponse.error("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户（仅管理员）
     */
    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除用户", level = AuditLog.LogLevel.IMPORTANT, recordParams = true)
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ApiResponse.error("权限不足，只有管理员可以删除用户");
        }

        // 不能删除自己
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            return ApiResponse.error("不能删除自己");
        }

        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ApiResponse.error("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 修改自己的密码（所有用户）
     */
    @PostMapping("/me/password")
    @AuditLog(operation = "修改密码", level = AuditLog.LogLevel.SENSITIVE, recordParams = false)
    public ApiResponse<Void> updateMyPassword(@RequestBody PasswordUpdateRequest request) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }

        try {
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ApiResponse.error("新密码不能为空");
            }

            userService.updatePassword(userId, request.getNewPassword());
            return ApiResponse.success("密码修改成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ApiResponse.error("修改密码失败: " + e.getMessage());
        }
    }

    /**
     * 修改自己的头像（所有用户）
     */
    @PostMapping("/me/avatar")
    @AuditLog(operation = "修改头像", recordParams = true)
    public ApiResponse<UserInfoDTO> updateMyAvatar(@RequestParam String avatar) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error("未登录");
        }

        try {
            User user = userService.updateAvatar(userId, avatar);
            // 更新 Session 中的头像
            UserContext.updateAvatar(avatar);
            return ApiResponse.success("头像修改成功", toUserInfoDTO(user));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("修改头像失败", e);
            return ApiResponse.error("修改头像失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户是否是管理员
     */
    @GetMapping("/me/is-admin")
    public ApiResponse<Boolean> isAdmin() {
        boolean isAdmin = UserContext.isAdmin();
        return ApiResponse.success(isAdmin);
    }

    private UserInfoDTO toUserInfoDTO(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setStatus(user.getStatus().name());
        dto.setRole(user.getRole().name());
        dto.setAvatar(user.getAvatar());
        return dto;
    }
}