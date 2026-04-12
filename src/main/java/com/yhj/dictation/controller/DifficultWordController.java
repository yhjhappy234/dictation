package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.DifficultWordAddRequest;
import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.service.DifficultWordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 生词本控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/difficult-words")
@RequiredArgsConstructor
public class DifficultWordController {

    private final DifficultWordService difficultWordService;

    @GetMapping
    public ApiResponse<List<DifficultWordDTO>> getAllDifficultWords() {
        List<DifficultWordDTO> words = difficultWordService.getDifficultWords();
        return ApiResponse.success(words);
    }

    @GetMapping("/dictator/{dictator}")
    public ApiResponse<List<DifficultWordDTO>> getDifficultWordsByDictator(@PathVariable String dictator) {
        List<DifficultWordDTO> words = difficultWordService.getDifficultWordsByDictator(dictator)
                .stream()
                .map(this::toDifficultWordDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(words);
    }

    @GetMapping("/difficult")
    public ApiResponse<List<DifficultWordDTO>> getDifficultWords(
            @RequestParam(defaultValue = "3") Integer maxMasteryLevel) {
        List<DifficultWordDTO> words = difficultWordService.getDifficultWordsByMasteryLevel(maxMasteryLevel)
                .stream()
                .map(this::toDifficultWordDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(words);
    }

    @GetMapping("/recommended")
    public ApiResponse<List<DifficultWordDTO>> getRecommendedWords(
            @RequestParam(defaultValue = "3") Integer minErrors,
            @RequestParam(defaultValue = "10") Integer minDuration) {
        List<DifficultWordDTO> words = difficultWordService.getRecommendedDifficultWords(minErrors, minDuration)
                .stream()
                .map(this::toDifficultWordDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(words);
    }

    @PostMapping
    public ApiResponse<DifficultWordDTO> addDifficultWord(@RequestBody DifficultWordAddRequest request) {
        try {
            DifficultWordDTO dto = difficultWordService.addDifficultWordDTO(
                    request.getWordText(), request.getDictator());
            return ApiResponse.success("已添加到生词本", dto);
        } catch (Exception e) {
            log.error("Failed to add difficult word", e);
            return ApiResponse.error("添加生词失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public ApiResponse<List<DifficultWordDTO>> addDifficultWordsBatch(
            @RequestBody List<DifficultWordDTO> words,
            @RequestParam(required = false) String dictator) {
        try {
            List<DifficultWord> result = difficultWordService.addDifficultWordsBatch(words, dictator);
            List<DifficultWordDTO> dtos = result.stream()
                    .map(this::toDifficultWordDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success("已批量添加到生词本", dtos);
        } catch (Exception e) {
            log.error("Failed to add difficult words batch", e);
            return ApiResponse.error("批量添加生词失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/mastery")
    public ApiResponse<DifficultWordDTO> updateMasteryLevel(
            @PathVariable Long id,
            @RequestParam Integer level) {
        try {
            return difficultWordService.getDifficultWordById(id)
                    .map(dw -> {
                        difficultWordService.updateMasteryLevelByText(dw.getWordText(), level);
                        return difficultWordService.getDifficultWordByText(dw.getWordText())
                                .map(this::toDifficultWordDTO)
                                .map(dto -> ApiResponse.success("掌握级别已更新", dto))
                                .orElse(ApiResponse.error("更新失败"));
                    })
                    .orElse(ApiResponse.error("生词不存在: " + id));
        } catch (Exception e) {
            log.error("Failed to update mastery level", e);
            return ApiResponse.error("更新掌握级别失败: " + e.getMessage());
        }
    }

    @PostMapping("/text/{wordText}/success")
    public ApiResponse<Void> practiceSuccessByText(@PathVariable String wordText) {
        try {
            difficultWordService.handlePracticeSuccessByText(wordText);
            return ApiResponse.success("练习成功", null);
        } catch (Exception e) {
            log.error("Failed to handle practice success", e);
            return ApiResponse.error("处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/text/{wordText}/failure")
    public ApiResponse<Void> practiceFailureByText(
            @PathVariable String wordText,
            @RequestParam(required = false) String dictator) {
        try {
            difficultWordService.handlePracticeFailureByText(wordText, dictator);
            return ApiResponse.success("已记录失败", null);
        } catch (Exception e) {
            log.error("Failed to handle practice failure", e);
            return ApiResponse.error("处理失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeDifficultWord(@PathVariable Long id) {
        try {
            difficultWordService.removeDifficultWord(id);
            return ApiResponse.success("已从生词本移除", null);
        } catch (Exception e) {
            log.error("Failed to remove difficult word", e);
            return ApiResponse.error("移除失败: " + e.getMessage());
        }
    }

    private DifficultWordDTO toDifficultWordDTO(DifficultWord dw) {
        DifficultWordDTO dto = new DifficultWordDTO();
        dto.setId(dw.getId());
        dto.setWordText(dw.getWordText());
        dto.setDictator(dw.getDictator());
        dto.setErrorCount(dw.getErrorCount());
        dto.setAvgDurationSeconds(dw.getAvgDurationSeconds());
        dto.setLastPracticeDate(dw.getLastPracticeDate());
        dto.setMasteryLevel(dw.getMasteryLevel());
        return dto;
    }
}
