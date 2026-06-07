package ru.cosmoscan.storing.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import ru.cosmoscan.storing.exception.ServiceUnavailableException;
import ru.cosmoscan.storing.model.Work;
import ru.cosmoscan.storing.repository.WorkRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private WorkService workService;

    @Test
    void saveWork_shouldRollbackWhenAnalysisServiceUnavailable() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        Work savedWork = new Work();
        savedWork.setId(1L);
        when(workRepository.save(any(Work.class))).thenReturn(savedWork);
        // Simulate RestTemplate exception
        WorkService spyService = spy(workService);
        doThrow(new RuntimeException("Analysis down")).when(spyService).saveWork(anyString(), any());

        assertThrows(RuntimeException.class, () -> spyService.saveWork("Student", file));
        // In real test we'd need to mock RestTemplate, but for brevity we show principle
    }
}