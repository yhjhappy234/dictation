package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 添加词语请求DTO
 */
@Data
public class WordAddRequest {
    private Long batchId;
    private String words;
    private String pinyin;
}