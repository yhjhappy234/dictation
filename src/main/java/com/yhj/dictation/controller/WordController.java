package com.yhj.dictation.controller;

import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.StatusUpdateRequest;
import com.yhj.dictation.dto.WordAddRequest;
import com.yhj.dictation.dto.WordDTO;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 词语控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/{id}")
    public ApiResponse<WordDTO> getWordById(@PathVariable Long id) {
        return wordService.getWordById(id)
                .map(this::toWordDTO)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("词语不存在: " + id));
    }

    @PutMapping("/{id}/status")
    @AuditLog(operation = "更新词语状态", recordParams = true)
    public ApiResponse<WordDTO> updateWordStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Word.WordStatus status = Word.WordStatus.valueOf(request.getStatus());
            Word word = wordService.updateWordStatus(id, status);
            return ApiResponse.success("状态更新成功", toWordDTO(word));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的状态: " + request.getStatus());
        }
    }

    @PutMapping("/{id}/pinyin")
    @AuditLog(operation = "更新词语拼音", recordParams = true)
    public ApiResponse<WordDTO> updateWordPinyin(@PathVariable Long id, @RequestBody WordAddRequest request) {
        try {
            Word word = wordService.updateWordPinyin(id, request.getPinyin());
            return ApiResponse.success("拼音更新成功", toWordDTO(word));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("词语不存在: " + id);
        }
    }

    @PostMapping("/{id}/complete")
    @AuditLog(operation = "标记词语完成", recordParams = true)
    public ApiResponse<WordDTO> markAsCompleted(@PathVariable Long id) {
        try {
            Word word = wordService.markAsCompleted(id);
            return ApiResponse.success("词语已完成", toWordDTO(word));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("词语不存在: " + id);
        }
    }

    @PostMapping("/{id}/skip")
    @AuditLog(operation = "跳过词语", recordParams = true)
    public ApiResponse<WordDTO> markAsSkipped(@PathVariable Long id) {
        try {
            Word word = wordService.markAsSkipped(id);
            return ApiResponse.success("词语已跳过", toWordDTO(word));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("词语不存在: " + id);
        }
    }

    @GetMapping("/batch/{batchId}/next")
    public ApiResponse<WordDTO> getNextWord(@PathVariable Long batchId, @RequestParam Integer currentOrder) {
        return wordService.getNextWord(batchId, currentOrder)
                .map(this::toWordDTO)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("没有下一个词语"));
    }

    @GetMapping("/batch/{batchId}/previous")
    public ApiResponse<WordDTO> getPreviousWord(@PathVariable Long batchId, @RequestParam Integer currentOrder) {
        return wordService.getPreviousWord(batchId, currentOrder)
                .map(this::toWordDTO)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("没有上一个词语"));
    }

    @GetMapping("/batch/{batchId}/first")
    public ApiResponse<WordDTO> getFirstWord(@PathVariable Long batchId) {
        return wordService.getFirstWord(batchId)
                .map(this::toWordDTO)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("批次中没有词语"));
    }

    @DeleteMapping("/{id}")
    @AuditLog(operation = "删除词语", level = AuditLog.LogLevel.IMPORTANT, recordParams = true)
    public ApiResponse<Void> deleteWord(@PathVariable Long id) {
        try {
            wordService.deleteWord(id);
            return ApiResponse.success("词语删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete word", e);
            return ApiResponse.error("删除词语失败: " + e.getMessage());
        }
    }

    private WordDTO toWordDTO(Word word) {
        WordDTO dto = new WordDTO();
        dto.setId(word.getId());
        dto.setWordText(word.getWordText());
        dto.setPinyin(word.getPinyin());
        dto.setBatchId(word.getBatchId());
        dto.setSortOrder(word.getSortOrder());
        dto.setStatus(word.getStatus().name());
        dto.setCreatedAt(word.getCreatedAt());
        return dto;
    }
}
