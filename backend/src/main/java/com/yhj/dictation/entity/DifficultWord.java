package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 生词本实体
 */
@Data
@Entity
@Table(name = "difficult_word", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"word_id"})
})
public class DifficultWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id", nullable = false, unique = true)
    private Long wordId;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "avg_duration_seconds")
    private Integer avgDurationSeconds;

    @Column(name = "last_practice_date")
    private LocalDateTime lastPracticeDate;

    @Column(name = "mastery_level", nullable = false)
    private Integer masteryLevel = 0; // 0-5级别，0为未掌握，5为完全掌握

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}