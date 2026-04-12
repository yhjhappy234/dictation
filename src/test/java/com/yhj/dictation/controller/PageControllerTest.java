package com.yhj.dictation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * PageController 单元测试
 */
@WebMvcTest(PageController.class)
@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

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