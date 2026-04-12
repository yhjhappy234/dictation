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
     * 记录单个词语的听写结果
     */
    @Transactional
    public TaskRecord recordWord(Long taskId, String word, Boolean isCorrect, Integer errorCount) {
        TaskRecord record = new TaskRecord();
        record.setTaskId(taskId);
        record.setWord(word);
        record.setIsCorrect(isCorrect);
        record.setErrorCount(errorCount != null ? errorCount : 0);
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