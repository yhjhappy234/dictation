package com.yhj.dictation.service;

import com.yhj.dictation.dto.TaskResultRequest;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.TaskRecord;
import com.yhj.dictation.repository.DictationTaskRepository;
import com.yhj.dictation.repository.TaskRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * TaskRecordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TaskRecordServiceTest {

    @Mock
    private TaskRecordRepository recordRepository;

    @Mock
    private DictationTaskRepository taskRepository;

    @InjectMocks
    private TaskRecordService taskRecordService;

    private TaskRecord testRecord;
    private DictationTask testTask;
    private static final Long TASK_ID = 1L;
    private static final Long RECORD_ID = 1L;
    private static final String TEST_WORD = "测试词语";

    @BeforeEach
    void setUp() {
        testRecord = new TaskRecord();
        testRecord.setId(RECORD_ID);
        testRecord.setTaskId(TASK_ID);
        testRecord.setWord(TEST_WORD);
        testRecord.setIsCorrect(true);
        testRecord.setErrorCount(0);
        testRecord.setReadCount(1);
        testRecord.setStartTime(LocalDateTime.now());
        testRecord.setCreatedAt(LocalDateTime.now());

        testTask = new DictationTask();
        testTask.setId(TASK_ID);
        testTask.setTaskName("Test Task");
        testTask.setWords("词语1 词语2 词语3");
        testTask.setWordCount(3);
        testTask.setStatus(DictationTask.TaskStatus.NOT_STARTED);
        testTask.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("startWord 方法测试")
    class StartWordTests {

        @Test
        @DisplayName("开始听写词语 - 新词语创建记录")
        void startWord_newWord_createsRecord() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Collections.emptyList());
            when(recordRepository.save(any(TaskRecord.class))).thenReturn(testRecord);

            // When
            TaskRecord result = taskRecordService.startWord(TASK_ID, TEST_WORD);

            // Then
            assertNotNull(result);
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("开始听写词语 - 已有记录则覆盖")
        void startWord_existingWord_updatesRecord() {
            // Given
            TaskRecord existingRecord = new TaskRecord();
            existingRecord.setId(RECORD_ID);
            existingRecord.setTaskId(TASK_ID);
            existingRecord.setWord(TEST_WORD);
            existingRecord.setIsCorrect(true);
            existingRecord.setErrorCount(2);
            existingRecord.setReadCount(5);
            existingRecord.setEndTime(LocalDateTime.now());

            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(existingRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenReturn(existingRecord);

            // When
            TaskRecord result = taskRecordService.startWord(TASK_ID, TEST_WORD);

            // Then
            assertNotNull(result);
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("开始听写词语 - 多个记录中查找正确的词语")
        void startWord_multipleRecords_findsCorrectWord() {
            // Given
            TaskRecord otherRecord = new TaskRecord();
            otherRecord.setTaskId(TASK_ID);
            otherRecord.setWord("其他词语");
            otherRecord.setIsCorrect(true);

            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(otherRecord, testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenReturn(testRecord);

            // When
            TaskRecord result = taskRecordService.startWord(TASK_ID, TEST_WORD);

            // Then
            assertNotNull(result);
            verify(recordRepository).save(any(TaskRecord.class));
        }
    }

    @Nested
    @DisplayName("incrementReadCountByWord 方法测试")
    class IncrementReadCountByWordTests {

        @Test
        @DisplayName("增加朗读次数 - 成功")
        void incrementReadCountByWord_success() {
            // Given
            testRecord.setReadCount(1);
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.incrementReadCountByWord(TASK_ID, TEST_WORD);

            // Then
            assertNotNull(result);
            assertEquals(2, result.getReadCount());
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("增加朗读次数 - readCount为null时设置为1")
        void incrementReadCountByWord_nullReadCount_setsToOne() {
            // Given
            testRecord.setReadCount(null);
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.incrementReadCountByWord(TASK_ID, TEST_WORD);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getReadCount());
        }

        @Test
        @DisplayName("增加朗读次数 - 记录不存在返回null")
        void incrementReadCountByWord_notFound_returnsNull() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Collections.emptyList());

            // When
            TaskRecord result = taskRecordService.incrementReadCountByWord(TASK_ID, TEST_WORD);

            // Then
            assertNull(result);
            verify(recordRepository, never()).save(any(TaskRecord.class));
        }
    }

    @Nested
    @DisplayName("incrementReadCount 方法测试")
    class IncrementReadCountTests {

        @Test
        @DisplayName("增加朗读次数 - 成功")
        void incrementReadCount_success() {
            // Given
            testRecord.setReadCount(3);
            when(recordRepository.findById(RECORD_ID)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.incrementReadCount(RECORD_ID);

            // Then
            assertNotNull(result);
            assertEquals(4, result.getReadCount());
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("增加朗读次数 - readCount为null时设置为1")
        void incrementReadCount_nullReadCount_setsToOne() {
            // Given
            testRecord.setReadCount(null);
            when(recordRepository.findById(RECORD_ID)).thenReturn(Optional.of(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.incrementReadCount(RECORD_ID);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getReadCount());
        }

        @Test
        @DisplayName("增加朗读次数 - 记录不存在抛出异常")
        void incrementReadCount_notFound_throwsException() {
            // Given
            when(recordRepository.findById(RECORD_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskRecordService.incrementReadCount(RECORD_ID);
            });
            verify(recordRepository, never()).save(any(TaskRecord.class));
        }
    }

    @Nested
    @DisplayName("completeWord 方法测试")
    class CompleteWordTests {

        @Test
        @DisplayName("完成听写词语 - 成功更新现有记录")
        void completeWord_existingRecord_updatesRecord() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.completeWord(TASK_ID, TEST_WORD, false);

            // Then
            assertNotNull(result);
            assertEquals(false, result.getIsCorrect());
            assertNotNull(result.getEndTime());
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("完成听写词语 - 记录不存在时创建新记录")
        void completeWord_noRecord_createsNewRecord() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Collections.emptyList());
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(RECORD_ID);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.completeWord(TASK_ID, TEST_WORD, true);

            // Then
            assertNotNull(result);
            assertEquals(true, result.getIsCorrect());
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("完成听写词语 - 标记为正确")
        void completeWord_markCorrect() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.completeWord(TASK_ID, TEST_WORD, true);

            // Then
            assertNotNull(result);
            assertTrue(result.getIsCorrect());
        }

        @Test
        @DisplayName("完成听写词语 - 标记为错误")
        void completeWord_markIncorrect() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord));
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.completeWord(TASK_ID, TEST_WORD, false);

            // Then
            assertNotNull(result);
            assertFalse(result.getIsCorrect());
        }
    }

    @Nested
    @DisplayName("recordWord 方法测试")
    class RecordWordTests {

        @Test
        @DisplayName("记录词语 - 成功创建记录")
        void recordWord_success() {
            // Given
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(RECORD_ID);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.recordWord(TASK_ID, TEST_WORD, true, 0);

            // Then
            assertNotNull(result);
            assertEquals(TASK_ID, result.getTaskId());
            assertEquals(TEST_WORD, result.getWord());
            assertTrue(result.getIsCorrect());
            assertEquals(0, result.getErrorCount());
            assertEquals(1, result.getReadCount());
            assertNotNull(result.getStartTime());
            assertNotNull(result.getEndTime());
            verify(recordRepository).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("记录词语 - errorCount为null时设置为0")
        void recordWord_nullErrorCount_setsToZero() {
            // Given
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(RECORD_ID);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.recordWord(TASK_ID, TEST_WORD, false, null);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getErrorCount());
        }

        @Test
        @DisplayName("记录词语 - 标记为错误并设置错误次数")
        void recordWord_incorrectWithErrors() {
            // Given
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(RECORD_ID);
                return savedRecord;
            });

            // When
            TaskRecord result = taskRecordService.recordWord(TASK_ID, TEST_WORD, false, 3);

            // Then
            assertNotNull(result);
            assertFalse(result.getIsCorrect());
            assertEquals(3, result.getErrorCount());
        }
    }

    @Nested
    @DisplayName("getRecordsByTaskId 方法测试")
    class GetRecordsByTaskIdTests {

        @Test
        @DisplayName("获取任务记录 - 成功")
        void getRecordsByTaskId_success() {
            // Given
            TaskRecord record2 = new TaskRecord();
            record2.setId(2L);
            record2.setTaskId(TASK_ID);
            record2.setWord("词语2");

            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Arrays.asList(testRecord, record2));

            // When
            List<TaskRecord> result = taskRecordService.getRecordsByTaskId(TASK_ID);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(recordRepository).findByTaskIdOrderByCreatedAtAsc(TASK_ID);
        }

        @Test
        @DisplayName("获取任务记录 - 空列表")
        void getRecordsByTaskId_emptyList() {
            // Given
            when(recordRepository.findByTaskIdOrderByCreatedAtAsc(TASK_ID))
                    .thenReturn(Collections.emptyList());

            // When
            List<TaskRecord> result = taskRecordService.getRecordsByTaskId(TASK_ID);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getErrorRecords 方法测试")
    class GetErrorRecordsTests {

        @Test
        @DisplayName("获取错误记录 - 成功")
        void getErrorRecords_success() {
            // Given
            TaskRecord errorRecord1 = new TaskRecord();
            errorRecord1.setId(1L);
            errorRecord1.setTaskId(TASK_ID);
            errorRecord1.setWord("错误词语1");
            errorRecord1.setIsCorrect(false);

            TaskRecord errorRecord2 = new TaskRecord();
            errorRecord2.setId(2L);
            errorRecord2.setTaskId(TASK_ID);
            errorRecord2.setWord("错误词语2");
            errorRecord2.setIsCorrect(false);

            when(recordRepository.findByTaskIdAndIsCorrectFalse(TASK_ID))
                    .thenReturn(Arrays.asList(errorRecord1, errorRecord2));

            // When
            List<TaskRecord> result = taskRecordService.getErrorRecords(TASK_ID);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            result.forEach(r -> assertFalse(r.getIsCorrect()));
            verify(recordRepository).findByTaskIdAndIsCorrectFalse(TASK_ID);
        }

        @Test
        @DisplayName("获取错误记录 - 无错误记录")
        void getErrorRecords_noErrors() {
            // Given
            when(recordRepository.findByTaskIdAndIsCorrectFalse(TASK_ID))
                    .thenReturn(Collections.emptyList());

            // When
            List<TaskRecord> result = taskRecordService.getErrorRecords(TASK_ID);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getCorrectCount 方法测试")
    class GetCorrectCountTests {

        @Test
        @DisplayName("获取正确数量 - 成功")
        void getCorrectCount_success() {
            // Given
            when(recordRepository.countByTaskIdAndIsCorrectTrue(TASK_ID)).thenReturn(5L);

            // When
            Long result = taskRecordService.getCorrectCount(TASK_ID);

            // Then
            assertEquals(5L, result);
            verify(recordRepository).countByTaskIdAndIsCorrectTrue(TASK_ID);
        }

        @Test
        @DisplayName("获取正确数量 - 无正确记录")
        void getCorrectCount_zero() {
            // Given
            when(recordRepository.countByTaskIdAndIsCorrectTrue(TASK_ID)).thenReturn(0L);

            // When
            Long result = taskRecordService.getCorrectCount(TASK_ID);

            // Then
            assertEquals(0L, result);
        }
    }

    @Nested
    @DisplayName("getErrorCount 方法测试")
    class GetErrorCountTests {

        @Test
        @DisplayName("获取错误数量 - 成功")
        void getErrorCount_success() {
            // Given
            when(recordRepository.countByTaskIdAndIsCorrectFalse(TASK_ID)).thenReturn(3L);

            // When
            Long result = taskRecordService.getErrorCount(TASK_ID);

            // Then
            assertEquals(3L, result);
            verify(recordRepository).countByTaskIdAndIsCorrectFalse(TASK_ID);
        }

        @Test
        @DisplayName("获取错误数量 - 无错误记录")
        void getErrorCount_zero() {
            // Given
            when(recordRepository.countByTaskIdAndIsCorrectFalse(TASK_ID)).thenReturn(0L);

            // When
            Long result = taskRecordService.getErrorCount(TASK_ID);

            // Then
            assertEquals(0L, result);
        }
    }

    @Nested
    @DisplayName("deleteRecordsByTaskId 方法测试")
    class DeleteRecordsByTaskIdTests {

        @Test
        @DisplayName("删除任务记录 - 成功")
        void deleteRecordsByTaskId_success() {
            // Given
            doNothing().when(recordRepository).deleteByTaskId(TASK_ID);

            // When
            taskRecordService.deleteRecordsByTaskId(TASK_ID);

            // Then
            verify(recordRepository).deleteByTaskId(TASK_ID);
        }
    }

    @Nested
    @DisplayName("saveTaskResults 方法测试")
    class SaveTaskResultsTests {

        @Test
        @DisplayName("保存任务结果 - 成功")
        void saveTaskResults_success() {
            // Given
            TaskResultRequest request = new TaskResultRequest();
            request.setTaskId(TASK_ID);

            TaskResultRequest.WordResult wordResult1 = new TaskResultRequest.WordResult();
            wordResult1.setWord("词语1");
            wordResult1.setIsCorrect(true);
            wordResult1.setErrorCount(0);

            TaskResultRequest.WordResult wordResult2 = new TaskResultRequest.WordResult();
            wordResult2.setWord("词语2");
            wordResult2.setIsCorrect(false);
            wordResult2.setErrorCount(2);

            request.setWordResults(Arrays.asList(wordResult1, wordResult2));

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
            doNothing().when(recordRepository).deleteByTaskId(TASK_ID);
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(System.currentTimeMillis());
                return savedRecord;
            });
            when(taskRepository.save(any(DictationTask.class))).thenReturn(testTask);

            // When
            List<TaskRecord> result = taskRecordService.saveTaskResults(request);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(taskRepository).findById(TASK_ID);
            verify(recordRepository).deleteByTaskId(TASK_ID);
            verify(recordRepository, times(2)).save(any(TaskRecord.class));
            verify(taskRepository).save(any(DictationTask.class));
            assertEquals(DictationTask.TaskStatus.COMPLETED, testTask.getStatus());
        }

        @Test
        @DisplayName("保存任务结果 - errorCount为null时设置为0")
        void saveTaskResults_nullErrorCount_setsToZero() {
            // Given
            TaskResultRequest request = new TaskResultRequest();
            request.setTaskId(TASK_ID);

            TaskResultRequest.WordResult wordResult = new TaskResultRequest.WordResult();
            wordResult.setWord("词语");
            wordResult.setIsCorrect(true);
            wordResult.setErrorCount(null);

            request.setWordResults(Arrays.asList(wordResult));

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
            doNothing().when(recordRepository).deleteByTaskId(TASK_ID);
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(1L);
                return savedRecord;
            });
            when(taskRepository.save(any(DictationTask.class))).thenReturn(testTask);

            // When
            List<TaskRecord> result = taskRecordService.saveTaskResults(request);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getErrorCount());
        }

        @Test
        @DisplayName("保存任务结果 - 任务不存在抛出异常")
        void saveTaskResults_taskNotFound_throwsException() {
            // Given
            TaskResultRequest request = new TaskResultRequest();
            request.setTaskId(TASK_ID);
            request.setWordResults(Collections.emptyList());

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskRecordService.saveTaskResults(request);
            });
            verify(recordRepository, never()).deleteByTaskId(anyLong());
            verify(recordRepository, never()).save(any(TaskRecord.class));
        }

        @Test
        @DisplayName("保存任务结果 - 空词语列表")
        void saveTaskResults_emptyWordResults() {
            // Given
            TaskResultRequest request = new TaskResultRequest();
            request.setTaskId(TASK_ID);
            request.setWordResults(Collections.emptyList());

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
            doNothing().when(recordRepository).deleteByTaskId(TASK_ID);
            when(taskRepository.save(any(DictationTask.class))).thenReturn(testTask);

            // When
            List<TaskRecord> result = taskRecordService.saveTaskResults(request);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(taskRepository).findById(TASK_ID);
            verify(recordRepository).deleteByTaskId(TASK_ID);
            verify(recordRepository, never()).save(any(TaskRecord.class));
            verify(taskRepository).save(any(DictationTask.class));
        }

        @Test
        @DisplayName("保存任务结果 - 更新任务状态为COMPLETED")
        void saveTaskResults_updatesTaskStatusToCompleted() {
            // Given
            TaskResultRequest request = new TaskResultRequest();
            request.setTaskId(TASK_ID);

            TaskResultRequest.WordResult wordResult = new TaskResultRequest.WordResult();
            wordResult.setWord("词语");
            wordResult.setIsCorrect(true);
            wordResult.setErrorCount(0);

            request.setWordResults(Arrays.asList(wordResult));

            testTask.setStatus(DictationTask.TaskStatus.IN_PROGRESS);

            when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(testTask));
            doNothing().when(recordRepository).deleteByTaskId(TASK_ID);
            when(recordRepository.save(any(TaskRecord.class))).thenAnswer(invocation -> {
                TaskRecord savedRecord = invocation.getArgument(0);
                savedRecord.setId(1L);
                return savedRecord;
            });
            when(taskRepository.save(any(DictationTask.class))).thenReturn(testTask);

            // When
            taskRecordService.saveTaskResults(request);

            // Then
            assertEquals(DictationTask.TaskStatus.COMPLETED, testTask.getStatus());
        }
    }
}