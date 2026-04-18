package com.yhj.dictation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SystemController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SystemControllerTest {

    @InjectMocks
    private SystemController systemController;

    @Nested
    @DisplayName("getVersion 方法测试")
    class GetVersionTests {

        @Test
        @DisplayName("获取版本信息成功")
        void getVersion_success() {
            var result = systemController.getVersion();

            assertNotNull(result);
            assertTrue(result.isSuccess());
            assertNotNull(result.getData());
            assertEquals("v1.3.0", result.getData().getVersion());
            assertNotNull(result.getData().getTimestamp());
        }

        @Test
        @DisplayName("版本信息包含正确字段")
        void getVersion_containsCorrectFields() {
            var result = systemController.getVersion();
            var versionInfo = result.getData();

            assertNotNull(versionInfo.getVersion());
            assertTrue(versionInfo.getTimestamp() > 0);
        }

        @Test
        @DisplayName("版本号格式正确")
        void getVersion_correctFormat() {
            var result = systemController.getVersion();
            var version = result.getData().getVersion();

            assertTrue(version.startsWith("v"));
            assertTrue(version.matches("^v\\d+\\.\\d+\\.\\d+$"));
        }

        @Test
        @DisplayName("时间戳为当前时间")
        void getVersion_currentTimestamp() {
            long beforeCall = System.currentTimeMillis();
            var result = systemController.getVersion();
            long afterCall = System.currentTimeMillis();

            long timestamp = result.getData().getTimestamp();
            assertTrue(timestamp >= beforeCall);
            assertTrue(timestamp <= afterCall);
        }
    }

    @Nested
    @DisplayName("VersionInfo 类测试")
    class VersionInfoTests {

        @Test
        @DisplayName("创建VersionInfo实例")
        void createVersionInfo() {
            SystemController.VersionInfo info = new SystemController.VersionInfo();
            assertNotNull(info);
        }

        @Test
        @DisplayName("设置和获取版本")
        void setAndGetVersion() {
            SystemController.VersionInfo info = new SystemController.VersionInfo();
            info.setVersion("v1.0.0");
            assertEquals("v1.0.0", info.getVersion());
        }

        @Test
        @DisplayName("设置和获取时间戳")
        void setAndGetTimestamp() {
            SystemController.VersionInfo info = new SystemController.VersionInfo();
            info.setTimestamp(123456789L);
            assertEquals(123456789L, info.getTimestamp());
        }
    }
}