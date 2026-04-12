package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.DifficultWordAddRequest;
import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DifficultWordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DifficultWordController 单元测试
 */
@WebMvcTest(DifficultWordController.class)
@ExtendWith(MockitoExtension.class)
class DifficultWordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DifficultWordService difficultWordService;

    private DifficultWord testDifficultWord;
    private DifficultWordDTO testDTO;
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
        testDifficultWord.setAvgDurationSeconds(10);
        testDifficultWord.setLastPracticeDate(LocalDateTime.now());

        testDTO = new DifficultWordDTO();
        testDTO.setId(1L);
        testDTO.setWordId(1L);
        testDTO.setWordText("测试");
        testDTO.setPinyin("ceshi");
        testDTO.setErrorCount(3);
        testDTO.setMasteryLevel(2);
        testDTO.setAvgDurationSeconds(10);
        testDTO.setLastPracticeDate(LocalDateTime.now());
    }

    @Nested
    @DisplayName("getAllDifficultWords API测试")
    class GetAllDifficultWordsApiTests {

        @Test
        @DisplayName("获取所有生词 - 成功")
        void getAllDifficultWords_success() throws Exception {
            // Given
            when(difficultWordService.getDifficultWords()).thenReturn(Arrays.asList(testDTO));

            // When & Then
            mockMvc.perform(get("/api/difficult-words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].id").value(1));
        }

        @Test
        @DisplayName("获取所有生词 - 空列表")
        void getAllDifficultWords_emptyList() throws Exception {
            // Given
            when(difficultWordService.getDifficultWords()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/difficult-words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getDifficultWords API测试")
    class GetDifficultWordsByMasteryApiTests {

        @Test
        @DisplayName("获取高难度生词 - 成功")
        void getDifficultWords_success() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordsByMasteryLevel(3))
                    .thenReturn(Arrays.asList(testDifficultWord));

            // When & Then
            mockMvc.perform(get("/api/difficult-words/difficult")
                    .param("maxMasteryLevel", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取高难度生词 - 使用默认值")
        void getDifficultWords_defaultParam() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordsByMasteryLevel(3))
                    .thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/difficult-words/difficult"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getRecommendedWords API测试")
    class GetRecommendedWordsApiTests {

        @Test
        @DisplayName("获取推荐生词 - 成功")
        void getRecommendedWords_success() throws Exception {
            // Given
            when(difficultWordService.getRecommendedDifficultWords(3, 10))
                    .thenReturn(Arrays.asList(testDifficultWord));

            // When & Then
            mockMvc.perform(get("/api/difficult-words/recommended")
                    .param("minErrors", "3")
                    .param("minDuration", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("获取推荐生词 - 使用默认值")
        void getRecommendedWords_defaultParams() throws Exception {
            // Given
            when(difficultWordService.getRecommendedDifficultWords(3, 10))
                    .thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/difficult-words/recommended"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("addDifficultWord API测试")
    class AddDifficultWordApiTests {

        @Test
        @DisplayName("添加生词 - 成功")
        void addDifficultWord_success() throws Exception {
            // Given
            DifficultWordAddRequest request = new DifficultWordAddRequest();
            request.setWordId(1L);

            when(difficultWordService.addDifficultWordDTO(1L)).thenReturn(testDTO);

            // When & Then
            mockMvc.perform(post("/api/difficult-words")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已添加到生词本"));
        }

        @Test
        @DisplayName("添加生词 - 异常情况")
        void addDifficultWord_exception() throws Exception {
            // Given
            DifficultWordAddRequest request = new DifficultWordAddRequest();
            request.setWordId(1L);

            when(difficultWordService.addDifficultWordDTO(anyLong()))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/difficult-words")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("updateMasteryLevel API测试")
    class UpdateMasteryLevelApiTests {

        @Test
        @DisplayName("更新掌握级别 - 成功")
        void updateMasteryLevel_success() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordById(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordService.updateMasteryLevel(1L, 3)).thenReturn(testDifficultWord);
            when(difficultWordService.getDifficultWordByWordId(1L)).thenReturn(Optional.of(testDifficultWord));

            // When & Then
            mockMvc.perform(put("/api/difficult-words/1/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("更新掌握级别 - 生词不存在")
        void updateMasteryLevel_notFound() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/difficult-words/999/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("更新掌握级别 - 异常情况")
        void updateMasteryLevel_exception() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordById(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordService.updateMasteryLevel(anyLong(), anyInt()))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(put("/api/difficult-words/1/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("practiceSuccess API测试")
    class PracticeSuccessApiTests {

        @Test
        @DisplayName("练习成功 - 成功")
        void practiceSuccess_success() throws Exception {
            // Given
            doNothing().when(difficultWordService).handlePracticeSuccess(1L);

            // When & Then
            mockMvc.perform(post("/api/difficult-words/1/success"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("练习成功"));
        }

        @Test
        @DisplayName("练习成功 - 异常情况")
        void practiceSuccess_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).handlePracticeSuccess(anyLong());

            // When & Then
            mockMvc.perform(post("/api/difficult-words/999/success"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("practiceFailure API测试")
    class PracticeFailureApiTests {

        @Test
        @DisplayName("练习失败 - 成功")
        void practiceFailure_success() throws Exception {
            // Given
            doNothing().when(difficultWordService).handlePracticeFailure(1L);

            // When & Then
            mockMvc.perform(post("/api/difficult-words/1/failure"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已记录失败"));
        }

        @Test
        @DisplayName("练习失败 - 异常情况")
        void practiceFailure_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).handlePracticeFailure(anyLong());

            // When & Then
            mockMvc.perform(post("/api/difficult-words/999/failure"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("removeDifficultWord API测试")
    class RemoveDifficultWordApiTests {

        @Test
        @DisplayName("移除生词 - 成功")
        void removeDifficultWord_success() throws Exception {
            // Given
            doNothing().when(difficultWordService).removeDifficultWord(1L);

            // When & Then
            mockMvc.perform(delete("/api/difficult-words/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已从生词本移除"));
        }

        @Test
        @DisplayName("移除生词 - 异常情况")
        void removeDifficultWord_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).removeDifficultWord(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/difficult-words/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}