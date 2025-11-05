package com.mainstream.fitfile.service;

import com.mainstream.fitfile.dto.FitFileUploadDto;
import com.mainstream.fitfile.dto.FitFileUploadRequestDto;
import com.mainstream.fitfile.dto.FitFileUploadResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FitFileService {

    FitFileUploadResponseDto uploadFitFile(MultipartFile file, Long userId, FitFileUploadRequestDto request);

    List<FitFileUploadDto> getUserUploads(Long userId);

    Page<FitFileUploadDto> getUserUploads(Long userId, Pageable pageable);

    Optional<FitFileUploadDto> getUploadById(Long uploadId, Long userId);

    List<FitFileUploadDto> getUserUploadsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    void deleteUpload(Long uploadId, Long userId);

    void processUpload(Long uploadId);

    void processPendingUploads();

    boolean isDuplicateFile(String fileHash);

    long getUserUploadCount(Long userId);
}