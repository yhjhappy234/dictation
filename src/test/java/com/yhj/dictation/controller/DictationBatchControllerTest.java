package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.dto.BatchResponse;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DictationBatchService;
import com.yhj.dictation.service.WordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictationBatchController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationBatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictationBatchService batchService;

    @Mock
    private WordService wordService;

    @InjectMocks
    private DictationBatchController batchController;

    private ObjectMapper objectMapper;
    private BatchResponse testBatchResponse;
    private DictationBatch testBatch;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
        objectMapper = new ObjectMapper();

        testBatchResponse = new BatchResponse();
        testBatchResponse.setId(1L);
        testBatchResponse.setBatchName("Test Batch");
        testBatchResponse.setCreatedAt(LocalDateTime.now());
        testBatchResponse.setTotalWords(5);
        testBatchResponse.setCompletedWords(0);
        testBatchResponse.setStatus("CREATED");
        testBatchResponse.setProgress(0.0);

        testBatch = new DictationBatch();
        testBatch.setId(1L);
        testBatch.setBatchName("Test Batch");
        testBatch.setStatus(DictationBatch.BatchStatus.CREATED);
        testBatch.setTotalWords(5);
        testBatch.setCompletedWords(0);
    }

    @Nested
    @DisplayName("createBatch API测试")
    class CreateBatchApiTests {

        @Test
        @DisplayName("创建批次 - 成功")
        void createBatch_success() throws Exception {
            // Given
            BatchCreateRequest request = new BatchCreateRequest();
            request.setBatchName("Test Batch");
            request.setWords("word1 word2");

            when(batchService.createBatchResponse(any(BatchCreateRequest.class))).thenReturn(testBatchResponse);

            // When & Then
            mockMvc.perform(post("/api/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("批次创建成功"))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("创建批次 - 异常情况")
        void createBatch_exception() throws Exception {
            // Given
            BatchCreateRequest request = new BatchCreateRequest();
            request.setBatchName("Test Batch");

            when(batchService.createBatchResponse(any(BatchCreateRequest.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("getAllBatches API测试")
    class GetAllBatchesApiTests {

        @Test
        @DisplayName("获取所有批次 - 成功")
        void getAllBatches_success() throws Exception {
            // Given
            List<BatchResponse> batches = Arrays.asList(testBatchResponse);
            when(batchService.getAllBatchResponses()).thenReturn(batches);

            // When & Then
            mockMvc.perform(get("/api/batches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @DisplayName("获取所有批次 - 空列表")
        void getAllBatches_emptyList() throws Exception {
            // Given
            when(batchService.getAllBatchResponses()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/batches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getBatchById API测试")
    class GetBatchByIdApiTests {

        @Test
        @DisplayName("获取批次详情 - 成功")
        void getBatchById_success() throws Exception {
            // Given
            when(batchService.getBatchResponseById(1L)).thenReturn(testBatchResponse);

            // When & Then
            mockMvc.perform(get("/api/batches/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("获取批次详情 - 批次不存在")
        void getBatchById_notFound() throws Exception {
            // Given
            when(batchService.getBatchResponseById(anyLong()))
                    .thenThrow(new IllegalArgumentException("Batch not found"));

            // When & Then
            mockMvc.perform(get("/api/batches/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("startBatch API测试")
    class StartBatchApiTests {

        @Test
        @DisplayName("开始批次 - 成功")
        void startBatch_success() throws Exception {
            // Given
            testBatch.setStatus(DictationBatch.BatchStatus.IN_PROGRESS);
            testBatchResponse.setStatus("IN_PROGRESS");
            when(batchService.startBatch(1L)).thenReturn(testBatch);
            when(batchService.toBatchResponse(any(DictationBatch.class))).thenReturn(testBatchResponse);

            // When & Then
            mockMvc.perform(post("/api/batches/1/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("批次开始成功"));
        }

        @Test
        @DisplayName("开始批次 - 批次不存在")
        void startBatch_notFound() throws Exception {
            // Given
            when(batchService.startBatch(anyLong()))
                    .thenThrow(new IllegalArgumentException("Batch not found"));

            // When & Then
            mockMvc.perform(post("/api/batches/999/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeBatch API测试")
    class CompleteBatchApiTests {

        @Test
        @DisplayName("完成批次 - 成功")
        void completeBatch_success() throws Exception {
            // Given
            testBatch.setStatus(DictationBatch.BatchStatus.COMPLETED);
            testBatchResponse.setStatus("COMPLETED");
            when(batchService.completeBatch(1L)).thenReturn(testBatch);
            when(batchService.toBatchResponse(any(DictationBatch.class))).thenReturn(testBatchResponse);

            // When & Then
            mockMvc.perform(post("/api/batches/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("批次完成"));
        }

        @Test
        @DisplayName("完成批次 - 批次不存在")
        void completeBatch_notFound() throws Exception {
            // Given
            when(batchService.completeBatch(anyLong()))
                    .thenThrow(new IllegalArgumentException("Batch not found"));

            // When & Then
            mockMvc.perform(post("/api/batches/999/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("cancelBatch API测试")
    class CancelBatchApiTests {

        @Test
        @DisplayName("取消批次 - 成功")
        void cancelBatch_success() throws Exception {
            // Given
            testBatch.setStatus(DictationBatch.BatchStatus.CANCELLED);
            testBatchResponse.setStatus("CANCELLED");
            when(batchService.cancelBatch(1L)).thenReturn(testBatch);
            when(batchService.toBatchResponse(any(DictationBatch.class))).thenReturn(testBatchResponse);

            // When & Then
            mockMvc.perform(post("/api/batches/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("批次已取消"));
        }

        @Test
        @DisplayName("取消批次 - 批次不存在")
        void cancelBatch_notFound() throws Exception {
            // Given
            when(batchService.cancelBatch(anyLong()))
                    .thenThrow(new IllegalArgumentException("Batch not found"));

            // When & Then
            mockMvc.perform(post("/api/batches/999/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteBatch API测试")
    class DeleteBatchApiTests {

        @Test
        @DisplayName("删除批次 - 成功")
        void deleteBatch_success() throws Exception {
            // Given
            doNothing().when(batchService).deleteBatch(1L);

            // When & Then
            mockMvc.perform(delete("/api/batches/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("批次删除成功"));
        }

        @Test
        @DisplayName("删除批次 - 异常情况")
        void deleteBatch_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(batchService).deleteBatch(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/batches/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getBatchWords API测试")
    class GetBatchWordsApiTests {

        @Test
        @DisplayName("获取批次词语 - 成功")
        void getBatchWords_success() throws Exception {
            // Given
            Word word = new Word();
            word.setId(1L);
            word.setWordText("测试");
            word.setBatchId(1L);
            word.setSortOrder(1);
            word.setStatus(Word.WordStatus.PENDING);

            when(wordService.getWordsByBatchId(1L)).thenReturn(Arrays.asList(word));

            // When & Then
            mockMvc.perform(get("/api/batches/1/words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取批次词语 - 空列表")
        void getBatchWords_emptyList() throws Exception {
            // Given
            when(wordService.getWordsByBatchId(anyLong())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/batches/999/words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("resetBatchWords API测试")
    class ResetBatchWordsApiTests {

        @Test
        @DisplayName("重置批次词语状态 - 成功")
        void resetBatchWords_success() throws Exception {
            // Given
            doNothing().when(wordService).resetBatchWords(1L);

            // When & Then
            mockMvc.perform(post("/api/batches/1/reset"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("词语状态已重置"));
        }

        @Test
        @DisplayName("重置批次词语状态 - 异常情况")
        void resetBatchWords_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(wordService).resetBatchWords(anyLong());

            // When & Then
            mockMvc.perform(post("/api/batches/999/reset"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}