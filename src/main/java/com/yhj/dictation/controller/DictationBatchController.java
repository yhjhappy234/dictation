package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.dto.BatchResponse;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DictationBatchService;
import com.yhj.dictation.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 听写批次控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class DictationBatchController {

    private final DictationBatchService batchService;
    private final WordService wordService;

    /**
     * 创建新批次
     */
    @PostMapping
    public ApiResponse<BatchResponse> createBatch(@RequestBody BatchCreateRequest request) {
        try {
            BatchResponse response = batchService.createBatchResponse(request);
            return ApiResponse.success("批次创建成功", response);
        } catch (Exception e) {
            log.error("Failed to create batch", e);
            return ApiResponse.error("创建批次失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有批次
     */
    @GetMapping
    public ApiResponse<List<BatchResponse>> getAllBatches() {
        List<BatchResponse> batches = batchService.getAllBatchResponses();
        return ApiResponse.success(batches);
    }

    /**
     * 获取今日批次
     */
    @GetMapping("/today")
    public ApiResponse<List<BatchResponse>> getTodayBatches() {
        List<BatchResponse> batches = batchService.getTodayBatchResponses();
        return ApiResponse.success(batches);
    }

    /**
     * 获取日期范围内的批次
     */
    @GetMapping("/range")
    public ApiResponse<List<BatchResponse>> getBatchesByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<BatchResponse> batches = batchService.getBatchesByDateRange(start, end);
        return ApiResponse.success(batches);
    }

    /**
     * 获取批次详情
     */
    @GetMapping("/{id}")
    public ApiResponse<BatchResponse> getBatchById(@PathVariable Long id) {
        try {
            BatchResponse response = batchService.getBatchResponseById(id);
            return ApiResponse.success(response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("批次不存在: " + id);
        }
    }

    /**
     * 开始批次听写
     */
    @PostMapping("/{id}/start")
    public ApiResponse<BatchResponse> startBatch(@PathVariable Long id) {
        try {
            DictationBatch batch = batchService.startBatch(id);
            return ApiResponse.success("批次开始成功", batchService.toBatchResponse(batch));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("批次不存在: " + id);
        }
    }

    /**
     * 完成批次听写
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<BatchResponse> completeBatch(@PathVariable Long id) {
        try {
            DictationBatch batch = batchService.completeBatch(id);
            return ApiResponse.success("批次完成", batchService.toBatchResponse(batch));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("批次不存在: " + id);
        }
    }

    /**
     * 取消批次
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<BatchResponse> cancelBatch(@PathVariable Long id) {
        try {
            DictationBatch batch = batchService.cancelBatch(id);
            return ApiResponse.success("批次已取消", batchService.toBatchResponse(batch));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("批次不存在: " + id);
        }
    }

    /**
     * 删除批次
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBatch(@PathVariable Long id) {
        try {
            batchService.deleteBatch(id);
            return ApiResponse.success("批次删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete batch", e);
            return ApiResponse.error("删除批次失败: " + e.getMessage());
        }
    }

    /**
     * 获取批次中的所有词语
     */
    @GetMapping("/{id}/words")
    public ApiResponse<List<Word>> getBatchWords(@PathVariable Long id) {
        List<Word> words = wordService.getWordsByBatchId(id);
        return ApiResponse.success(words);
    }

    /**
     * 重置批次词语状态
     */
    @PostMapping("/{id}/reset")
    public ApiResponse<Void> resetBatchWords(@PathVariable Long id) {
        try {
            wordService.resetBatchWords(id);
            return ApiResponse.success("词语状态已重置", null);
        } catch (Exception e) {
            log.error("Failed to reset batch words", e);
            return ApiResponse.error("重置失败: " + e.getMessage());
        }
    }
}