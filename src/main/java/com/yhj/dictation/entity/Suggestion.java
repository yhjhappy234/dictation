package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 建议实体
 */
@Data
@Entity
@Table(name = "suggestion")
public class Suggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", nullable = false)
    private SuggestionType suggestionType;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // 1-5，数字越大优先级越高

    @Column(name = "message")
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum SuggestionType {
        REVIEW_NEEDED,      // 需要复习
        HIGH_DIFFICULTY,    // 高难度词语
        FREQUENT_ERROR,     // 常错词
        LONG_DURATION,      // 反应时间长
        NEW_WORD            // 新词
    }
}