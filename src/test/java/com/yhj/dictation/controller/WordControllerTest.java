package com.yhj.dictation.controller;

import tools.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.StatusUpdateRequest;
import com.yhj.dictation.dto.WordAddRequest;
import com.yhj.dictation.dto.WordDTO;
import com.yhj.dictation.entity.Word;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WordController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WordService wordService;

    @InjectMocks
    private WordController wordController;

    private ObjectMapper objectMapper;
    private Word testWord;
    private WordDTO testWordDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(wordController).build();
        objectMapper = new ObjectMapper();

        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("测试");
        testWord.setPinyin("ceshi");
        testWord.setBatchId(1L);
        testWord.setSortOrder(1);
        testWord.setStatus(Word.WordStatus.PENDING);
        testWord.setCreatedAt(LocalDateTime.now());

        testWordDTO = new WordDTO();
        testWordDTO.setId(1L);
        testWordDTO.setWordText("测试");
        testWordDTO.setPinyin("ceshi");
        testWordDTO.setBatchId(1L);
        testWordDTO.setSortOrder(1);
        testWordDTO.setStatus("PENDING");
        testWordDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getWordById API测试")
    class GetWordByIdApiTests {

        @Test
        @DisplayName("获取词语详情 - 成功")
        void getWordById_success() throws Exception {
            // Given
            when(wordService.getWordById(1L)).thenReturn(Optional.of(testWord));

            // When & Then
            mockMvc.perform(get("/api/v1/words/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.wordText").value("测试"));
        }

        @Test
        @DisplayName("获取词语详情 - 词语不存在")
        void getWordById_notFound() throws Exception {
            // Given
            when(wordService.getWordById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/words/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("updateWordStatus API测试")
    class UpdateWordStatusApiTests {

        @Test
        @DisplayName("更新词语状态 - 成功")
        void updateWordStatus_success() throws Exception {
            // Given
            StatusUpdateRequest request = new StatusUpdateRequest();
            request.setStatus("COMPLETED");

            Word completedWord = new Word();
            completedWord.setId(1L);
            completedWord.setStatus(Word.WordStatus.COMPLETED);

            when(wordService.updateWordStatus(1L, Word.WordStatus.COMPLETED)).thenReturn(completedWord);

            // When & Then
            mockMvc.perform(put("/api/v1/words/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("状态更新成功"));
        }

        @Test
        @DisplayName("更新词语状态 - 无效状态")
        void updateWordStatus_invalidStatus() throws Exception {
            // Given
            StatusUpdateRequest request = new StatusUpdateRequest();
            request.setStatus("INVALID_STATUS");

            // When & Then
            mockMvc.perform(put("/api/v1/words/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("更新词语状态 - 词语不存在")
        void updateWordStatus_wordNotFound() throws Exception {
            // Given
            StatusUpdateRequest request = new StatusUpdateRequest();
            request.setStatus("COMPLETED");

            when(wordService.updateWordStatus(anyLong(), any()))
                    .thenThrow(new IllegalArgumentException("Word not found"));

            // When & Then
            mockMvc.perform(put("/api/v1/words/999/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("updateWordPinyin API测试")
    class UpdateWordPinyinApiTests {

        @Test
        @DisplayName("更新词语拼音 - 成功")
        void updateWordPinyin_success() throws Exception {
            // Given
            WordAddRequest request = new WordAddRequest();
            request.setPinyin("newpinyin");

            Word updatedWord = new Word();
            updatedWord.setId(1L);
            updatedWord.setPinyin("newpinyin");

            when(wordService.updateWordPinyin(1L, "newpinyin")).thenReturn(updatedWord);

            // When & Then
            mockMvc.perform(put("/api/v1/words/1/pinyin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("拼音更新成功"));
        }

        @Test
        @DisplayName("更新词语拼音 - 词语不存在")
        void updateWordPinyin_wordNotFound() throws Exception {
            // Given
            WordAddRequest request = new WordAddRequest();
            request.setPinyin("pinyin");

            when(wordService.updateWordPinyin(anyLong(), any()))
                    .thenThrow(new IllegalArgumentException("Word not found"));

            // When & Then
            mockMvc.perform(put("/api/v1/words/999/pinyin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("markAsCompleted API测试")
    class MarkAsCompletedApiTests {

        @Test
        @DisplayName("标记词语完成 - 成功")
        void markAsCompleted_success() throws Exception {
            // Given
            Word completedWord = new Word();
            completedWord.setId(1L);
            completedWord.setStatus(Word.WordStatus.COMPLETED);

            when(wordService.markAsCompleted(1L)).thenReturn(completedWord);

            // When & Then
            mockMvc.perform(post("/api/v1/words/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("词语已完成"));
        }

        @Test
        @DisplayName("标记词语完成 - 词语不存在")
        void markAsCompleted_wordNotFound() throws Exception {
            // Given
            when(wordService.markAsCompleted(anyLong()))
                    .thenThrow(new IllegalArgumentException("Word not found"));

            // When & Then
            mockMvc.perform(post("/api/v1/words/999/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("markAsSkipped API测试")
    class MarkAsSkippedApiTests {

        @Test
        @DisplayName("标记词语跳过 - 成功")
        void markAsSkipped_success() throws Exception {
            // Given
            Word skippedWord = new Word();
            skippedWord.setId(1L);
            skippedWord.setStatus(Word.WordStatus.SKIPPED);

            when(wordService.markAsSkipped(1L)).thenReturn(skippedWord);

            // When & Then
            mockMvc.perform(post("/api/v1/words/1/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("词语已跳过"));
        }

        @Test
        @DisplayName("标记词语跳过 - 词语不存在")
        void markAsSkipped_wordNotFound() throws Exception {
            // Given
            when(wordService.markAsSkipped(anyLong()))
                    .thenThrow(new IllegalArgumentException("Word not found"));

            // When & Then
            mockMvc.perform(post("/api/v1/words/999/skip"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getNextWord API测试")
    class GetNextWordApiTests {

        @Test
        @DisplayName("获取下一个词语 - 成功")
        void getNextWord_success() throws Exception {
            // Given
            Word nextWord = new Word();
            nextWord.setId(2L);
            nextWord.setWordText("下一个");
            nextWord.setSortOrder(2);

            when(wordService.getNextWord(1L, 1)).thenReturn(Optional.of(nextWord));

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/1/next")
                    .param("currentOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(2));
        }

        @Test
        @DisplayName("获取下一个词语 - 没有下一个")
        void getNextWord_notFound() throws Exception {
            // Given
            when(wordService.getNextWord(anyLong(), anyInt())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/1/next")
                    .param("currentOrder", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("没有下一个词语"));
        }
    }

    @Nested
    @DisplayName("getPreviousWord API测试")
    class GetPreviousWordApiTests {

        @Test
        @DisplayName("获取上一个词语 - 成功")
        void getPreviousWord_success() throws Exception {
            // Given
            Word prevWord = new Word();
            prevWord.setId(1L);
            prevWord.setWordText("上一个");
            prevWord.setSortOrder(1);

            when(wordService.getPreviousWord(1L, 2)).thenReturn(Optional.of(prevWord));

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/1/previous")
                    .param("currentOrder", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取上一个词语 - 没有上一个")
        void getPreviousWord_notFound() throws Exception {
            // Given
            when(wordService.getPreviousWord(anyLong(), anyInt())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/1/previous")
                    .param("currentOrder", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("getFirstWord API测试")
    class GetFirstWordApiTests {

        @Test
        @DisplayName("获取第一个词语 - 成功")
        void getFirstWord_success() throws Exception {
            // Given
            when(wordService.getFirstWord(1L)).thenReturn(Optional.of(testWord));

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/1/first"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取第一个词语 - 批次没有词语")
        void getFirstWord_notFound() throws Exception {
            // Given
            when(wordService.getFirstWord(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/v1/words/batch/999/first"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("批次中没有词语"));
        }
    }

    @Nested
    @DisplayName("deleteWord API测试")
    class DeleteWordApiTests {

        @Test
        @DisplayName("删除词语 - 成功")
        void deleteWord_success() throws Exception {
            // Given
            doNothing().when(wordService).deleteWord(1L);

            // When & Then
            mockMvc.perform(delete("/api/v1/words/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("词语删除成功"));
        }

        @Test
        @DisplayName("删除词语 - 异常情况")
        void deleteWord_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(wordService).deleteWord(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/v1/words/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}