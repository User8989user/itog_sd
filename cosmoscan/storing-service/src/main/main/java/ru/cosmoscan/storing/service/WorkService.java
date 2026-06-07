package ru.cosmoscan.storing.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.cosmoscan.storing.dto.AnalyzeRequest;
import ru.cosmoscan.storing.exception.ResourceNotFoundException;
import ru.cosmoscan.storing.exception.ServiceUnavailableException;
import ru.cosmoscan.storing.model.Work;
import ru.cosmoscan.storing.repository.WorkRepository;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkService {

    private final WorkRepository workRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${file.upload.dir}")
    private String uploadDir;

    @Value("${analysis.service.url}")
    private String analysisUrl;

    @Transactional
    public Work saveWork(String studentName, MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }
        String uniqueName = UUID.randomUUID() + ext;
        Path targetPath = Paths.get(uploadDir).resolve(uniqueName);
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Work work = new Work();
        work.setStudentName(studentName);
        work.setFileName(originalName);
        work.setFilePath(targetPath.toString());
        work.setFileSize(file.getSize());
        work.setFileType(file.getContentType());
        work.setUploadTime(LocalDateTime.now());
        work = workRepository.save(work);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<AnalyzeRequest> request = new HttpEntity<>(new AnalyzeRequest(work.getId()), headers);
            restTemplate.postForEntity(analysisUrl + "/api/analyze", request, Void.class);
            log.info("Analysis triggered for workId {}", work.getId());
        } catch (Exception e) {
            log.error("Analysis service unavailable, rolling back work {}", work.getId(), e);
            // Rollback: удалить файл и запись в БД
            Files.deleteIfExists(targetPath);
            workRepository.delete(work);
            throw new ServiceUnavailableException("Analysis service is unavailable. Please try again later.", e);
        }
        return work;
    }

    public Resource loadFileAsResource(Long id) throws IOException {
        Work work = workRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work not found with id: " + id));
        Path filePath = Paths.get(work.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new ResourceNotFoundException("File not found for work id: " + id);
        }
    }
}