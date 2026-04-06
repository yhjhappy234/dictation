package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 建议DTO
 */
@Data
public class SuggestionDTO {
    private Long id;
    private Long wordId;
    private String wordText;
    private String suggestionType;
    private Integer priority;
    private String message;
    private LocalDateTime createdAt;
}