package com.yhj.dictation.service;

import com.yhj.dictation.dto.SuggestionDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Suggestion;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DifficultWordRepository;
import com.yhj.dictation.repository.DictationRecordRepository;
import com.yhj.dictation.repository.SuggestionRepository;
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
 * SuggestionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private SuggestionRepository suggestionRepository;

    @Mock
    private DifficultWordRepository difficultWordRepository;

    @Mock
    private DictationRecordRepository recordRepository;

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private SuggestionService suggestionService;

    private Suggestion testSuggestion;
    private Word testWord;

    @BeforeEach
    void setUp() {
        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试");

        testSuggestion = new Suggestion();
        testSuggestion.setId(1L);
        testSuggestion.setWordId(1L);
        testSuggestion.setSuggestionType(Suggestion.SuggestionType.REVIEW_NEEDED);
        testSuggestion.setPriority(3);
        testSuggestion.setMessage("建议复习");
        testSuggestion.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createSuggestion 方法测试")
    class CreateSuggestionTests {

        @Test
        @DisplayName("创建建议 - 成功")
        void createSuggestion_success() {
            // Given
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            Suggestion result = suggestionService.createSuggestion(1L, Suggestion.SuggestionType.REVIEW_NEEDED, 3, "建议复习");

            // Then
            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.REVIEW_NEEDED, result.getSuggestionType());
            verify(suggestionRepository).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("创建建议 - 优先级为null时默认为1")
        void createSuggestion_nullPriority_defaultsToOne() {
            // Given
            Suggestion savedSuggestion = new Suggestion();
            savedSuggestion.setPriority(1);
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            Suggestion result = suggestionService.createSuggestion(1L, Suggestion.SuggestionType.REVIEW_NEEDED, null, "消息");

            // Then
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getSuggestionById 方法测试")
    class GetSuggestionByIdTests {

        @Test
        @DisplayName("根据ID获取建议 - 找到")
        void getSuggestionById_found() {
            // Given
            when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));

            // When
            Optional<Suggestion> result = suggestionService.getSuggestionById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testSuggestion, result.get());
        }

        @Test
        @DisplayName("根据ID获取建议 - 未找到")
        void getSuggestionById_notFound() {
            // Given
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<Suggestion> result = suggestionService.getSuggestionById(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getSuggestionsByWordId 方法测试")
    class GetSuggestionsByWordIdTests {

        @Test
        @DisplayName("获取词语建议 - 成功")
        void getSuggestionsByWordId_success() {
            // Given
            List<Suggestion> suggestions = Arrays.asList(testSuggestion);
            when(suggestionRepository.findByWordId(1L)).thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getSuggestionsByWordId(1L);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取词语建议 - 返回空列表")
        void getSuggestionsByWordId_emptyList() {
            // Given
            when(suggestionRepository.findByWordId(anyLong())).thenReturn(Collections.emptyList());

            // When
            List<Suggestion> result = suggestionService.getSuggestionsByWordId(999L);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getSuggestionsByType 方法测试")
    class GetSuggestionsByTypeTests {

        @Test
        @DisplayName("获取指定类型建议 - 成功")
        void getSuggestionsByType_success() {
            // Given
            List<Suggestion> suggestions = Arrays.asList(testSuggestion);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(Suggestion.SuggestionType.REVIEW_NEEDED))
                    .thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getSuggestionsByType(Suggestion.SuggestionType.REVIEW_NEEDED);

            // Then
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAllSuggestions 方法测试")
    class GetAllSuggestionsTests {

        @Test
        @DisplayName("获取所有建议 - 成功")
        void getAllSuggestions_success() {
            // Given
            List<Suggestion> suggestions = Arrays.asList(testSuggestion);
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getAllSuggestions();

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取所有建议 - 返回空列表")
        void getAllSuggestions_emptyList() {
            // Given
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(Collections.emptyList());

            // When
            List<Suggestion> result = suggestionService.getAllSuggestions();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("updatePriority 方法测试")
    class UpdatePriorityTests {

        @Test
        @DisplayName("更新优先级 - 成功")
        void updatePriority_success() {
            // Given
            when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            Suggestion result = suggestionService.updatePriority(1L, 4);

            // Then
            assertNotNull(result);
            verify(suggestionRepository).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("更新优先级 - 建议不存在抛出异常")
        void updatePriority_notFound_throwsException() {
            // Given
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> suggestionService.updatePriority(999L, 3));
        }

        @Test
        @DisplayName("更新优先级 - 大于5限制为5")
        void updatePriority_exceedMax() {
            // Given
            when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            suggestionService.updatePriority(1L, 10);

            // Then
            verify(suggestionRepository).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("更新优先级 - 小于1限制为1")
        void updatePriority_belowMin() {
            // Given
            when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            suggestionService.updatePriority(1L, 0);

            // Then
            verify(suggestionRepository).save(any(Suggestion.class));
        }
    }

    @Nested
    @DisplayName("updateMessage 方法测试")
    class UpdateMessageTests {

        @Test
        @DisplayName("更新消息 - 成功")
        void updateMessage_success() {
            // Given
            when(suggestionRepository.findById(1L)).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            Suggestion result = suggestionService.updateMessage(1L, "新消息");

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("更新消息 - 建议不存在抛出异常")
        void updateMessage_notFound_throwsException() {
            // Given
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> suggestionService.updateMessage(999L, "消息"));
        }
    }

    @Nested
    @DisplayName("deleteSuggestion 方法测试")
    class DeleteSuggestionTests {

        @Test
        @DisplayName("删除建议 - 成功")
        void deleteSuggestion_success() {
            // When
            suggestionService.deleteSuggestion(1L);

            // Then
            verify(suggestionRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteSuggestionsByWordId 方法测试")
    class DeleteSuggestionsByWordIdTests {

        @Test
        @DisplayName("删除词语建议 - 成功")
        void deleteSuggestionsByWordId_success() {
            // Given
            List<Suggestion> suggestions = Arrays.asList(testSuggestion);
            when(suggestionRepository.findByWordId(1L)).thenReturn(suggestions);

            // When
            suggestionService.deleteSuggestionsByWordId(1L);

            // Then
            verify(suggestionRepository).deleteAll(suggestions);
        }

        @Test
        @DisplayName("删除词语建议 - 没有建议")
        void deleteSuggestionsByWordId_empty() {
            // Given
            when(suggestionRepository.findByWordId(1L)).thenReturn(Collections.emptyList());

            // When
            suggestionService.deleteSuggestionsByWordId(1L);

            // Then
            verify(suggestionRepository).deleteAll(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("创建特定类型建议方法测试")
    class CreateSpecificSuggestionTests {

        @Test
        @DisplayName("创建复习建议 - 成功")
        void createReviewSuggestion_success() {
            // Given
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);

            // When
            Suggestion result = suggestionService.createReviewSuggestion(1L, "复习建议");

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("创建高难度建议 - 成功")
        void createHighDifficultySuggestion_success() {
            // Given
            Suggestion savedSuggestion = new Suggestion();
            savedSuggestion.setSuggestionType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            Suggestion result = suggestionService.createHighDifficultySuggestion(1L, "高难度");

            // Then
            assertEquals(Suggestion.SuggestionType.HIGH_DIFFICULTY, result.getSuggestionType());
        }

        @Test
        @DisplayName("创建常错词建议 - 成功")
        void createFrequentErrorSuggestion_success() {
            // Given
            Suggestion savedSuggestion = new Suggestion();
            savedSuggestion.setSuggestionType(Suggestion.SuggestionType.FREQUENT_ERROR);
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            Suggestion result = suggestionService.createFrequentErrorSuggestion(1L, "常错词");

            // Then
            assertEquals(Suggestion.SuggestionType.FREQUENT_ERROR, result.getSuggestionType());
        }

        @Test
        @DisplayName("创建反应时间长建议 - 成功")
        void createLongDurationSuggestion_success() {
            // Given
            Suggestion savedSuggestion = new Suggestion();
            savedSuggestion.setSuggestionType(Suggestion.SuggestionType.LONG_DURATION);
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            Suggestion result = suggestionService.createLongDurationSuggestion(1L, "时间长");

            // Then
            assertEquals(Suggestion.SuggestionType.LONG_DURATION, result.getSuggestionType());
        }

        @Test
        @DisplayName("创建新词建议 - 成功")
        void createNewWordSuggestion_success() {
            // Given
            Suggestion savedSuggestion = new Suggestion();
            savedSuggestion.setSuggestionType(Suggestion.SuggestionType.NEW_WORD);
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(savedSuggestion);

            // When
            Suggestion result = suggestionService.createNewWordSuggestion(1L, "新词");

            // Then
            assertEquals(Suggestion.SuggestionType.NEW_WORD, result.getSuggestionType());
        }
    }

    @Nested
    @DisplayName("获取特定类型建议方法测试")
    class GetSpecificSuggestionTests {

        @Test
        @DisplayName("获取需要复习建议 - 成功")
        void getReviewNeededSuggestions_success() {
            // Given
            List<Suggestion> suggestions = Arrays.asList(testSuggestion);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(Suggestion.SuggestionType.REVIEW_NEEDED))
                    .thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getReviewNeededSuggestions();

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取高难度建议 - 成功")
        void getHighDifficultySuggestions_success() {
            // Given
            Suggestion highDifficulty = new Suggestion();
            highDifficulty.setSuggestionType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
            List<Suggestion> suggestions = Arrays.asList(highDifficulty);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(Suggestion.SuggestionType.HIGH_DIFFICULTY))
                    .thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getHighDifficultySuggestions();

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取常错词建议 - 成功")
        void getFrequentErrorSuggestions_success() {
            // Given
            Suggestion frequentError = new Suggestion();
            frequentError.setSuggestionType(Suggestion.SuggestionType.FREQUENT_ERROR);
            List<Suggestion> suggestions = Arrays.asList(frequentError);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(Suggestion.SuggestionType.FREQUENT_ERROR))
                    .thenReturn(suggestions);

            // When
            List<Suggestion> result = suggestionService.getFrequentErrorSuggestions();

            // Then
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAllSuggestionDTOs 方法测试")
    class GetAllSuggestionDTOsTests {

        @Test
        @DisplayName("获取所有建议DTO - 成功")
        void getAllSuggestionDTOs_success() {
            // Given
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(Arrays.asList(testSuggestion));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

            // When
            List<SuggestionDTO> result = suggestionService.getAllSuggestionDTOs();

            // Then
            assertEquals(1, result.size());
            assertEquals("测试", result.get(0).getWordText());
        }

        @Test
        @DisplayName("获取所有建议DTO - 词语不存在")
        void getAllSuggestionDTOs_wordNotFound() {
            // Given
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(Arrays.asList(testSuggestion));
            when(wordRepository.findById(1L)).thenReturn(Optional.empty());

            // When
            List<SuggestionDTO> result = suggestionService.getAllSuggestionDTOs();

            // Then
            assertEquals(1, result.size());
            assertNull(result.get(0).getWordText());
        }
    }

    @Nested
    @DisplayName("generateSuggestions 方法测试")
    class GenerateSuggestionsTests {

        @Test
        @DisplayName("生成建议 - 成功")
        void generateSuggestions_success() {
            // Given
            DictationRecord record = new DictationRecord();
            record.setWordId(1L);
            record.setRepeatCount(3);
            record.setDurationSeconds(15);

            Word word = new Word();
            word.setId(1L);
            word.setWordText("测试");

            DifficultWord difficultWord = new DifficultWord();
            difficultWord.setWordId(1L);
            difficultWord.setErrorCount(5);
            difficultWord.setMasteryLevel(1);

            when(recordRepository.findByBatchId(1L)).thenReturn(Arrays.asList(record));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(word));
            when(suggestionRepository.save(any(Suggestion.class))).thenReturn(testSuggestion);
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Arrays.asList(difficultWord));

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, atLeast(1)).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 词语不存在时跳过")
        void generateSuggestions_wordNotFound() {
            // Given
            DictationRecord record = new DictationRecord();
            record.setWordId(1L);
            record.setRepeatCount(3);

            when(recordRepository.findByBatchId(1L)).thenReturn(Arrays.asList(record));
            when(wordRepository.findById(1L)).thenReturn(Optional.empty());
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Collections.emptyList());

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 无记录")
        void generateSuggestions_noRecords() {
            // Given
            when(recordRepository.findByBatchId(1L)).thenReturn(Collections.emptyList());
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Collections.emptyList());

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 重复次数小于等于2不创建")
        void generateSuggestions_lowRepeatCount() {
            // Given
            DictationRecord record = new DictationRecord();
            record.setWordId(1L);
            record.setRepeatCount(2);
            record.setDurationSeconds(5);

            when(recordRepository.findByBatchId(1L)).thenReturn(Arrays.asList(record));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Collections.emptyList());

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 反应时间短不创建")
        void generateSuggestions_shortDuration() {
            // Given
            DictationRecord record = new DictationRecord();
            record.setWordId(1L);
            record.setRepeatCount(0);
            record.setDurationSeconds(5);

            when(recordRepository.findByBatchId(1L)).thenReturn(Arrays.asList(record));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Collections.emptyList());

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - durationSeconds为null不创建")
        void generateSuggestions_nullDuration() {
            // Given
            DictationRecord record = new DictationRecord();
            record.setWordId(1L);
            record.setRepeatCount(0);
            record.setDurationSeconds(null);

            when(recordRepository.findByBatchId(1L)).thenReturn(Arrays.asList(record));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3))
                    .thenReturn(Collections.emptyList());

            // When
            suggestionService.generateSuggestions(1L);

            // Then
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }
    }

    @Nested
    @DisplayName("toSuggestionDTO 方法测试")
    class ToSuggestionDTOTests {

        @Test
        @DisplayName("转换为DTO - 成功")
        void toSuggestionDTO_success() {
            // Given
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(Arrays.asList(testSuggestion));
            when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

            // When
            List<SuggestionDTO> result = suggestionService.getAllSuggestionDTOs();

            // Then
            assertEquals(1L, result.get(0).getId());
            assertEquals("REVIEW_NEEDED", result.get(0).getSuggestionType());
            assertEquals(3, result.get(0).getPriority());
        }
    }
}