package com.yhj.dictation.service;

import com.yhj.dictation.entity.Word;
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
 * WordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    private Word testWord;

    @BeforeEach
    void setUp() {
        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试词");
        testWord.setBatchId(1L);
        testWord.setSortOrder(1);
        testWord.setStatus(Word.WordStatus.PENDING);
        testWord.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createWord 方法测试")
    class CreateWordTests {

        @Test
        @DisplayName("创建词语")
        void createWord() {
            when(wordRepository.save(any())).thenAnswer(invocation -> {
                Word w = invocation.getArgument(0);
                w.setId(1L);
                return w;
            });

            Word result = wordService.createWord("新词", 1L, 1);

            assertNotNull(result);
            assertEquals("新词", result.getWordText());
            assertEquals(1L, result.getBatchId());
            assertEquals(1, result.getSortOrder());
            assertEquals(Word.WordStatus.PENDING, result.getStatus());
            verify(wordRepository).save(any(Word.class));
        }
    }

    @Nested
    @DisplayName("getWordById 方法测试")
    class GetWordByIdTests {

        @Test
        @DisplayName("获取词语成功")
        void getWordById() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));

            Optional<Word> result = wordService.getWordById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("词语不存在")
        void getWordByIdNotFound() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.empty());

            Optional<Word> result = wordService.getWordById(999L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getWordsByBatchId 方法测试")
    class GetWordsByBatchIdTests {

        @Test
        @DisplayName("获取批次词语")
        void getWordsByBatchId() {
            when(wordRepository.findByBatchIdOrderBySortOrder(anyLong())).thenReturn(List.of(testWord));

            List<Word> result = wordService.getWordsByBatchId(1L);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getWordsByBatchIdAndStatus 方法测试")
    class GetWordsByBatchIdAndStatusTests {

        @Test
        @DisplayName("获取指定状态词语")
        void getWordsByBatchIdAndStatus() {
            when(wordRepository.findByBatchIdAndStatus(anyLong(), any())).thenReturn(List.of(testWord));

            List<Word> result = wordService.getWordsByBatchIdAndStatus(1L, Word.WordStatus.PENDING);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getFirstWord 方法测试")
    class GetFirstWordTests {

        @Test
        @DisplayName("获取第一个词语")
        void getFirstWord() {
            when(wordRepository.findByBatchIdAndSortOrder(anyLong(), anyInt())).thenReturn(Optional.of(testWord));

            Optional<Word> result = wordService.getFirstWord(1L);

            assertTrue(result.isPresent());
            assertEquals(1, result.get().getSortOrder());
        }

        @Test
        @DisplayName("批次没有词语")
        void getFirstWordEmpty() {
            when(wordRepository.findByBatchIdAndSortOrder(anyLong(), anyInt())).thenReturn(Optional.empty());

            Optional<Word> result = wordService.getFirstWord(1L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getNextWord 方法测试")
    class GetNextWordTests {

        @Test
        @DisplayName("获取下一个词语")
        void getNextWord() {
            Word nextWord = new Word();
            nextWord.setId(2L);
            nextWord.setSortOrder(2);
            when(wordRepository.findByBatchIdAndSortOrderGreaterThanOrderBySortOrder(anyLong(), anyInt()))
                    .thenReturn(List.of(nextWord));

            Optional<Word> result = wordService.getNextWord(1L, 1);

            assertTrue(result.isPresent());
            assertEquals(2, result.get().getSortOrder());
        }

        @Test
        @DisplayName("没有下一个词语")
        void getNextWordEmpty() {
            when(wordRepository.findByBatchIdAndSortOrderGreaterThanOrderBySortOrder(anyLong(), anyInt()))
                    .thenReturn(List.of());

            Optional<Word> result = wordService.getNextWord(1L, 5);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getPreviousWord 方法测试")
    class GetPreviousWordTests {

        @Test
        @DisplayName("获取上一个词语")
        void getPreviousWord() {
            Word prevWord = new Word();
            prevWord.setId(2L);
            prevWord.setSortOrder(2);
            when(wordRepository.findByBatchIdAndSortOrderLessThanOrderBySortOrderDesc(anyLong(), anyInt()))
                    .thenReturn(List.of(prevWord));

            Optional<Word> result = wordService.getPreviousWord(1L, 3);

            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("没有上一个词语")
        void getPreviousWordEmpty() {
            when(wordRepository.findByBatchIdAndSortOrderLessThanOrderBySortOrderDesc(anyLong(), anyInt()))
                    .thenReturn(List.of());

            Optional<Word> result = wordService.getPreviousWord(1L, 1);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("updateWordStatus 方法测试")
    class UpdateWordStatusTests {

        @Test
        @DisplayName("更新词语状态")
        void updateWordStatus() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(wordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Word result = wordService.updateWordStatus(1L, Word.WordStatus.COMPLETED);

            assertNotNull(result);
            assertEquals(Word.WordStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("词语不存在")
        void updateWordStatusNotFound() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> wordService.updateWordStatus(999L, Word.WordStatus.COMPLETED));
        }
    }

    @Nested
    @DisplayName("updateWordPinyin 方法测试")
    class UpdateWordPinyinTests {

        @Test
        @DisplayName("更新词语拼音")
        void updateWordPinyin() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(wordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Word result = wordService.updateWordPinyin(1L, "cè shì cí");

            assertNotNull(result);
            assertEquals("cè shì cí", result.getPinyin());
        }

        @Test
        @DisplayName("词语不存在")
        void updateWordPinyinNotFound() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> wordService.updateWordPinyin(999L, "pinyin"));
        }
    }

    @Nested
    @DisplayName("markAsCompleted 方法测试")
    class MarkAsCompletedTests {

        @Test
        @DisplayName("标记为已完成")
        void markAsCompleted() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(wordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Word result = wordService.markAsCompleted(1L);

            assertNotNull(result);
            assertEquals(Word.WordStatus.COMPLETED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("markAsSkipped 方法测试")
    class MarkAsSkippedTests {

        @Test
        @DisplayName("标记为已跳过")
        void markAsSkipped() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(wordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Word result = wordService.markAsSkipped(1L);

            assertNotNull(result);
            assertEquals(Word.WordStatus.SKIPPED, result.getStatus());
        }
    }

    @Nested
    @DisplayName("markAsPlaying 方法测试")
    class MarkAsPlayingTests {

        @Test
        @DisplayName("标记为正在播放")
        void markAsPlaying() {
            when(wordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            when(wordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            Word result = wordService.markAsPlaying(1L);

            assertNotNull(result);
            assertEquals(Word.WordStatus.PLAYING, result.getStatus());
        }
    }

    @Nested
    @DisplayName("resetBatchWords 方法测试")
    class ResetBatchWordsTests {

        @Test
        @DisplayName("重置批次词语状态")
        void resetBatchWords() {
            Word word1 = new Word();
            word1.setStatus(Word.WordStatus.COMPLETED);
            Word word2 = new Word();
            word2.setStatus(Word.WordStatus.SKIPPED);

            when(wordRepository.findByBatchIdOrderBySortOrder(anyLong())).thenReturn(List.of(word1, word2));
            when(wordRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

            wordService.resetBatchWords(1L);

            assertEquals(Word.WordStatus.PENDING, word1.getStatus());
            assertEquals(Word.WordStatus.PENDING, word2.getStatus());
            verify(wordRepository).saveAll(any(List.class));
        }
    }

    @Nested
    @DisplayName("deleteWord 方法测试")
    class DeleteWordTests {

        @Test
        @DisplayName("删除词语")
        void deleteWord() {
            doNothing().when(wordRepository).deleteById(anyLong());

            wordService.deleteWord(1L);

            verify(wordRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("countWordsByBatchId 方法测试")
    class CountWordsByBatchIdTests {

        @Test
        @DisplayName("统计批次词语数量")
        void countWordsByBatchId() {
            when(wordRepository.countByBatchId(anyLong())).thenReturn(5L);

            long result = wordService.countWordsByBatchId(1L);

            assertEquals(5L, result);
        }
    }

    @Nested
    @DisplayName("countWordsByBatchIdAndStatus 方法测试")
    class CountWordsByBatchIdAndStatusTests {

        @Test
        @DisplayName("统计指定状态词语数量")
        void countWordsByBatchIdAndStatus() {
            when(wordRepository.countByBatchIdAndStatus(anyLong(), any())).thenReturn(3L);

            long result = wordService.countWordsByBatchIdAndStatus(1L, Word.WordStatus.COMPLETED);

            assertEquals(3L, result);
        }
    }
}
