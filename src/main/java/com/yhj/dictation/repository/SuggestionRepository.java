package com.yhj.dictation.repository;

import com.yhj.dictation.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findByWordId(Long wordId);

    List<Suggestion> findBySuggestionTypeOrderByPriorityDesc(Suggestion.SuggestionType suggestionType);

    List<Suggestion> findAllByOrderByPriorityDescCreatedAtDesc();
}