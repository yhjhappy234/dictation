package com.yhj.dictation.config;

import com.yhj.dictation.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 应用启动时自动初始化默认用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;

    @Value("${app.default.username}")
    private String defaultUsername;

    @Value("${app.default.password}")
    private String defaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始数据初始化...");

        // 初始化默认用户
        initDefaultUser();

        log.info("数据初始化完成");
    }

    /**
     * 初始化默认用户
     */
    private void initDefaultUser() {
        try {
            userService.initDefaultUser(defaultUsername, defaultPassword);
            log.info("默认用户初始化完成");
        } catch (Exception e) {
            log.error("初始化默认用户失败: {}", e.getMessage(), e);
        }
    }
}