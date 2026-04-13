package com.yhj.dictation.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 从 Session 中获取当前登录用户的信息
 */
public class UserContext {

    private static final String USER_ID_SESSION_KEY = "userId";
    private static final String USERNAME_SESSION_KEY = "username";
    private static final String USER_ROLE_SESSION_KEY = "userRole";
    private static final String USER_AVATAR_SESSION_KEY = "userAvatar";

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpSession session = getSession();
        if (session != null) {
            Object userId = session.getAttribute(USER_ID_SESSION_KEY);
            if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        HttpSession session = getSession();
        if (session != null) {
            Object username = session.getAttribute(USERNAME_SESSION_KEY);
            if (username instanceof String) {
                return (String) username;
            }
        }
        return null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        HttpSession session = getSession();
        if (session != null) {
            Object role = session.getAttribute(USER_ROLE_SESSION_KEY);
            if (role instanceof String) {
                return (String) role;
            }
        }
        return null;
    }

    /**
     * 获取当前用户头像
     */
    public static String getCurrentUserAvatar() {
        HttpSession session = getSession();
        if (session != null) {
            Object avatar = session.getAttribute(USER_AVATAR_SESSION_KEY);
            if (avatar instanceof String) {
                return (String) avatar;
            }
        }
        return null;
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * 设置当前用户信息到Session
     */
    public static void setCurrentUser(Long userId, String username, String role, String avatar) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(USER_ID_SESSION_KEY, userId);
            session.setAttribute(USERNAME_SESSION_KEY, username);
            session.setAttribute(USER_ROLE_SESSION_KEY, role);
            session.setAttribute(USER_AVATAR_SESSION_KEY, avatar);
        }
    }

    /**
     * 更新当前用户头像
     */
    public static void updateAvatar(String avatar) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(USER_AVATAR_SESSION_KEY, avatar);
        }
    }

    /**
     * 清除当前用户信息（登出）
     */
    public static void clearCurrentUser() {
        HttpSession session = getSession();
        if (session != null) {
            session.removeAttribute(USER_ID_SESSION_KEY);
            session.removeAttribute(USERNAME_SESSION_KEY);
            session.removeAttribute(USER_ROLE_SESSION_KEY);
            session.removeAttribute(USER_AVATAR_SESSION_KEY);
            session.invalidate();
        }
    }

    /**
     * 检查是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUserId() != null && getCurrentUsername() != null;
    }

    /**
     * 获取当前Session
     */
    private static HttpSession getSession() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getSession(false);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    /**
     * 获取当前请求
     */
    public static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }
}