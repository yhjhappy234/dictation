package com.yhj.dictation.service;

import com.yhj.dictation.dto.TaskResultRequest;
import com.yhj.dictation.entity.TaskRecord;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.repository.TaskRecordRepository;
import com.yhj.dictation.repository.DictationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 任务听写记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRecordService {

    private final TaskRecordRepository recordRepository;
    private final DictationTaskRepository taskRepository;

    /**
     * 保存任务听写结果
     */
    @Transactional
    public List<TaskRecord> saveTaskResults(TaskResultRequest request) {
        DictationTask task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + request.getTaskId()));

        // 删除旧的记录（如果有）
        recordRepository.deleteByTaskId(request.getTaskId());

        List<TaskRecord> records = new ArrayList<>();

        for (TaskResultRequest.WordResult result : request.getWordResults()) {
            TaskRecord record = new TaskRecord();
            record.setTaskId(request.getTaskId());
            record.setWord(result.getWord());
            record.setIsCorrect(result.getIsCorrect());
            record.setErrorCount(result.getErrorCount() != null ? result.getErrorCount() : 0);
            record.setCreatedAt(LocalDateTime.now());

            records.add(recordRepository.save(record));
        }

        // 更新任务状态为已完成
        task.setStatus(DictationTask.TaskStatus.COMPLETED);
        taskRepository.save(task);

        log.info("Saved {} records for task: {}", records.size(), request.getTaskId());
        return records;
    }

    /**
     * 开始听写词语（创建记录并设置开始时间）
     * 如果词语已有记录则覆盖开始时间和朗读次数
     */
    @Transactional
    public TaskRecord startWord(Long taskId, String word) {
        // 查找该词语是否已有记录
        List<TaskRecord> existingRecords = recordRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        Optional<TaskRecord> existingRecord = existingRecords.stream()
                .filter(r -> r.getWord().equals(word))
                .findFirst();

        if (existingRecord.isPresent()) {
            // 已有记录，覆盖开始时间和朗读次数
            TaskRecord record = existingRecord.get();
            record.setStartTime(LocalDateTime.now());
            record.setEndTime(null);  // 清除结束时间
            record.setReadCount(1);   // 重置朗读次数
            record.setCreatedAt(LocalDateTime.now());

            record = recordRepository.save(record);
            log.info("Restarted word: {} for task: {}", word, taskId);
            return record;
        }

        // 新记录
        TaskRecord record = new TaskRecord();
        record.setTaskId(taskId);
        record.setWord(word);
        record.setIsCorrect(true);  // 默认正确，完成时可修改
        record.setErrorCount(0);
        record.setReadCount(1);     // 第一次朗读
        record.setStartTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        record = recordRepository.save(record);
        log.info("Started word: {} for task: {}", word, taskId);
        return record;
    }

    /**
     * 增加朗读次数（根据词语查找最新记录）
     */
    @Transactional
    public TaskRecord incrementReadCountByWord(Long taskId, String word) {
        List<TaskRecord> records = recordRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        Optional<TaskRecord> recordOpt = records.stream()
                .filter(r -> r.getWord().equals(word))
                .findFirst();

        if (recordOpt.isEmpty()) {
            log.warn("No record found for task: {}, word: {}", taskId, word);
            return null;
        }

        TaskRecord record = recordOpt.get();
        record.setReadCount(record.getReadCount() != null ? record.getReadCount() + 1 : 1);
        record = recordRepository.save(record);
        log.info("Incremented read count for task: {}, word: {}, count: {}", taskId, word, record.getReadCount());
        return record;
    }

    /**
     * 增加朗读次数
     */
    @Transactional
    public TaskRecord incrementReadCount(Long recordId) {
        Optional<TaskRecord> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            throw new IllegalArgumentException("Record not found: " + recordId);
        }

        TaskRecord record = recordOpt.get();
        record.setReadCount(record.getReadCount() != null ? record.getReadCount() + 1 : 1);
        record = recordRepository.save(record);
        log.info("Incremented read count for record: {}, word: {}, count: {}", recordId, record.getWord(), record.getReadCount());
        return record;
    }

    /**
     * 完成听写词语（设置结束时间和结果）
     * 同时更新任务的正确/错误统计
     */
    @Transactional
    public TaskRecord completeWord(Long taskId, String word, Boolean isCorrect) {
        List<TaskRecord> records = recordRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        Optional<TaskRecord> recordOpt = records.stream()
                .filter(r -> r.getWord().equals(word))
                .findFirst();

        if (recordOpt.isEmpty()) {
            // 如果没有记录，创建一个新的（兼容旧逻辑）
            return recordWord(taskId, word, isCorrect, 0);
        }

        TaskRecord record = recordOpt.get();
        record.setEndTime(LocalDateTime.now());
        record.setIsCorrect(isCorrect);

        record = recordRepository.save(record);

        // 更新任务的正确/错误统计
        updateTaskStatistics(taskId);

        log.info("Completed word: {} for task: {}, correct: {}", word, taskId, isCorrect);
        return record;
    }

    /**
     * 更新任务的正确/错误统计（从TaskRecord重新计算）
     */
    @Transactional
    public void updateTaskStatistics(Long taskId) {
        Optional<DictationTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return;
        }

        DictationTask task = taskOpt.get();
        Long correctCount = recordRepository.countByTaskIdAndIsCorrectTrue(taskId);
        Long wrongCount = recordRepository.countByTaskIdAndIsCorrectFalse(taskId);

        task.setCorrectCount(correctCount != null ? correctCount.intValue() : 0);
        task.setWrongCount(wrongCount != null ? wrongCount.intValue() : 0);

        taskRepository.save(task);
        log.info("Updated task {} statistics: correct={}, wrong={}", taskId, correctCount, wrongCount);
    }

    /**
     * 记录单个词语的听写结果（兼容旧方法）
     */
    @Transactional
    public TaskRecord recordWord(Long taskId, String word, Boolean isCorrect, Integer errorCount) {
        TaskRecord record = new TaskRecord();
        record.setTaskId(taskId);
        record.setWord(word);
        record.setIsCorrect(isCorrect);
        record.setErrorCount(errorCount != null ? errorCount : 0);
        record.setReadCount(1);
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(LocalDateTime.now());
        record.setCreatedAt(LocalDateTime.now());

        record = recordRepository.save(record);
        log.info("Recorded word: {} for task: {}, correct: {}", word, taskId, isCorrect);
        return record;
    }

    /**
     * 获取任务的听写记录
     */
    public List<TaskRecord> getRecordsByTaskId(Long taskId) {
        return recordRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    /**
     * 获取任务的错误记录
     */
    public List<TaskRecord> getErrorRecords(Long taskId) {
        return recordRepository.findByTaskIdAndIsCorrectFalse(taskId);
    }

    /**
     * 统计任务的正确数
     */
    public Long getCorrectCount(Long taskId) {
        return recordRepository.countByTaskIdAndIsCorrectTrue(taskId);
    }

    /**
     * 统计任务的错误数
     */
    public Long getErrorCount(Long taskId) {
        return recordRepository.countByTaskIdAndIsCorrectFalse(taskId);
    }

    /**
     * 删除任务的记录
     */
    @Transactional
    public void deleteRecordsByTaskId(Long taskId) {
        recordRepository.deleteByTaskId(taskId);
        log.info("Deleted records for task: {}", taskId);
    }
}