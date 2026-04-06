package com.yhj.dictation.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 批次创建请求DTO
 */
@Data
public class BatchCreateRequest {
    private String batchName;
    private String words; // 空格分隔的词语
}