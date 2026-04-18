package com.yhj.dictation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 通用API响应DTO
 * 符合规范要求：包含 code, message, data, traceId, timestamp 字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;
    private long timestamp;

    // 向后兼容字段
    private boolean success;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, generateTraceId(), System.currentTimeMillis(), true);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, generateTraceId(), System.currentTimeMillis(), true);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null, generateTraceId(), System.currentTimeMillis(), false);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, generateTraceId(), System.currentTimeMillis(), false);
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}