package com.yhj.dictation.repository;

import com.yhj.dictation.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    List<Word> findByBatchIdOrderBySortOrder(Long batchId);

    Optional<Word> findByBatchIdAndSortOrder(Long batchId, Integer sortOrder);

    List<Word> findByBatchIdAndStatus(Long batchId, Word.WordStatus status);

    long countByBatchId(Long batchId);

    long countByBatchIdAndStatus(Long batchId, Word.WordStatus status);

    List<Word> findByBatchIdAndSortOrderGreaterThanOrderBySortOrder(Long batchId, Integer sortOrder);

    List<Word> findByBatchIdAndSortOrderLessThanOrderBySortOrderDesc(Long batchId, Integer sortOrder);
}