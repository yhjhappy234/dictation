package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.service.AuditLogService;
import com.yhj.dictation.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuditLogController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuditLogController auditLogController;

    private AuditLogEntity testLog;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController)
                .build();

        testLog = new AuditLogEntity();
        testLog.setId(1L);
        testLog.setUserId(1L);
        testLog.setUsername("admin");
        testLog.setOperation("LOGIN");
        testLog.setMethod("AuthController.login");
        testLog.setParams("{\"username\":\"admin\"}");
        testLog.setResult("{\"success\":true}");
        testLog.setIpAddress("127.0.0.1");
        testLog.setTimestamp(LocalDateTime.now());
        testLog.setDurationMs(100L);
        testLog.setSuccess(true);
        testLog.setErrorMessage(null);
    }

    @Nested
    @DisplayName("getAuditLogs 方法测试")
    class GetAuditLogsTests {

        @Test
        @DisplayName("管理员获取审计日志列表成功")
        void getAuditLogs_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                Page<AuditLogEntity> page = new PageImpl<>(List.of(testLog), PageRequest.of(0, 20), 1);
                when(auditLogService.getAuditLogs(anyInt(), anyInt())).thenReturn(page);

                mockMvc.perform(get("/api/v1/audit-logs?page=0&size=20"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("非管理员无权限获取审计日志")
        void getAuditLogs_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }

    @Nested
    @DisplayName("searchAuditLogs 方法测试")
    class SearchAuditLogsTests {

        @Test
        @DisplayName("管理员搜索审计日志成功")
        void searchAuditLogs_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                Page<AuditLogEntity> page = new PageImpl<>(List.of(testLog), PageRequest.of(0, 20), 1);
                when(auditLogService.searchAuditLogs(anyString(), anyString(), any(), any(), anyInt(), anyInt()))
                        .thenReturn(page);

                mockMvc.perform(get("/api/v1/audit-logs/search?username=admin&operation=LOGIN&page=0&size=20"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("非管理员无权限搜索审计日志")
        void searchAuditLogs_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs/search"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }

    @Nested
    @DisplayName("getAuditLogById 方法测试")
    class GetAuditLogByIdTests {

        @Test
        @DisplayName("管理员获取审计日志详情成功")
        void getAuditLogById_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(auditLogService.getAuditLogById(1L)).thenReturn(testLog);

                mockMvc.perform(get("/api/v1/audit-logs/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(1))
                        .andExpect(jsonPath("$.data.username").value("admin"));
            }
        }

        @Test
        @DisplayName("管理员获取不存在的审计日志")
        void getAuditLogById_admin_notFound() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(auditLogService.getAuditLogById(999L)).thenReturn(null);

                mockMvc.perform(get("/api/v1/audit-logs/999"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("审计日志不存在"));
            }
        }

        @Test
        @DisplayName("非管理员无权限获取审计日志详情")
        void getAuditLogById_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }

    @Nested
    @DisplayName("getAllUsernames 方法测试")
    class GetAllUsernamesTests {

        @Test
        @DisplayName("管理员获取用户名列表成功")
        void getAllUsernames_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(auditLogService.getAllUsernames()).thenReturn(List.of("admin", "testuser"));

                mockMvc.perform(get("/api/v1/audit-logs/usernames"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").isArray())
                        .andExpect(jsonPath("$.data[0]").value("admin"));
            }
        }

        @Test
        @DisplayName("非管理员无权限获取用户名列表")
        void getAllUsernames_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs/usernames"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }

    @Nested
    @DisplayName("getAllOperations 方法测试")
    class GetAllOperationsTests {

        @Test
        @DisplayName("管理员获取操作类型列表成功")
        void getAllOperations_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(auditLogService.getAllOperations()).thenReturn(List.of("LOGIN", "CREATE_USER"));

                mockMvc.perform(get("/api/v1/audit-logs/operations"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").isArray())
                        .andExpect(jsonPath("$.data[0]").value("LOGIN"));
            }
        }

        @Test
        @DisplayName("非管理员无权限获取操作类型列表")
        void getAllOperations_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs/operations"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }

    @Nested
    @DisplayName("countAuditLogs 方法测试")
    class CountAuditLogsTests {

        @Test
        @DisplayName("管理员统计审计日志成功")
        void countAuditLogs_admin_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(auditLogService.countAuditLogs(any(), any())).thenReturn(100L);

                mockMvc.perform(get("/api/v1/audit-logs/count"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(100));
            }
        }

        @Test
        @DisplayName("非管理员无权限统计审计日志")
        void countAuditLogs_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/v1/audit-logs/count"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("需要管理员权限"));
            }
        }
    }
}