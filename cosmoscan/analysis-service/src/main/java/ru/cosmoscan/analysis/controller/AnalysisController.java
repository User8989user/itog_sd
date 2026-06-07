package ru.cosmoscan.analysis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cosmoscan.analysis.dto.AnalyzeRequest;
import ru.cosmoscan.analysis.dto.ReportDto;
import ru.cosmoscan.analysis.service.AnalysisService;
import ru.cosmoscan.analysis.service.WordCloudService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final WordCloudService wordCloudService;

    @PostMapping("/analyze")
    public ResponseEntity<Void> triggerAnalysis(@RequestBody AnalyzeRequest request) throws IOException {
        analysisService.analyzeWork(request.getWorkId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/works/{workId}")
    public ResponseEntity<ReportDto> getReport(@PathVariable Long workId) {
        return ResponseEntity.ok(analysisService.getReportByWorkId(workId));
    }

    @GetMapping(value = "/wordcloud/{workId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getWordCloud(@PathVariable Long workId) throws IOException {
        byte[] png = wordCloudService.generateWordCloud(workId);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }
}