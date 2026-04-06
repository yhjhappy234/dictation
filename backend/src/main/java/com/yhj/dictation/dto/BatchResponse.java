package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 批次响应DTO
 */
@Data
public class BatchResponse {
    private Long id;
    private String batchName;
    private LocalDateTime createdAt;
    private Integer totalWords;
    private Integer completedWords;
    private String status;
    private Double progress;
}