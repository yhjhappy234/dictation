package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.DictationResponse;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DictationRecordService;
import com.yhj.dictation.service.DifficultWordService;
import com.yhj.dictation.service.SuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictationRecordController 单元测试
 */
@WebMvcTest(DictationRecordController.class)
@ExtendWith(MockitoExtension.class)
class DictationRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictationRecordService recordService;

    @MockBean
    private DifficultWordService difficultWordService;

    @MockBean
    private SuggestionService suggestionService;

    private DictationRecord testRecord;
    private DictationResponse testResponse;

    @BeforeEach
    void setUp() {
        testRecord = new DictationRecord();
        testRecord.setId(1L);
        testRecord.setWordId(1L);
        testRecord.setBatchId(1L);
        testRecord.setStartTime(LocalDateTime.now());
        testRecord.setStatus(DictationRecord.RecordStatus.STARTED);
        testRecord.setRepeatCount(0);

        testResponse = new DictationResponse();
        testResponse.setId(1L);
        testResponse.setWordId(1L);
        testResponse.setBatchId(1L);
        testResponse.setStartTime(LocalDateTime.now());
        testResponse.setStatus("STARTED");
        testResponse.setRepeatCount(0);
    }

    @Nested
    @DisplayName("startRecord API测试")
    class StartRecordApiTests {

        @Test
        @DisplayName("开始听写记录 - 成功")
        void startRecord_success() throws Exception {
            // Given
            when(recordService.startRecord(1L, 1L)).thenReturn(testRecord);

            // When & Then
            mockMvc.perform(post("/api/records/start")
                    .param("wordId", "1")
                    .param("batchId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("开始听写"));
        }

        @Test
        @DisplayName("开始听写记录 - 异常情况")
        void startRecord_exception() throws Exception {
            // Given
            when(recordService.startRecord(anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/records/start")
                    .param("wordId", "1")
                    .param("batchId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeRecord API测试")
    class CompleteRecordApiTests {

        @Test
        @DisplayName("完成听写记录 - 成功")
        void completeRecord_success() throws Exception {
            // Given
            DictationRecord completedRecord = new DictationRecord();
            completedRecord.setId(1L);
            completedRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);
            completedRecord.setWordId(1L);

            when(recordService.completeRecord(1L)).thenReturn(completedRecord);
            doNothing().when(difficultWordService).handlePracticeSuccess(1L);

            // When & Then
            mockMvc.perform(post("/api/records/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("听写完成"));
        }

        @Test
        @DisplayName("完成听写记录 - 记录不存在")
        void completeRecord_notFound() throws Exception {
            // Given
            when(recordService.completeRecord(anyLong()))
                    .thenThrow(new IllegalArgumentException("Record not found"));

            // When & Then
            mockMvc.perform(post("/api/records/999/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("skipRecord API测试")
    class SkipRecordApiTests {

        @Test
        @DisplayName("跳过听写记录 - 成功")
        void skipRecord_success() throws Exception {
            // Given
            DictationRecord skippedRecord = new DictationRecord();
            skippedRecord.setId(1L);
            skippedRecord.setStatus(DictationRecord.RecordStatus.SKIPPED);
            skippedRecord.setWordId(1L);

            when(recordService.skipRecord(1L)).thenReturn(skippedRecord);
            doNothing().when(difficultWordService).handlePracticeFailure(1L);

            // When & Then
            mockMvc.perform(post("/api/records/1/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已跳过"));
        }

        @Test
        @DisplayName("跳过听写记录 - 记录不存在")
        void skipRecord_notFound() throws Exception {
            // Given
            when(recordService.skipRecord(anyLong()))
                    .thenThrow(new IllegalArgumentException("Record not found"));

            // When & Then
            mockMvc.perform(post("/api/records/999/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("incrementRepeatCount API测试")
    class IncrementRepeatCountApiTests {

        @Test
        @DisplayName("增加重复次数 - 成功")
        void incrementRepeatCount_success() throws Exception {
            // Given
            DictationRecord updatedRecord = new DictationRecord();
            updatedRecord.setId(1L);
            updatedRecord.setRepeatCount(1);

            when(recordService.incrementRepeatCount(1L)).thenReturn(updatedRecord);

            // When & Then
            mockMvc.perform(post("/api/records/1/repeat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("重复次数已更新"));
        }

        @Test
        @DisplayName("增加重复次数 - 记录不存在")
        void incrementRepeatCount_notFound() throws Exception {
            // Given
            when(recordService.incrementRepeatCount(anyLong()))
                    .thenThrow(new IllegalArgumentException("Record not found"));

            // When & Then
            mockMvc.perform(post("/api/records/999/repeat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getRecordById API测试")
    class GetRecordByIdApiTests {

        @Test
        @DisplayName("获取记录详情 - 成功")
        void getRecordById_success() throws Exception {
            // Given
            when(recordService.getRecordById(1L)).thenReturn(Optional.of(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取记录详情 - 记录不存在")
        void getRecordById_notFound() throws Exception {
            // Given
            when(recordService.getRecordById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/records/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getRecordsByBatchId API测试")
    class GetRecordsByBatchIdApiTests {

        @Test
        @DisplayName("获取批次记录 - 成功")
        void getRecordsByBatchId_success() throws Exception {
            // Given
            when(recordService.getRecordsByBatchId(1L)).thenReturn(Arrays.asList(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/batch/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取批次记录 - 空列表")
        void getRecordsByBatchId_emptyList() throws Exception {
            // Given
            when(recordService.getRecordsByBatchId(anyLong())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/records/batch/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getTodayRecords API测试")
    class GetTodayRecordsApiTests {

        @Test
        @DisplayName("获取今日记录 - 成功")
        void getTodayRecords_success() throws Exception {
            // Given
            when(recordService.getTodayRecords()).thenReturn(Arrays.asList(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取今日记录 - 空列表")
        void getTodayRecords_emptyList() throws Exception {
            // Given
            when(recordService.getTodayRecords()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/records/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getRecordsByDateRange API测试")
    class GetRecordsByDateRangeApiTests {

        @Test
        @DisplayName("获取日期范围记录 - 成功")
        void getRecordsByDateRange_success() throws Exception {
            // Given
            when(recordService.getRecordsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Arrays.asList(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/range")
                    .param("start", "2024-01-01")
                    .param("end", "2024-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取日期范围记录 - 空列表")
        void getRecordsByDateRange_emptyList() throws Exception {
            // Given
            when(recordService.getRecordsByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/records/range")
                    .param("start", "2024-01-01")
                    .param("end", "2024-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getTodayReport API测试")
    class GetTodayReportApiTests {

        @Test
        @DisplayName("获取今日统计报告 - 成功")
        void getTodayReport_success() throws Exception {
            // Given
            testRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);
            testRecord.setDurationSeconds(10);
            when(recordService.getTodayRecords()).thenReturn(Arrays.asList(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/report/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalCount").value(1));
        }

        @Test
        @DisplayName("获取今日统计报告 - 空记录")
        void getTodayReport_emptyRecords() throws Exception {
            // Given
            when(recordService.getTodayRecords()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/records/report/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalCount").value(0));
        }
    }

    @Nested
    @DisplayName("getBatchReport API测试")
    class GetBatchReportApiTests {

        @Test
        @DisplayName("获取批次统计报告 - 成功")
        void getBatchReport_success() throws Exception {
            // Given
            testRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);
            testRecord.setDurationSeconds(10);
            when(recordService.getRecordsByBatchId(1L)).thenReturn(Arrays.asList(testRecord));

            // When & Then
            mockMvc.perform(get("/api/records/report/batch/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("finishBatchWithSuggestions API测试")
    class FinishBatchWithSuggestionsApiTests {

        @Test
        @DisplayName("完成批次并生成建议 - 成功")
        void finishBatchWithSuggestions_success() throws Exception {
            // Given
            doNothing().when(suggestionService).generateSuggestions(1L);

            // When & Then
            mockMvc.perform(post("/api/records/batch/1/finish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("建议已生成"));
        }

        @Test
        @DisplayName("完成批次并生成建议 - 异常情况")
        void finishBatchWithSuggestions_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(suggestionService).generateSuggestions(anyLong());

            // When & Then
            mockMvc.perform(post("/api/records/batch/999/finish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteRecord API测试")
    class DeleteRecordApiTests {

        @Test
        @DisplayName("删除记录 - 成功")
        void deleteRecord_success() throws Exception {
            // Given
            doNothing().when(recordService).deleteRecord(1L);

            // When & Then
            mockMvc.perform(delete("/api/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("记录删除成功"));
        }

        @Test
        @DisplayName("删除记录 - 异常情况")
        void deleteRecord_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(recordService).deleteRecord(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/records/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}