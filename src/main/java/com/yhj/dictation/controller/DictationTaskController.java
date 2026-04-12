package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.service.DictationTaskService;
import com.yhj.dictation.service.DictationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 听写任务模板控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class DictationTaskController {

    private final DictationTaskService taskService;
    private final DictationBatchService batchService;

    /**
     * 创建任务模板
     */
    @PostMapping
    public ApiResponse<TaskDTO> createTask(@RequestBody TaskCreateRequest request) {
        try {
            if (request.getTaskName() == null || request.getTaskName().trim().isEmpty()) {
                return ApiResponse.error("任务名称不能为空");
            }
            if (request.getWords() == null || request.getWords().trim().isEmpty()) {
                return ApiResponse.error("词语不能为空");
            }

            DictationTask task = taskService.createTask(request);
            return ApiResponse.success("任务模板创建成功", taskService.toTaskDTO(task));
        } catch (Exception e) {
            log.error("Failed to create task", e);
            return ApiResponse.error("创建任务模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有任务模板
     */
    @GetMapping
    public ApiResponse<List<TaskDTO>> getAllTasks() {
        try {
            List<TaskDTO> tasks = taskService.getAllTaskDTOs();
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            log.error("Failed to get tasks", e);
            return ApiResponse.error("获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取任务模板
     */
    @GetMapping("/{id}")
    public ApiResponse<TaskDTO> getTaskById(@PathVariable Long id) {
        try {
            Optional<DictationTask> taskOpt = taskService.getTaskById(id);
            if (taskOpt.isEmpty()) {
                return ApiResponse.error("任务模板不存在: " + id);
            }
            return ApiResponse.success(taskService.toTaskDTO(taskOpt.get()));
        } catch (Exception e) {
            log.error("Failed to get task: {}", id, e);
            return ApiResponse.error("获取任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务模板
     */
    @PutMapping("/{id}")
    public ApiResponse<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskCreateRequest request) {
        try {
            DictationTask task = taskService.updateTask(id, request);
            return ApiResponse.success("任务模板更新成功", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update task: {}", id, e);
            return ApiResponse.error("更新任务模板失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务模板
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ApiResponse.success("任务模板删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete task: {}", id, e);
            return ApiResponse.error("删除任务模板失败: " + e.getMessage());
        }
    }

    /**
     * 设置/取消收藏
     */
    @PostMapping("/{id}/favorite")
    public ApiResponse<TaskDTO> setFavorite(@PathVariable Long id, @RequestParam boolean isFavorite) {
        try {
            DictationTask task = taskService.setFavorite(id, isFavorite);
            return ApiResponse.success(isFavorite ? "已添加收藏" : "已取消收藏", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to set favorite for task: {}", id, e);
            return ApiResponse.error("设置收藏失败: " + e.getMessage());
        }
    }

    /**
     * 从任务模板创建批次并开始听写
     */
    @PostMapping("/{id}/start")
    public ApiResponse<Long> startFromTask(@PathVariable Long id) {
        try {
            Optional<DictationTask> taskOpt = taskService.getTaskById(id);
            if (taskOpt.isEmpty()) {
                return ApiResponse.error("任务模板不存在: " + id);
            }

            DictationTask task = taskOpt.get();

            // 创建批次
            BatchCreateRequest batchRequest = new BatchCreateRequest();
            batchRequest.setBatchName(task.getTaskName());
            batchRequest.setWords(task.getWords());

            DictationBatch batch = batchService.createBatch(batchRequest);

            // 开始批次
            batchService.startBatch(batch.getId());

            log.info("Started dictation from task: {}, batch: {}", id, batch.getId());
            return ApiResponse.success("已从任务模板开始听写", batch.getId());
        } catch (Exception e) {
            log.error("Failed to start from task: {}", id, e);
            return ApiResponse.error("开始听写失败: " + e.getMessage());
        }
    }

    /**
     * 获取收藏的任务模板
     */
    @GetMapping("/favorites")
    public ApiResponse<List<TaskDTO>> getFavoriteTasks() {
        try {
            List<DictationTask> tasks = taskService.getFavoriteTasks();
            List<TaskDTO> taskDTOs = tasks.stream()
                    .map(taskService::toTaskDTO)
                    .collect(java.util.stream.Collectors.toList());
            return ApiResponse.success(taskDTOs);
        } catch (Exception e) {
            log.error("Failed to get favorite tasks", e);
            return ApiResponse.error("获取收藏任务失败: " + e.getMessage());
        }
    }
}