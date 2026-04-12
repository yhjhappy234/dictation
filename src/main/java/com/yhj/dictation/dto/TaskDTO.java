package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 听写任务DTO
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