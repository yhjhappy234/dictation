package com.yhj.dictation.service;

import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DifficultWordRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * DifficultWordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DifficultWordServiceTest {

    @Mock
    private DifficultWordRepository difficultWordRepository;

    @Mock
    private DictationRecordRepository recordRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private DifficultWordService difficultWordService;

    private DifficultWord testDifficultWord;
    private Word testWord;

    @BeforeEach
    void setUp() {
        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试");
        testWord.setPinyin("ceshi");

        testDifficultWord = new DifficultWord();
        testDifficultWord.setId(1L);
        testDifficultWord.setWordId(1L);
        testDifficultWord.setErrorCount(3);
        testDifficultWord.setMasteryLevel(2);
        testDifficultWord.setCreatedAt(LocalDateTime.now());
        testDifficultWord.setUpdatedAt(LocalDateTime.now());
        testDifficultWord.setAvgDurationSeconds(10);
    }

    @Nested
    @DisplayName("addOrUpdateDifficultWord 方法测试")
    class AddOrUpdateDifficultWordTests {

        @Test
        @DisplayName("添加或更新生词 - 新添加")
        void addOrUpdateDifficultWord_newAdd() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.empty());
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(15.0);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.addOrUpdateDifficultWord(1L);

            // Then
            assertNotNull(result);
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("添加或更新生词 - 更新已存在的")
        void addOrUpdateDifficultWord_updateExisting() {
            // Given
            DifficultWord existingWord = new DifficultWord();
            existingWord.setId(1L);
            existingWord.setWordId(1L);
            existingWord.setErrorCount(2);
            existingWord.setMasteryLevel(3);

            DifficultWord updatedWord = new DifficultWord();
            updatedWord.setErrorCount(3);
            updatedWord.setMasteryLevel(2);

            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(existingWord));
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(20.0);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(updatedWord);

            // When
            DifficultWord result = difficultWordService.addOrUpdateDifficultWord(1L);

            // Then
            assertNotNull(result);
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("添加或更新生词 - masteryLevel已为0不降低")
        void addOrUpdateDifficultWord_masteryLevelZeroNoDecrease() {
            // Given
            DifficultWord existingWord = new DifficultWord();
            existingWord.setWordId(1L);
            existingWord.setMasteryLevel(0);
            existingWord.setErrorCount(1);

            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(existingWord));
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(null);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(existingWord);

            // When
            DifficultWord result = difficultWordService.addOrUpdateDifficultWord(1L);

            // Then
            assertEquals(0, result.getMasteryLevel());
        }

        @Test
        @DisplayName("添加或更新生词 - avgDuration为null")
        void addOrUpdateDifficultWord_nullAvgDuration() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.empty());
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(null);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.addOrUpdateDifficultWord(1L);

            // Then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getDifficultWordById 方法测试")
    class GetDifficultWordByIdTests {

        @Test
        @DisplayName("根据ID获取生词 - 找到")
        void getDifficultWordById_found() {
            // Given
            when(difficultWordRepository.findById(1L)).thenReturn(Optional.of(testDifficultWord));

            // When
            Optional<DifficultWord> result = difficultWordService.getDifficultWordById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testDifficultWord, result.get());
        }

        @Test
        @DisplayName("根据ID获取生词 - 未找到")
        void getDifficultWordById_notFound() {
            // Given
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<DifficultWord> result = difficultWordService.getDifficultWordById(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getDifficultWordByWordId 方法测试")
    class GetDifficultWordByWordIdTests {

        @Test
        @DisplayName("根据词语ID获取生词 - 找到")
        void getDifficultWordByWordId_found() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));

            // When
            Optional<DifficultWord> result = difficultWordService.getDifficultWordByWordId(1L);

            // Then
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("根据词语ID获取生词 - 未找到")
        void getDifficultWordByWordId_notFound() {
            // Given
            when(difficultWordRepository.findByWordId(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<DifficultWord> result = difficultWordService.getDifficultWordByWordId(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getAllDifficultWords 方法测试")
    class GetAllDifficultWordsTests {

        @Test
        @DisplayName("获取所有生词 - 成功")
        void getAllDifficultWords_success() {
            // Given
            List<DifficultWord> words = Arrays.asList(testDifficultWord);
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(words);

            // When
            List<DifficultWord> result = difficultWordService.getAllDifficultWords();

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取所有生词 - 返回空列表")
        void getAllDifficultWords_emptyList() {
            // Given
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(Collections.emptyList());

            // When
            List<DifficultWord> result = difficultWordService.getAllDifficultWords();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getDifficultWordsByMasteryLevel 方法测试")
    class GetDifficultWordsByMasteryLevelTests {

        @Test
        @DisplayName("获取指定掌握级别的生词 - 成功")
        void getDifficultWordsByMasteryLevel_success() {
            // Given
            List<DifficultWord> words = Arrays.asList(testDifficultWord);
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3)).thenReturn(words);

            // When
            List<DifficultWord> result = difficultWordService.getDifficultWordsByMasteryLevel(3);

            // Then
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getRecommendedDifficultWords 方法测试")
    class GetRecommendedDifficultWordsTests {

        @Test
        @DisplayName("获取推荐生词 - 成功")
        void getRecommendedDifficultWords_success() {
            // Given
            List<DifficultWord> words = Arrays.asList(testDifficultWord);
            when(difficultWordRepository.findDifficultWords(3, 10)).thenReturn(words);

            // When
            List<DifficultWord> result = difficultWordService.getRecommendedDifficultWords(3, 10);

            // Then
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("updateMasteryLevel 方法测试")
    class UpdateMasteryLevelTests {

        @Test
        @DisplayName("更新掌握级别 - 成功")
        void updateMasteryLevel_success() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.updateMasteryLevel(1L, 4);

            // Then
            assertNotNull(result);
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("更新掌握级别 - 生词不存在抛出异常")
        void updateMasteryLevel_notFound_throwsException() {
            // Given
            when(difficultWordRepository.findByWordId(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> difficultWordService.updateMasteryLevel(999L, 3));
        }

        @Test
        @DisplayName("更新掌握级别 - 级别大于5限制为5")
        void updateMasteryLevel_exceedMaxLimit() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            difficultWordService.updateMasteryLevel(1L, 10);

            // Then
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("更新掌握级别 - 级别小于0限制为0")
        void updateMasteryLevel_belowMinLimit() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            difficultWordService.updateMasteryLevel(1L, -5);

            // Then
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }
    }

    @Nested
    @DisplayName("increaseMasteryLevel 方法测试")
    class IncreaseMasteryLevelTests {

        @Test
        @DisplayName("增加掌握级别 - 成功")
        void increaseMasteryLevel_success() {
            // Given
            DifficultWord updatedWord = new DifficultWord();
            updatedWord.setMasteryLevel(3);
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(updatedWord);

            // When
            DifficultWord result = difficultWordService.increaseMasteryLevel(1L);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("增加掌握级别 - 生词不存在返回null")
        void increaseMasteryLevel_notFound_returnsNull() {
            // Given
            when(difficultWordRepository.findByWordId(anyLong())).thenReturn(Optional.empty());

            // When
            DifficultWord result = difficultWordService.increaseMasteryLevel(999L);

            // Then
            assertNull(result);
        }

        @Test
        @DisplayName("增加掌握级别 - 已为5不增加")
        void increaseMasteryLevel_alreadyMax() {
            // Given
            testDifficultWord.setMasteryLevel(5);
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.increaseMasteryLevel(1L);

            // Then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("decreaseMasteryLevel 方法测试")
    class DecreaseMasteryLevelTests {

        @Test
        @DisplayName("减少掌握级别 - 成功")
        void decreaseMasteryLevel_success() {
            // Given
            testDifficultWord.setMasteryLevel(4);
            testDifficultWord.setErrorCount(2);

            DifficultWord updatedWord = new DifficultWord();
            updatedWord.setMasteryLevel(3);
            updatedWord.setErrorCount(3);

            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(updatedWord);

            // When
            DifficultWord result = difficultWordService.decreaseMasteryLevel(1L);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("减少掌握级别 - 不存在时创建新记录")
        void decreaseMasteryLevel_notFound_createNew() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.empty());
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(10.0);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.decreaseMasteryLevel(1L);

            // Then
            assertNotNull(result);
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("减少掌握级别 - 已为0不减少")
        void decreaseMasteryLevel_alreadyMin() {
            // Given
            testDifficultWord.setMasteryLevel(0);
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            DifficultWord result = difficultWordService.decreaseMasteryLevel(1L);

            // Then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("deleteDifficultWord 方法测试")
    class DeleteDifficultWordTests {

        @Test
        @DisplayName("删除生词 - 成功")
        void deleteDifficultWord_success() {
            // When
            difficultWordService.deleteDifficultWord(1L);

            // Then
            verify(difficultWordRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteDifficultWordByWordId 方法测试")
    class DeleteDifficultWordByWordIdTests {

        @Test
        @DisplayName("根据词语ID删除生词 - 找到并删除")
        void deleteDifficultWordByWordId_found() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));

            // When
            difficultWordService.deleteDifficultWordByWordId(1L);

            // Then
            verify(difficultWordRepository).delete(testDifficultWord);
        }

        @Test
        @DisplayName("根据词语ID删除生词 - 未找到不执行删除")
        void deleteDifficultWordByWordId_notFound() {
            // Given
            when(difficultWordRepository.findByWordId(anyLong())).thenReturn(Optional.empty());

            // When
            difficultWordService.deleteDifficultWordByWordId(999L);

            // Then
            verify(difficultWordRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("handlePracticeSuccess 方法测试")
    class HandlePracticeSuccessTests {

        @Test
        @DisplayName("处理练习成功 - 存在生词时更新")
        void handlePracticeSuccess_exists() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            difficultWordService.handlePracticeSuccess(1L);

            // Then
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("处理练习成功 - 不存在时不操作")
        void handlePracticeSuccess_notExists() {
            // Given
            when(difficultWordRepository.findByWordId(anyLong())).thenReturn(Optional.empty());

            // When
            difficultWordService.handlePracticeSuccess(999L);

            // Then
            verify(difficultWordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handlePracticeFailure 方法测试")
    class HandlePracticeFailureTests {

        @Test
        @DisplayName("处理练习失败 - 添加或更新生词")
        void handlePracticeFailure_success() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.empty());
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(10.0);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);

            // When
            difficultWordService.handlePracticeFailure(1L);

            // Then
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }
    }

    @Nested
    @DisplayName("getDifficultWords DTO方法测试")
    class GetDifficultWordsDTOTests {

        @Test
        @DisplayName("获取生词DTO列表 - 成功")
        void getDifficultWords_success() {
            // Given
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(Arrays.asList(testDifficultWord));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

            // When
            List<DifficultWordDTO> result = difficultWordService.getDifficultWords();

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取生词DTO列表 - 词语不存在")
        void getDifficultWords_wordNotFound() {
            // Given
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(Arrays.asList(testDifficultWord));
            when(wordRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            List<DifficultWordDTO> result = difficultWordService.getDifficultWords();

            // Then
            assertEquals(1, result.size());
            assertNull(result.get(0).getWordText());
        }
    }

    @Nested
    @DisplayName("addDifficultWordDTO 方法测试")
    class AddDifficultWordDTOTests {

        @Test
        @DisplayName("添加生词返回DTO - 成功")
        void addDifficultWordDTO_success() {
            // Given
            when(difficultWordRepository.findByWordId(1L)).thenReturn(Optional.empty());
            when(recordRepository.findAvgDurationByWordId(1L)).thenReturn(10.0);
            when(difficultWordRepository.save(any(DifficultWord.class))).thenReturn(testDifficultWord);
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

            // When
            DifficultWordDTO result = difficultWordService.addDifficultWordDTO(1L);

            // Then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("removeDifficultWord 方法测试")
    class RemoveDifficultWordTests {

        @Test
        @DisplayName("移除生词 - 成功")
        void removeDifficultWord_success() {
            // Given
            when(difficultWordRepository.findById(1L)).thenReturn(Optional.of(testDifficultWord));

            // When
            difficultWordService.removeDifficultWord(1L);

            // Then
            verify(difficultWordRepository).delete(testDifficultWord);
        }

        @Test
        @DisplayName("移除生词 - 不存在抛出异常")
        void removeDifficultWord_notFound_throwsException() {
            // Given
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> difficultWordService.removeDifficultWord(999L));
        }
    }

    @Nested
    @DisplayName("toDifficultWordDTO 方法测试")
    class ToDifficultWordDTOTests {

        @Test
        @DisplayName("转换为DTO - 成功")
        void toDifficultWordDTO_success() {
            // Given
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(Arrays.asList(testDifficultWord));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

            // When
            List<DifficultWordDTO> result = difficultWordService.getDifficultWords();

            // Then
            assertEquals(1L, result.get(0).getId());
            assertEquals("测试", result.get(0).getWordText());
            assertEquals("ceshi", result.get(0).getPinyin());
        }
    }
}