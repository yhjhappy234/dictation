package com.yhj.dictation.repository;

import com.yhj.dictation.entity.DictationBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DictationBatchRepository extends JpaRepository<DictationBatch, Long> {

    List<DictationBatch> findByStatusOrderByCreatedAtDesc(DictationBatch.BatchStatus status);

    List<DictationBatch> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<DictationBatch> findAllByOrderByCreatedAtDesc();
}