package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 词语DTO
 */
@Data
public class WordDTO {
    private Long id;
    private String wordText;
    private String pinyin;
    private Long batchId;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createdAt;
}