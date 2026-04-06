package com.yhj.dictation.service;

import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 词语服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;

    /**
     * 创建词语
     */
    @Transactional
    public Word createWord(String wordText, Long batchId, Integer sortOrder) {
        Word word = new Word();
        word.setWordText(wordText);
        word.setBatchId(batchId);
        word.setSortOrder(sortOrder);
        word.setStatus(Word.WordStatus.PENDING);
        word.setCreatedAt(LocalDateTime.now());

        word = wordRepository.save(word);
        log.info("Created word: {} in batch: {}", word.getId(), batchId);
        return word;
    }

    /**
     * 根据ID获取词语
     */
    public Optional<Word> getWordById(Long id) {
        return wordRepository.findById(id);
    }

    /**
     * 获取批次中的所有词语（按顺序）
     */
    public List<Word> getWordsByBatchId(Long batchId) {
        return wordRepository.findByBatchIdOrderBySortOrder(batchId);
    }

    /**
     * 获取批次中指定状态的词语
     */
    public List<Word> getWordsByBatchIdAndStatus(Long batchId, Word.WordStatus status) {
        return wordRepository.findByBatchIdAndStatus(batchId, status);
    }

    /**
     * 获取批次中的第一个词语
     */
    public Optional<Word> getFirstWord(Long batchId) {
        return wordRepository.findByBatchIdAndSortOrder(batchId, 1);
    }

    /**
     * 获取下一个词语
     */
    public Optional<Word> getNextWord(Long batchId, Integer currentSortOrder) {
        List<Word> words = wordRepository.findByBatchIdAndSortOrderGreaterThanOrderBySortOrder(batchId, currentSortOrder);
        return words.isEmpty() ? Optional.empty() : Optional.of(words.get(0));
    }

    /**
     * 获取上一个词语
     */
    public Optional<Word> getPreviousWord(Long batchId, Integer currentSortOrder) {
        List<Word> words = wordRepository.findByBatchIdAndSortOrderLessThanOrderBySortOrderDesc(batchId, currentSortOrder);
        return words.isEmpty() ? Optional.empty() : Optional.of(words.get(0));
    }

    /**
     * 更新词语状态
     */
    @Transactional
    public Word updateWordStatus(Long wordId, Word.WordStatus status) {
        Optional<Word> wordOpt = wordRepository.findById(wordId);
        if (wordOpt.isEmpty()) {
            throw new IllegalArgumentException("Word not found: " + wordId);
        }

        Word word = wordOpt.get();
        word.setStatus(status);
        word = wordRepository.save(word);

        log.info("Updated word {} status to {}", wordId, status);
        return word;
    }

    /**
     * 更新词语拼音
     */
    @Transactional
    public Word updateWordPinyin(Long wordId, String pinyin) {
        Optional<Word> wordOpt = wordRepository.findById(wordId);
        if (wordOpt.isEmpty()) {
            throw new IllegalArgumentException("Word not found: " + wordId);
        }

        Word word = wordOpt.get();
        word.setPinyin(pinyin);
        word = wordRepository.save(word);

        log.info("Updated word {} pinyin to {}", wordId, pinyin);
        return word;
    }

    /**
     * 标记词语为已完成
     */
    @Transactional
    public Word markAsCompleted(Long wordId) {
        return updateWordStatus(wordId, Word.WordStatus.COMPLETED);
    }

    /**
     * 标记词语为已跳过
     */
    @Transactional
    public Word markAsSkipped(Long wordId) {
        return updateWordStatus(wordId, Word.WordStatus.SKIPPED);
    }

    /**
     * 标记词语为正在播放
     */
    @Transactional
    public Word markAsPlaying(Long wordId) {
        return updateWordStatus(wordId, Word.WordStatus.PLAYING);
    }

    /**
     * 重置批次中所有词语状态
     */
    @Transactional
    public void resetBatchWords(Long batchId) {
        List<Word> words = wordRepository.findByBatchIdOrderBySortOrder(batchId);
        for (Word word : words) {
            word.setStatus(Word.WordStatus.PENDING);
        }
        wordRepository.saveAll(words);
        log.info("Reset all words status in batch: {}", batchId);
    }

    /**
     * 删除词语
     */
    @Transactional
    public void deleteWord(Long wordId) {
        wordRepository.deleteById(wordId);
        log.info("Deleted word: {}", wordId);
    }

    /**
     * 统计批次中的词语数量
     */
    public long countWordsByBatchId(Long batchId) {
        return wordRepository.countByBatchId(batchId);
    }

    /**
     * 统计批次中指定状态的词语数量
     */
    public long countWordsByBatchIdAndStatus(Long batchId, Word.WordStatus status) {
        return wordRepository.countByBatchIdAndStatus(batchId, status);
    }
}
