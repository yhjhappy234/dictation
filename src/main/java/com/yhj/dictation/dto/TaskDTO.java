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
    private String status;  // 任务状态
    private Integer currentIndex;  // 当前进度
    private Integer correctCount;  // 正确数
    private Integer wrongCount;  // 错误数
    private String dictator;  // 听写人
}