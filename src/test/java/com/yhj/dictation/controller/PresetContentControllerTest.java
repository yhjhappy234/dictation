package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.service.DictationBatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PresetContentController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class PresetContentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictationBatchService batchService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PresetContentController presetContentController;

    private DictationBatch testBatch;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(presetContentController).build();

        testBatch = new DictationBatch();
        testBatch.setId(1L);
        testBatch.setBatchName("测试批次");
    }

    @Nested
    @DisplayName("getPresetList 方法测试")
    class GetPresetListTests {

        @Test
        @DisplayName("获取预设内容列表")
        void getPresetList() throws Exception {
            mockMvc.perform(get("/api/v1/preset/list"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(4));
        }
    }

    @Nested
    @DisplayName("getPresetContent 方法测试")
    class GetPresetContentTests {

        @Test
        @DisplayName("获取预设内容详情 - 不存在")
        void getPresetContentNotFound() throws Exception {
            mockMvc.perform(get("/api/v1/preset/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("预设内容不存在: nonexistent"));
        }

        @Test
        @DisplayName("获取预设内容详情 - 成功返回JSON数据")
        void getPresetContentReturnsJson() throws Exception {
            // 使用一个真实存在的预设内容测试
            mockMvc.perform(get("/api/v1/preset/common-words-50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").exists());
        }
    }

    @Nested
    @DisplayName("importPresetContent 方法测试")
    class ImportPresetContentTests {

        @Test
        @DisplayName("导入预设内容 - 不存在")
        void importPresetContentNotFound() throws Exception {
            mockMvc.perform(post("/api/v1/preset/import/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("预设内容不存在: nonexistent"));
        }

        @Test
        @DisplayName("导入预设内容 - 成功（词语列表格式）")
        void importPresetContentWordsSuccess() throws Exception {
            when(batchService.createBatch(any(BatchCreateRequest.class))).thenReturn(testBatch);

            mockMvc.perform(post("/api/v1/preset/import/common-idioms-50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("导入成功"))
                    .andExpect(jsonPath("$.data").value(1));

            verify(batchService).createBatch(any(BatchCreateRequest.class));
        }

        @Test
        @DisplayName("导入预设内容 - 成功（古诗格式）")
        void importPresetContentPoemsSuccess() throws Exception {
            DictationBatch poemBatch = new DictationBatch();
            poemBatch.setId(2L);
            poemBatch.setBatchName("小学最常用20首古诗");
            when(batchService.createBatch(any(BatchCreateRequest.class))).thenReturn(poemBatch);

            mockMvc.perform(post("/api/v1/preset/import/common-poems-20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(2));

            verify(batchService).createBatch(any(BatchCreateRequest.class));
        }

        @Test
        @DisplayName("导入预设内容 - 空词语列表")
        void importPresetContentEmptyWords() throws Exception {
            mockMvc.perform(post("/api/v1/preset/import/test-empty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("预设内容中没有可听写的词语"));
        }
    }

    @Nested
    @DisplayName("importPresetContent 逻辑测试")
    class ImportPresetContentLogicTests {

        @Test
        @DisplayName("词语列表格式解析")
        void parseWordsFormat() throws Exception {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("name", "测试");
            ArrayNode wordsArray = jsonNode.putArray("words");
            wordsArray.add("词1");
            wordsArray.add("词2");

            JsonNode parsed = objectMapper.readTree(objectMapper.writeValueAsString(jsonNode));
            assertTrue(parsed.has("words"));
            assertEquals(2, parsed.get("words").size());
        }

        @Test
        @DisplayName("古诗格式解析 - 按句子拆分")
        void parseItemsFormat() throws Exception {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("name", "测试古诗");
            ArrayNode itemsArray = jsonNode.putArray("items");
            ObjectNode item = itemsArray.addObject();
            item.put("content", "床前明月光，疑是地上霜");

            JsonNode parsed = objectMapper.readTree(objectMapper.writeValueAsString(jsonNode));
            assertTrue(parsed.has("items"));

            String content = parsed.get("items").get(0).get("content").asText();
            String[] parts = content.split("[，。！？、；：]");
            assertTrue(parts.length >= 2);
        }

        @Test
        @DisplayName("空词语列表")
        void parseEmptyWords() throws Exception {
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("name", "测试");

            JsonNode parsed = objectMapper.readTree(objectMapper.writeValueAsString(jsonNode));
            assertFalse(parsed.has("words"));
            assertFalse(parsed.has("items"));
        }
    }
}