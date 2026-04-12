package com.yhj.dictation.repository;

import com.yhj.dictation.entity.DifficultWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DifficultWordRepository extends JpaRepository<DifficultWord, Long> {

    Optional<DifficultWord> findByWordId(Long wordId);

    List<DifficultWord> findByMasteryLevelLessThanOrderByErrorCountDesc(Integer masteryLevel);

    List<DifficultWord> findAllByOrderByErrorCountDesc();

    @Query("SELECT dw FROM DifficultWord dw WHERE dw.errorCount >= :minErrors OR dw.avgDurationSeconds >= :minDuration ORDER BY dw.errorCount DESC")
    List<DifficultWord> findDifficultWords(@org.springframework.data.repository.query.Param("minErrors") Integer minErrors, @org.springframework.data.repository.query.Param("minDuration") Integer minDuration);
}