package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 听写记录实体
 */
@Data
@Entity
@Table(name = "dictation_record")
public class DictationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "repeat_count", nullable = false)
    private Integer repeatCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecordStatus status = RecordStatus.STARTED;

    public enum RecordStatus {
        STARTED,    // 已开始
        COMPLETED,  // 已完成
        SKIPPED     // 已跳过
    }
}