package com.yhj.dictation.service;

import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.DictationTask.TaskStatus;
import com.yhj.dictation.repository.DictationTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * DictationTaskService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationTaskServiceTest {

    @Mock
    private DictationTaskRepository taskRepository;

    @InjectMocks
    private DictationTaskService taskService;

    private DictationTask testTask;
    private TaskCreateRequest testRequest;

    @BeforeEach
    void setUp() {
        testTask = new DictationTask();
        testTask.setId(1L);
        testTask.setTaskName("Test Task");
        testTask.setWords("apple banana orange");
        testTask.setWordCount(3);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setIsFavorite(false);
        testTask.setStatus(TaskStatus.NOT_STARTED);
        testTask.setCurrentIndex(0);
        testTask.setCorrectCount(0);
        testTask.setWrongCount(0);
        testTask.setDictator("Tom");

        testRequest = new TaskCreateRequest();
        testRequest.setTaskName("Test Task");
        testRequest.setWords("apple banana orange");
    }

    @Nested
    @DisplayName("createTask 方法测试")
    class CreateTaskTests {

        @Test
        @DisplayName("创建任务 - 成功")
        void createTask_success() {
            // Given
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> {
                DictationTask task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            DictationTask result = taskService.createTask(testRequest);

            // Then
            assertNotNull(result);
            assertEquals("Test Task", result.getTaskName());
            assertEquals("apple banana orange", result.getWords());
            assertEquals(3, result.getWordCount());
            assertEquals(TaskStatus.NOT_STARTED, result.getStatus());
            assertFalse(result.getIsFavorite());
            verify(taskRepository).save(any(DictationTask.class));
        }

        @Test
        @DisplayName("创建任务 - 空词语列表")
        void createTask_emptyWords() {
            // Given
            testRequest.setWords("");
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> {
                DictationTask task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            DictationTask result = taskService.createTask(testRequest);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getWordCount());
        }

        @Test
        @DisplayName("创建任务 - 词语包含换行符")
        void createTask_wordsWithNewlines() {
            // Given
            testRequest.setWords("apple\nbanana\norange");
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> {
                DictationTask task = invocation.getArgument(0);
                task.setId(1L);
                return task;
            });

            // When
            DictationTask result = taskService.createTask(testRequest);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getWordCount());
        }
    }

    @Nested
    @DisplayName("getAllTasks 方法测试")
    class GetAllTasksTests {

        @Test
        @DisplayName("获取所有任务 - 成功")
        void getAllTasks_success() {
            // Given
            DictationTask task2 = new DictationTask();
            task2.setId(2L);
            task2.setTaskName("Task 2");
            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(testTask, task2));

            // When
            List<DictationTask> result = taskService.getAllTasks();

            // Then
            assertEquals(2, result.size());
            verify(taskRepository).findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("获取所有任务 - 空列表")
        void getAllTasks_emptyList() {
            // Given
            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            // When
            List<DictationTask> result = taskService.getAllTasks();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTaskById 方法测试")
    class GetTaskByIdTests {

        @Test
        @DisplayName("根据ID获取任务 - 成功")
        void getTaskById_success() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            // When
            Optional<DictationTask> result = taskService.getTaskById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals("Test Task", result.get().getTaskName());
        }

        @Test
        @DisplayName("根据ID获取任务 - 任务不存在")
        void getTaskById_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When
            Optional<DictationTask> result = taskService.getTaskById(999L);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("updateTask 方法测试")
    class UpdateTaskTests {

        @Test
        @DisplayName("更新任务 - 成功")
        void updateTask_success() {
            // Given
            TaskCreateRequest updateRequest = new TaskCreateRequest();
            updateRequest.setTaskName("Updated Task");
            updateRequest.setWords("new words");

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.updateTask(1L, updateRequest);

            // Then
            assertEquals("Updated Task", result.getTaskName());
            assertEquals("new words", result.getWords());
            assertEquals(2, result.getWordCount());
        }

        @Test
        @DisplayName("更新任务 - 任务不存在")
        void updateTask_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.updateTask(999L, testRequest);
            });
        }
    }

    @Nested
    @DisplayName("deleteTask 方法测试")
    class DeleteTaskTests {

        @Test
        @DisplayName("删除任务 - 成功")
        void deleteTask_success() {
            // Given
            doNothing().when(taskRepository).deleteById(1L);

            // When
            taskService.deleteTask(1L);

            // Then
            verify(taskRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("setFavorite 方法测试")
    class SetFavoriteTests {

        @Test
        @DisplayName("设置收藏 - 成功")
        void setFavorite_success() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.setFavorite(1L, true);

            // Then
            assertTrue(result.getIsFavorite());
        }

        @Test
        @DisplayName("取消收藏 - 成功")
        void setFavorite_cancel() {
            // Given
            testTask.setIsFavorite(true);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.setFavorite(1L, false);

            // Then
            assertFalse(result.getIsFavorite());
        }

        @Test
        @DisplayName("设置收藏 - 任务不存在")
        void setFavorite_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.setFavorite(999L, true);
            });
        }
    }

    @Nested
    @DisplayName("getFavoriteTasks 方法测试")
    class GetFavoriteTasksTests {

        @Test
        @DisplayName("获取收藏任务 - 成功")
        void getFavoriteTasks_success() {
            // Given
            testTask.setIsFavorite(true);
            when(taskRepository.findByIsFavoriteTrueOrderByCreatedAtDesc()).thenReturn(Arrays.asList(testTask));

            // When
            List<DictationTask> result = taskService.getFavoriteTasks();

            // Then
            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsFavorite());
        }

        @Test
        @DisplayName("获取收藏任务 - 空列表")
        void getFavoriteTasks_emptyList() {
            // Given
            when(taskRepository.findByIsFavoriteTrueOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            // When
            List<DictationTask> result = taskService.getFavoriteTasks();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("startTask 方法测试")
    class StartTaskTests {

        @Test
        @DisplayName("开始任务 - 成功")
        void startTask_success() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.startTask(1L);

            // Then
            assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        }

        @Test
        @DisplayName("开始任务 - 任务不存在")
        void startTask_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.startTask(999L);
            });
        }
    }

    @Nested
    @DisplayName("completeTask 方法测试")
    class CompleteTaskTests {

        @Test
        @DisplayName("完成任务 - 成功")
        void completeTask_success() {
            // Given
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.completeTask(1L);

            // Then
            assertEquals(TaskStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("完成任务 - 任务不存在")
        void completeTask_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.completeTask(999L);
            });
        }
    }

    @Nested
    @DisplayName("resetTask 方法测试")
    class ResetTaskTests {

        @Test
        @DisplayName("重置任务 - 成功")
        void resetTask_success() {
            // Given
            testTask.setStatus(TaskStatus.COMPLETED);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.resetTask(1L);

            // Then
            assertEquals(TaskStatus.NOT_STARTED, result.getStatus());
        }

        @Test
        @DisplayName("重置任务 - 任务不存在")
        void resetTask_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.resetTask(999L);
            });
        }
    }

    @Nested
    @DisplayName("setDictator 方法测试")
    class SetDictatorTests {

        @Test
        @DisplayName("设置听写人 - 成功")
        void setDictator_success() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.setDictator(1L, "Jerry");

            // Then
            assertEquals("Jerry", result.getDictator());
        }

        @Test
        @DisplayName("设置听写人 - 任务不存在")
        void setDictator_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.setDictator(999L, "Jerry");
            });
        }
    }

    @Nested
    @DisplayName("getTasksByStatus 方法测试")
    class GetTasksByStatusTests {

        @Test
        @DisplayName("按状态获取任务 - 成功")
        void getTasksByStatus_success() {
            // Given
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.IN_PROGRESS))
                    .thenReturn(Arrays.asList(testTask));

            // When
            List<DictationTask> result = taskService.getTasksByStatus(TaskStatus.IN_PROGRESS);

            // Then
            assertEquals(1, result.size());
            assertEquals(TaskStatus.IN_PROGRESS, result.get(0).getStatus());
        }

        @Test
        @DisplayName("按状态获取任务 - 空列表")
        void getTasksByStatus_emptyList() {
            // Given
            when(taskRepository.findByStatusOrderByCreatedAtDesc(any(TaskStatus.class)))
                    .thenReturn(Collections.emptyList());

            // When
            List<DictationTask> result = taskService.getTasksByStatus(TaskStatus.COMPLETED);

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("updateProgress 方法测试")
    class UpdateProgressTests {

        @Test
        @DisplayName("更新进度 - 成功")
        void updateProgress_success() {
            // Given
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.updateProgress(1L, 2, 1, 1);

            // Then
            assertEquals(2, result.getCurrentIndex());
            assertEquals(1, result.getCorrectCount());
            assertEquals(1, result.getWrongCount());
            assertEquals(TaskStatus.NOT_STARTED, result.getStatus()); // 进度是0时不改变状态
        }

        @Test
        @DisplayName("更新进度 - 自动改变状态为进行中")
        void updateProgress_autoChangeStatus() {
            // Given
            testTask.setStatus(TaskStatus.NOT_STARTED);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.updateProgress(1L, 1, 1, 0);

            // Then
            assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        }

        @Test
        @DisplayName("更新进度 - 任务不存在")
        void updateProgress_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.updateProgress(999L, 1, 1, 0);
            });
        }
    }

    @Nested
    @DisplayName("recordWordResult 方法测试")
    class RecordWordResultTests {

        @Test
        @DisplayName("记录词语结果 - 正确")
        void recordWordResult_correct() {
            // Given
            testTask.setCurrentIndex(0);
            testTask.setCorrectCount(0);
            testTask.setWrongCount(0);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.recordWordResult(1L, "apple", true);

            // Then
            assertEquals(1, result.getCurrentIndex());
            assertEquals(1, result.getCorrectCount());
            assertEquals(0, result.getWrongCount());
            assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        }

        @Test
        @DisplayName("记录词语结果 - 错误")
        void recordWordResult_wrong() {
            // Given
            testTask.setCurrentIndex(0);
            testTask.setCorrectCount(0);
            testTask.setWrongCount(0);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.recordWordResult(1L, "apple", false);

            // Then
            assertEquals(1, result.getCurrentIndex());
            assertEquals(0, result.getCorrectCount());
            assertEquals(1, result.getWrongCount());
        }

        @Test
        @DisplayName("记录词语结果 - 多次记录累计")
        void recordWordResult_cumulative() {
            // Given
            testTask.setCurrentIndex(2);
            testTask.setCorrectCount(1);
            testTask.setWrongCount(1);
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.recordWordResult(1L, "orange", true);

            // Then
            assertEquals(3, result.getCurrentIndex());
            assertEquals(2, result.getCorrectCount());
            assertEquals(1, result.getWrongCount());
        }

        @Test
        @DisplayName("记录词语结果 - 任务不存在")
        void recordWordResult_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.recordWordResult(999L, "apple", true);
            });
        }
    }

    @Nested
    @DisplayName("resetProgress 方法测试")
    class ResetProgressTests {

        @Test
        @DisplayName("重置进度 - 成功")
        void resetProgress_success() {
            // Given
            testTask.setCurrentIndex(3);
            testTask.setCorrectCount(2);
            testTask.setWrongCount(1);
            testTask.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(DictationTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            DictationTask result = taskService.resetProgress(1L);

            // Then
            assertEquals(0, result.getCurrentIndex());
            assertEquals(0, result.getCorrectCount());
            assertEquals(0, result.getWrongCount());
            assertEquals(TaskStatus.NOT_STARTED, result.getStatus());
        }

        @Test
        @DisplayName("重置进度 - 任务不存在")
        void resetProgress_notFound() {
            // Given
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                taskService.resetProgress(999L);
            });
        }
    }

    @Nested
    @DisplayName("getAllDictators 方法测试")
    class GetAllDictatorsTests {

        @Test
        @DisplayName("获取所有听写人 - 成功")
        void getAllDictators_success() {
            // Given
            DictationTask task2 = new DictationTask();
            task2.setId(2L);
            task2.setDictator("Jerry");
            DictationTask task3 = new DictationTask();
            task3.setId(3L);
            task3.setDictator("Tom"); // 重复

            when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2, task3));

            // When
            List<String> result = taskService.getAllDictators();

            // Then
            assertEquals(2, result.size());
            assertTrue(result.contains("Tom"));
            assertTrue(result.contains("Jerry"));
            // 验证排序
            assertEquals("Jerry", result.get(0));
            assertEquals("Tom", result.get(1));
        }

        @Test
        @DisplayName("获取所有听写人 - 过滤空值")
        void getAllDictators_filterNullAndEmpty() {
            // Given
            DictationTask task2 = new DictationTask();
            task2.setId(2L);
            task2.setDictator(null);
            DictationTask task3 = new DictationTask();
            task3.setId(3L);
            task3.setDictator("  "); // 空白字符串

            when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask, task2, task3));

            // When
            List<String> result = taskService.getAllDictators();

            // Then
            assertEquals(1, result.size());
            assertTrue(result.contains("Tom"));
        }

        @Test
        @DisplayName("获取所有听写人 - 空列表")
        void getAllDictators_emptyList() {
            // Given
            testTask.setDictator(null);
            when(taskRepository.findAll()).thenReturn(Arrays.asList(testTask));

            // When
            List<String> result = taskService.getAllDictators();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getUncompletedTasks 方法测试")
    class GetUncompletedTasksTests {

        @Test
        @DisplayName("获取未完成任务 - 成功")
        void getUncompletedTasks_success() {
            // Given
            testTask.setStatus(TaskStatus.NOT_STARTED);
            DictationTask task2 = new DictationTask();
            task2.setId(2L);
            task2.setStatus(TaskStatus.IN_PROGRESS);
            when(taskRepository.findByStatusInOrderByCreatedAtDesc(anyList()))
                    .thenReturn(Arrays.asList(testTask, task2));

            // When
            List<DictationTask> result = taskService.getUncompletedTasks();

            // Then
            assertEquals(2, result.size());
            verify(taskRepository).findByStatusInOrderByCreatedAtDesc(
                    Arrays.asList(TaskStatus.NOT_STARTED, TaskStatus.IN_PROGRESS));
        }

        @Test
        @DisplayName("获取未完成任务 - 空列表")
        void getUncompletedTasks_emptyList() {
            // Given
            when(taskRepository.findByStatusInOrderByCreatedAtDesc(anyList()))
                    .thenReturn(Collections.emptyList());

            // When
            List<DictationTask> result = taskService.getUncompletedTasks();

            // Then
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("toTaskDTO 方法测试")
    class ToTaskDTOTests {

        @Test
        @DisplayName("转换为DTO - 成功")
        void toTaskDTO_success() {
            // When
            TaskDTO dto = taskService.toTaskDTO(testTask);

            // Then
            assertEquals(1L, dto.getId());
            assertEquals("Test Task", dto.getTaskName());
            assertEquals(3, dto.getWords().size());
            assertEquals(3, dto.getWordCount());
            assertEquals(TaskStatus.NOT_STARTED.name(), dto.getStatus());
            assertFalse(dto.getIsFavorite());
            assertEquals(0, dto.getCurrentIndex());
            assertEquals(0, dto.getCorrectCount());
            assertEquals(0, dto.getWrongCount());
            assertEquals("Tom", dto.getDictator());
        }

        @Test
        @DisplayName("转换为DTO - 空词语列表")
        void toTaskDTO_emptyWords() {
            // Given
            testTask.setWords("");

            // When
            TaskDTO dto = taskService.toTaskDTO(testTask);

            // Then
            assertTrue(dto.getWords().isEmpty());
        }

        @Test
        @DisplayName("转换为DTO - 词语包含换行符")
        void toTaskDTO_wordsWithNewlines() {
            // Given
            testTask.setWords("apple\nbanana\norange");

            // When
            TaskDTO dto = taskService.toTaskDTO(testTask);

            // Then
            assertEquals(3, dto.getWords().size());
            assertTrue(dto.getWords().contains("apple"));
            assertTrue(dto.getWords().contains("banana"));
            assertTrue(dto.getWords().contains("orange"));
        }

        @Test
        @DisplayName("转换为DTO - null词语")
        void toTaskDTO_nullWords() {
            // Given
            testTask.setWords(null);

            // When
            TaskDTO dto = taskService.toTaskDTO(testTask);

            // Then
            assertTrue(dto.getWords().isEmpty());
        }
    }
}