package com.yhj.dictation.config;

import com.yhj.dictation.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 配置认证拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/login",                    // 登录页面
                        "/api/auth/login",           // 登录 API
                        "/api/auth/status",          // 登录状态检查
                        "/api/preset/**",            // 预设内容 API
                        "/static/**",                // 静态资源
                        "/css/**",                   // CSS
                        "/js/**",                    // JavaScript
                        "/images/**",                // 图片
                        "/favicon.ico",              // 图标
                        "/error"                     // 错误页面
                );
    }
}