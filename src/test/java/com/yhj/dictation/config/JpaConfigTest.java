package com.yhj.dictation.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JpaConfig 单元测试
 */
class JpaConfigTest {

    @Test
    @DisplayName("创建 JpaConfig 实例")
    void createInstance() {
        JpaConfig config = new JpaConfig();
        assertNotNull(config);
    }
}