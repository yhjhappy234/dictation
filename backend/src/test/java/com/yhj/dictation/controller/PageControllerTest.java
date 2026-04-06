package com.yhj.dictation.controller;

import com.yhj.dictation.service.DictationBatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DictationBatchService batchService;

    @Test
    void testIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testHistoryPage() throws Exception {
        mockMvc.perform(get("/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"));
    }

    @Test
    void testDifficultWordsPage() throws Exception {
        mockMvc.perform(get("/difficult-words"))
                .andExpect(status().isOk())
                .andExpect(view().name("difficult-words"));
    }

    @Test
    void testReportsPage() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"));
    }
}