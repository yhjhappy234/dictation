package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.SuggestionDTO;
import com.yhj.dictation.entity.Suggestion;
import com.yhj.dictation.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 建议控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public ApiResponse<List<SuggestionDTO>> getAllSuggestions() {
        List<SuggestionDTO> suggestions = suggestionService.getAllSuggestionDTOs();
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<SuggestionDTO>> getSuggestionsByType(@PathVariable String type) {
        try {
            Suggestion.SuggestionType suggestionType = Suggestion.SuggestionType.valueOf(type);
            List<SuggestionDTO> suggestions = suggestionService.getSuggestionsByType(suggestionType)
                    .stream()
                    .map(this::toSuggestionDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success(suggestions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的建议类型: " + type);
        }
    }

    @GetMapping("/word/{wordId}")
    public ApiResponse<List<SuggestionDTO>> getSuggestionsByWordId(@PathVariable Long wordId) {
        List<SuggestionDTO> suggestions = suggestionService.getSuggestionsByWordId(wordId)
                .stream()
                .map(this::toSuggestionDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/review")
    public ApiResponse<List<SuggestionDTO>> getReviewNeededSuggestions() {
        List<SuggestionDTO> suggestions = suggestionService.getReviewNeededSuggestions()
                .stream()
                .map(this::toSuggestionDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/high-difficulty")
    public ApiResponse<List<SuggestionDTO>> getHighDifficultySuggestions() {
        List<SuggestionDTO> suggestions = suggestionService.getHighDifficultySuggestions()
                .stream()
                .map(this::toSuggestionDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/frequent-error")
    public ApiResponse<List<SuggestionDTO>> getFrequentErrorSuggestions() {
        List<SuggestionDTO> suggestions = suggestionService.getFrequentErrorSuggestions()
                .stream()
                .map(this::toSuggestionDTO)
                .collect(Collectors.toList());
        return ApiResponse.success(suggestions);
    }

    @PutMapping("/{id}/priority")
    public ApiResponse<SuggestionDTO> updatePriority(
            @PathVariable Long id,
            @RequestParam Integer priority) {
        try {
            Suggestion suggestion = suggestionService.updatePriority(id, priority);
            return ApiResponse.success("优先级已更新", toSuggestionDTO(suggestion));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("建议不存在: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSuggestion(@PathVariable Long id) {
        try {
            suggestionService.deleteSuggestion(id);
            return ApiResponse.success("建议已删除", null);
        } catch (Exception e) {
            log.error("Failed to delete suggestion", e);
            return ApiResponse.error("删除建议失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/word/{wordId}")
    public ApiResponse<Void> deleteSuggestionsByWordId(@PathVariable Long wordId) {
        try {
            suggestionService.deleteSuggestionsByWordId(wordId);
            return ApiResponse.success("词语建议已清空", null);
        } catch (Exception e) {
            log.error("Failed to delete suggestions by word id", e);
            return ApiResponse.error("删除建议失败: " + e.getMessage());
        }
    }

    private SuggestionDTO toSuggestionDTO(Suggestion suggestion) {
        SuggestionDTO dto = new SuggestionDTO();
        dto.setId(suggestion.getId());
        dto.setWordId(suggestion.getWordId());
        dto.setSuggestionType(suggestion.getSuggestionType().name());
        dto.setPriority(suggestion.getPriority());
        dto.setMessage(suggestion.getMessage());
        dto.setCreatedAt(suggestion.getCreatedAt());
        return dto;
    }
}
