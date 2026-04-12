package com.yhj.dictation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预设内容DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresetContentDTO {
    private String id;
    private String name;
    private String category;
    private Integer count;
}
