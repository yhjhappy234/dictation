package com.yhj.dictation.service;

import com.yhj.dictation.dto.SuggestionDTO;
import com.yhj.dictation.entity.DifficultWord;
import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Suggestion;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DifficultWordRepository;
import com.yhj.dictation.repository.DictationRecordRepository;
import com.yhj.dictation.repository.SuggestionRepository;
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
 * 建议服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final SuggestionRepository suggestionRepository;
    private final DifficultWordRepository difficultWordRepository;
    private final DictationRecordRepository recordRepository;
    private final WordRepository wordRepository;

    /**
     * 创建建议
     */
    @Transactional
    public Suggestion createSuggestion(Long wordId, Suggestion.SuggestionType suggestionType,
                                       Integer priority, String message) {
        Suggestion suggestion = new Suggestion();
        suggestion.setWordId(wordId);
        suggestion.setSuggestionType(suggestionType);
        suggestion.setPriority(priority != null ? priority : 1);
        suggestion.setMessage(message);
        suggestion.setCreatedAt(LocalDateTime.now());

        suggestion = suggestionRepository.save(suggestion);
        log.info("Created suggestion: {} for word: {}", suggestion.getId(), wordId);
        return suggestion;
    }

    /**
     * 根据ID获取建议
     */
    public Optional<Suggestion> getSuggestionById(Long id) {
        return suggestionRepository.findById(id);
    }

    /**
     * 获取词语的所有建议
     */
    public List<Suggestion> getSuggestionsByWordId(Long wordId) {
        return suggestionRepository.findByWordId(wordId);
    }

    /**
     * 获取指定类型的建议（按优先级降序）
     */
    public List<Suggestion> getSuggestionsByType(Suggestion.SuggestionType suggestionType) {
        return suggestionRepository.findBySuggestionTypeOrderByPriorityDesc(suggestionType);
    }

    /**
     * 获取所有建议（按优先级和创建时间降序）
     */
    public List<Suggestion> getAllSuggestions() {
        return suggestionRepository.findAllByOrderByPriorityDescCreatedAtDesc();
    }

    /**
     * 更新建议优先级
     */
    @Transactional
    public Suggestion updatePriority(Long suggestionId, Integer priority) {
        Optional<Suggestion> suggestionOpt = suggestionRepository.findById(suggestionId);
        if (suggestionOpt.isEmpty()) {
            throw new IllegalArgumentException("Suggestion not found: " + suggestionId);
        }

        Suggestion suggestion = suggestionOpt.get();
        suggestion.setPriority(Math.max(1, Math.min(5, priority)));
        suggestion = suggestionRepository.save(suggestion);

        log.info("Updated suggestion {} priority to {}", suggestionId, priority);
        return suggestion;
    }

    /**
     * 更新建议消息
     */
    @Transactional
    public Suggestion updateMessage(Long suggestionId, String message) {
        Optional<Suggestion> suggestionOpt = suggestionRepository.findById(suggestionId);
        if (suggestionOpt.isEmpty()) {
            throw new IllegalArgumentException("Suggestion not found: " + suggestionId);
        }

        Suggestion suggestion = suggestionOpt.get();
        suggestion.setMessage(message);
        suggestion = suggestionRepository.save(suggestion);

        log.info("Updated suggestion {} message", suggestionId);
        return suggestion;
    }

    /**
     * 删除建议
     */
    @Transactional
    public void deleteSuggestion(Long suggestionId) {
        suggestionRepository.deleteById(suggestionId);
        log.info("Deleted suggestion: {}", suggestionId);
    }

    /**
     * 删除词语的所有建议
     */
    @Transactional
    public void deleteSuggestionsByWordId(Long wordId) {
        List<Suggestion> suggestions = suggestionRepository.findByWordId(wordId);
        suggestionRepository.deleteAll(suggestions);
        log.info("Deleted {} suggestions for word: {}", suggestions.size(), wordId);
    }

    /**
     * 创建复习建议
     */
    @Transactional
    public Suggestion createReviewSuggestion(Long wordId, String message) {
        return createSuggestion(wordId, Suggestion.SuggestionType.REVIEW_NEEDED, 3, message);
    }

    /**
     * 创建高难度建议
     */
    @Transactional
    public Suggestion createHighDifficultySuggestion(Long wordId, String message) {
        return createSuggestion(wordId, Suggestion.SuggestionType.HIGH_DIFFICULTY, 4, message);
    }

    /**
     * 创建常错词建议
     */
    @Transactional
    public Suggestion createFrequentErrorSuggestion(Long wordId, String message) {
        return createSuggestion(wordId, Suggestion.SuggestionType.FREQUENT_ERROR, 5, message);
    }

    /**
     * 创建反应时间长建议
     */
    @Transactional
    public Suggestion createLongDurationSuggestion(Long wordId, String message) {
        return createSuggestion(wordId, Suggestion.SuggestionType.LONG_DURATION, 3, message);
    }

    /**
     * 创建新词建议
     */
    @Transactional
    public Suggestion createNewWordSuggestion(Long wordId, String message) {
        return createSuggestion(wordId, Suggestion.SuggestionType.NEW_WORD, 2, message);
    }

    /**
     * 获取需要复习的词语建议
     */
    public List<Suggestion> getReviewNeededSuggestions() {
        return getSuggestionsByType(Suggestion.SuggestionType.REVIEW_NEEDED);
    }

    /**
     * 获取高难度词语建议
     */
    public List<Suggestion> getHighDifficultySuggestions() {
        return getSuggestionsByType(Suggestion.SuggestionType.HIGH_DIFFICULTY);
    }

    /**
     * 获取常错词建议
     */
    public List<Suggestion> getFrequentErrorSuggestions() {
        return getSuggestionsByType(Suggestion.SuggestionType.FREQUENT_ERROR);
    }

    /**
     * 获取所有建议（返回DTO）
     */
    public List<SuggestionDTO> getAllSuggestionDTOs() {
        return getAllSuggestions().stream()
                .map(this::toSuggestionDTO)
                .collect(Collectors.toList());
    }

    /**
     * 为批次生成建议
     */
    @Transactional
    public void generateSuggestions(Long batchId) {
        List<DictationRecord> records = recordRepository.findByBatchId(batchId);

        for (DictationRecord record : records) {
            Word word = wordRepository.findById(record.getWordId()).orElse(null);
            if (word == null) continue;

            // 检查是否需要复习
            if (record.getRepeatCount() > 2) {
                createFrequentErrorSuggestion(word.getId(),
                        "词语 \"" + word.getWordText() + "\" 重复播放次数较多(" + record.getRepeatCount() + "次)，建议加强练习");
            }

            // 检查反应时间
            if (record.getDurationSeconds() != null && record.getDurationSeconds() > 10) {
                createLongDurationSuggestion(word.getId(),
                        "词语 \"" + word.getWordText() + "\" 反应时间较长(" + record.getDurationSeconds() + "秒)，需要熟悉");
            }
        }

        // 检查生词本 - 使用 wordText 字段
        List<DifficultWord> difficultWords = difficultWordRepository.findByMasteryLevelLessThanOrderByErrorCountDesc(3);
        for (DifficultWord dw : difficultWords) {
            String wordText = dw.getWordText();
            if (wordText == null) continue;

            // 直接使用词语文本创建建议，暂不关联 wordId
            // 通过 Batch 中的 Word 查找第一个匹配的词语
            List<Word> matchingWords = wordRepository.findAll().stream()
                    .filter(w -> w.getWordText().equals(wordText))
                    .limit(1)
                    .toList();

            if (!matchingWords.isEmpty()) {
                Word word = matchingWords.get(0);
                createHighDifficultySuggestion(word.getId(),
                        "词语 \"" + wordText + "\" 错误次数:" + dw.getErrorCount() + "，掌握度:" + dw.getMasteryLevel() + "/5，建议重点复习");
            } else {
                // 如果找不到对应的 Word 实体，记录日志但不创建建议
                log.info("DifficultWord '{}' not found in Word table", wordText);
            }
        }

        log.info("Generated suggestions for batch: {}", batchId);
    }

    /**
     * 转换为DTO
     */
    private SuggestionDTO toSuggestionDTO(Suggestion suggestion) {
        SuggestionDTO dto = new SuggestionDTO();
        dto.setId(suggestion.getId());
        dto.setWordId(suggestion.getWordId());
        dto.setSuggestionType(suggestion.getSuggestionType().name());
        dto.setPriority(suggestion.getPriority());
        dto.setMessage(suggestion.getMessage());
        dto.setCreatedAt(suggestion.getCreatedAt());

        wordRepository.findById(suggestion.getWordId()).ifPresent(word -> {
            dto.setWordText(word.getWordText());
        });

        return dto;
    }
}