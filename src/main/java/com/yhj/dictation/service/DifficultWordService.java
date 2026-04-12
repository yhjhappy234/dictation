package com.yhj.dictation.service;

import com.yhj.dictation.dto.DifficultWordDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.repository.DifficultWordRepository;
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

    /**
     * 添加或更新生词（基于词语文本）
     */
    @Transactional
    public DifficultWord addOrUpdateDifficultWordByText(String wordText, String dictator) {
        Optional<DifficultWord> existingOpt = difficultWordRepository.findByWordText(wordText);

        DifficultWord difficultWord;
        if (existingOpt.isPresent()) {
            difficultWord = existingOpt.get();
            difficultWord.setErrorCount(difficultWord.getErrorCount() + 1);
            difficultWord.setUpdatedAt(LocalDateTime.now());
            if (dictator != null && !dictator.isEmpty()) {
                difficultWord.setDictator(dictator);
            }

            // 降低掌握级别
            if (difficultWord.getMasteryLevel() > 0) {
                difficultWord.setMasteryLevel(difficultWord.getMasteryLevel() - 1);
            }
        } else {
            difficultWord = new DifficultWord();
            difficultWord.setWordText(wordText);
            difficultWord.setDictator(dictator);
            difficultWord.setErrorCount(1);
            difficultWord.setMasteryLevel(0);
            difficultWord.setCreatedAt(LocalDateTime.now());
            difficultWord.setUpdatedAt(LocalDateTime.now());
        }

        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord = difficultWordRepository.save(difficultWord);

        log.info("Added/Updated difficult word: {} with error count: {}", wordText, difficultWord.getErrorCount());
        return difficultWord;
    }

    /**
     * 批量添加生词
     */
    @Transactional
    public List<DifficultWord> addDifficultWordsBatch(List<DifficultWordDTO> words, String dictator) {
        List<DifficultWord> result = new java.util.ArrayList<>();
        for (DifficultWordDTO wordDTO : words) {
            DifficultWord dw = addOrUpdateDifficultWordByText(wordDTO.getWordText(), dictator);
            result.add(dw);
        }
        return result;
    }

    /**
     * 根据词语文本获取生词
     */
    public Optional<DifficultWord> getDifficultWordByText(String wordText) {
        return difficultWordRepository.findByWordText(wordText);
    }

    /**
     * 根据ID获取生词
     */
    public Optional<DifficultWord> getDifficultWordById(Long id) {
        return difficultWordRepository.findById(id);
    }

    /**
     * 获取所有生词（按错误次数降序）
     */
    public List<DifficultWord> getAllDifficultWords() {
        return difficultWordRepository.findAllByOrderByErrorCountDesc();
    }

    /**
     * 根据听写人获取生词
     */
    public List<DifficultWord> getDifficultWordsByDictator(String dictator) {
        return difficultWordRepository.findByDictatorOrderByErrorCountDesc(dictator);
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
     * 更新掌握级别（基于词语文本）
     */
    @Transactional
    public DifficultWord updateMasteryLevelByText(String wordText, Integer masteryLevel) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordText(wordText);
        if (difficultWordOpt.isEmpty()) {
            throw new IllegalArgumentException("Difficult word not found: " + wordText);
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        difficultWord.setMasteryLevel(Math.max(0, Math.min(5, masteryLevel)));
        difficultWord.setUpdatedAt(LocalDateTime.now());

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Updated mastery level for word: {} to {}", wordText, masteryLevel);
        return difficultWord;
    }

    /**
     * 增加掌握级别（练习成功后）
     */
    @Transactional
    public DifficultWord increaseMasteryLevelByText(String wordText) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordText(wordText);
        if (difficultWordOpt.isEmpty()) {
            return null;
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        int newLevel = Math.min(5, difficultWord.getMasteryLevel() + 1);
        difficultWord.setMasteryLevel(newLevel);
        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord.setUpdatedAt(LocalDateTime.now());

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Increased mastery level for word: {} to {}", wordText, newLevel);
        return difficultWord;
    }

    /**
     * 减少掌握级别（练习失败后）
     */
    @Transactional
    public DifficultWord decreaseMasteryLevelByText(String wordText, String dictator) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordText(wordText);
        if (difficultWordOpt.isEmpty()) {
            return addOrUpdateDifficultWordByText(wordText, dictator);
        }

        DifficultWord difficultWord = difficultWordOpt.get();
        int newLevel = Math.max(0, difficultWord.getMasteryLevel() - 1);
        difficultWord.setMasteryLevel(newLevel);
        difficultWord.setErrorCount(difficultWord.getErrorCount() + 1);
        difficultWord.setLastPracticeDate(LocalDateTime.now());
        difficultWord.setUpdatedAt(LocalDateTime.now());
        if (dictator != null && !dictator.isEmpty()) {
            difficultWord.setDictator(dictator);
        }

        difficultWord = difficultWordRepository.save(difficultWord);
        log.info("Decreased mastery level for word: {} to {}", wordText, newLevel);
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
     * 根据词语文本删除生词记录
     */
    @Transactional
    public void deleteDifficultWordByText(String wordText) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordText(wordText);
        if (difficultWordOpt.isPresent()) {
            difficultWordRepository.delete(difficultWordOpt.get());
            log.info("Deleted difficult word: {}", wordText);
        }
    }

    /**
     * 练习成功处理（基于词语文本）
     */
    @Transactional
    public void handlePracticeSuccessByText(String wordText) {
        Optional<DifficultWord> difficultWordOpt = difficultWordRepository.findByWordText(wordText);
        if (difficultWordOpt.isPresent()) {
            DifficultWord difficultWord = difficultWordOpt.get();
            int newLevel = Math.min(5, difficultWord.getMasteryLevel() + 1);
            difficultWord.setMasteryLevel(newLevel);
            difficultWord.setLastPracticeDate(LocalDateTime.now());
            difficultWord.setUpdatedAt(LocalDateTime.now());
            difficultWordRepository.save(difficultWord);
            log.info("Practice success - mastery level increased for word: {} to {}", wordText, newLevel);
        }
    }

    /**
     * 练习失败处理（基于词语文本）
     */
    @Transactional
    public void handlePracticeFailureByText(String wordText, String dictator) {
        addOrUpdateDifficultWordByText(wordText, dictator);
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
    public DifficultWordDTO addDifficultWordDTO(String wordText, String dictator) {
        DifficultWord difficultWord = addOrUpdateDifficultWordByText(wordText, dictator);
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
        dto.setWordText(difficultWord.getWordText());
        dto.setDictator(difficultWord.getDictator());
        dto.setErrorCount(difficultWord.getErrorCount());
        dto.setAvgDurationSeconds(difficultWord.getAvgDurationSeconds());
        dto.setLastPracticeDate(difficultWord.getLastPracticeDate());
        dto.setMasteryLevel(difficultWord.getMasteryLevel());
        return dto;
    }
}