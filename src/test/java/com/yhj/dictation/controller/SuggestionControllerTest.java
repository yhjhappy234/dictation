package com.yhj.dictation.controller;

import tools.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.SuggestionDTO;
import com.yhj.dictation.entity.Suggestion;
import com.yhj.dictation.service.SuggestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SuggestionController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SuggestionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SuggestionService suggestionService;

    @InjectMocks
    private SuggestionController suggestionController;

    private ObjectMapper objectMapper;
    private Suggestion testSuggestion;
    private SuggestionDTO testDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(suggestionController).build();
        objectMapper = new ObjectMapper();

        testSuggestion = new Suggestion();
        testSuggestion.setId(1L);
        testSuggestion.setWordId(1L);
        testSuggestion.setSuggestionType(Suggestion.SuggestionType.REVIEW_NEEDED);
        testSuggestion.setPriority(3);
        testSuggestion.setMessage("建议复习");
        testSuggestion.setCreatedAt(LocalDateTime.now());

        testDTO = new SuggestionDTO();
        testDTO.setId(1L);
        testDTO.setWordId(1L);
        testDTO.setWordText("测试");
        testDTO.setSuggestionType("REVIEW_NEEDED");
        testDTO.setPriority(3);
        testDTO.setMessage("建议复习");
        testDTO.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getAllSuggestions API测试")
    class GetAllSuggestionsApiTests {

        @Test
        @DisplayName("获取所有建议 - 成功")
        void getAllSuggestions_success() throws Exception {
            // Given
            when(suggestionService.getAllSuggestionDTOs()).thenReturn(Arrays.asList(testDTO));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @DisplayName("获取所有建议 - 空列表")
        void getAllSuggestions_emptyList() throws Exception {
            // Given
            when(suggestionService.getAllSuggestionDTOs()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getSuggestionsByType API测试")
    class GetSuggestionsByTypeApiTests {

        @Test
        @DisplayName("获取指定类型建议 - 成功")
        void getSuggestionsByType_success() throws Exception {
            // Given
            when(suggestionService.getSuggestionsByType(Suggestion.SuggestionType.REVIEW_NEEDED))
                    .thenReturn(Arrays.asList(testSuggestion));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/REVIEW_NEEDED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取指定类型建议 - 无效类型")
        void getSuggestionsByType_invalidType() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/INVALID_TYPE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("获取指定类型建议 - HIGH_DIFFICULTY")
        void getSuggestionsByType_highDifficulty() throws Exception {
            // Given
            Suggestion highDifficulty = new Suggestion();
            highDifficulty.setSuggestionType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
            when(suggestionService.getSuggestionsByType(Suggestion.SuggestionType.HIGH_DIFFICULTY))
                    .thenReturn(Arrays.asList(highDifficulty));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/HIGH_DIFFICULTY"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取指定类型建议 - FREQUENT_ERROR")
        void getSuggestionsByType_frequentError() throws Exception {
            // Given
            Suggestion frequentError = new Suggestion();
            frequentError.setSuggestionType(Suggestion.SuggestionType.FREQUENT_ERROR);
            when(suggestionService.getSuggestionsByType(Suggestion.SuggestionType.FREQUENT_ERROR))
                    .thenReturn(Arrays.asList(frequentError));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/FREQUENT_ERROR"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取指定类型建议 - LONG_DURATION")
        void getSuggestionsByType_longDuration() throws Exception {
            // Given
            Suggestion longDuration = new Suggestion();
            longDuration.setSuggestionType(Suggestion.SuggestionType.LONG_DURATION);
            when(suggestionService.getSuggestionsByType(Suggestion.SuggestionType.LONG_DURATION))
                    .thenReturn(Arrays.asList(longDuration));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/LONG_DURATION"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取指定类型建议 - NEW_WORD")
        void getSuggestionsByType_newWord() throws Exception {
            // Given
            Suggestion newWord = new Suggestion();
            newWord.setSuggestionType(Suggestion.SuggestionType.NEW_WORD);
            when(suggestionService.getSuggestionsByType(Suggestion.SuggestionType.NEW_WORD))
                    .thenReturn(Arrays.asList(newWord));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/type/NEW_WORD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getSuggestionsByWordId API测试")
    class GetSuggestionsByWordIdApiTests {

        @Test
        @DisplayName("获取词语建议 - 成功")
        void getSuggestionsByWordId_success() throws Exception {
            // Given
            when(suggestionService.getSuggestionsByWordId(1L)).thenReturn(Arrays.asList(testSuggestion));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/word/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取词语建议 - 空列表")
        void getSuggestionsByWordId_emptyList() throws Exception {
            // Given
            when(suggestionService.getSuggestionsByWordId(anyLong())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/word/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getReviewNeededSuggestions API测试")
    class GetReviewNeededSuggestionsApiTests {

        @Test
        @DisplayName("获取需要复习建议 - 成功")
        void getReviewNeededSuggestions_success() throws Exception {
            // Given
            when(suggestionService.getReviewNeededSuggestions()).thenReturn(Arrays.asList(testSuggestion));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/review"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取需要复习建议 - 空列表")
        void getReviewNeededSuggestions_emptyList() throws Exception {
            // Given
            when(suggestionService.getReviewNeededSuggestions()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/review"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getHighDifficultySuggestions API测试")
    class GetHighDifficultySuggestionsApiTests {

        @Test
        @DisplayName("获取高难度建议 - 成功")
        void getHighDifficultySuggestions_success() throws Exception {
            // Given
            Suggestion highDifficulty = new Suggestion();
            highDifficulty.setSuggestionType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
            when(suggestionService.getHighDifficultySuggestions()).thenReturn(Arrays.asList(highDifficulty));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/high-difficulty"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getFrequentErrorSuggestions API测试")
    class GetFrequentErrorSuggestionsApiTests {

        @Test
        @DisplayName("获取常错词建议 - 成功")
        void getFrequentErrorSuggestions_success() throws Exception {
            // Given
            Suggestion frequentError = new Suggestion();
            frequentError.setSuggestionType(Suggestion.SuggestionType.FREQUENT_ERROR);
            when(suggestionService.getFrequentErrorSuggestions()).thenReturn(Arrays.asList(frequentError));

            // When & Then
            mockMvc.perform(get("/api/v1/suggestions/frequent-error"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("updatePriority API测试")
    class UpdatePriorityApiTests {

        @Test
        @DisplayName("更新优先级 - 成功")
        void updatePriority_success() throws Exception {
            // Given
            when(suggestionService.updatePriority(1L, 4)).thenReturn(testSuggestion);

            // When & Then
            mockMvc.perform(put("/api/v1/suggestions/1/priority")
                    .param("priority", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("优先级已更新"));
        }

        @Test
        @DisplayName("更新优先级 - 建议不存在")
        void updatePriority_notFound() throws Exception {
            // Given
            when(suggestionService.updatePriority(anyLong(), any()))
                    .thenThrow(new IllegalArgumentException("Suggestion not found"));

            // When & Then
            mockMvc.perform(put("/api/v1/suggestions/999/priority")
                    .param("priority", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteSuggestion API测试")
    class DeleteSuggestionApiTests {

        @Test
        @DisplayName("删除建议 - 成功")
        void deleteSuggestion_success() throws Exception {
            // Given
            doNothing().when(suggestionService).deleteSuggestion(1L);

            // When & Then
            mockMvc.perform(delete("/api/v1/suggestions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("建议已删除"));
        }

        @Test
        @DisplayName("删除建议 - 异常情况")
        void deleteSuggestion_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(suggestionService).deleteSuggestion(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/v1/suggestions/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteSuggestionsByWordId API测试")
    class DeleteSuggestionsByWordIdApiTests {

        @Test
        @DisplayName("删除词语建议 - 成功")
        void deleteSuggestionsByWordId_success() throws Exception {
            // Given
            doNothing().when(suggestionService).deleteSuggestionsByWordId(1L);

            // When & Then
            mockMvc.perform(delete("/api/v1/suggestions/word/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("词语建议已清空"));
        }

        @Test
        @DisplayName("删除词语建议 - 异常情况")
        void deleteSuggestionsByWordId_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(suggestionService).deleteSuggestionsByWordId(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/v1/suggestions/word/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}