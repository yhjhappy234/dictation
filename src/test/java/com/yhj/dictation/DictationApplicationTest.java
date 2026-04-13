package com.yhj.dictation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DictationApplication 单元测试
 */
class DictationApplicationTest {

    @Test
    @DisplayName("创建 DictationApplication 实例")
    void createInstance() {
        DictationApplication app = new DictationApplication();
        assertNotNull(app);
    }

    @Test
    @DisplayName("main 方法存在")
    void mainMethodExists() {
        // 验证 main 方法可以调用（不实际启动应用）
        // 使用反射验证方法存在
        try {
            var method = DictationApplication.class.getMethod("main", String[].class);
            assertNotNull(method);
        } catch (NoSuchMethodException e) {
            fail("main 方法不存在");
        }
    }
}