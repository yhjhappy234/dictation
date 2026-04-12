package com.yhj.dictation.service;

import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.dto.BatchResponse;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DictationBatchRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * DictationBatchService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationBatchServiceTest {

    @Mock
    private DictationBatchRepository batchRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private DictationBatchService batchService;

    private DictationBatch testBatch;
    private BatchCreateRequest testRequest;

    @BeforeEach
    void setUp() {
        testBatch = new DictationBatch();
        testBatch.setId(1L);
        testBatch.setBatchName("Test Batch");
        testBatch.setCreatedAt(LocalDateTime.now());
        testBatch.setStatus(DictationBatch.BatchStatus.CREATED);
        testBatch.setTotalWords(5);
        testBatch.setCompletedWords(0);

        testRequest = new BatchCreateRequest();
        testRequest.setBatchName("Test Batch");
        testRequest.setWords("word1 word2 word3");
    }

    @Nested
    @DisplayName("createBatch 方法测试")
    class CreateBatchTests {

        @Test
        @DisplayName("创建批次 - 成功创建空词语批次")
        void createBatch_emptyWords_success() {
            // Given
            BatchCreateRequest request = new BatchCreateRequest();
            request.setBatchName("Empty Batch");
            request.setWords(null);

            DictationBatch savedBatch = new DictationBatch();
            savedBatch.setId(1L);
            savedBatch.setBatchName("Empty Batch");
            savedBatch.setStatus(DictationBatch.BatchStatus.CREATED);
            savedBatch.setTotalWords(0);
            savedBatch.setCompletedWords(0);

            when(batchRepository.save(any(DictationBatch.class))).thenReturn(savedBatch);

            // When
            DictationBatch result = batchService.createBatch(request);

            // Then
            assertNotNull(result);
            assertEquals("Empty Batch", result.getBatchName());
            assertEquals(0, result.getTotalWords());
            verify(batchRepository, times(1)).save(any(DictationBatch.class));
            verify(wordRepository, never()).save(any(Word.class));
        }

        @Test
        @DisplayName("创建批次 - 成功创建包含词语的批次")
        void createBatch_withWords_success() {
            // Given
            DictationBatch firstSavedBatch = new DictationBatch();
            firstSavedBatch.setId(1L);
            firstSavedBatch.setBatchName("Test Batch");
            firstSavedBatch.setStatus(DictationBatch.BatchStatus.CREATED);
            firstSavedBatch.setTotalWords(0);
            firstSavedBatch.setCompletedWords(0);

            DictationBatch finalSavedBatch = new DictationBatch();
            finalSavedBatch.setId(1L);
            finalSavedBatch.setBatchName("Test Batch");
            finalSavedBatch.setStatus(DictationBatch.BatchStatus.CREATED);
            finalSavedBatch.setTotalWords(3);
            finalSavedBatch.setCompletedWords(0);

            when(batchRepository.save(any(DictationBatch.class)))
                    .thenReturn(firstSavedBatch)
                    .thenReturn(finalSavedBatch);
            when(wordRepository.save(any(Word.class))).thenReturn(new Word());

            // When
            DictationBatch result = batchService.createBatch(testRequest);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getTotalWords());
            verify(batchRepository, times(2)).save(any(DictationBatch.class));
            verify(wordRepository, times(3)).save(any(Word.class));
        }

        @Test
        @DisplayName("创建批次 - 词语包含空字符串时过滤处理")
        void createBatch_withEmptyWordStrings_filtersEmpty() {
            // Given
            BatchCreateRequest request = new BatchCreateRequest();
            request.setBatchName("Test Batch");
            request.setWords("word1   word2  ");  // Contains empty parts

            DictationBatch firstSavedBatch = new DictationBatch();
            firstSavedBatch.setId(1L);
            firstSavedBatch.setTotalWords(0);

            DictationBatch finalSavedBatch = new DictationBatch();
            finalSavedBatch.setId(1L);
            finalSavedBatch.setTotalWords(2);

            when(batchRepository.save(any(DictationBatch.class)))
                    .thenReturn(firstSavedBatch)
                    .thenReturn(finalSavedBatch);
            when(wordRepository.save(any(Word.class))).thenReturn(new Word());

            // When
            DictationBatch result = batchService.createBatch(request);

            // Then
            assertEquals(2, result.getTotalWords());
            verify(wordRepository, times(2)).save(any(Word.class));
        }

        @Test
        @DisplayName("创建批次 - 空字符串词语")
        void createBatch_emptyStringWords() {
            // Given
            BatchCreateRequest request = new BatchCreateRequest();
            request.setBatchName("Test Batch");
            request.setWords("");

            DictationBatch savedBatch = new DictationBatch();
            savedBatch.setId(1L);
            savedBatch.setTotalWords(0);

            when(batchRepository.save(any(DictationBatch.class))).thenReturn(savedBatch);

            // When
            DictationBatch result = batchService.createBatch(request);

            // Then
            assertEquals(0, result.getTotalWords());
            verify(wordRepository, never()).save(any(Word.class));
        }
    }

    @Nested
    @DisplayName("createBatchResponse 方法测试")
    class CreateBatchResponseTests {

        @Test
        @DisplayName("创建批次响应 - 成功")
        void createBatchResponse_success() {
            // Given
            when(batchRepository.save(any(DictationBatch.class))).thenReturn(testBatch);

            // When
            BatchResponse response = batchService.createBatchResponse(testRequest);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.getId());
            assertEquals("Test Batch", response.getBatchName());
        }
    }

    @Nested
    @DisplayName("getAllBatches 方法测试")
    class GetAllBatchesTests {

        @Test
        @DisplayName("获取所有批次 - 成功返回列表")
        void getAllBatches_success() {
            // Given
            List<DictationBatch> batches = Arrays.asList(testBatch);
            when(batchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(batches);

            // When
            List<DictationBatch> result = batchService.getAllBatches();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testBatch, result.get(0));
        }

        @Test
        @DisplayName("获取所有批次 - 返回空列表")
        void getAllBatches_emptyList() {
            // Given
            when(batchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            // When
            List<DictationBatch> result = batchService.getAllBatches();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getBatchById 方法测试")
    class GetBatchByIdTests {

        @Test
        @DisplayName("根据ID获取批次 - 成功找到")
        void getBatchById_found() {
            // Given
            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));

            // When
            Optional<DictationBatch> result = batchService.getBatchById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testBatch, result.get());
        }

        @Test
        @DisplayName("根据ID获取批次 - 未找到")
        void getBatchById_notFound() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<DictationBatch> result = batchService.getBatchById(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getBatchResponseById 方法测试")
    class GetBatchResponseByIdTests {

        @Test
        @DisplayName("根据ID获取批次响应 - 成功")
        void getBatchResponseById_success() {
            // Given
            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));

            // When
            BatchResponse response = batchService.getBatchResponseById(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.getId());
        }

        @Test
        @DisplayName("根据ID获取批次响应 - 批次不存在抛出异常")
        void getBatchResponseById_notFound_throwsException() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> batchService.getBatchResponseById(999L));
        }
    }

    @Nested
    @DisplayName("toBatchResponse 方法测试")
    class ToBatchResponseTests {

        @Test
        @DisplayName("转换为批次响应 - 有词语时计算进度")
        void toBatchResponse_withWords_calculatesProgress() {
            // Given
            testBatch.setTotalWords(5);
            testBatch.setCompletedWords(3);

            // When
            BatchResponse response = batchService.toBatchResponse(testBatch);

            // Then
            assertEquals(60.0, response.getProgress());
        }

        @Test
        @DisplayName("转换为批次响应 - 无词语时进度为0")
        void toBatchResponse_noWords_zeroProgress() {
            // Given
            testBatch.setTotalWords(0);
            testBatch.setCompletedWords(0);

            // When
            BatchResponse response = batchService.toBatchResponse(testBatch);

            // Then
            assertEquals(0.0, response.getProgress());
        }

        @Test
        @DisplayName("转换为批次响应 - 状态转换正确")
        void toBatchResponse_statusConversion() {
            // Given
            testBatch.setStatus(DictationBatch.BatchStatus.IN_PROGRESS);

            // When
            BatchResponse response = batchService.toBatchResponse(testBatch);

            // Then
            assertEquals("IN_PROGRESS", response.getStatus());
        }
    }

    @Nested
    @DisplayName("startBatch 方法测试")
    class StartBatchTests {

        @Test
        @DisplayName("开始批次 - 成功")
        void startBatch_success() {
            // Given
            DictationBatch inProgressBatch = new DictationBatch();
            inProgressBatch.setId(1L);
            inProgressBatch.setStatus(DictationBatch.BatchStatus.IN_PROGRESS);

            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
            when(batchRepository.save(any(DictationBatch.class))).thenReturn(inProgressBatch);

            // When
            DictationBatch result = batchService.startBatch(1L);

            // Then
            assertEquals(DictationBatch.BatchStatus.IN_PROGRESS, result.getStatus());
        }

        @Test
        @DisplayName("开始批次 - 批次不存在抛出异常")
        void startBatch_notFound_throwsException() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> batchService.startBatch(999L));
        }
    }

    @Nested
    @DisplayName("completeBatch 方法测试")
    class CompleteBatchTests {

        @Test
        @DisplayName("完成批次 - 成功")
        void completeBatch_success() {
            // Given
            DictationBatch completedBatch = new DictationBatch();
            completedBatch.setId(1L);
            completedBatch.setStatus(DictationBatch.BatchStatus.COMPLETED);

            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
            when(batchRepository.save(any(DictationBatch.class))).thenReturn(completedBatch);

            // When
            DictationBatch result = batchService.completeBatch(1L);

            // Then
            assertEquals(DictationBatch.BatchStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("完成批次 - 批次不存在抛出异常")
        void completeBatch_notFound_throwsException() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> batchService.completeBatch(999L));
        }
    }

    @Nested
    @DisplayName("cancelBatch 方法测试")
    class CancelBatchTests {

        @Test
        @DisplayName("取消批次 - 成功")
        void cancelBatch_success() {
            // Given
            DictationBatch cancelledBatch = new DictationBatch();
            cancelledBatch.setId(1L);
            cancelledBatch.setStatus(DictationBatch.BatchStatus.CANCELLED);

            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
            when(batchRepository.save(any(DictationBatch.class))).thenReturn(cancelledBatch);

            // When
            DictationBatch result = batchService.cancelBatch(1L);

            // Then
            assertEquals(DictationBatch.BatchStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("取消批次 - 批次不存在抛出异常")
        void cancelBatch_notFound_throwsException() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> batchService.cancelBatch(999L));
        }
    }

    @Nested
    @DisplayName("deleteBatch 方法测试")
    class DeleteBatchTests {

        @Test
        @DisplayName("删除批次 - 成功删除批次和词语")
        void deleteBatch_success() {
            // Given
            List<Word> words = Arrays.asList(new Word(), new Word());
            when(wordRepository.findByBatchIdOrderBySortOrder(1L)).thenReturn(words);

            // When
            batchService.deleteBatch(1L);

            // Then
            verify(wordRepository).findByBatchIdOrderBySortOrder(1L);
            verify(wordRepository).deleteAll(words);
            verify(batchRepository).deleteById(1L);
        }

        @Test
        @DisplayName("删除批次 - 批次没有词语")
        void deleteBatch_noWords() {
            // Given
            when(wordRepository.findByBatchIdOrderBySortOrder(1L)).thenReturn(Collections.emptyList());

            // When
            batchService.deleteBatch(1L);

            // Then
            verify(wordRepository).deleteAll(Collections.emptyList());
            verify(batchRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("getBatchesByStatus 方法测试")
    class GetBatchesByStatusTests {

        @Test
        @DisplayName("根据状态获取批次 - 成功")
        void getBatchesByStatus_success() {
            // Given
            List<DictationBatch> batches = Arrays.asList(testBatch);
            when(batchRepository.findByStatusOrderByCreatedAtDesc(DictationBatch.BatchStatus.CREATED))
                    .thenReturn(batches);

            // When
            List<DictationBatch> result = batchService.getBatchesByStatus(DictationBatch.BatchStatus.CREATED);

            // Then
            assertEquals(1, result.size());
            verify(batchRepository).findByStatusOrderByCreatedAtDesc(DictationBatch.BatchStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("getBatchesByDateRange 方法测试")
    class GetBatchesByDateRangeTests {

        @Test
        @DisplayName("根据日期范围获取批次 - 成功")
        void getBatchesByDateRange_success() {
            // Given
            LocalDateTime start = LocalDateTime.now().minusDays(1);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            List<DictationBatch> batches = Arrays.asList(testBatch);
            when(batchRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end))
                    .thenReturn(batches);

            // When
            List<DictationBatch> result = batchService.getBatchesByDateRange(start, end);

            // Then
            assertEquals(1, result.size());
            verify(batchRepository).findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
        }
    }

    @Nested
    @DisplayName("updateCompletedWords 方法测试")
    class UpdateCompletedWordsTests {

        @Test
        @DisplayName("更新完成数量 - 批次存在")
        void updateCompletedWords_batchExists() {
            // Given
            when(batchRepository.findById(1L)).thenReturn(Optional.of(testBatch));
            when(wordRepository.countByBatchIdAndStatus(1L, Word.WordStatus.COMPLETED)).thenReturn(3L);
            when(batchRepository.save(any(DictationBatch.class))).thenReturn(testBatch);

            // When
            batchService.updateCompletedWords(1L);

            // Then
            verify(batchRepository).findById(1L);
            verify(wordRepository).countByBatchIdAndStatus(1L, Word.WordStatus.COMPLETED);
            verify(batchRepository).save(any(DictationBatch.class));
        }

        @Test
        @DisplayName("更新完成数量 - 批次不存在")
        void updateCompletedWords_batchNotExists() {
            // Given
            when(batchRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            batchService.updateCompletedWords(999L);

            // Then
            verify(batchRepository).findById(999L);
            verify(wordRepository, never()).countByBatchIdAndStatus(anyLong(), any());
            verify(batchRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAllBatchResponses 方法测试")
    class GetAllBatchResponsesTests {

        @Test
        @DisplayName("获取所有批次响应 - 成功")
        void getAllBatchResponses_success() {
            // Given
            List<DictationBatch> batches = Arrays.asList(testBatch);
            when(batchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(batches);

            // When
            List<BatchResponse> responses = batchService.getAllBatchResponses();

            // Then
            assertEquals(1, responses.size());
            assertEquals(1L, responses.get(0).getId());
        }

        @Test
        @DisplayName("获取所有批次响应 - 多个批次")
        void getAllBatchResponses_multipleBatches() {
            // Given
            DictationBatch batch2 = new DictationBatch();
            batch2.setId(2L);
            batch2.setBatchName("Batch 2");
            batch2.setTotalWords(10);
            batch2.setCompletedWords(5);
            batch2.setStatus(DictationBatch.BatchStatus.IN_PROGRESS);

            List<DictationBatch> batches = Arrays.asList(testBatch, batch2);
            when(batchRepository.findAllByOrderByCreatedAtDesc()).thenReturn(batches);

            // When
            List<BatchResponse> responses = batchService.getAllBatchResponses();

            // Then
            assertEquals(2, responses.size());
        }
    }
}