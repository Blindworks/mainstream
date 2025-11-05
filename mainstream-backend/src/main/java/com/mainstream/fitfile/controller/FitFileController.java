package com.mainstream.fitfile.controller;

import com.mainstream.fitfile.dto.FitFileUploadDto;
import com.mainstream.fitfile.dto.FitFileUploadRequestDto;
import com.mainstream.fitfile.dto.FitFileUploadResponseDto;
import com.mainstream.fitfile.service.FitFileService;
import com.mainstream.fitfile.service.impl.EnhancedFitFileServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/fit-files")
@RequiredArgsConstructor
@Slf4j
public class FitFileController {

    private final EnhancedFitFileServiceImpl fitFileService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("FIT File Service is running");
    }

    @PostMapping("/upload")
    public ResponseEntity<FitFileUploadResponseDto> uploadFitFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @ModelAttribute FitFileUploadRequestDto request) {
        
        log.info("Processing FIT file upload: {} for user: {}", file.getOriginalFilename(), userId);

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(FitFileUploadResponseDto.builder()
                    .originalFilename(file.getOriginalFilename())
                    .errorMessage("File is empty")
                    .build());
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".fit")) {
            return ResponseEntity.badRequest()
                .body(FitFileUploadResponseDto.builder()
                    .originalFilename(file.getOriginalFilename())
                    .errorMessage("Only .fit files are supported")
                    .build());
        }

        FitFileUploadResponseDto response = fitFileService.uploadFitFile(file, userId, request);
        
        if (response.getProcessingStatus().name().equals("FAILED")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        if (response.getProcessingStatus().name().equals("DUPLICATE")) {
            return ResponseEntity.status(409).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<FitFileUploadDto>> getUserUploads(
            @RequestHeader("X-User-Id") Long userId) {
        
        log.debug("Getting FIT file uploads for user: {}", userId);
        List<FitFileUploadDto> uploads = fitFileService.getUserUploads(userId);
        return ResponseEntity.ok(uploads);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<FitFileUploadDto>> getUserUploadsPaginated(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("Getting paginated FIT file uploads for user: {}", userId);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FitFileUploadDto> uploads = fitFileService.getUserUploads(userId, pageable);
        
        return ResponseEntity.ok(uploads);
    }

    @GetMapping("/{uploadId}")
    public ResponseEntity<FitFileUploadDto> getUploadById(
            @PathVariable Long uploadId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.debug("Getting FIT file upload {} for user: {}", uploadId, userId);
        
        return fitFileService.getUploadById(uploadId, userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<FitFileUploadDto>> getUserUploadsByDateRange(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Getting FIT file uploads for user: {} between {} and {}", userId, startDate, endDate);
        
        List<FitFileUploadDto> uploads = fitFileService.getUserUploadsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(uploads);
    }

    @DeleteMapping("/{uploadId}")
    public ResponseEntity<Void> deleteUpload(
            @PathVariable Long uploadId,
            @RequestHeader("X-User-Id") Long userId) {
        
        log.info("Deleting FIT file upload {} for user: {}", uploadId, userId);
        
        fitFileService.deleteUpload(uploadId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{uploadId}/reprocess")
    public ResponseEntity<Void> reprocessUpload(
            @PathVariable Long uploadId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String currentUserRole) {
        
        // Only admins can reprocess uploads
        if (!"ADMIN".equals(currentUserRole)) {
            return ResponseEntity.status(403).build();
        }
        
        log.info("Reprocessing FIT file upload {} requested by user: {}", uploadId, userId);
        
        fitFileService.processUpload(uploadId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<UploadStats> getUserUploadStats(
            @RequestHeader("X-User-Id") Long userId) {
        
        log.debug("Getting upload stats for user: {}", userId);
        
        long totalUploads = fitFileService.getUserUploadCount(userId);
        
        UploadStats stats = UploadStats.builder()
            .totalUploads(totalUploads)
            .build();
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/process-pending")
    public ResponseEntity<Void> processPendingUploads(
            @RequestHeader("X-User-Role") String currentUserRole) {
        
        // Only admins can trigger batch processing
        if (!"ADMIN".equals(currentUserRole)) {
            return ResponseEntity.status(403).build();
        }
        
        log.info("Processing pending uploads requested");
        
        fitFileService.processPendingUploads();
        return ResponseEntity.ok().build();
    }

    // Inner class for stats response
    public static class UploadStats {
        private long totalUploads;

        public static UploadStatsBuilder builder() {
            return new UploadStatsBuilder();
        }

        public long getTotalUploads() {
            return totalUploads;
        }

        public void setTotalUploads(long totalUploads) {
            this.totalUploads = totalUploads;
        }

        public static class UploadStatsBuilder {
            private long totalUploads;

            public UploadStatsBuilder totalUploads(long totalUploads) {
                this.totalUploads = totalUploads;
                return this;
            }

            public UploadStats build() {
                UploadStats stats = new UploadStats();
                stats.setTotalUploads(totalUploads);
                return stats;
            }
        }
    }
}