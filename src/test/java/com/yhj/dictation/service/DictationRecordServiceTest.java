package com.yhj.dictation.service;

import com.yhj.dictation.entity.DictationRecord;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
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

    @BeforeEach
    void setUp() {
        testRecord = new DictationRecord();
        testRecord.setId(1L);
        testRecord.setWordId(1L);
        testRecord.setBatchId(1L);
        testRecord.setStartTime(LocalDateTime.now());
        testRecord.setStatus(DictationRecord.RecordStatus.STARTED);
        testRecord.setRepeatCount(0);
    }

    @Nested
    @DisplayName("startRecord 方法测试")
    class StartRecordTests {

        @Test
        @DisplayName("开始听写记录 - 成功")
        void startRecord_success() {
            // Given
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(testRecord);

            // When
            DictationRecord result = recordService.startRecord(1L, 1L);

            // Then
            assertNotNull(result);
            assertEquals(DictationRecord.RecordStatus.STARTED, result.getStatus());
            assertEquals(0, result.getRepeatCount());
            verify(recordRepository).save(any(DictationRecord.class));
        }

        @Test
        @DisplayName("开始听写记录 - 设置正确的wordId和batchId")
        void startRecord_setsCorrectIds() {
            // Given
            DictationRecord savedRecord = new DictationRecord();
            savedRecord.setWordId(100L);
            savedRecord.setBatchId(200L);
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(savedRecord);

            // When
            DictationRecord result = recordService.startRecord(100L, 200L);

            // Then
            assertEquals(100L, result.getWordId());
            assertEquals(200L, result.getBatchId());
        }
    }

    @Nested
    @DisplayName("completeRecord 方法测试")
    class CompleteRecordTests {

        @Test
        @DisplayName("完成听写记录 - 成功")
        void completeRecord_success() {
            // Given
            testRecord.setStartTime(LocalDateTime.now().minusSeconds(10));

            DictationRecord completedRecord = new DictationRecord();
            completedRecord.setId(1L);
            completedRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);
            completedRecord.setStartTime(testRecord.getStartTime());
            completedRecord.setEndTime(LocalDateTime.now());
            completedRecord.setDurationSeconds(10);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(completedRecord);

            // When
            DictationRecord result = recordService.completeRecord(1L);

            // Then
            assertEquals(DictationRecord.RecordStatus.COMPLETED, result.getStatus());
            assertNotNull(result.getEndTime());
            verify(recordRepository).save(any(DictationRecord.class));
        }

        @Test
        @DisplayName("完成听写记录 - 记录不存在抛出异常")
        void completeRecord_notFound_throwsException() {
            // Given
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> recordService.completeRecord(999L));
        }

        @Test
        @DisplayName("完成听写记录 - 计算时长正确")
        void completeRecord_calculatesDuration() {
            // Given
            LocalDateTime startTime = LocalDateTime.now().minusSeconds(30);
            LocalDateTime endTime = LocalDateTime.now();

            testRecord.setStartTime(startTime);

            DictationRecord savedRecord = new DictationRecord();
            savedRecord.setStartTime(startTime);
            savedRecord.setEndTime(endTime);
            savedRecord.setDurationSeconds(30);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(savedRecord);

            // When
            DictationRecord result = recordService.completeRecord(1L);

            // Then
            assertEquals(30, result.getDurationSeconds());
        }

        @Test
        @DisplayName("完成听写记录 - startTime为null时不计算时长")
        void completeRecord_nullStartTime_noDuration() {
            // Given
            testRecord.setStartTime(null);

            DictationRecord savedRecord = new DictationRecord();
            savedRecord.setStartTime(null);
            savedRecord.setEndTime(LocalDateTime.now());
            savedRecord.setDurationSeconds(null);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(savedRecord);

            // When
            DictationRecord result = recordService.completeRecord(1L);

            // Then
            assertNull(result.getDurationSeconds());
        }
    }

    @Nested
    @DisplayName("skipRecord 方法测试")
    class SkipRecordTests {

        @Test
        @DisplayName("跳过听写记录 - 成功")
        void skipRecord_success() {
            // Given
            DictationRecord skippedRecord = new DictationRecord();
            skippedRecord.setId(1L);
            skippedRecord.setStatus(DictationRecord.RecordStatus.SKIPPED);
            skippedRecord.setEndTime(LocalDateTime.now());

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(skippedRecord);

            // When
            DictationRecord result = recordService.skipRecord(1L);

            // Then
            assertEquals(DictationRecord.RecordStatus.SKIPPED, result.getStatus());
            assertNotNull(result.getEndTime());
        }

        @Test
        @DisplayName("跳过听写记录 - 记录不存在抛出异常")
        void skipRecord_notFound_throwsException() {
            // Given
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> recordService.skipRecord(999L));
        }
    }

    @Nested
    @DisplayName("incrementRepeatCount 方法测试")
    class IncrementRepeatCountTests {

        @Test
        @DisplayName("增加重复次数 - 成功")
        void incrementRepeatCount_success() {
            // Given
            testRecord.setRepeatCount(2);
            DictationRecord updatedRecord = new DictationRecord();
            updatedRecord.setId(1L);
            updatedRecord.setRepeatCount(3);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(updatedRecord);

            // When
            DictationRecord result = recordService.incrementRepeatCount(1L);

            // Then
            assertEquals(3, result.getRepeatCount());
        }

        @Test
        @DisplayName("增加重复次数 - 记录不存在抛出异常")
        void incrementRepeatCount_notFound_throwsException() {
            // Given
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> recordService.incrementRepeatCount(999L));
        }

        @Test
        @DisplayName("增加重复次数 - 从0开始增加")
        void incrementRepeatCount_fromZero() {
            // Given
            testRecord.setRepeatCount(0);
            DictationRecord updatedRecord = new DictationRecord();
            updatedRecord.setRepeatCount(1);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(DictationRecord.class))).thenReturn(updatedRecord);

            // When
            DictationRecord result = recordService.incrementRepeatCount(1L);

            // Then
            assertEquals(1, result.getRepeatCount());
        }
    }

    @Nested
    @DisplayName("getRecordById 方法测试")
    class GetRecordByIdTests {

        @Test
        @DisplayName("根据ID获取记录 - 找到")
        void getRecordById_found() {
            // Given
            when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecord));

            // When
            Optional<DictationRecord> result = recordService.getRecordById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testRecord, result.get());
        }

        @Test
        @DisplayName("根据ID获取记录 - 未找到")
        void getRecordById_notFound() {
            // Given
            when(recordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<DictationRecord> result = recordService.getRecordById(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getRecordsByBatchId 方法测试")
    class GetRecordsByBatchIdTests {

        @Test
        @DisplayName("获取批次记录 - 成功")
        void getRecordsByBatchId_success() {
            // Given
            DictationRecord record2 = new DictationRecord();
            record2.setId(2L);
            List<DictationRecord> records = Arrays.asList(testRecord, record2);
            when(recordRepository.findByBatchId(1L)).thenReturn(records);

            // When
            List<DictationRecord> result = recordService.getRecordsByBatchId(1L);

            // Then
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("获取批次记录 - 返回空列表")
        void getRecordsByBatchId_emptyList() {
            // Given
            when(recordRepository.findByBatchId(anyLong())).thenReturn(Collections.emptyList());

            // When
            List<DictationRecord> result = recordService.getRecordsByBatchId(999L);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getRecordsByWordId 方法测试")
    class GetRecordsByWordIdTests {

        @Test
        @DisplayName("获取词语记录 - 成功")
        void getRecordsByWordId_success() {
            // Given
            List<DictationRecord> records = Arrays.asList(testRecord);
            when(recordRepository.findByWordId(1L)).thenReturn(records);

            // When
            List<DictationRecord> result = recordService.getRecordsByWordId(1L);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取词语记录 - 返回空列表")
        void getRecordsByWordId_emptyList() {
            // Given
            when(recordRepository.findByWordId(anyLong())).thenReturn(Collections.emptyList());

            // When
            List<DictationRecord> result = recordService.getRecordsByWordId(999L);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTodayRecords 方法测试")
    class GetTodayRecordsTests {

        @Test
        @DisplayName("获取今日记录 - 成功")
        void getTodayRecords_success() {
            // Given
            List<DictationRecord> records = Arrays.asList(testRecord);
            when(recordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(records);

            // When
            List<DictationRecord> result = recordService.getTodayRecords();

            // Then
            assertEquals(1, result.size());
            verify(recordRepository).findByStartTimeBetweenOrderByStartTimeDesc(any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("获取今日记录 - 返回空列表")
        void getTodayRecords_emptyList() {
            // Given
            when(recordRepository.findByStartTimeBetweenOrderByStartTimeDesc(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<DictationRecord> result = recordService.getTodayRecords();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getRecordsByDateRange 方法测试")
    class GetRecordsByDateRangeTests {

        @Test
        @DisplayName("获取日期范围记录 - 成功")
        void getRecordsByDateRange_success() {
            // Given
            LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
            List<DictationRecord> records = Arrays.asList(testRecord);
            when(recordRepository.findByDateRange(start, end)).thenReturn(records);

            // When
            List<DictationRecord> result = recordService.getRecordsByDateRange(start, end);

            // Then
            assertEquals(1, result.size());
            verify(recordRepository).findByDateRange(start, end);
        }

        @Test
        @DisplayName("获取日期范围记录 - 返回空列表")
        void getRecordsByDateRange_emptyList() {
            // Given
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now();
            when(recordRepository.findByDateRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<DictationRecord> result = recordService.getRecordsByDateRange(start, end);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getAvgDurationByWordId 方法测试")
    class GetAvgDurationByWordIdTests {

        @Test
        @DisplayName("获取平均时长 - 成功")
        void getAvgDurationByWordId_success() {
            // Given
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(15.5);

            // When
            Double result = recordService.getAvgDurationByWordId(1L);

            // Then
            assertEquals(15.5, result);
        }

        @Test
        @DisplayName("获取平均时长 - 返回null")
        void getAvgDurationByWordId_null() {
            // Given
            when(recordRepository.findAvgDurationByWordId(anyLong())).thenReturn(null);

            // When
            Double result = recordService.getAvgDurationByWordId(999L);

            // Then
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("getRepeatCountByWordId 方法测试")
    class GetRepeatCountByWordIdTests {

        @Test
        @DisplayName("获取重复次数 - 成功")
        void getRepeatCountByWordId_success() {
            // Given
            when(recordRepository.countRepeatByWordId(1L)).thenReturn(5);

            // When
            Integer result = recordService.getRepeatCountByWordId(1L);

            // Then
            assertEquals(5, result);
        }

        @Test
        @DisplayName("获取重复次数 - 返回null时返回0")
        void getRepeatCountByWordId_nullReturnsZero() {
            // Given
            when(recordRepository.countRepeatByWordId(anyLong())).thenReturn(null);

            // When
            Integer result = recordService.getRepeatCountByWordId(999L);

            // Then
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("deleteRecord 方法测试")
    class DeleteRecordTests {

        @Test
        @DisplayName("删除记录 - 成功")
        void deleteRecord_success() {
            // When
            recordService.deleteRecord(1L);

            // Then
            verify(recordRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteRecordsByBatchId 方法测试")
    class DeleteRecordsByBatchIdTests {

        @Test
        @DisplayName("删除批次记录 - 成功")
        void deleteRecordsByBatchId_success() {
            // Given
            DictationRecord record2 = new DictationRecord();
            record2.setId(2L);
            List<DictationRecord> records = Arrays.asList(testRecord, record2);
            when(recordRepository.findByBatchId(1L)).thenReturn(records);

            // When
            recordService.deleteRecordsByBatchId(1L);

            // Then
            verify(recordRepository).findByBatchId(1L);
            verify(recordRepository).deleteAll(records);
        }

        @Test
        @DisplayName("删除批次记录 - 批次没有记录")
        void deleteRecordsByBatchId_emptyBatch() {
            // Given
            when(recordRepository.findByBatchId(1L)).thenReturn(Collections.emptyList());

            // When
            recordService.deleteRecordsByBatchId(1L);

            // Then
            verify(recordRepository).deleteAll(Collections.emptyList());
        }
    }
}