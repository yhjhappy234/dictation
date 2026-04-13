package com.yhj.dictation.config;

import com.yhj.dictation.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GlobalExceptionHandler 单元测试
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Nested
    @DisplayName("handleRuntimeException 方法测试")
    class HandleRuntimeExceptionTests {

        @Test
        @DisplayName("处理运行时异常")
        void handleRuntimeException() {
            RuntimeException e = new RuntimeException("测试运行时异常");

            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(e);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("测试运行时异常", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException 方法测试")
    class HandleIllegalArgumentExceptionTests {

        @Test
        @DisplayName("处理非法参数异常")
        void handleIllegalArgumentException() {
            IllegalArgumentException e = new IllegalArgumentException("测试非法参数");

            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalArgumentException(e);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("测试非法参数", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("handleException 方法测试")
    class HandleExceptionTests {

        @Test
        @DisplayName("处理通用异常")
        void handleException() {
            Exception e = new Exception("测试通用异常");

            ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleException(e);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertFalse(response.getBody().isSuccess());
            assertEquals("系统错误: 测试通用异常", response.getBody().getMessage());
        }
    }
}