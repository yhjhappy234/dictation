package com.yhj.dictation.service;

import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.entity.DictationTask;
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
     * 创建任务模板
     */
    @Transactional
    public DictationTask createTask(TaskCreateRequest request) {
        DictationTask task = new DictationTask();
        task.setTaskName(request.getTaskName());
        task.setWords(request.getWords());
        task.setCreatedAt(LocalDateTime.now());
        task.setIsFavorite(false);

        // 计算词语数量
        int wordCount = parseWords(request.getWords()).size();
        task.setWordCount(wordCount);

        task = taskRepository.save(task);
        log.info("Created task: {} with {} words", task.getId(), wordCount);
        return task;
    }

    /**
     * 获取所有任务模板
     */
    public List<DictationTask> getAllTasks() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 根据ID获取任务模板
     */
    public Optional<DictationTask> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * 更新任务模板
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
     * 删除任务模板
     */
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
        log.info("Deleted task: {}", id);
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
     * 获取收藏的任务模板
     */
    public List<DictationTask> getFavoriteTasks() {
        return taskRepository.findByIsFavoriteTrueOrderByCreatedAtDesc();
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
        return dto;
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
}