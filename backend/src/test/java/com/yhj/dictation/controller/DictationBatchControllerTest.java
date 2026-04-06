package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.BatchCreateRequest;
import com.yhj.dictation.dto.BatchResponse;
import com.yhj.dictation.entity.DictationBatch;
import com.yhj.dictation.service.DictationBatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DictationBatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DictationBatchService batchService;

    @Test
    void testCreateBatch() throws Exception {
        BatchResponse response = new BatchResponse();
        response.setId(1L);
        response.setBatchName("测试批次");
        response.setTotalWords(3);

        when(batchService.createBatch(any(List.class))).thenReturn(response);

        mockMvc.perform(post("/api/batches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"words\":[\"苹果\",\"香蕉\",\"橘子\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testGetAllBatches() throws Exception {
        DictationBatch batch = new DictationBatch();
        batch.setId(1L);
        batch.setBatchName("测试批次");
        batch.setCreatedAt(LocalDateTime.now());
        batch.setStatus("DRAFT");

        when(batchService.getAllBatches()).thenReturn(Arrays.asList(batch));

        mockMvc.perform(get("/api/batches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetBatchById() throws Exception {
        DictationBatch batch = new DictationBatch();
        batch.setId(1L);
        batch.setBatchName("测试批次");

        when(batchService.getBatchById(1L)).thenReturn(batch);

        mockMvc.perform(get("/api/batches/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testDeleteBatch() throws Exception {
        when(batchService.deleteBatch(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/batches/1"))
                .andExpect(status().isOk());
    }
}