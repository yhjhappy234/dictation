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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private DictationRecord testRecord;
    private DifficultWord testDifficultWord;

    @BeforeEach
    void setUp() {
        testSuggestion = new Suggestion();
        testSuggestion.setId(1L);
        testSuggestion.setWordId(1L);
        testSuggestion.setSuggestionType(Suggestion.SuggestionType.REVIEW_NEEDED);
        testSuggestion.setPriority(3);
        testSuggestion.setMessage("测试建议");
        testSuggestion.setCreatedAt(LocalDateTime.now());

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试词");
        testWord.setBatchId(1L);

        testRecord = new DictationRecord();
        testRecord.setId(1L);
        testRecord.setWordId(1L);
        testRecord.setBatchId(1L);
        testRecord.setRepeatCount(3);
        testRecord.setDurationSeconds(15);
        testRecord.setStatus(DictationRecord.RecordStatus.COMPLETED);

        testDifficultWord = new DifficultWord();
        testDifficultWord.setId(1L);
        testDifficultWord.setWordText("测试词");
        testDifficultWord.setErrorCount(5);
        testDifficultWord.setMasteryLevel(2);
    }

    @Nested
    @DisplayName("createSuggestion 方法测试")
    class CreateSuggestionTests {

        @Test
        @DisplayName("创建建议")
        void createSuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> {
                Suggestion s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });

            Suggestion result = suggestionService.createSuggestion(1L, Suggestion.SuggestionType.REVIEW_NEEDED, 3, "测试");

            assertNotNull(result);
            assertEquals(1L, result.getWordId());
            assertEquals(Suggestion.SuggestionType.REVIEW_NEEDED, result.getSuggestionType());
            assertEquals(3, result.getPriority());
            assertEquals("测试", result.getMessage());
        }

        @Test
        @DisplayName("创建建议 - 默认优先级")
        void createSuggestionDefaultPriority() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createSuggestion(1L, Suggestion.SuggestionType.REVIEW_NEEDED, null, "测试");

            assertNotNull(result);
            assertEquals(1, result.getPriority()); // 默认优先级为1
        }
    }

    @Nested
    @DisplayName("getSuggestionById 方法测试")
    class GetSuggestionByIdTests {

        @Test
        @DisplayName("获取建议成功")
        void getSuggestionById() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.of(testSuggestion));

            Optional<Suggestion> result = suggestionService.getSuggestionById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("建议不存在")
        void getSuggestionByIdNotFound() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            Optional<Suggestion> result = suggestionService.getSuggestionById(999L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getSuggestionsByWordId 方法测试")
    class GetSuggestionsByWordIdTests {

        @Test
        @DisplayName("获取词语建议")
        void getSuggestionsByWordId() {
            when(suggestionRepository.findByWordId(anyLong())).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getSuggestionsByWordId(1L);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getSuggestionsByType 方法测试")
    class GetSuggestionsByTypeTests {

        @Test
        @DisplayName("获取指定类型建议")
        void getSuggestionsByType() {
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(any())).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getSuggestionsByType(Suggestion.SuggestionType.REVIEW_NEEDED);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAllSuggestions 方法测试")
    class GetAllSuggestionsTests {

        @Test
        @DisplayName("获取所有建议")
        void getAllSuggestions() {
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getAllSuggestions();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("updatePriority 方法测试")
    class UpdatePriorityTests {

        @Test
        @DisplayName("更新优先级成功")
        void updatePriority() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.updatePriority(1L, 5);

            assertNotNull(result);
            assertEquals(5, result.getPriority());
        }

        @Test
        @DisplayName("更新优先级 - 超出上限")
        void updatePriorityBeyondMax() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.updatePriority(1L, 10);

            assertEquals(5, result.getPriority()); // 最大值为5
        }

        @Test
        @DisplayName("更新优先级 - 超出下限")
        void updatePriorityBelowMin() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.updatePriority(1L, 0);

            assertEquals(1, result.getPriority()); // 最小值为1
        }

        @Test
        @DisplayName("建议不存在")
        void updatePriorityNotFound() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> suggestionService.updatePriority(999L, 3));
        }
    }

    @Nested
    @DisplayName("updateMessage 方法测试")
    class UpdateMessageTests {

        @Test
        @DisplayName("更新消息成功")
        void updateMessage() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.of(testSuggestion));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.updateMessage(1L, "新消息");

            assertNotNull(result);
            assertEquals("新消息", result.getMessage());
        }

        @Test
        @DisplayName("建议不存在")
        void updateMessageNotFound() {
            when(suggestionRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> suggestionService.updateMessage(999L, "新消息"));
        }
    }

    @Nested
    @DisplayName("deleteSuggestion 方法测试")
    class DeleteSuggestionTests {

        @Test
        @DisplayName("删除建议")
        void deleteSuggestion() {
            doNothing().when(suggestionRepository).deleteById(anyLong());

            suggestionService.deleteSuggestion(1L);

            verify(suggestionRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteSuggestionsByWordId 方法测试")
    class DeleteSuggestionsByWordIdTests {

        @Test
        @DisplayName("删除词语所有建议")
        void deleteSuggestionsByWordId() {
            when(suggestionRepository.findByWordId(anyLong())).thenReturn(List.of(testSuggestion));
            doNothing().when(suggestionRepository).deleteAll(any());

            suggestionService.deleteSuggestionsByWordId(1L);

            verify(suggestionRepository).deleteAll(List.of(testSuggestion));
        }
    }

    @Nested
    @DisplayName("创建特定类型建议方法测试")
    class CreateSpecificSuggestionTests {

        @Test
        @DisplayName("创建复习建议")
        void createReviewSuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createReviewSuggestion(1L, "需要复习");

            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.REVIEW_NEEDED, result.getSuggestionType());
            assertEquals(3, result.getPriority());
        }

        @Test
        @DisplayName("创建高难度建议")
        void createHighDifficultySuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createHighDifficultySuggestion(1L, "高难度");

            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.HIGH_DIFFICULTY, result.getSuggestionType());
            assertEquals(4, result.getPriority());
        }

        @Test
        @DisplayName("创建常错词建议")
        void createFrequentErrorSuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createFrequentErrorSuggestion(1L, "常错");

            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.FREQUENT_ERROR, result.getSuggestionType());
            assertEquals(5, result.getPriority());
        }

        @Test
        @DisplayName("创建反应时间长建议")
        void createLongDurationSuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createLongDurationSuggestion(1L, "反应慢");

            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.LONG_DURATION, result.getSuggestionType());
            assertEquals(3, result.getPriority());
        }

        @Test
        @DisplayName("创建新词建议")
        void createNewWordSuggestion() {
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Suggestion result = suggestionService.createNewWordSuggestion(1L, "新词");

            assertNotNull(result);
            assertEquals(Suggestion.SuggestionType.NEW_WORD, result.getSuggestionType());
            assertEquals(2, result.getPriority());
        }
    }

    @Nested
    @DisplayName("getReviewNeededSuggestions 方法测试")
    class GetReviewNeededSuggestionsTests {

        @Test
        @DisplayName("获取复习建议")
        void getReviewNeededSuggestions() {
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(any())).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getReviewNeededSuggestions();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getHighDifficultySuggestions 方法测试")
    class GetHighDifficultySuggestionsTests {

        @Test
        @DisplayName("获取高难度建议")
        void getHighDifficultySuggestions() {
            testSuggestion.setSuggestionType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(any())).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getHighDifficultySuggestions();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getFrequentErrorSuggestions 方法测试")
    class GetFrequentErrorSuggestionsTests {

        @Test
        @DisplayName("获取常错词建议")
        void getFrequentErrorSuggestions() {
            testSuggestion.setSuggestionType(Suggestion.SuggestionType.FREQUENT_ERROR);
            when(suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(any())).thenReturn(List.of(testSuggestion));

            List<Suggestion> result = suggestionService.getFrequentErrorSuggestions();

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getAllSuggestionDTOs 方法测试")
    class GetAllSuggestionDTOsTests {

        @Test
        @DisplayName("获取所有建议DTO")
        void getAllSuggestionDTOs() {
            when(suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc()).thenReturn(List.of(testSuggestion));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));

            List<SuggestionDTO> result = suggestionService.getAllSuggestionDTOs();

            assertEquals(1, result.size());
            assertEquals("测试词", result.get(0).getWordText());
        }
    }

    @Nested
    @DisplayName("generateSuggestions 方法测试")
    class GenerateSuggestionsTests {

        @Test
        @DisplayName("生成建议 - 重复次数多")
        void generateSuggestionsHighRepeatCount() {
            testRecord.setRepeatCount(5);
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of());

            suggestionService.generateSuggestions(1L);

            verify(suggestionRepository, atLeast(1)).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 反应时间长")
        void generateSuggestionsLongDuration() {
            testRecord.setDurationSeconds(20);
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of());

            suggestionService.generateSuggestions(1L);

            verify(suggestionRepository, atLeast(1)).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 词语不存在")
        void generateSuggestionsWordNotFound() {
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.empty());
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of());

            suggestionService.generateSuggestions(1L);

            // 不应该创建建议
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 包含生词")
        void generateSuggestionsWithDifficultWord() {
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of(testDifficultWord));
            when(wordRepository.findAll()).thenReturn(List.of(testWord));
            when(suggestionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            suggestionService.generateSuggestions(1L);

            verify(suggestionRepository, atLeast(1)).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 生词找不到对应Word")
        void generateSuggestionsDifficultWordNoMatch() {
            // 设置不会触发重复次数和反应时间的条件
            testRecord.setRepeatCount(1);
            testRecord.setDurationSeconds(5);
            testDifficultWord.setWordText("不存在的词");
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of(testDifficultWord));
            when(wordRepository.findAll()).thenReturn(List.of(testWord));

            suggestionService.generateSuggestions(1L);

            // 不会为生词创建高难度建议，因为找不到匹配的Word
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }

        @Test
        @DisplayName("生成建议 - 正常情况无触发")
        void generateSuggestionsNormalCase() {
            testRecord.setRepeatCount(1);
            testRecord.setDurationSeconds(5);
            when(recordRepository.findByBatchId(anyLong())).thenReturn(List.of(testRecord));
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of());

            suggestionService.generateSuggestions(1L);

            // 正常情况不创建建议
            verify(suggestionRepository, never()).save(any(Suggestion.class));
        }
    }
}