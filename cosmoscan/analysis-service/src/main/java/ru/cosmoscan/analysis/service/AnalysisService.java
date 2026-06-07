package ru.cosmoscan.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.cosmoscan.analysis.dto.ReportDto;
import ru.cosmoscan.analysis.exception.ResourceNotFoundException;
import ru.cosmoscan.analysis.model.Report;
import ru.cosmoscan.analysis.repository.ReportRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ReportRepository reportRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${storing.service.url}")
    private String storingUrl;

    @Transactional
    public void analyzeWork(Long workId) throws IOException {
        // 1. Получить файл из Storing Service
        ResponseEntity<byte[]> fileResponse = restTemplate.getForEntity(
                storingUrl + "/api/works/" + workId + "/file", byte[].class);
        byte[] fileData = fileResponse.getBody();
        if (fileData == null || fileData.length == 0) {
            throw new IOException("Empty file from storing service for workId " + workId);
        }

        List<String> remarks = new ArrayList<>();

        // 2. Проверка размера (1 MB = 1048576 bytes)
        if (fileData.length > 1_048_576) {
            remarks.add("Размер файла превышает 1 МБ (фактический: " + fileData.length + " байт)");
        }

        // 3. Проверка формата с помощью Tika
        try (InputStream is = new ByteArrayInputStream(fileData)) {
            Tika tika = new Tika();
            String mime = tika.detect(is);
            Set<String> allowedMimes = Set.of("application/pdf", "application/msword", "text/plain");
            if (!allowedMimes.contains(mime)) {
                remarks.add("Недопустимый формат файла (MIME: " + mime + "). Разрешены: PDF, DOC, TXT");
            }
        }

        String status = remarks.isEmpty() ? "ACCEPTED" : "NEEDS_REWORK";
        String remarksStr = String.join("; ", remarks);

        Report report = new Report();
        report.setWorkId(workId);
        report.setStatus(status);
        report.setRemarks(remarksStr);
        report.setAnalyzedAt(LocalDateTime.now());
        reportRepository.save(report);
        log.info("Report saved for workId {} with status {}", workId, status);
    }

    public ReportDto getReportByWorkId(Long workId) {
        Report report = reportRepository.findByWorkId(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found for workId: " + workId));
        return new ReportDto(report.getWorkId(), report.getStatus(), report.getRemarks(), report.getAnalyzedAt());
    }
}