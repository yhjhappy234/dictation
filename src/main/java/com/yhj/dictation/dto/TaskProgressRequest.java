package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 任务进度更新请求DTO
 */
@Data
public class TaskProgressRequest {
    private Long taskId;
    private Integer currentIndex;
    private Integer correctCount;
    private Integer wrongCount;
    private String lastWord;  // 最后听写的词语
    private Boolean lastCorrect;  // 最后一个是否正确
}