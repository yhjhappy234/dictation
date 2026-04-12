package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 听写任务创建请求DTO
 */
@Data
public class TaskCreateRequest {
    private String taskName;       // 任务名称
    private String words;          // 词语（空格或换行分隔）
}