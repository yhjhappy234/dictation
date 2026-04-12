package com.yhj.dictation.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * PageController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PageController pageController;

    @BeforeEach
    void setUp() {
        // 配置视图解析器以避免循环视图路径问题
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(pageController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Nested
    @DisplayName("页面路由测试")
    class PageRoutingTests {

        @Test
        @DisplayName("访问根路径 - 返回index视图")
        void testIndexPage() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));
        }

        @Test
        @DisplayName("访问/index路径 - 返回index视图")
        void testHomePage() throws Exception {
            mockMvc.perform(get("/index"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));
        }

        @Test
        @DisplayName("访问/history路径 - 返回history视图")
        void testHistoryPage() throws Exception {
            mockMvc.perform(get("/history"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("history"));
        }

        @Test
        @DisplayName("访问/difficult-words路径 - 返回difficult-words视图")
        void testDifficultWordsPage() throws Exception {
            mockMvc.perform(get("/difficult-words"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("difficult-words"));
        }

        @Test
        @DisplayName("访问/reports路径 - 返回reports视图")
        void testReportsPage() throws Exception {
            mockMvc.perform(get("/reports"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("reports"));
        }
    }
}