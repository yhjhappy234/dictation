package com.yhj.dictation.dto;

import lombok.Data;

/**
 * 用户信息DTO
 */
@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String status;
    private String role;
    private String avatar;
}