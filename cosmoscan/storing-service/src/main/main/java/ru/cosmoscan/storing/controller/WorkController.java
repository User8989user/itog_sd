package ru.cosmoscan.storing.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.cosmoscan.storing.dto.WorkResponse;
import ru.cosmoscan.storing.model.Work;
import ru.cosmoscan.storing.service.WorkService;

import java.io.IOException;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkResponse> uploadWork(
            @RequestParam String studentName,
            @RequestParam MultipartFile file) throws IOException {
        Work work = workService.saveWork(studentName, file);
        return ResponseEntity.ok(new WorkResponse(work.getId(), work.getStudentName(), work.getUploadTime()));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        Resource resource = workService.loadFileAsResource(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}