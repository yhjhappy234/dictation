package com.yhj.dictation.dto;

import lombok.Data;
import java.util.List;

/**
 * 任务听写结果请求DTO
 */
@Data
public class TaskResultRequest {
    private Long taskId;
    private List<WordResult> wordResults;

    @Data
    public static class WordResult {
        private String word;
        private Boolean isCorrect;
        private Integer errorCount;
    }
}