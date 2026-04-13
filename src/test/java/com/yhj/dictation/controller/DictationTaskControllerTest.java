package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.TaskDTO;
import com.yhj.dictation.dto.TaskCreateRequest;
import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.TaskRecord;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.service.DictationTaskService;
import com.yhj.dictation.service.DictationBatchService;
import com.yhj.dictation.service.TaskRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DictationTaskController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DictationTaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DictationTaskService taskService;

    @Mock
    private DictationBatchService batchService;

    @Mock
    private TaskRecordService taskRecordService;

    @InjectMocks
    private DictationTaskController taskController;

    private ObjectMapper objectMapper;
    private TaskDTO testTaskDTO;
    private DictationTask testTask;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();

        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1L);
        testTaskDTO.setTaskName("Test Task");
        testTaskDTO.setWords(Arrays.asList("词语1", "词语2"));
        testTaskDTO.setWordCount(2);
        testTaskDTO.setStatus("NOT_STARTED");
        testTaskDTO.setCreatedAt(LocalDateTime.now());

        testTask = new DictationTask();
        testTask.setId(1L);
        testTask.setTaskName("Test Task");
        testTask.setWords("词语1 词语2");
        testTask.setWordCount(2);
        testTask.setStatus(DictationTask.TaskStatus.NOT_STARTED);
        testTask.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createTask API测试")
    class CreateTaskApiTests {

        @Test
        @DisplayName("创建任务 - 成功")
        void createTask_success() throws Exception {
            // Given
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTaskName("Test Task");
            request.setWords("词语1 词语2");

            when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("创建任务 - 名称为空")
        void createTask_emptyName() throws Exception {
            // Given
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTaskName("");
            request.setWords("词语");

            // When & Then
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("任务名称不能为空"));
        }

        @Test
        @DisplayName("创建任务 - 词语为空")
        void createTask_emptyWords() throws Exception {
            // Given
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTaskName("Test");
            request.setWords("");

            // When & Then
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("词语不能为空"));
        }
    }

    @Nested
    @DisplayName("getAllTasks API测试")
    class GetAllTasksApiTests {

        @Test
        @DisplayName("获取所有任务 - 成功")
        void getAllTasks_success() throws Exception {
            // Given
            List<TaskDTO> tasks = Arrays.asList(testTaskDTO);
            when(taskService.getAllTaskDTOs()).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("获取所有任务 - 空列表")
        void getAllTasks_emptyList() throws Exception {
            // Given
            when(taskService.getAllTaskDTOs()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("getTaskById API测试")
    class GetTaskByIdApiTests {

        @Test
        @DisplayName("获取任务详情 - 成功")
        void getTaskById_success() throws Exception {
            // Given
            when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(get("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1));
        }

        @Test
        @DisplayName("获取任务详情 - 任务不存在")
        void getTaskById_notFound() throws Exception {
            // Given
            when(taskService.getTaskById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(get("/api/tasks/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("updateTask API测试")
    class UpdateTaskApiTests {

        @Test
        @DisplayName("更新任务 - 成功")
        void updateTask_success() throws Exception {
            // Given
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTaskName("Updated Task");
            request.setWords("新词语");

            when(taskService.updateTask(eq(1L), any(TaskCreateRequest.class))).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(put("/api/tasks/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("更新任务 - 任务不存在")
        void updateTask_notFound() throws Exception {
            // Given
            TaskCreateRequest request = new TaskCreateRequest();
            request.setTaskName("Updated");

            when(taskService.updateTask(anyLong(), any(TaskCreateRequest.class)))
                    .thenThrow(new IllegalArgumentException("Task not found"));

            // When & Then
            mockMvc.perform(put("/api/tasks/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("deleteTask API测试")
    class DeleteTaskApiTests {

        @Test
        @DisplayName("删除任务 - 成功")
        void deleteTask_success() throws Exception {
            // Given
            doNothing().when(taskService).deleteTask(1L);

            // When & Then
            mockMvc.perform(delete("/api/tasks/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("删除任务 - 异常情况")
        void deleteTask_exception() throws Exception {
            // Given
            doThrow(new RuntimeException("Database error")).when(taskService).deleteTask(anyLong());

            // When & Then
            mockMvc.perform(delete("/api/tasks/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("startTask API测试")
    class StartTaskApiTests {

        @Test
        @DisplayName("开始任务 - 成功")
        void startTask_success() throws Exception {
            // Given
            testTask.setStatus(DictationTask.TaskStatus.IN_PROGRESS);
            testTaskDTO.setStatus("IN_PROGRESS");
            when(taskService.startTask(1L)).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("开始任务 - 任务不存在")
        void startTask_notFound() throws Exception {
            // Given
            when(taskService.startTask(anyLong()))
                    .thenThrow(new IllegalArgumentException("Task not found"));

            // When & Then
            mockMvc.perform(post("/api/tasks/999/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeTask API测试")
    class CompleteTaskApiTests {

        @Test
        @DisplayName("完成任务 - 成功")
        void completeTask_success() throws Exception {
            // Given
            testTask.setStatus(DictationTask.TaskStatus.COMPLETED);
            testTaskDTO.setStatus("COMPLETED");
            when(taskService.completeTask(1L)).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("setDictator API测试")
    class SetDictatorApiTests {

        @Test
        @DisplayName("设置听写人 - 成功")
        void setDictator_success() throws Exception {
            // Given
            testTask.setDictator("小明");
            testTaskDTO.setDictator("小明");
            when(taskService.setDictator(eq(1L), anyString())).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/dictator?dictator=小明"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("resetProgress API测试")
    class ResetProgressApiTests {

        @Test
        @DisplayName("重置进度 - 成功")
        void resetProgress_success() throws Exception {
            // Given
            when(taskService.resetProgress(1L)).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/reset-progress"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getTaskRecords API测试")
    class GetTaskRecordsApiTests {

        @Test
        @DisplayName("获取任务记录 - 成功")
        void getTaskRecords_success() throws Exception {
            // Given
            TaskRecord record = new TaskRecord();
            record.setId(1L);
            record.setTaskId(1L);
            record.setWord("词语1");
            record.setIsCorrect(true);
            record.setReadCount(2);

            when(taskRecordService.getRecordsByTaskId(1L)).thenReturn(Arrays.asList(record));

            // When & Then
            mockMvc.perform(get("/api/tasks/1/records"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("startWord API测试")
    class StartWordApiTests {

        @Test
        @DisplayName("开始听写词语 - 成功")
        void startWord_success() throws Exception {
            // Given
            TaskRecord record = new TaskRecord();
            record.setId(1L);
            record.setTaskId(1L);
            record.setWord("词语1");
            record.setReadCount(1);

            when(taskRecordService.startWord(eq(1L), anyString())).thenReturn(record);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/start-word?word=词语1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("incrementReadCount API测试")
    class IncrementReadCountApiTests {

        @Test
        @DisplayName("增加朗读次数 - 成功")
        void incrementReadCount_success() throws Exception {
            // Given
            TaskRecord record = new TaskRecord();
            record.setId(1L);
            record.setTaskId(1L);
            record.setWord("词语1");
            record.setReadCount(2);

            when(taskRecordService.incrementReadCountByWord(eq(1L), anyString())).thenReturn(record);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/read-word?word=词语1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("增加朗读次数 - 记录不存在")
        void incrementReadCount_notFound() throws Exception {
            // Given
            when(taskRecordService.incrementReadCountByWord(anyLong(), anyString())).thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/read-word?word=词语"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("completeWord API测试")
    class CompleteWordApiTests {

        @Test
        @DisplayName("完成词语听写 - 正确")
        void completeWord_correct() throws Exception {
            // Given
            TaskRecord record = new TaskRecord();
            record.setId(1L);
            record.setTaskId(1L);
            record.setWord("词语1");
            record.setIsCorrect(true);

            when(taskRecordService.completeWord(eq(1L), anyString(), eq(true))).thenReturn(record);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/complete-word?word=词语1&isCorrect=true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("完成词语听写 - 错误")
        void completeWord_wrong() throws Exception {
            // Given
            TaskRecord record = new TaskRecord();
            record.setId(1L);
            record.setTaskId(1L);
            record.setWord("词语1");
            record.setIsCorrect(false);

            when(taskRecordService.completeWord(eq(1L), anyString(), eq(false))).thenReturn(record);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/complete-word?word=词语1&isCorrect=false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getUncompletedTasks API测试")
    class GetUncompletedTasksApiTests {

        @Test
        @DisplayName("获取未完成任务 - 成功")
        void getUncompletedTasks_success() throws Exception {
            // Given
            List<TaskDTO> tasks = Arrays.asList(testTaskDTO);
            when(taskService.getUncompletedTaskDTOs()).thenReturn(tasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/uncompleted"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getTasksByStatus API测试")
    class GetTasksByStatusApiTests {

        @Test
        @DisplayName("获取已完成任务 - 成功")
        void getTasksByStatus_success() throws Exception {
            // Given
            testTask.setStatus(DictationTask.TaskStatus.COMPLETED);
            testTaskDTO.setStatus("COMPLETED");
            when(taskService.getTasksByStatus(DictationTask.TaskStatus.COMPLETED)).thenReturn(Arrays.asList(testTask));
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(get("/api/tasks/status/COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getFavoriteTasks API测试")
    class GetFavoriteTasksApiTests {

        @Test
        @DisplayName("获取收藏任务 - 成功")
        void getFavoriteTasks_success() throws Exception {
            // Given
            testTask.setIsFavorite(true);
            testTaskDTO.setIsFavorite(true);
            when(taskService.getFavoriteTasks()).thenReturn(Arrays.asList(testTask));
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(get("/api/tasks/favorites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("setFavorite API测试")
    class SetFavoriteApiTests {

        @Test
        @DisplayName("设置收藏 - 成功")
        void setFavorite_success() throws Exception {
            // Given
            testTask.setIsFavorite(true);
            testTaskDTO.setIsFavorite(true);
            when(taskService.setFavorite(eq(1L), eq(true))).thenReturn(testTask);
            when(taskService.toTaskDTO(any(DictationTask.class))).thenReturn(testTaskDTO);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/favorite?isFavorite=true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("getDictators API测试")
    class GetDictatorsApiTests {

        @Test
        @DisplayName("获取听写人列表 - 成功")
        void getDictators_success() throws Exception {
            // Given
            when(taskService.getAllDictators()).thenReturn(Arrays.asList("小明", "小红"));

            // When & Then
            mockMvc.perform(get("/api/tasks/dictators"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("startDictationFromTask API测试")
    class StartDictationFromTaskApiTests {

        @Test
        @DisplayName("从任务开始听写 - 成功")
        void startDictationFromTask_success() throws Exception {
            // Given
            DictationBatch batch = new DictationBatch();
            batch.setId(1L);

            when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));
            when(taskService.startTask(1L)).thenReturn(testTask);
            when(batchService.createBatch(any())).thenReturn(batch);
            when(batchService.startBatch(1L)).thenReturn(batch);

            // When & Then
            mockMvc.perform(post("/api/tasks/1/dictation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("从任务开始听写 - 任务不存在")
        void startDictationFromTask_notFound() throws Exception {
            // Given
            when(taskService.getTaskById(anyLong())).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(post("/api/tasks/999/dictation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("从任务开始听写 - 任务已完成")
        void startDictationFromTask_alreadyCompleted() throws Exception {
            // Given
            testTask.setStatus(DictationTask.TaskStatus.COMPLETED);
            when(taskService.getTaskById(1L)).thenReturn(Optional.of(testTask));

            // When & Then
            mockMvc.perform(post("/api/tasks/1/dictation"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}