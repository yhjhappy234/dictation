package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 创建用户请求DTO
 */
@Data
public class UserCreateRequest {
    private String username;
    private String password;
    private String role;
}