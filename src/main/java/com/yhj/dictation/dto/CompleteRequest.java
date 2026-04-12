package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 完成听写请求
 */
@Data
public class CompleteRequest {
    private Integer duration;
    private Boolean isCorrect;
}
