package ru.cosmoscan.storing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WorkResponse {
    private Long id;
    private String studentName;
    private LocalDateTime uploadTime;
}