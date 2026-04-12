package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务模板DTO
 */
@Data
public class TaskDTO {
    private Long id;
    private String taskName;
    private List<String> words;  // 词语列表
    private Integer wordCount;
    private LocalDateTime createdAt;
    private Boolean isFavorite;
}