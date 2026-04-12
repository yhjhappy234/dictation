package com.yhj.dictation.repository;

import com.yhj.dictation.entity.DictationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DictationRecordRepository extends JpaRepository<DictationRecord, Long> {

    List<DictationRecord> findByBatchId(Long batchId);

    List<DictationRecord> findByWordId(Long wordId);

    Optional<DictationRecord> findByWordIdAndStatus(Long wordId, DictationRecord.RecordStatus status);

    List<DictationRecord> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT dr FROM DictationRecord dr WHERE dr.startTime >= :start AND dr.startTime < :end ORDER BY dr.startTime DESC")
    List<DictationRecord> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(dr.durationSeconds) FROM DictationRecord dr WHERE dr.wordId = :wordId AND dr.durationSeconds IS NOT NULL")
    Double findAvgDurationByWordId(@Param("wordId") Long wordId);

    @Query("SELECT COUNT(dr) FROM DictationRecord dr WHERE dr.wordId = :wordId AND dr.repeatCount > 0")
    Integer countRepeatByWordId(@Param("wordId") Long wordId);
}