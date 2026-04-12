package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 听写响应DTO
 */
@Data
public class DictationResponse {
    private Long id;
    private Long wordId;
    private Long batchId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private Integer repeatCount;
    private String status;
}