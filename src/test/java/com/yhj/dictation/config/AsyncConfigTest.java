package com.yhj.dictation.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AsyncConfig 单元测试
 */
class AsyncConfigTest {

    @Test
    @DisplayName("创建 AsyncConfig 实例")
    void createInstance() {
        AsyncConfig config = new AsyncConfig();
        assertNotNull(config);
    }
}