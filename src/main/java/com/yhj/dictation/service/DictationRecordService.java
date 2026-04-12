package com.yhj.dictation.service;

import com.yhj.dictation.entity.DictationRecord;
import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.DictationRecordRepository;
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
 * 听写记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictationRecordService {

    private final DictationRecordRepository recordRepository;
    private final WordRepository wordRepository;

    /**
     * 开始听写记录
     */
    @Transactional
    public DictationRecord startRecord(Long wordId, Long batchId) {
        DictationRecord record = new DictationRecord();
        record.setWordId(wordId);
        record.setBatchId(batchId);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(DictationRecord.RecordStatus.STARTED);
        record.setRepeatCount(0);

        record = recordRepository.save(record);
        log.info("Started record: {} for word: {}", record.getId(), wordId);
        return record;
    }

    /**
     * 完成听写记录
     */
    @Transactional
    public DictationRecord completeRecord(Long recordId) {
        Optional<DictationRecord> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Record not found: " + recordId);
        }

        DictationRecord record = recordOpt.get();
        record.setEndTime(LocalDateTime.now());
        record.setStatus(DictationRecord.RecordStatus.COMPLETED);

        if (record.getStartTime() != null && record.getEndTime() != null) {
            int duration = (int) java.time.Duration.between(record.getStartTime(), record.getEndTime()).getSeconds();
            record.setDurationSeconds(duration);
        }

        record = recordRepository.save(record);

        // 更新生词本
        updateDifficultWord(record);

        log.info("Completed record: {}", recordId);
        return record;
    }

    /**
     * 通过词语ID完成听写记录（用于前端直接调用）
     */
    @Transactional
    public DictationRecord completeByWordId(Long wordId, Integer duration) {
        // 查找该词语当前正在进行的记录
        Optional<DictationRecord> recordOpt = recordRepository.findByWordIdAndStatus(wordId, DictationRecord.RecordStatus.STARTED);

        DictationRecord record;
        if (recordOpt.isEmpty()) {
            // 如果没有开始的记录，创建一个新的记录
            Word word = wordRepository.findById(wordId)
                    .orElseThrow(() -> new IllegalArgumentException("Word not found: " + wordId));
            record = new DictationRecord();
            record.setWordId(wordId);
            record.setBatchId(word.getBatchId());
            record.setStartTime(LocalDateTime.now());
            record.setStatus(DictationRecord.RecordStatus.STARTED);
            record.setRepeatCount(0);
            record = recordRepository.save(record);
        } else {
            record = recordOpt.get();
        }

        // 完成记录
        record.setEndTime(LocalDateTime.now());
        record.setStatus(DictationRecord.RecordStatus.COMPLETED);

        if (duration != null) {
            record.setDurationSeconds(duration);
        } else if (record.getStartTime() != null && record.getEndTime() != null) {
            int calculatedDuration = (int) java.time.Duration.between(record.getStartTime(), record.getEndTime()).getSeconds();
            record.setDurationSeconds(calculatedDuration);
        }

        record = recordRepository.save(record);

        // 更新生词本
        updateDifficultWord(record);

        log.info("Completed record for word: {} with duration: {}s", wordId, record.getDurationSeconds());
        return record;
    }

    /**
     * 跳过听写记录
     */
    @Transactional
    public DictationRecord skipRecord(Long recordId) {
        Optional<DictationRecord> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Record not found: " + recordId);
        }

        DictationRecord record = recordOpt.get();
        record.setEndTime(LocalDateTime.now());
        record.setStatus(DictationRecord.RecordStatus.SKIPPED);

        record = recordRepository.save(record);
        log.info("Skipped record: {}", recordId);
        return record;
    }

    /**
     * 增加重复次数
     */
    @Transactional
    public DictationRecord incrementRepeatCount(Long recordId) {
        Optional<DictationRecord> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Record not found: " + recordId);
        }

        DictationRecord record = recordOpt.get();
        record.setRepeatCount(record.getRepeatCount() + 1);
        record = recordRepository.save(record);

        log.info("Incremented repeat count for record: {} to {}", recordId, record.getRepeatCount());
        return record;
    }

    /**
     * 根据ID获取记录
     */
    public Optional<DictationRecord> getRecordById(Long id) {
        return recordRepository.findById(id);
    }

    /**
     * 获取批次的所有记录
     */
    public List<DictationRecord> getRecordsByBatchId(Long batchId) {
        return recordRepository.findByBatchId(batchId);
    }

    /**
     * 获取词语的所有记录
     */
    public List<DictationRecord> getRecordsByWordId(Long wordId) {
        return recordRepository.findByWordId(wordId);
    }

    /**
     * 获取今日记录
     */
    public List<DictationRecord> getTodayRecords() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return recordRepository.findByStartTimeBetweenOrderByStartTimeDesc(startOfDay, endOfDay);
    }

    /**
     * 获取指定日期范围的记录
     */
    public List<DictationRecord> getRecordsByDateRange(LocalDateTime start, LocalDateTime end) {
        return recordRepository.findByDateRange(start, end);
    }

    /**
     * 获取词语的平均听写时长
     */
    public Double getAvgDurationByWordId(Long wordId) {
        return recordRepository.findAvgDurationByWordId(wordId);
    }

    /**
     * 获取词语的重复次数统计
     */
    public Integer getRepeatCountByWordId(Long wordId) {
        Integer count = recordRepository.countRepeatByWordId(wordId);
        return count != null ? count : 0;
    }

    /**
     * 更新生词本信息
     */
    private void updateDifficultWord(DictationRecord record) {
        // 这里会调用DifficultWordService来更新生词本
        // 具体实现在DifficultWordService中
        log.debug("Updating difficult word for word: {}", record.getWordId());
    }

    /**
     * 删除记录
     */
    @Transactional
    public void deleteRecord(Long recordId) {
        recordRepository.deleteById(recordId);
        log.info("Deleted record: {}", recordId);
    }

    /**
     * 删除批次的所有记录
     */
    @Transactional
    public void deleteRecordsByBatchId(Long batchId) {
        List<DictationRecord> records = recordRepository.findByBatchId(batchId);
        recordRepository.deleteAll(records);
        log.info("Deleted {} records for batch: {}", records.size(), batchId);
    }
}