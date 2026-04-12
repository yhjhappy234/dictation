package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 添加生词请求DTO
 */
@Data
public class DifficultWordAddRequest {
    private Long wordId;       // 兼容旧字段
    private String wordText;   // 词语文本
    private String dictator;   // 听写人
}