package com.yhj.dictation.config;

import com.yhj.dictation.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * DataInitializer 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Mock
    private ApplicationArguments args;

    @Nested
    @DisplayName("run 方法测试")
    class RunTests {

        @Test
        @DisplayName("初始化默认用户成功")
        void run_success() {
            // 设置配置值
            ReflectionTestUtils.setField(dataInitializer, "defaultUsername", "admin");
            ReflectionTestUtils.setField(dataInitializer, "defaultPassword", "password");

            // 默认用户不存在，需要创建
            doNothing().when(userService).initDefaultUser(any(), any());

            // 执行
            dataInitializer.run(args);

            // 验证
            verify(userService).initDefaultUser(any(), any());
        }

        @Test
        @DisplayName("初始化失败 - 异常处理")
        void run_exception() {
            // 设置配置值
            ReflectionTestUtils.setField(dataInitializer, "defaultUsername", "admin");
            ReflectionTestUtils.setField(dataInitializer, "defaultPassword", "password");

            // 抛出异常
            doThrow(new RuntimeException("初始化失败")).when(userService)
                    .initDefaultUser(any(), any());

            // 执行（不应该抛出异常）
            dataInitializer.run(args);

            // 验证仍然被调用
            verify(userService).initDefaultUser(any(), any());
        }
    }
}