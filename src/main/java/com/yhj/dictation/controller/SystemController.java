package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * 系统信息控制器
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "系统信息", description = "版本、配置等系统信息接口")
public class SystemController {

    private static final String APP_VERSION = "v1.3.0";

    /**
     * 获取应用版本信息
     */
    @GetMapping("/version")
    @Operation(summary = "获取版本", description = "获取应用当前版本号和时间戳")
    public ApiResponse<VersionInfo> getVersion() {
        VersionInfo info = new VersionInfo();
        info.setVersion(APP_VERSION);
        info.setTimestamp(Instant.now().toEpochMilli());
        return ApiResponse.success(info);
    }

    /**
     * 版本信息DTO
     */
    public static class VersionInfo {
        private String version;
        private Long timestamp;

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}