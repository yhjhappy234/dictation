package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.DifficultWordAddRequest;
import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.service.DifficultWordService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DifficultWordController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DifficultWordControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DifficultWordService difficultWordService;

    @InjectMocks
    private DifficultWordController difficultWordController;

    private ObjectMapper objectMapper;
    private DifficultWord testDifficultWord;
    private DifficultWordDTO testDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(difficultWordController).build();
        objectMapper = new ObjectMapper();

        testDifficultWord = new DifficultWord();
        testDifficultWord.setId(1L);
        testDifficultWord.setWordText("测试");
        testDifficultWord.setDictator("小明");
        testDifficultWord.setErrorCount(3);
        testDifficultWord.setMasteryLevel(2);
        testDifficultWord.setAvgDurationSeconds(10);
        testDifficultWord.setLastPracticeDate(LocalDateTime.now());

        testDTO = new DifficultWordDTO();
        testDTO.setId(1L);
        testDTO.setWordText("测试");
        testDTO.setDictator("小明");
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
            mockMvc.perform(get("/api/v1/difficult-words"))
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
            mockMvc.perform(get("/api/v1/difficult-words"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getDifficultWordsByDictator API测试")
    class GetDifficultWordsByDictatorApiTests {

        @Test
        @DisplayName("根据听写人获取生词 - 成功")
        void getDifficultWordsByDictator_success() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordsByDictator("小明"))
                    .thenReturn(Arrays.asList(testDifficultWord));

            // When & Then
            mockMvc.perform(get("/api/v1/difficult-words/dictator/小明"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
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
            mockMvc.perform(get("/api/v1/difficult-words/difficult")
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
            mockMvc.perform(get("/api/v1/difficult-words/difficult"))
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
            mockMvc.perform(get("/api/v1/difficult-words/recommended")
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
            mockMvc.perform(get("/api/v1/difficult-words/recommended"))
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
            request.setWordText("测试");
            request.setDictator("小明");

            when(difficultWordService.addDifficultWordDTO(anyString(), anyString())).thenReturn(testDTO);

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words")
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
            request.setWordText("测试");

            when(difficultWordService.addDifficultWordDTO(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words")
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
            when(difficultWordService.updateMasteryLevelByText("测试", 3)).thenReturn(testDifficultWord);
            when(difficultWordService.getDifficultWordByText("测试")).thenReturn(Optional.of(testDifficultWord));

            // When & Then
            mockMvc.perform(put("/api/v1/difficult-words/1/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("更新掌握级别 - 生词不存在")
        void updateMasteryLevel_notFound() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordById(any())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(put("/api/v1/difficult-words/999/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("更新掌握级别 - 异常情况")
        void updateMasteryLevel_exception() throws Exception {
            // Given
            when(difficultWordService.getDifficultWordById(1L)).thenReturn(Optional.of(testDifficultWord));
            when(difficultWordService.updateMasteryLevelByText(anyString(), anyInt()))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(put("/api/v1/difficult-words/1/mastery")
                    .param("level", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("practiceSuccessByText API测试")
    class PracticeSuccessApiTests {

        @Test
        @DisplayName("练习成功 - 成功")
        void practiceSuccess_success() throws Exception {
            // Given
            doNothing().when(difficultWordService).handlePracticeSuccessByText("测试");

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words/text/测试/success"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("练习成功"));
        }

        @Test
        @DisplayName("练习成功 - 异常情况")
        void practiceSuccess_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).handlePracticeSuccessByText(anyString());

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words/text/测试/success"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("practiceFailureByText API测试")
    class PracticeFailureApiTests {

        @Test
        @DisplayName("练习失败 - 成功")
        void practiceFailure_success() throws Exception {
            // Given
            doNothing().when(difficultWordService).handlePracticeFailureByText("测试", "小明");

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words/text/测试/failure")
                    .param("dictator", "小明"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已记录失败"));
        }

        @Test
        @DisplayName("练习失败 - 异常情况")
        void practiceFailure_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).handlePracticeFailureByText(anyString(), anyString());

            // When & Then
            mockMvc.perform(post("/api/v1/difficult-words/text/测试/failure")
                    .param("dictator", "小明"))
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
            mockMvc.perform(delete("/api/v1/difficult-words/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("已从生词本移除"));
        }

        @Test
        @DisplayName("移除生词 - 异常情况")
        void removeDifficultWord_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(difficultWordService).removeDifficultWord(any());

            // When & Then
            mockMvc.perform(delete("/api/v1/difficult-words/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}