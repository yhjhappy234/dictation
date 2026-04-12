package com.yhj.dictation.service;

import com.yhj.dictation.entity.Word;
import com.yhj.dictation.repository.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @InjectMocks
    private WordService wordService;

    private Word testWord;

    @BeforeEach
    void setUp() {
        testWord = new Word();
        testWord.setId(1L);
        testWord.setWordText("苹果");
        testWord.setBatchId(1L);
        testWord.setSortOrder(0);
        testWord.setStatus(Word.WordStatus.PENDING);
    }

    @Test
    void testGetWordsByBatchId() {
        List<Word> words = Arrays.asList(testWord);
        when(wordRepository.findByBatchIdOrderBySortOrder(1L)).thenReturn(words);

        List<Word> result = wordService.getWordsByBatchId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("苹果", result.get(0).getWordText());
        verify(wordRepository, times(1)).findByBatchIdOrderBySortOrder(1L);
    }

    @Test
    void testGetWordById() {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

        Optional<Word> result = wordService.getWordById(1L);

        assertTrue(result.isPresent());
        assertEquals("苹果", result.get().getWordText());
        verify(wordRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateWordStatus() {
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        Word result = wordService.updateWordStatus(1L, Word.WordStatus.COMPLETED);

        assertNotNull(result);
        verify(wordRepository, times(1)).save(any(Word.class));
    }
}
