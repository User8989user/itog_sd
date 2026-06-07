package ru.cosmoscan.analysis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.cosmoscan.analysis.model.Report;
import ru.cosmoscan.analysis.repository.ReportRepository;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AnalysisService analysisService;

    @Test
    void analyzeWork_validSmallTxtFile_shouldAccept() throws IOException {
        byte[] validFile = "Hello world".getBytes();
        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(validFile));

        analysisService.analyzeWork(1L);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report saved = captor.getValue();
        assertEquals("ACCEPTED", saved.getStatus());
        assertTrue(saved.getRemarks().isEmpty());
    }

    @Test
    void analyzeWork_largeFile_shouldReject() throws IOException {
        byte[] largeFile = new byte[2_000_000];
        when(restTemplate.getForEntity(anyString(), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(largeFile));

        analysisService.analyzeWork(2L);

        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(captor.capture());
        Report saved = captor.getValue();
        assertEquals("NEEDS_REWORK", saved.getStatus());
        assertTrue(saved.getRemarks().contains("превышает 1 МБ"));
    }
}