package com.yhj.dictation.service;

import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.repository.DifficultWordRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DifficultWordService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DifficultWordServiceTest {

    @Mock
    private DifficultWordRepository difficultWordRepository;

    @InjectMocks
    private DifficultWordService difficultWordService;

    private DifficultWord testWord;

    @BeforeEach
    void setUp() {
        testWord = new DifficultWord();
        testWord.setId(1L);
        testWord.setWordText("测试词");
        testWord.setDictator("teacher");
        testWord.setErrorCount(2);
        testWord.setMasteryLevel(3);
        testWord.setCreatedAt(LocalDateTime.now());
        testWord.setUpdatedAt(LocalDateTime.now());
        testWord.setLastPracticeDate(LocalDateTime.now());
    }

    @Nested
    @DisplayName("addOrUpdateDifficultWordByText 方法测试")
    class AddOrUpdateDifficultWordByTextTests {

        @Test
        @DisplayName("添加新生词")
        void addNewWord() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.addOrUpdateDifficultWordByText("新词", "teacher");

            assertNotNull(result);
            assertEquals("新词", result.getWordText());
            assertEquals(1, result.getErrorCount());
            assertEquals(0, result.getMasteryLevel());
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("更新已存在的生词")
        void updateExistingWord() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.addOrUpdateDifficultWordByText("测试词", "newTeacher");

            assertNotNull(result);
            assertEquals(3, result.getErrorCount()); // 增加了1
            assertEquals(2, result.getMasteryLevel()); // 减少了1
            assertEquals("newTeacher", result.getDictator());
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("更新已存在的生词 - 掌握级别已经是0")
        void updateExistingWord_MasteryLevelZero() {
            testWord.setMasteryLevel(0);
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.addOrUpdateDifficultWordByText("测试词", null);

            assertNotNull(result);
            assertEquals(0, result.getMasteryLevel()); // 保持0
            verify(difficultWordRepository).save(any(DifficultWord.class));
        }
    }

    @Nested
    @DisplayName("addDifficultWordsBatch 方法测试")
    class AddDifficultWordsBatchTests {

        @Test
        @DisplayName("批量添加生词")
        void addBatch() {
            DifficultWordDTO dto1 = new DifficultWordDTO();
            dto1.setWordText("词1");
            DifficultWordDTO dto2 = new DifficultWordDTO();
            dto2.setWordText("词2");

            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            List<DifficultWord> result = difficultWordService.addDifficultWordsBatch(List.of(dto1, dto2), "teacher");

            assertEquals(2, result.size());
            verify(difficultWordRepository, times(2)).save(any(DifficultWord.class));
        }
    }

    @Nested
    @DisplayName("getDifficultWordByText 方法测试")
    class GetDifficultWordByTextTests {

        @Test
        @DisplayName("获取生词成功")
        void getSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));

            Optional<DifficultWord> result = difficultWordService.getDifficultWordByText("测试词");

            assertTrue(result.isPresent());
            assertEquals("测试词", result.get().getWordText());
        }

        @Test
        @DisplayName("生词不存在")
        void getNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());

            Optional<DifficultWord> result = difficultWordService.getDifficultWordByText("不存在的词");

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getDifficultWordById 方法测试")
    class GetDifficultWordByIdTests {

        @Test
        @DisplayName("获取生词成功")
        void getSuccess() {
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));

            Optional<DifficultWord> result = difficultWordService.getDifficultWordById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("生词不存在")
        void getNotFound() {
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.empty());

            Optional<DifficultWord> result = difficultWordService.getDifficultWordById(999L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("getAllDifficultWords 方法测试")
    class GetAllDifficultWordsTests {

        @Test
        @DisplayName("获取所有生词")
        void getAll() {
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(List.of(testWord));

            List<DifficultWord> result = difficultWordService.getAllDifficultWords();

            assertEquals(1, result.size());
            assertEquals("测试词", result.get(0).getWordText());
        }
    }

    @Nested
    @DisplayName("getDifficultWordsByDictator 方法测试")
    class GetDifficultWordsByDictatorTests {

        @Test
        @DisplayName("根据听写人获取生词")
        void getByDictator() {
            when(difficultWordRepository.findByDictatorOrderByErrorCountDesc(anyString())).thenReturn(List.of(testWord));

            List<DifficultWord> result = difficultWordService.getDifficultWordsByDictator("teacher");

            assertEquals(1, result.size());
            assertEquals("teacher", result.get(0).getDictator());
        }
    }

    @Nested
    @DisplayName("getDifficultWordsByMasteryLevel 方法测试")
    class GetDifficultWordsByMasteryLevelTests {

        @Test
        @DisplayName("获取高难度生词")
        void getByMasteryLevel() {
            when(difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(anyInt())).thenReturn(List.of(testWord));

            List<DifficultWord> result = difficultWordService.getDifficultWordsByMasteryLevel(3);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("getRecommendedDifficultWords 方法测试")
    class GetRecommendedDifficultWordsTests {

        @Test
        @DisplayName("获取推荐生词")
        void getRecommended() {
            when(difficultWordRepository.findDifficultWords(anyInt(), anyInt())).thenReturn(List.of(testWord));

            List<DifficultWord> result = difficultWordService.getRecommendedDifficultWords(3, 10);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("updateMasteryLevelByText 方法测试")
    class UpdateMasteryLevelByTextTests {

        @Test
        @DisplayName("更新掌握级别成功")
        void updateSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.updateMasteryLevelByText("测试词", 5);

            assertNotNull(result);
            assertEquals(5, result.getMasteryLevel());
        }

        @Test
        @DisplayName("更新掌握级别 - 超出范围上限")
        void updateBeyondMax() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.updateMasteryLevelByText("测试词", 10);

            assertNotNull(result);
            assertEquals(5, result.getMasteryLevel()); // 限制为最大值5
        }

        @Test
        @DisplayName("更新掌握级别 - 超出范围下限")
        void updateBelowMin() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.updateMasteryLevelByText("测试词", -1);

            assertNotNull(result);
            assertEquals(0, result.getMasteryLevel()); // 限制为最小值0
        }

        @Test
        @DisplayName("生词不存在")
        void updateNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                difficultWordService.updateMasteryLevelByText("不存在的词", 3));
        }
    }

    @Nested
    @DisplayName("increaseMasteryLevelByText 方法测试")
    class IncreaseMasteryLevelByTextTests {

        @Test
        @DisplayName("增加掌握级别成功")
        void increaseSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.increaseMasteryLevelByText("测试词");

            assertNotNull(result);
            assertEquals(4, result.getMasteryLevel());
        }

        @Test
        @DisplayName("增加掌握级别 - 已是最大值")
        void increaseAtMax() {
            testWord.setMasteryLevel(5);
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.increaseMasteryLevelByText("测试词");

            assertNotNull(result);
            assertEquals(5, result.getMasteryLevel()); // 保持最大值5
        }

        @Test
        @DisplayName("生词不存在")
        void increaseNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());

            DifficultWord result = difficultWordService.increaseMasteryLevelByText("不存在的词");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("decreaseMasteryLevelByText 方法测试")
    class DecreaseMasteryLevelByTextTests {

        @Test
        @DisplayName("减少掌握级别成功")
        void decreaseSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.decreaseMasteryLevelByText("测试词", "teacher");

            assertNotNull(result);
            assertEquals(2, result.getMasteryLevel());
            assertEquals(3, result.getErrorCount());
        }

        @Test
        @DisplayName("减少掌握级别 - 已是最小值")
        void decreaseAtMin() {
            testWord.setMasteryLevel(0);
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.decreaseMasteryLevelByText("测试词", "teacher");

            assertNotNull(result);
            assertEquals(0, result.getMasteryLevel()); // 保持最小值0
        }

        @Test
        @DisplayName("生词不存在 - 创建新词")
        void decreaseNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWord result = difficultWordService.decreaseMasteryLevelByText("新词", "teacher");

            assertNotNull(result);
            assertEquals("新词", result.getWordText());
        }
    }

    @Nested
    @DisplayName("deleteDifficultWord 方法测试")
    class DeleteDifficultWordTests {

        @Test
        @DisplayName("删除生词成功")
        void deleteSuccess() {
            doNothing().when(difficultWordRepository).deleteById(anyLong());

            difficultWordService.deleteDifficultWord(1L);

            verify(difficultWordRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("deleteDifficultWordByText 方法测试")
    class DeleteDifficultWordByTextTests {

        @Test
        @DisplayName("根据文本删除生词成功")
        void deleteSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordRepository).delete(any());

            difficultWordService.deleteDifficultWordByText("测试词");

            verify(difficultWordRepository).delete(testWord);
        }

        @Test
        @DisplayName("生词不存在 - 不删除")
        void deleteNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());

            difficultWordService.deleteDifficultWordByText("不存在的词");

            verify(difficultWordRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("handlePracticeSuccessByText 方法测试")
    class HandlePracticeSuccessByTextTests {

        @Test
        @DisplayName("练习成功处理")
        void handleSuccess() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            difficultWordService.handlePracticeSuccessByText("测试词");

            verify(difficultWordRepository).save(any(DifficultWord.class));
        }

        @Test
        @DisplayName("生词不存在 - 不处理")
        void handleNotFound() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());

            difficultWordService.handlePracticeSuccessByText("不存在的词");

            verify(difficultWordRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("handlePracticeFailureByText 方法测试")
    class HandlePracticeFailureByTextTests {

        @Test
        @DisplayName("练习失败处理")
        void handleFailure() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.of(testWord));
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            difficultWordService.handlePracticeFailureByText("测试词", "teacher");

            verify(difficultWordRepository).save(any(DifficultWord.class));
        }
    }

    @Nested
    @DisplayName("getDifficultWords 方法测试")
    class GetDifficultWordsTests {

        @Test
        @DisplayName("获取生词DTO列表")
        void getDTOs() {
            when(difficultWordRepository.findAllByOrderByErrorCountDesc()).thenReturn(List.of(testWord));

            List<DifficultWordDTO> result = difficultWordService.getDifficultWords();

            assertEquals(1, result.size());
            assertEquals("测试词", result.get(0).getWordText());
        }
    }

    @Nested
    @DisplayName("addDifficultWordDTO 方法测试")
    class AddDifficultWordDTOTests {

        @Test
        @DisplayName("添加生词返回DTO")
        void addDTO() {
            when(difficultWordRepository.findByWordText(anyString())).thenReturn(Optional.empty());
            when(difficultWordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            DifficultWordDTO result = difficultWordService.addDifficultWordDTO("新词", "teacher");

            assertNotNull(result);
            assertEquals("新词", result.getWordText());
        }
    }

    @Nested
    @DisplayName("removeDifficultWord 方法测试")
    class RemoveDifficultWordTests {

        @Test
        @DisplayName("移除生词成功")
        void removeSuccess() {
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.of(testWord));
            doNothing().when(difficultWordRepository).delete(any());

            difficultWordService.removeDifficultWord(1L);

            verify(difficultWordRepository).delete(testWord);
        }

        @Test
        @DisplayName("生词不存在")
        void removeNotFound() {
            when(difficultWordRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> difficultWordService.removeDifficultWord(999L));
        }
    }
}