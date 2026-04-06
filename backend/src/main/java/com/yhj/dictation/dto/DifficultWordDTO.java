package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 生词DTO
 */
@Data
public class DifficultWordDTO {
    private Long id;
    private Long wordId;
    private String wordText;
    private String pinyin;
    private Integer errorCount;
    private Integer avgDurationSeconds;
    private LocalDateTime lastPracticeDate;
    private Integer masteryLevel;
}