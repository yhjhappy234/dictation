package com.yhj.dictation.service;

import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.dto.BatchResponse;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DictationBatchRepository;
import com.yhj.dictation.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * 听写批次服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictationBatchService {

    private final DictationBatchRepository batchRepository;
    private final WordRepository wordRepository;

    /**
     * 创建新的听写批次
     */
    @Transactional
    public DictationBatch createBatch(BatchCreateRequest request) {
        DictationBatch batch = new DictationBatch();
        batch.setBatchName(request.getBatchName());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setStatus(DictationBatch.BatchStatus.CREATED);
        batch.setTotalWords(0);
        batch.setCompletedWords(0);

        batch = batchRepository.save(batch);

        // 解析词语并创建Word记录
        if (request.getWords() != null && !request.getWords().trim().isEmpty()) {
            String[] wordArray = request.getWords().split("\\s+");
            int sortOrder = 1;

            for (String wordText : wordArray) {
                if (!wordText.trim().isEmpty()) {
                    Word word = new Word();
                    word.setWordText(wordText.trim());
                    word.setBatchId(batch.getId());
                    word.setSortOrder(sortOrder++);
                    word.setStatus(Word.WordStatus.PENDING);
                    word.setCreatedAt(LocalDateTime.now());
                    wordRepository.save(word);
                }
            }

            batch.setTotalWords(sortOrder - 1);
            batch = batchRepository.save(batch);
        }

        log.info("Created batch: {} with {} words", batch.getId(), batch.getTotalWords());
        return batch;
    }

    /**
     * 获取所有批次
     */
    public List<DictationBatch> getAllBatches() {
        return batchRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取批次
     */
    public Optional<DictationBatch> getBatchById(Long id) {
        return batchRepository.findById(id);
    }

    /**
     * 获取批次响应DTO
     */
    public BatchResponse toBatchResponse(DictationBatch batch) {
        BatchResponse response = new BatchResponse();
        response.setId(batch.getId());
        response.setBatchName(batch.getBatchName());
        response.setCreatedAt(batch.getCreatedAt());
        response.setTotalWords(batch.getTotalWords());
        response.setCompletedWords(batch.getCompletedWords());
        response.setStatus(batch.getStatus().name());

        if (batch.getTotalWords() > 0) {
            response.setProgress((double) batch.getCompletedWords() / batch.getTotalWords() * 100);
        } else {
            response.setProgress(0.0);
        }

        return response;
    }

    /**
     * 开始听写批次
     */
    @Transactional
    public DictationBatch startBatch(Long batchId) {
        Optional<DictationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + batchId);
        }

        DictationBatch batch = batchOpt.get();
        batch.setStatus(DictationBatch.BatchStatus.IN_PROGRESS);
        batch = batchRepository.save(batch);

        log.info("Started batch: {}", batchId);
        return batch;
    }

    /**
     * 完成听写批次
     */
    @Transactional
    public DictationBatch completeBatch(Long batchId) {
        Optional<DictationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + batchId);
        }

        DictationBatch batch = batchOpt.get();
        batch.setStatus(DictationBatch.BatchStatus.COMPLETED);
        batch = batchRepository.save(batch);

        log.info("Completed batch: {}", batchId);
        return batch;
    }

    /**
     * 取消听写批次
     */
    @Transactional
    public DictationBatch cancelBatch(Long batchId) {
        Optional<DictationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + batchId);
        }

        DictationBatch batch = batchOpt.get();
        batch.setStatus(DictationBatch.BatchStatus.CANCELLED);
        batch = batchRepository.save(batch);

        log.info("Cancelled batch: {}", batchId);
        return batch;
    }

    /**
     * 删除批次
     */
    @Transactional
    public void deleteBatch(Long batchId) {
        // 先删除批次中的所有词语
        List<Word> words = wordRepository.findByBatchIdOrderBySortOrder(batchId);
        wordRepository.deleteAll(words);

        // 再删除批次
        batchRepository.deleteById(batchId);

        log.info("Deleted batch: {}", batchId);
    }

    /**
     * 根据状态获取批次列表
     */
    public List<DictationBatch> getBatchesByStatus(DictationBatch.BatchStatus status) {
        return batchRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 获取指定日期范围内的批次
     */
    public List<DictationBatch> getBatchesByDateRange(LocalDateTime start, LocalDateTime end) {
        return batchRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    /**
     * 更新批次完成数量
     */
    @Transactional
    public void updateCompletedWords(Long batchId) {
        Optional<DictationBatch> batchOpt = batchRepository.findById(batchId);
        if (batchOpt.isPresent()) {
            DictationBatch batch = batchOpt.get();
            long completedCount = wordRepository.countByBatchIdAndStatus(batchId, Word.WordStatus.COMPLETED);
            batch.setCompletedWords((int) completedCount);
            batchRepository.save(batch);
        }
    }

    /**
     * 获取所有批次响应DTO列表
     */
    public List<BatchResponse> getAllBatchResponses() {
        return getAllBatches().stream()
                .map(this::toBatchResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据ID获取批次响应DTO
     */
    public BatchResponse getBatchResponseById(Long id) {
        Optional<DictationBatch> batchOpt = getBatchById(id);
        if (batchOpt.isEmpty()) {
            throw new IllegalArgumentException("Batch not found: " + id);
        }
        return toBatchResponse(batchOpt.get());
    }

    /**
     * 创建批次并返回响应DTO
     */
    @Transactional
    public BatchResponse createBatchResponse(BatchCreateRequest request) {
        DictationBatch batch = createBatch(request);
        return toBatchResponse(batch);
    }

    /**
     * 获取今日批次响应DTO列表
     */
    public List<BatchResponse> getTodayBatchResponses() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return getBatchesByDateRange(startOfDay, endOfDay).stream()
                .map(this::toBatchResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 根据日期范围获取批次响应DTO列表
     */
    public List<BatchResponse> getBatchesByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return getBatchesByDateRange(startDateTime, endDateTime).stream()
                .map(this::toBatchResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}