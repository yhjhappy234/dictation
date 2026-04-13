package com.yhj.dictation.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CorsConfig 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    @InjectMocks
    private CorsConfig corsConfig;

    @Nested
    @DisplayName("corsFilter 方法测试")
    class CorsFilterTests {

        @Test
        @DisplayName("默认配置 - 所有允许通配符")
        void corsFilter_defaultConfig() {
            ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedMethods", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");
            ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);

            CorsFilter filter = corsConfig.corsFilter();

            assertNotNull(filter);
        }

        @Test
        @DisplayName("自定义配置 - 多个域名")
        void corsFilter_customOrigins() {
            ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost,http://example.com");
            ReflectionTestUtils.setField(corsConfig, "allowedMethods", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");
            ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);

            CorsFilter filter = corsConfig.corsFilter();

            assertNotNull(filter);
        }

        @Test
        @DisplayName("自定义配置 - 多个方法")
        void corsFilter_customMethods() {
            ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedMethods", "GET,POST,PUT,DELETE");
            ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");
            ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);

            CorsFilter filter = corsConfig.corsFilter();

            assertNotNull(filter);
        }

        @Test
        @DisplayName("自定义配置 - 多个头部")
        void corsFilter_customHeaders() {
            ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedMethods", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "Authorization,Content-Type");
            ReflectionTestUtils.setField(corsConfig, "allowCredentials", true);

            CorsFilter filter = corsConfig.corsFilter();

            assertNotNull(filter);
        }

        @Test
        @DisplayName("不允许凭证")
        void corsFilter_noCredentials() {
            ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedMethods", "*");
            ReflectionTestUtils.setField(corsConfig, "allowedHeaders", "*");
            ReflectionTestUtils.setField(corsConfig, "allowCredentials", false);

            CorsFilter filter = corsConfig.corsFilter();

            assertNotNull(filter);
        }
    }
}