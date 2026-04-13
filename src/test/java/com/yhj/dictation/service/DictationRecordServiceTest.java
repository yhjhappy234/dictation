package com.yhj.dictation.service;

import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DictationRecordRepository;
import com.yhj.dictation.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * DictationRecordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationRecordServiceTest {

    @Mock
    private DictationRecordRepository recordRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private DictationRecordService recordService;

    private DictationRecord testRecord;
    private Word testWord;

    @BeforeEach
    void setUp() {
        testRecord = new DictationRecord();
        testRecord.setId(1L);
        testRecord.setWordId(1L);
        testRecord.setBatchId(1L);
        testRecord.setStartTime(LocalDateTime.now().minusSeconds(30));
        testRecord.setStatus(DictationRecord.RecordStatus.STARTED);
        testRecord.setRepeatCount(0);

        testWord = new Word();
        testWord.setId(1L);
        testWord.setBatchId(1L);
    }

    @Nested
    @DisplayName("startRecord 方法测试")
    class StartRecordTests {

        @Test
        @DisplayName("开始听写记录")
        void startRecord() {
            when(recordRepository.save(any())).thenAnswer(invocation -> {
                DictationRecord r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            DictationRecord result = recordService.startRecord(1L, 1L);

            assertNotNull(result);
            assertEquals(1L, result.getWordId());
            assertEquals(1L, result.getBatchId());
            assertEquals(DictationRecord.RecordStatus.STARTED, result.getStatus());
            assertEquals(0, result.getRepeatCount());
            verify(recordRepository).save(any(DictationRecord.class));
        }
    }

    @Nested
    @DisplayName("completeRecord 方法测试")
    class CompleteRecordTests {

        @Test
        @DisplayName("完成听写记录")
        void completeRecord() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DictationRecord result = recordService.completeRecord(1L);

            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.COMPLETED, result.getStatus());
            assertNotNull(result.getEndTime());
            assertNotNull(result.getDurationSeconds());
            verify(recordRepository).save(any(DictationRecord.class));
        }

        @Test
        @DisplayName("记录不存在")
        void completeRecordNotFound() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> recordService.completeRecord(1L));
        }
    }

    @Nested
    @DisplayName("completeByWordId 方法测试")
    class CompleteByWordIdTests {

        @Test
        @DisplayName("通过词语ID完成记录 - 已有开始记录")
        void completeByWordIdWithExistingRecord() {
            when(recordRepository.findByWordIdAndStatus(anyLong(), any())).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DictationRecord result = recordService.completeByWordId(1L, 20);

            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.COMPLETED, result.getStatus());
            assertEquals(20, result.getDurationSeconds());
        }

        @Test
        @DisplayName("通过词语ID完成记录 - 无开始记录，创建新记录")
        void completeByWordIdWithoutExistingRecord() {
            when(recordRepository.findByWordIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(recordRepository.save(any())).thenAnswer(invocation -> {
                DictationRecord r = invocation.getArgument(0);
                r.setId(2L);
                return r;
            });

            DictationRecord result = recordService.completeByWordId(1L, 15);

            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.COMPLETED, result.getStatus());
            assertEquals(15, result.getDurationSeconds());
        }

        @Test
        @DisplayName("通过词语ID完成记录 - 无持续时间，自动计算")
        void completeByWordIdWithoutDuration() {
            when(recordRepository.findByWordIdAndStatus(anyLong(), any())).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DictationRecord result = recordService.completeByWordId(1L, null);

            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.COMPLETED, result.getStatus());
            assertNotNull(result.getDurationSeconds());
        }

        @Test
        @DisplayName("词语不存在")
        void completeByWordIdWordNotFound() {
            when(recordRepository.findByWordIdAndStatus(anyLong(), any())).thenReturn(Optional.empty());
            when(wordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> recordService.completeByWordId(999L, 10));
        }
    }

    @Nested
    @DisplayName("skipRecord 方法测试")
    class SkipRecordTests {

        @Test
        @DisplayName("跳过听写记录")
        void skipRecord() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DictationRecord result = recordService.skipRecord(1L);

            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.SKIPPED, result.getStatus());
            assertNotNull(result.getEndTime());
        }

        @Test
        @DisplayName("记录不存在")
        void skipRecordNotFound() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> recordService.skipRecord(1L));
        }
    }

    @Nested
    @DisplayName("incrementRepeatCount 方法测试")
    class IncrementRepeatCountTests {

        @Test
        @DisplayName("增加重复次数")
        void incrementRepeatCount() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DictationRecord result = recordService.incrementRepeatCount(1L);

            assertNotNull(result);
            assertEquals(1, result.getRepeatCount());
        }

        @Test
        @DisplayName("记录不存在")
        void incrementRepeatCountNotFound() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> recordService.incrementRepeatCount(1L));
        }
    }

    @Nested
    @DisplayName("getRecordById 方法测试")
    class GetRecordByIdTests {

        @Test
        @DisplayName("获取记录成功")
        void getRecordById() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.of(testRecord));

            Optional<DictationRecord> result = recordService.getRecordById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("记录不存在")
        void getRecordByIdNotFound() {
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            Optional<DictationRecord> result = recordService.getRecordById(999L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getRecordsByBatchId 方法测试")
    class GetRecordsByBatchIdTests {

        @Test
        @DisplayName("获取批次记录")
        void getRecordsByBatchId() {
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));

            List<DictationRecord> result = recordService.getRecordsByBatchId(1L);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getRecordsByWordId 方法测试")
    class GetRecordsByWordIdTests {

        @Test
        @DisplayName("获取词语记录")
        void getRecordsByWordId() {
            when(recordRepository.findByWordId(anyLong())).thenReturn(List.of(testRecord));

            List<DictationRecord> result = recordService.getRecordsByWordId(1L);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getTodayRecords 方法测试")
    class GetTodayRecordsTests {

        @Test
        @DisplayName("获取今日记录")
        void getTodayRecords() {
            when(recordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(), any()))
                    .thenReturn(List.of(testRecord));

            List<DictationRecord> result = recordService.getTodayRecords();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getRecordsByDateRange 方法测试")
    class GetRecordsByDateRangeTests {

        @Test
        @DisplayName("获取日期范围记录")
        void getRecordsByDateRange() {
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(recordRepository.findByDateRange(any(), any())).thenReturn(List.of(testRecord));

            List<DictationRecord> result = recordService.getRecordsByDateRange(start, end);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAvgDurationByWordId 方法测试")
    class GetAvgDurationByWordIdTests {

        @Test
        @DisplayName("获取平均时长")
        void getAvgDuration() {
            when(recordRepository.findAvgDurationByWordId(anyLong())).thenReturn(15.5);

            Double result = recordService.getAvgDurationByWordId(1L);

            assertEquals(15.5, result);
        }

        @Test
        @DisplayName("无记录")
        void getAvgDurationNoRecords() {
            when(recordRepository.findAvgDurationByWordId(anyLong())).thenReturn(null);

            Double result = recordService.getAvgDurationByWordId(1L);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getRepeatCountByWordId 方法测试")
    class GetRepeatCountByWordIdTests {

        @Test
        @DisplayName("获取重复次数")
        void getRepeatCount() {
            when(recordRepository.countRepeatByWordId(anyLong())).thenReturn(5);

            Integer result = recordService.getRepeatCountByWordId(1L);

            assertEquals(5, result);
        }

        @Test
        @DisplayName("无记录")
        void getRepeatCountNoRecords() {
            when(recordRepository.countRepeatByWordId(anyLong())).thenReturn(null);

            Integer result = recordService.getRepeatCountByWordId(1L);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("deleteRecord 方法测试")
    class DeleteRecordTests {

        @Test
        @DisplayName("删除记录")
        void deleteRecord() {
            doNothing().when(recordRepository).deleteById(anyLong());

            recordService.deleteRecord(1L);

            verify(recordRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteRecordsByBatchId 方法测试")
    class DeleteRecordsByBatchIdTests {

        @Test
        @DisplayName("删除批次记录")
        void deleteRecordsByBatchId() {
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            doNothing().when(recordRepository).deleteAll(any());

            recordService.deleteRecordsByBatchId(1L);

            verify(recordRepository).deleteAll(List.of(testRecord));
        }
    }
}