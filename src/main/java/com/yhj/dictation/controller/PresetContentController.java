package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.dto.PresetContentDTO;
import com.yhj.dictation.service.DictationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 预设内容控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/preset")
@RequiredArgsConstructor
public class PresetContentController {

    private final DictationBatchService batchService;
    private final ObjectMapper objectMapper;

    /**
     * 获取所有预设内容列表
     */
    @GetMapping("/list")
    public ApiResponse<List<PresetContentDTO>> getPresetList() {
        List<PresetContentDTO> presets = new ArrayList<>();
        
        presets.add(new PresetContentDTO("common-words-50", "常用词语分类", "常用词", 300));
        presets.add(new PresetContentDTO("common-idioms-50", "常用成语分类", "成语", 150));
        presets.add(new PresetContentDTO("common-poems-20", "常用古诗分类", "古诗", 80));
        presets.add(new PresetContentDTO("classics-5", "常用古文分类", "古文", 60));
        
        return ApiResponse.success(presets);
    }

    /**
     * 获取预设内容详情
     * 直接返回 JSON 字符串，避免 JsonNode 序列化问题
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getPresetContent(@PathVariable String id) {
        try {
            ClassPathResource resource = new ClassPathResource("preset-content/" + id + ".json");
            if (!resource.exists()) {
                return "{\"success\":false,\"message\":\"预设内容不存在: " + id + "\",\"data\":null}";
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 直接包装成 ApiResponse 格式返回
            return "{\"success\":true,\"message\":\"操作成功\",\"data\":" + content + "}";
        } catch (IOException e) {
            log.error("Failed to read preset content: {}", id, e);
            return "{\"success\":false,\"message\":\"读取预设内容失败: " + e.getMessage() + "\",\"data\":null}";
        }
    }

    /**
     * 导入预设内容创建听写批次
     */
    @PostMapping("/import/{id}")
    public ApiResponse<Long> importPresetContent(@PathVariable String id) {
        try {
            ClassPathResource resource = new ClassPathResource("preset-content/" + id + ".json");
            if (!resource.exists()) {
                return ApiResponse.error("预设内容不存在: " + id);
            }
            
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(content);
            
            // 提取词语列表
            List<String> words = new ArrayList<>();

            if (jsonNode.has("words")) {
                // 简单词语列表格式
                JsonNode wordsNode = jsonNode.get("words");
                for (JsonNode word : wordsNode) {
                    words.add(word.asText());
                }
            } else if (jsonNode.has("units")) {
                // 年级/分类格式（有单元）
                JsonNode unitsNode = jsonNode.get("units");
                for (JsonNode unit : unitsNode) {
                    if (unit.has("lessons")) {
                        for (JsonNode lesson : unit.get("lessons")) {
                            if (lesson.has("words")) {
                                for (JsonNode word : lesson.get("words")) {
                                    words.add(word.asText());
                                }
                            }
                        }
                    }
                }
            } else if (jsonNode.has("items")) {
                // 古诗/古文格式 - 按句子拆分
                JsonNode itemsNode = jsonNode.get("items");
                for (JsonNode item : itemsNode) {
                    String itemContent = item.get("content").asText();
                    // 按标点符号拆分句子
                    String[] parts = itemContent.split("[，。！？、；：]");
                    for (String part : parts) {
                        if (!part.trim().isEmpty()) {
                            words.add(part.trim());
                        }
                    }
                }
            }
            
            if (words.isEmpty()) {
                return ApiResponse.error("预设内容中没有可听写的词语");
            }
            
            // 创建批次
            String name = jsonNode.has("name") ? jsonNode.get("name").asText() : "预设内容";
            com.yhj.dictation.dto.BatchCreateRequest request = new com.yhj.dictation.dto.BatchCreateRequest();
            request.setBatchName(name);
            request.setWords(String.join(" ", words));
            
            com.yhj.dictation.entity.DictationBatch batch = batchService.createBatch(request);
            
            log.info("Imported preset content: {} with {} words, batchId: {}", id, words.size(), batch.getId());
            
            return ApiResponse.success("导入成功", batch.getId());
        } catch (IOException e) {
            log.error("Failed to import preset content: {}", id, e);
            return ApiResponse.error("导入预设内容失败: " + e.getMessage());
        }
    }
}
