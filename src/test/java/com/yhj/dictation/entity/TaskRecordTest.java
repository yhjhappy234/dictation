package com.yhj.dictation.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskRecord 单元测试
 */
class TaskRecordTest {

    @Test
    @DisplayName("创建 TaskRecord 实例")
    void createTaskRecord() {
        TaskRecord record = new TaskRecord();
        assertNotNull(record);
    }

    @Test
    @DisplayName("设置和获取属性")
    void setAndGetProperties() {
        TaskRecord record = new TaskRecord();
        LocalDateTime now = LocalDateTime.now();

        record.setId(1L);
        record.setTaskId(2L);
        record.setWord("测试词");
        record.setIsCorrect(true);
        record.setErrorCount(2);
        record.setReadCount(3);
        record.setDictator("teacher");
        record.setStartTime(now.minusMinutes(5));
        record.setEndTime(now);
        record.setCreatedAt(now);

        assertEquals(1L, record.getId());
        assertEquals(2L, record.getTaskId());
        assertEquals("测试词", record.getWord());
        assertTrue(record.getIsCorrect());
        assertEquals(2, record.getErrorCount());
        assertEquals(3, record.getReadCount());
        assertEquals("teacher", record.getDictator());
        assertNotNull(record.getStartTime());
        assertNotNull(record.getEndTime());
        assertEquals(now, record.getCreatedAt());
    }

    @Test
    @DisplayName("onCreate 预构造方法 - 设置默认值")
    void onCreateSetsDefaults() {
        TaskRecord record = new TaskRecord();
        record.onCreate();

        assertNotNull(record.getCreatedAt());
        assertEquals(0, record.getErrorCount());
        assertEquals(0, record.getReadCount());
    }

    @Test
    @DisplayName("onCreate 预构造方法 - 已有值不变")
    void onCreatePreservesExistingValues() {
        TaskRecord record = new TaskRecord();
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        record.setCreatedAt(existingTime);
        record.setErrorCount(5);
        record.setReadCount(10);

        record.onCreate();

        assertEquals(existingTime, record.getCreatedAt());
        assertEquals(5, record.getErrorCount());
        assertEquals(10, record.getReadCount());
    }

    @Test
    @DisplayName("isCorrect 为 false")
    void setIsCorrectFalse() {
        TaskRecord record = new TaskRecord();
        record.setIsCorrect(false);

        assertFalse(record.getIsCorrect());
    }
}