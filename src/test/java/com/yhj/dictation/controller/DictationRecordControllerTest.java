package com.yhj.dictation.controller;

import com.yhj.dictation.dto.CompleteRequest;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DictationRecordService;
import com.yhj.dictation.service.DifficultWordService;
import com.yhj.dictation.service.SuggestionService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictationRecordController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationRecordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictationRecordService recordService;

    @Mock
    private DifficultWordService difficultWordService;

    @Mock
    private SuggestionService suggestionService;

    @Mock
    private WordService wordService;

    @InjectMocks
    private DictationRecordController recordController;

    private DictationRecord testRecord;
    private Word testWord;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(recordController).build();

        testRecord = new DictationRecord();
        testRecord.setId(1L);
        testRecord.setWordId(1L);
        testRecord.setBatchId(1L);
        testRecord.setStartTime(LocalDateTime.now().minusSeconds(30));
        testRecord.setEndTime(LocalDateTime.now());
        testRecord.setDurationSeconds(30);
        testRecord.setRepeatCount(0);
        testRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试词");
    }

    @Nested
    @DisplayName("startRecord 方法测试")
    class StartRecordTests {

        @Test
        @DisplayName("开始听写记录")
        void startRecord() throws Exception {
            when(recordService.startRecord(anyLong(), anyLong())).thenReturn(testRecord);

            mockMvc.perform(post("/api/records/start?wordId=1&batchId=1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("开始听写"));

            verify(recordService).startRecord(1L, 1L);
        }

        @Test
        @DisplayName("开始听写记录失败")
        void startRecordFail() throws Exception {
            when(recordService.startRecord(anyLong(), anyLong())).thenThrow(new RuntimeException("失败"));

            mockMvc.perform(post("/api/records/start?wordId=1&batchId=1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeRecord 方法测试")
    class CompleteRecordTests {

        @Test
        @DisplayName("完成听写记录")
        void completeRecord() throws Exception {
            when(recordService.completeRecord(anyLong())).thenReturn(testRecord);
            when(wordService.getWordById(anyLong())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordService).handlePracticeSuccessByText(any());

            mockMvc.perform(post("/api/records/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("听写完成"));

            verify(recordService).completeRecord(1L);
            verify(difficultWordService).handlePracticeSuccessByText("测试词");
        }

        @Test
        @DisplayName("完成听写记录 - 记录不存在")
        void completeRecordNotFound() throws Exception {
            when(recordService.completeRecord(anyLong())).thenThrow(new IllegalArgumentException("记录不存在"));

            mockMvc.perform(post("/api/records/999/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("完成听写记录 - 词语不存在")
        void completeRecordWordNotFound() throws Exception {
            when(recordService.completeRecord(anyLong())).thenReturn(testRecord);
            when(wordService.getWordById(anyLong())).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/records/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(difficultWordService, never()).handlePracticeSuccessByText(any());
        }
    }

    @Nested
    @DisplayName("completeByWordId 方法测试")
    class CompleteByWordIdTests {

        @Test
        @DisplayName("通过词语ID完成记录")
        void completeByWordId() throws Exception {
            when(recordService.completeByWordId(anyLong(), anyInt())).thenReturn(testRecord);
            when(wordService.getWordById(anyLong())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordService).handlePracticeSuccessByText(any());

            CompleteRequest request = new CompleteRequest();
            request.setDuration(30);

            mockMvc.perform(post("/api/records/complete/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"duration\":30}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("通过词语ID完成记录 - 无持续时间")
        void completeByWordIdNoDuration() throws Exception {
            when(recordService.completeByWordId(anyLong(), any())).thenReturn(testRecord);
            when(wordService.getWordById(anyLong())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordService).handlePracticeSuccessByText(any());

            mockMvc.perform(post("/api/records/complete/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("通过词语ID完成记录失败")
        void completeByWordIdFail() throws Exception {
            when(recordService.completeByWordId(anyLong(), any())).thenThrow(new IllegalArgumentException("词语不存在"));

            mockMvc.perform(post("/api/records/complete/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("endDictation 方法测试")
    class EndDictationTests {

        @Test
        @DisplayName("结束听写批次")
        void endDictation() throws Exception {
            doNothing().when(suggestionService).generateSuggestions(anyLong());

            mockMvc.perform(post("/api/records/end/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("听写结束"));

            verify(suggestionService).generateSuggestions(1L);
        }

        @Test
        @DisplayName("结束听写批次失败")
        void endDictationFail() throws Exception {
            doThrow(new RuntimeException("失败")).when(suggestionService).generateSuggestions(anyLong());

            mockMvc.perform(post("/api/records/end/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("skipRecord 方法测试")
    class SkipRecordTests {

        @Test
        @DisplayName("跳过听写记录")
        void skipRecord() throws Exception {
            testRecord.setStatus(DictationRecord.RecordStatus.SKIPPED);
            when(recordService.skipRecord(anyLong())).thenReturn(testRecord);
            when(wordService.getWordById(anyLong())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordService).handlePracticeFailureByText(any(), any());

            mockMvc.perform(post("/api/records/1/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已跳过"));

            verify(difficultWordService).handlePracticeFailureByText("测试词", null);
        }

        @Test
        @DisplayName("跳过听写记录 - 记录不存在")
        void skipRecordNotFound() throws Exception {
            when(recordService.skipRecord(anyLong())).thenThrow(new IllegalArgumentException("记录不存在"));

            mockMvc.perform(post("/api/records/999/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("incrementRepeatCount 方法测试")
    class IncrementRepeatCountTests {

        @Test
        @DisplayName("增加重复次数")
        void incrementRepeatCount() throws Exception {
            testRecord.setRepeatCount(1);
            when(recordService.incrementRepeatCount(anyLong())).thenReturn(testRecord);

            mockMvc.perform(post("/api/records/1/repeat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("增加重复次数 - 记录不存在")
        void incrementRepeatCountNotFound() throws Exception {
            when(recordService.incrementRepeatCount(anyLong())).thenThrow(new IllegalArgumentException("记录不存在"));

            mockMvc.perform(post("/api/records/999/repeat"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getRecordById 方法测试")
    class GetRecordByIdTests {

        @Test
        @DisplayName("获取记录详情")
        void getRecordById() throws Exception {
            when(recordService.getRecordById(anyLong())).thenReturn(Optional.of(testRecord));

            mockMvc.perform(get("/api/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取记录详情 - 记录不存在")
        void getRecordByIdNotFound() throws Exception {
            when(recordService.getRecordById(anyLong())).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/records/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getRecordsByBatchId 方法测试")
    class GetRecordsByBatchIdTests {

        @Test
        @DisplayName("获取批次记录")
        void getRecordsByBatchId() throws Exception {
            when(recordService.getRecordsByBatchId(anyLong())).thenReturn(List.of(testRecord));

            mockMvc.perform(get("/api/records/batch/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getTodayRecords 方法测试")
    class GetTodayRecordsTests {

        @Test
        @DisplayName("获取今日记录")
        void getTodayRecords() throws Exception {
            when(recordService.getTodayRecords()).thenReturn(List.of(testRecord));

            mockMvc.perform(get("/api/records/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getRecordsByDateRange 方法测试")
    class GetRecordsByDateRangeTests {

        @Test
        @DisplayName("获取日期范围记录")
        void getRecordsByDateRange() throws Exception {
            when(recordService.getRecordsByDateRange(any(), any())).thenReturn(List.of(testRecord));

            mockMvc.perform(get("/api/records/range?start=2024-01-01&end=2024-01-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getTodayReport 方法测试")
    class GetTodayReportTests {

        @Test
        @DisplayName("获取今日报告")
        void getTodayReport() throws Exception {
            when(recordService.getTodayRecords()).thenReturn(List.of(testRecord));

            mockMvc.perform(get("/api/records/report/today"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getBatchReport 方法测试")
    class GetBatchReportTests {

        @Test
        @DisplayName("获取批次报告")
        void getBatchReport() throws Exception {
            when(recordService.getRecordsByBatchId(anyLong())).thenReturn(List.of(testRecord));

            mockMvc.perform(get("/api/records/report/batch/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("finishBatchWithSuggestions 方法测试")
    class FinishBatchWithSuggestionsTests {

        @Test
        @DisplayName("完成批次并生成建议")
        void finishBatchWithSuggestions() throws Exception {
            doNothing().when(suggestionService).generateSuggestions(anyLong());

            mockMvc.perform(post("/api/records/batch/1/finish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(suggestionService).generateSuggestions(1L);
        }

        @Test
        @DisplayName("完成批次并生成建议失败")
        void finishBatchWithSuggestionsFail() throws Exception {
            doThrow(new RuntimeException("失败")).when(suggestionService).generateSuggestions(anyLong());

            mockMvc.perform(post("/api/records/batch/1/finish"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteRecord 方法测试")
    class DeleteRecordTests {

        @Test
        @DisplayName("删除记录")
        void deleteRecord() throws Exception {
            doNothing().when(recordService).deleteRecord(anyLong());

            mockMvc.perform(delete("/api/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(recordService).deleteRecord(1L);
        }

        @Test
        @DisplayName("删除记录失败")
        void deleteRecordFail() throws Exception {
            doThrow(new RuntimeException("失败")).when(recordService).deleteRecord(anyLong());

            mockMvc.perform(delete("/api/records/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}