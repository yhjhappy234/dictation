package com.yhj.dictation.service;

import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DifficultWordRepository;
import com.yhj.dictation.repository.DictationRecordRepository;
import com.yhj.dictation.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 生词本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifficultWordService {

    private final DifficultWordRepository difficultWordRepository;
    private final DictationRecordRepository recordRepository;
    private final WordRepository wordRepository;

    /**
     * 添加或更新生词
     */
    @Transactional
    public DifficultWord addOrUpdateDifficultWord(Long wordId) {
        Optional<DifficultWord> existingOpt = difficultWordRepository.findByWordId(wordId);

        DifficultWord difficultWord;
        if (existingOpt.isPresent()) {
            difficultWord = existingOpt.get();
            difficultWord.setErrorCount(difficultWord.getErrorCount() + 1);
            difficultWord.setUpdatedAt(LocalDateTime.now());

            // 更新平均时长
            Double avgDuration = recordRepository.findAvgDurationByWordId(wordId);
            if (avgDuration != null) {
                difficultWord.setAvgDurationSeconds(avgDuration.intValue());
            }

            // 降低掌握级别
            if (difficultWord.getMasteryLevel() > 0) {
                difficultWord.setMasteryLevel(difficultWord.getMasteryLevel() - 1);
            }
        } else {
            difficultWord = new DifficultWord();
            difficultWord.setWordId(wordId);
            difficultWord.setErrorCount(1);
            difficultWord.setMasteryLevel(0);
            difficultWord.setCreatedAt(LocalDateTime.now());
            difficultWord.setUpdatedAt(LocalDateTime.now());

            Double avgDuration = recordRepository.findAvgDurationByWordId(wordId);
            if (avgDuration != null) {
                difficultWord.setAvgDurationSeconds(avgDuration.intValue());
            }
        }

        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord = difficultWordRepository.save(difficultWord);

        log.info("Added/Updated difficult word: {} with error count: {}", wordId, difficultWord.getErrorCount());
        return difficultWord;
    }

    /**
     * 根据ID获取生词
     */
    public Optional<DifficultWord> getDifficultWordById(Long id) {
        return difficultWordRepository.findById(id);
    }

    /**
     * 根据词语ID获取生词
     */
    public Optional<DifficultWord> getDifficultWordByWordId(Long wordId) {
        return difficultWordRepository.findByWordId(wordId);
    }

    /**
     * 获取所有生词（按错误次数降序）
     */
    public List<DifficultWord> getAllDifficultWords() {
        return difficultWordRepository.findAllByOrderByErrorCountDesc();
    }

    /**
     * 获取高难度生词
     */
    public List<DifficultWord> getDifficultWordsByMasteryLevel(Integer maxMasteryLevel) {
        return difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(maxMasteryLevel);
    }

    /**
     * 获取智能推荐的生词（错误多或耗时长）
     */
    public List<DifficultWord> getRecommendedDifficultWords(Integer minErrors, Integer minDuration) {
        return difficultWordRepository.findDifficultWords(minErrors, minDuration);
    }

    /**
     * 更新掌握级别
     */
    @Transactional
    public DifficultWord updateMasteryLevel(Long wordId, Integer masteryLevel) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordId(wordId);
        if (difficultWordOpt.isEmpty()) {
            throw new IllegalArgumentException("Difficult word not found for word: " + wordId);
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        difficultWord.setMasteryLevel(Math.max(0, Math.min(5, masteryLevel)));
        difficultWord.setUpdatedAt(LocalDateTime.now());

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Updated mastery level for word: {} to {}", wordId, masteryLevel);
        return difficultWord;
    }

    /**
     * 增加掌握级别（练习成功后）
     */
    @Transactional
    public DifficultWord increaseMasteryLevel(Long wordId) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordId(wordId);
        if (difficultWordOpt.isEmpty()) {
            // 如果不存在，说明已经掌握得很好，不需要记录
            return null;
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        int newLevel = Math.min(5, difficultWord.getMasteryLevel() + 1);
        difficultWord.setMasteryLevel(newLevel);
        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord.setUpdatedAt(LocalDateTime.now());

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Increased mastery level for word: {} to {}", wordId, newLevel);
        return difficultWord;
    }

    /**
     * 减少掌握级别（练习失败后）
     */
    @Transactional
    public DifficultWord decreaseMasteryLevel(Long wordId) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordId(wordId);
        if (difficultWordOpt.isEmpty()) {
            // 如果不存在，创建一个新的
            return addOrUpdateDifficultWord(wordId);
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        int newLevel = Math.max(0, difficultWord.getMasteryLevel() - 1);
        difficultWord.setMasteryLevel(newLevel);
        difficultWord.setErrorCount(difficultWord.getErrorCount() + 1);
        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord.setUpdatedAt(LocalDateTime.now());

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Decreased mastery level for word: {} to {}", wordId, newLevel);
        return difficultWord;
    }

    /**
     * 删除生词记录
     */
    @Transactional
    public void deleteDifficultWord(Long id) {
        difficultWordRepository.deleteById(id);
        log.info("Deleted difficult word: {}", id);
    }

    /**
     * 根据词语ID删除生词记录
     */
    @Transactional
    public void deleteDifficultWordByWordId(Long wordId) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordId(wordId);
        if (difficultWordOpt.isPresent()) {
            difficultWordRepository.delete(difficultWordOpt.get());
            log.info("Deleted difficult word for word: {}", wordId);
        }
    }

    /**
     * 练习成功处理
     */
    @Transactional
    public void handlePracticeSuccess(Long wordId) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordId(wordId);
        if (difficultWordOpt.isPresent()) {
            DifficultWord difficultWord = difficultWordOpt.get();
            int newLevel = Math.min(5, difficultWord.getMasteryLevel() + 1);
            difficultWord.setMasteryLevel(newLevel);
            difficultWord.setLastPracticeDate(LocalDateTime.now());
            difficultWord.setUpdatedAt(LocalDateTime.now());
            difficultWordRepository.save(difficultWord);
            log.info("Practice success - mastery level increased for word: {} to {}", wordId, newLevel);
        }
    }

    /**
     * 练习失败处理
     */
    @Transactional
    public void handlePracticeFailure(Long wordId) {
        addOrUpdateDifficultWord(wordId);
    }

    /**
     * 获取生词本列表（返回DTO）
     */
    public List<DifficultWordDTO> getDifficultWords() {
        return difficultWordRepository.findAllByOrderByErrorCountDesc()
                .stream()
                .map(this::toDifficultWordDTO)
                .collect(Collectors.toList());
    }

    /**
     * 添加生词（返回DTO）
     */
    @Transactional
    public DifficultWordDTO addDifficultWordDTO(Long wordId) {
        DifficultWord difficultWord = addOrUpdateDifficultWord(wordId);
        return toDifficultWordDTO(difficultWord);
    }

    /**
     * 移除生词
     */
    @Transactional
    public void removeDifficultWord(Long id) {
        DifficultWord difficultWord = difficultWordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("生词不存在: " + id));
        difficultWordRepository.delete(difficultWord);
        log.info("Removed difficult word: {}", id);
    }

    /**
     * 转换为DTO
     */
    private DifficultWordDTO toDifficultWordDTO(DifficultWord difficultWord) {
        DifficultWordDTO dto = new DifficultWordDTO();
        dto.setId(difficultWord.getId());
        dto.setWordId(difficultWord.getWordId());
        dto.setErrorCount(difficultWord.getErrorCount());
        dto.setAvgDurationSeconds(difficultWord.getAvgDurationSeconds());
        dto.setLastPracticeDate(difficultWord.getLastPracticeDate());
        dto.setMasteryLevel(difficultWord.getMasteryLevel());

        // 获取词语信息
        wordRepository.findById(difficultWord.getWordId()).ifPresent(word -> {
            dto.setWordText(word.getWordText());
            dto.setPinyin(word.getPinyin());
        });

        return dto;
    }
}