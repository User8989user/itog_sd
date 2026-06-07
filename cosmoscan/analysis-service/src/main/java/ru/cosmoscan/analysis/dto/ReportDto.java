package ru.cosmoscan.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReportDto {
    private Long workId;
    private String status;
    private String remarks;
    private LocalDateTime analyzedAt;
}