package com.yhj.dictation.service;

import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.DictationTask.TaskStatus;
import com.yhj.dictation.repository.DictationTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 听写任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictationTaskService {

    private final DictationTaskRepository taskRepository;

    /**
     * 创建听写任务
     */
    @Transactional
    public DictationTask createTask(TaskCreateRequest request) {
        DictationTask task = new DictationTask();
        task.setTaskName(request.getTaskName());
        task.setWords(request.getWords());
        task.setCreatedAt(LocalDateTime.now());
        task.setIsFavorite(false);
        task.setStatus(TaskStatus.NOT_STARTED);  // 默认状态为未开始

        // 计算词语数量
        int wordCount = parseWords(request.getWords()).size();
        task.setWordCount(wordCount);

        task = taskRepository.save(task);
        log.info("Created task: {} with {} words, status: {}", task.getId(), wordCount, task.getStatus());
        return task;
    }

    /**
     * 获取所有任务
     */
    public List<DictationTask> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 获取未完成的任务（未开始+进行中）
     */
    public List<DictationTask> getUncompletedTasks() {
        return taskRepository.findByStatusInOrderByCreatedAtDesc(
                Arrays.asList(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS));
    }

    /**
     * 根据ID获取任务
     */
    public Optional<DictationTask> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * 更新任务
     */
    @Transactional
    public DictationTask updateTask(Long id, TaskCreateRequest request) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setTaskName(request.getTaskName());
        task.setWords(request.getWords());

        int wordCount = parseWords(request.getWords()).size();
        task.setWordCount(wordCount);

        task = taskRepository.save(task);
        log.info("Updated task: {}", id);
        return task;
    }

    /**
     * 删除任务
     */
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
        log.info("Deleted task: {}", id);
    }

    /**
     * 更新任务状态
     */
    @Transactional
    public DictationTask updateStatus(Long id, TaskStatus status) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setStatus(status);
        task = taskRepository.save(task);
        log.info("Updated task {} status to: {}", id, status);
        return task;
    }

    /**
     * 开始任务（状态改为进行中）
     */
    @Transactional
    public DictationTask startTask(Long id) {
        return updateStatus(id, TaskStatus.IN_PROGRESS);
    }

    /**
     * 完成任务（状态改为已完成）
     */
    @Transactional
    public DictationTask completeTask(Long id) {
        return updateStatus(id, TaskStatus.COMPLETED);
    }

    /**
     * 重置任务（状态改为未开始）
     */
    @Transactional
    public DictationTask resetTask(Long id) {
        return updateStatus(id, TaskStatus.NOT_STARTED);
    }

    /**
     * 设置收藏
     */
    @Transactional
    public DictationTask setFavorite(Long id, boolean isFavorite) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setIsFavorite(isFavorite);
        task = taskRepository.save(task);
        log.info("Set task {} favorite to: {}", id, isFavorite);
        return task;
    }

    /**
     * 获取收藏的任务
     */
    public List<DictationTask> getFavoriteTasks() {
        return taskRepository.findByIsFavoriteTrueOrderByCreatedAtDesc();
    }

    /**
     * 获取指定状态的任务
     */
    public List<DictationTask> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 转换为DTO
     */
    public TaskDTO toTaskDTO(DictationTask task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setWords(parseWords(task.getWords()));
        dto.setWordCount(task.getWordCount());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setIsFavorite(task.getIsFavorite());
        dto.setStatus(task.getStatus().name());
        dto.setCurrentIndex(task.getCurrentIndex());
        dto.setCorrectCount(task.getCorrectCount());
        dto.setWrongCount(task.getWrongCount());
        dto.setDictator(task.getDictator());
        return dto;
    }

    /**
     * 更新任务进度
     */
    @Transactional
    public DictationTask updateProgress(Long id, Integer currentIndex, Integer correctCount, Integer wrongCount) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setCurrentIndex(currentIndex);
        task.setCorrectCount(correctCount);
        task.setWrongCount(wrongCount);

        // 如果有进度，状态改为进行中
        if (currentIndex > 0 && task.getStatus() == TaskStatus.NOT_STARTED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        task = taskRepository.save(task);
        log.info("Updated task {} progress: index={}, correct={}, wrong={}", id, currentIndex, correctCount, wrongCount);
        return task;
    }

    /**
     * 记录单个词语的听写结果
     */
    @Transactional
    public DictationTask recordWordResult(Long id, String word, boolean isCorrect) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();

        // 更新进度
        Integer currentIndex = task.getCurrentIndex() != null ? task.getCurrentIndex() : 0;
        Integer correctCount = task.getCorrectCount() != null ? task.getCorrectCount() : 0;
        Integer wrongCount = task.getWrongCount() != null ? task.getWrongCount() : 0;

        currentIndex++;
        if (isCorrect) {
            correctCount++;
        } else {
            wrongCount++;
        }

        task.setCurrentIndex(currentIndex);
        task.setCorrectCount(correctCount);
        task.setWrongCount(wrongCount);

        // 状态改为进行中
        if (task.getStatus() == TaskStatus.NOT_STARTED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }

        task = taskRepository.save(task);
        log.info("Recorded word result for task {}: word={}, correct={}", id, word, isCorrect);
        return task;
    }

    /**
     * 重置任务进度（用于重新开始）
     */
    @Transactional
    public DictationTask resetProgress(Long id) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setCurrentIndex(0);
        task.setCorrectCount(0);
        task.setWrongCount(0);
        task.setStatus(TaskStatus.NOT_STARTED);

        task = taskRepository.save(task);
        log.info("Reset progress for task: {}", id);
        return task;
    }

    /**
     * 设置听写人
     */
    @Transactional
    public DictationTask setDictator(Long id, String dictator) {
        Optional<DictationTask> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            throw new IllegalArgumentException("Task not found: " + id);
        }

        DictationTask task = taskOpt.get();
        task.setDictator(dictator);
        task = taskRepository.save(task);
        log.info("Set dictator for task {} to: {}", id, dictator);
        return task;
    }

    /**
     * 获取所有任务DTO列表
     */
    public List<TaskDTO> getAllTaskDTOs() {
        return getAllTasks().stream()
                .map(this::toTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取未完成任务DTO列表
     */
    public List<TaskDTO> getUncompletedTaskDTOs() {
        return getUncompletedTasks().stream()
                .map(this::toTaskDTO)
                .collect(Collectors.toList());
    }

    /**
     * 解析词语字符串
     */
    private List<String> parseWords(String words) {
        if (words == null || words.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(words.split("[\\s\\n]+"))
                .filter(w -> !w.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有听写人列表（去重）
     */
    public List<String> getAllDictators() {
        List<DictationTask> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(DictationTask::getDictator)
                .filter(d -> d != null && !d.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}