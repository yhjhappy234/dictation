package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class PasswordUpdateRequest {
    private String newPassword;
}