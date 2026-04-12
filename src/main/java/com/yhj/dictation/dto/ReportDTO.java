package com.yhj.dictation.dto;

import lombok.Data;
import java.util.List;

/**
 * 报表DTO
 */
@Data
public class ReportDTO {
    private Integer totalCount;
    private Integer completedCount;
    private Integer skippedCount;
    private Integer totalDuration;
    private Integer totalRepeats;
    private Integer avgDuration;
    private List<DailyReport> dailyReports;

    @Data
    public static class DailyReport {
        private String date;
        private Integer batchCount;
        private Integer wordCount;
        private Integer completedCount;
        private Integer avgDuration;
    }
}