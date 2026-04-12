package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.dto.TaskProgressRequest;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.DictationTask.TaskStatus;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.entity.TaskRecord;
import com.yhj.dictation.service.DictationTaskService;
import com.yhj.dictation.service.DictationBatchService;
import com.yhj.dictation.service.TaskRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 听写任务控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class DictationTaskController {

    private final DictationTaskService taskService;
    private final DictationBatchService batchService;
    private final TaskRecordService taskRecordService;

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
            return ApiResponse.success("听写任务创建成功", taskService.toTaskDTO(task));
        } catch (Exception e) {
            log.error("Failed to create task", e);
            return ApiResponse.error("创建听写任务失败: " + e.getMessage());
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
     * 获取未完成的任务（未开始+进行中）- 用于首页下拉
     */
    @GetMapping("/uncompleted")
    public ApiResponse<List<TaskDTO>> getUncompletedTasks() {
        try {
            List<TaskDTO> tasks = taskService.getUncompletedTaskDTOs();
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            log.error("Failed to get uncompleted tasks", e);
            return ApiResponse.error("获取未完成任务失败: " + e.getMessage());
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
                return ApiResponse.error("听写任务不存在: " + id);
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
            return ApiResponse.success("听写任务更新成功", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update task: {}", id, e);
            return ApiResponse.error("更新听写任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务模板
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ApiResponse.success("听写任务删除成功", null);
        } catch (Exception e) {
            log.error("Failed to delete task: {}", id, e);
            return ApiResponse.error("删除听写任务失败: " + e.getMessage());
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
     * 更新任务状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<TaskDTO> updateStatus(@PathVariable Long id, @RequestParam TaskStatus status) {
        try {
            DictationTask task = taskService.updateStatus(id, status);
            return ApiResponse.success("任务状态已更新", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update task status: {}", id, e);
            return ApiResponse.error("更新任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 开始任务（状态改为进行中）
     */
    @PostMapping("/{id}/start")
    public ApiResponse<TaskDTO> startTaskStatus(@PathVariable Long id) {
        try {
            DictationTask task = taskService.startTask(id);
            return ApiResponse.success("任务已开始", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to start task: {}", id, e);
            return ApiResponse.error("开始任务失败: " + e.getMessage());
        }
    }

    /**
     * 完成任务（状态改为已完成）
     */
    @PostMapping("/{id}/complete")
    public ApiResponse<TaskDTO> completeTaskStatus(@PathVariable Long id) {
        try {
            DictationTask task = taskService.completeTask(id);
            return ApiResponse.success("任务已完成", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to complete task: {}", id, e);
            return ApiResponse.error("完成任务失败: " + e.getMessage());
        }
    }

    /**
     * 设置听写人
     */
    @PostMapping("/{id}/dictator")
    public ApiResponse<TaskDTO> setDictator(@PathVariable Long id, @RequestParam String dictator) {
        try {
            DictationTask task = taskService.setDictator(id, dictator);
            return ApiResponse.success("听写人已设置", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to set dictator for task: {}", id, e);
            return ApiResponse.error("设置听写人失败: " + e.getMessage());
        }
    }

    /**
     * 重置任务（状态改为未开始）
     */
    @PostMapping("/{id}/reset")
    public ApiResponse<TaskDTO> resetTaskStatus(@PathVariable Long id) {
        try {
            DictationTask task = taskService.resetTask(id);
            return ApiResponse.success("任务已重置", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to reset task: {}", id, e);
            return ApiResponse.error("重置任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定状态的任务
     */
    @GetMapping("/status/{status}")
    public ApiResponse<List<TaskDTO>> getTasksByStatus(@PathVariable TaskStatus status) {
        try {
            List<DictationTask> tasks = taskService.getTasksByStatus(status);
            List<TaskDTO> taskDTOs = tasks.stream()
                    .map(taskService::toTaskDTO)
                    .collect(Collectors.toList());
            return ApiResponse.success(taskDTOs);
        } catch (Exception e) {
            log.error("Failed to get tasks by status: {}", status, e);
            return ApiResponse.error("获取任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新任务进度
     */
    @PostMapping("/{id}/progress")
    public ApiResponse<TaskDTO> updateProgress(@PathVariable Long id, @RequestBody TaskProgressRequest request) {
        try {
            DictationTask task = taskService.updateProgress(id, request.getCurrentIndex(), request.getCorrectCount(), request.getWrongCount());
            return ApiResponse.success("进度已保存", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to update progress for task: {}", id, e);
            return ApiResponse.error("保存进度失败: " + e.getMessage());
        }
    }

    /**
     * 记录单个词语的听写结果
     */
    @PostMapping("/{id}/record")
    public ApiResponse<TaskDTO> recordWordResult(@PathVariable Long id, @RequestParam String word, @RequestParam boolean isCorrect) {
        try {
            DictationTask task = taskService.recordWordResult(id, word, isCorrect);
            return ApiResponse.success("听写结果已记录", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to record word result for task: {}", id, e);
            return ApiResponse.error("记录失败: " + e.getMessage());
        }
    }

    /**
     * 重置任务进度
     */
    @PostMapping("/{id}/reset-progress")
    public ApiResponse<TaskDTO> resetProgress(@PathVariable Long id) {
        try {
            DictationTask task = taskService.resetProgress(id);
            return ApiResponse.success("进度已重置", taskService.toTaskDTO(task));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to reset progress for task: {}", id, e);
            return ApiResponse.error("重置进度失败: " + e.getMessage());
        }
    }

    /**
     * 从任务模板创建批次并开始听写
     */
    @PostMapping("/{id}/dictation")
    public ApiResponse<Long> startDictationFromTask(@PathVariable Long id) {
        try {
            Optional<DictationTask> taskOpt = taskService.getTaskById(id);
            if (taskOpt.isEmpty()) {
                return ApiResponse.error("听写任务不存在: " + id);
            }

            DictationTask task = taskOpt.get();

            // 检查任务状态是否为未完成
            if (task.getStatus() == TaskStatus.COMPLETED) {
                return ApiResponse.error("该任务已完成，请先重置任务状态");
            }

            // 将任务状态改为进行中
            taskService.startTask(id);

            // 创建批次
            BatchCreateRequest batchRequest = new BatchCreateRequest();
            batchRequest.setBatchName(task.getTaskName());
            batchRequest.setWords(task.getWords());

            DictationBatch batch = batchService.createBatch(batchRequest);

            // 开始批次
            batchService.startBatch(batch.getId());

            log.info("Started dictation from task: {}, batch: {}", id, batch.getId());
            return ApiResponse.success("已从听写任务开始听写", batch.getId());
        } catch (Exception e) {
            log.error("Failed to start dictation from task: {}", id, e);
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

    /**
     * 获取任务的听写记录
     */
    @GetMapping("/{id}/records")
    public ApiResponse<List<TaskRecord>> getTaskRecords(@PathVariable Long id) {
        try {
            List<TaskRecord> records = taskRecordService.getRecordsByTaskId(id);
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("Failed to get task records: {}", id, e);
            return ApiResponse.error("获取听写记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有听写人列表
     */
    @GetMapping("/dictators")
    public ApiResponse<List<String>> getDictators() {
        try {
            List<String> dictators = taskService.getAllDictators();
            return ApiResponse.success(dictators);
        } catch (Exception e) {
            log.error("Failed to get dictators", e);
            return ApiResponse.error("获取听写人列表失败: " + e.getMessage());
        }
    }

    /**
     * 开始听写词语（记录开始时间）
     */
    @PostMapping("/{id}/start-word")
    public ApiResponse<TaskRecord> startWord(@PathVariable Long id, @RequestParam String word) {
        try {
            TaskRecord record = taskRecordService.startWord(id, word);
            return ApiResponse.success("开始听写词语", record);
        } catch (Exception e) {
            log.error("Failed to start word for task: {}", id, e);
            return ApiResponse.error("开始听写词语失败: " + e.getMessage());
        }
    }

    /**
     * 增加词语朗读次数
     */
    @PostMapping("/{id}/read-word")
    public ApiResponse<TaskRecord> incrementReadCount(@PathVariable Long id, @RequestParam String word) {
        try {
            TaskRecord record = taskRecordService.incrementReadCountByWord(id, word);
            if (record == null) {
                return ApiResponse.error("未找到词语记录");
            }
            return ApiResponse.success("朗读次数已更新", record);
        } catch (Exception e) {
            log.error("Failed to increment read count for task: {}", id, e);
            return ApiResponse.error("更新朗读次数失败: " + e.getMessage());
        }
    }

    /**
     * 完成听写词语（记录结束时间和结果）
     */
    @PostMapping("/{id}/complete-word")
    public ApiResponse<TaskRecord> completeWord(@PathVariable Long id, @RequestParam String word, @RequestParam boolean isCorrect) {
        try {
            TaskRecord record = taskRecordService.completeWord(id, word, isCorrect);
            return ApiResponse.success("词语听写完成", record);
        } catch (Exception e) {
            log.error("Failed to complete word for task: {}", id, e);
            return ApiResponse.error("完成词语听写失败: " + e.getMessage());
        }
    }
}