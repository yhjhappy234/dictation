package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 词语实体
 */
@Data
@Entity
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_text", nullable = false)
    private String wordText;

    @Column(name = "pinyin")
    private String pinyin;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WordStatus status = WordStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum WordStatus {
        PENDING,     // 待听写
        PLAYING,     // 正在播放
        COMPLETED,   // 已完成
        SKIPPED      // 已跳过
    }
}