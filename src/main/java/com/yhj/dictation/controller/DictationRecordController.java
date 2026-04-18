package com.yhj.dictation.controller;

import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.CompleteRequest;
import com.yhj.dictation.dto.DictationResponse;
import com.yhj.dictation.dto.ReportDTO;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.service.DictationRecordService;
import com.yhj.dictation.service.DifficultWordService;
import com.yhj.dictation.service.SuggestionService;
import com.yhj.dictation.service.WordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 听写记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class DictationRecordController {

    private final DictationRecordService recordService;
    private final DifficultWordService difficultWordService;
    private final SuggestionService suggestionService;
    private final WordService wordService;

    /**
     * 开始听写记录
     */
    @PostMapping("/start")
    public ApiResponse<DictationResponse> startRecord(@RequestParam Long wordId, @RequestParam Long batchId) {
        try {
            DictationRecord record = recordService.startRecord(wordId, batchId);
            return ApiResponse.success("开始听写", toDictationResponse(record));
        } catch (Exception e) {
            log.error("Failed to start record", e);
            return ApiResponse.error("开始听写失败: " + e.getMessage());
        }
    }

    /**
     * 完成听写记录
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<DictationResponse> completeRecord(@PathVariable Long id) {
        try {
            DictationRecord record = recordService.completeRecord(id);
            Word word = wordService.getWordById(record.getWordId()).orElse(null);
            if (word != null) {
                difficultWordService.handlePracticeSuccessByText(word.getWordText());
            }
            return ApiResponse.success("听写完成", toDictationResponse(record));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("记录不存在: " + id);
        }
    }

    /**
     * 通过词语ID完成听写（前端调用）
     */
    @PostMapping("/complete/{wordId}")
    public ApiResponse<DictationResponse> completeByWordId(
            @PathVariable Long wordId,
            @RequestBody(required = false) CompleteRequest request) {
        try {
            Integer duration = request != null ? request.getDuration() : null;
            DictationRecord record = recordService.completeByWordId(wordId, duration);

            Word word = wordService.getWordById(wordId).orElse(null);
            if (word != null) {
                difficultWordService.handlePracticeSuccessByText(word.getWordText());
            }

            return ApiResponse.success("听写完成", toDictationResponse(record));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("记录不存在: " + e.getMessage());
        }
    }

    /**
     * 结束听写批次
     */
    @PostMapping("/end/{batchId}")
    public ApiResponse<Void> endDictation(@PathVariable Long batchId) {
        try {
            suggestionService.generateSuggestions(batchId);
            return ApiResponse.success("听写结束", null);
        } catch (Exception e) {
            log.error("Failed to end dictation", e);
            return ApiResponse.error("结束听写失败: " + e.getMessage());
        }
    }

    /**
     * 跳过听写记录
     */
    @PostMapping("/{id}/skip")
    public ApiResponse<DictationResponse> skipRecord(@PathVariable Long id) {
        try {
            DictationRecord record = recordService.skipRecord(id);

            Word word = wordService.getWordById(record.getWordId()).orElse(null);
            if (word != null) {
                difficultWordService.handlePracticeFailureByText(word.getWordText(), null);
            }

            return ApiResponse.success("已跳过", toDictationResponse(record));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("记录不存在: " + id);
        }
    }

    /**
     * 增加重复次数
     */
    @PostMapping("/{id}/repeat")
    public ApiResponse<DictationResponse> incrementRepeatCount(@PathVariable Long id) {
        try {
            DictationRecord record = recordService.incrementRepeatCount(id);
            return ApiResponse.success("重复次数已更新", toDictationResponse(record));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("记录不存在: " + id);
        }
    }

    /**
     * 获取记录详情
     */
    @GetMapping("/{id}")
    @AuditLog(operation = "获取听写记录详情")
    public ApiResponse<DictationResponse> getRecordById(@PathVariable Long id) {
        return recordService.getRecordById(id)
                .map(this::toDictationResponse)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("记录不存在: " + id));
    }

    /**
     * 获取批次的所有记录
     */
    @GetMapping("/batch/{batchId}")
    @AuditLog(operation = "获取批次记录")
    public ApiResponse<List<DictationResponse>> getRecordsByBatchId(@PathVariable Long batchId) {
        List<DictationResponse> records = recordService.getRecordsByBatchId(batchId)
                .stream()
                .map(this::toDictationResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(records);
    }

    /**
     * 获取今日记录
     */
    @GetMapping("/today")
    @AuditLog(operation = "获取今日记录")
    public ApiResponse<List<DictationResponse>> getTodayRecords() {
        List<DictationResponse> records = recordService.getTodayRecords()
                .stream()
                .map(this::toDictationResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(records);
    }

    /**
     * 获取指定日期范围的记录
     */
    @GetMapping("/range")
    @AuditLog(operation = "获取日期范围记录")
    public ApiResponse<List<DictationResponse>> getRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);

        List<DictationResponse> records = recordService.getRecordsByDateRange(startDateTime, endDateTime)
                .stream()
                .map(this::toDictationResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(records);
    }

    /**
     * 获取今日统计报告
     */
    @GetMapping("/report/today")
    @AuditLog(operation = "获取今日统计报告")
    public ApiResponse<ReportDTO> getTodayReport() {
        List<DictationRecord> records = recordService.getTodayRecords();
        ReportDTO report = buildReport(records);
        return ApiResponse.success(report);
    }

    /**
     * 获取批次统计报告
     */
    @GetMapping("/report/batch/{batchId}")
    @AuditLog(operation = "获取批次统计报告")
    public ApiResponse<ReportDTO> getBatchReport(@PathVariable Long batchId) {
        List<DictationRecord> records = recordService.getRecordsByBatchId(batchId);
        ReportDTO report = buildReport(records);
        return ApiResponse.success(report);
    }

    /**
     * 完成批次并生成建议
     */
    @PostMapping("/batch/{batchId}/finish")
    public ApiResponse<Void> finishBatchWithSuggestions(@PathVariable Long batchId) {
        try {
            // 生成建议
            suggestionService.generateSuggestions(batchId);
            return ApiResponse.success("建议已生成", null);
        } catch (Exception e) {
            log.error("Failed to generate suggestions", e);
            return ApiResponse.error("生成建议失败: " + e.getMessage());
        }
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        try {
            recordService.deleteRecord(id);
            return ApiResponse.success("记录删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete record", e);
            return ApiResponse.error("删除记录失败: " + e.getMessage());
        }
    }

    /**
     * 转换为DTO
     */
    private DictationResponse toDictationResponse(DictationRecord record) {
        DictationResponse response = new DictationResponse();
        response.setId(record.getId());
        response.setWordId(record.getWordId());
        response.setBatchId(record.getBatchId());
        response.setStartTime(record.getStartTime());
        response.setEndTime(record.getEndTime());
        response.setDurationSeconds(record.getDurationSeconds());
        response.setRepeatCount(record.getRepeatCount());
        response.setStatus(record.getStatus().name());
        return response;
    }

    /**
     * 构建统计报告
     */
    private ReportDTO buildReport(List<DictationRecord> records) {
        ReportDTO report = new ReportDTO();
        report.setTotalCount(records.size());

        int completedCount = 0;
        int skippedCount = 0;
        int totalDuration = 0;
        int totalRepeats = 0;

        for (DictationRecord record : records) {
            if (record.getStatus() == DictationRecord.RecordStatus.COMPLETED) {
                completedCount++;
            } else if (record.getStatus() == DictationRecord.RecordStatus.SKIPPED) {
                skippedCount++;
            }
            if (record.getDurationSeconds() != null) {
                totalDuration += record.getDurationSeconds();
            }
            totalRepeats += record.getRepeatCount();
        }

        report.setCompletedCount(completedCount);
        report.setSkippedCount(skippedCount);
        report.setTotalDuration(totalDuration);
        report.setTotalRepeats(totalRepeats);
        report.setAvgDuration(records.isEmpty() ? 0 : totalDuration / completedCount);

        return report;
    }
}