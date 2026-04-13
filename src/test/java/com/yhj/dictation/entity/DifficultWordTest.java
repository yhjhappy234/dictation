package com.yhj.dictation.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DifficultWord 单元测试
 */
class DifficultWordTest {

    @Test
    @DisplayName("创建 DifficultWord 实例")
    void createDifficultWord() {
        DifficultWord word = new DifficultWord();
        assertNotNull(word);
        assertEquals(0, word.getErrorCount()); // 默认值
        assertEquals(0, word.getMasteryLevel()); // 默认值
    }

    @Test
    @DisplayName("设置和获取属性")
    void setAndGetProperties() {
        DifficultWord word = new DifficultWord();
        LocalDateTime now = LocalDateTime.now();

        word.setId(1L);
        word.setWordText("困难词");
        word.setErrorCount(5);
        word.setDictator("teacher");
        word.setAvgDurationSeconds(10);
        word.setLastPracticeDate(now);
        word.setMasteryLevel(3);
        word.setCreatedAt(now.minusDays(1));
        word.setUpdatedAt(now);

        assertEquals(1L, word.getId());
        assertEquals("困难词", word.getWordText());
        assertEquals(5, word.getErrorCount());
        assertEquals("teacher", word.getDictator());
        assertEquals(10, word.getAvgDurationSeconds());
        assertEquals(now, word.getLastPracticeDate());
        assertEquals(3, word.getMasteryLevel());
        assertNotNull(word.getCreatedAt());
        assertNotNull(word.getUpdatedAt());
    }

    @Test
    @DisplayName("onCreate 预构造方法 - 设置默认值")
    void onCreateSetsDefaults() {
        DifficultWord word = new DifficultWord();
        word.onCreate();

        assertNotNull(word.getCreatedAt());
        assertNotNull(word.getUpdatedAt());
    }

    @Test
    @DisplayName("onCreate 预构造方法 - 已有值不变")
    void onCreatePreservesExistingValues() {
        DifficultWord word = new DifficultWord();
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        word.setCreatedAt(existingTime);
        word.setUpdatedAt(existingTime);

        word.onCreate();

        assertEquals(existingTime, word.getCreatedAt());
        assertEquals(existingTime, word.getUpdatedAt());
    }

    @Test
    @DisplayName("onUpdate 预更新方法")
    void onUpdate() {
        DifficultWord word = new DifficultWord();
        LocalDateTime oldTime = LocalDateTime.now().minusDays(1);
        word.setUpdatedAt(oldTime);

        word.onUpdate();

        assertNotNull(word.getUpdatedAt());
        assertTrue(word.getUpdatedAt().isAfter(oldTime));
    }

    @Test
    @DisplayName("掌握级别范围")
    void masteryLevelRange() {
        DifficultWord word = new DifficultWord();

        word.setMasteryLevel(0);
        assertEquals(0, word.getMasteryLevel());

        word.setMasteryLevel(5);
        assertEquals(5, word.getMasteryLevel());
    }

    @Test
    @DisplayName("错误计数设置")
    void errorCountSetting() {
        DifficultWord word = new DifficultWord();

        word.setErrorCount(10);
        assertEquals(10, word.getErrorCount());

        word.setErrorCount(0);
        assertEquals(0, word.getErrorCount());
    }
}