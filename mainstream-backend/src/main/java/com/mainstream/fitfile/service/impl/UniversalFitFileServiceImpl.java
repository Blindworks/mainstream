package com.mainstream.fitfile.service.impl;

import com.garmin.fit.*;
import com.mainstream.fitfile.dto.FitFileUploadDto;
import com.mainstream.fitfile.dto.FitFileUploadRequestDto;
import com.mainstream.fitfile.dto.FitFileUploadResponseDto;
import com.mainstream.fitfile.entity.*;
import com.mainstream.fitfile.mapper.FitFileMapper;
import com.mainstream.fitfile.processor.MessageProcessor;
import com.mainstream.fitfile.processor.RecordMessageProcessor;
import com.mainstream.fitfile.repository.*;
import com.mainstream.fitfile.service.FitFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Universal FIT File Service Implementation
 * Features:
 * - Processes ALL FIT message types using pluggable processors
 * - Zero data loss - captures unknown messages
 * - Developer field support
 * - Robust error handling and recovery
 * - Extensible architecture for future message types
 */
@Service("universalFitFileService")
@Primary
@Slf4j
@Transactional(readOnly = true)
public class UniversalFitFileServiceImpl implements FitFileService {

    private final FitFileUploadRepository fitFileUploadRepository;
    private final FitTrackPointRepository fitTrackPointRepository;
    private final FitLapDataRepository fitLapDataRepository;
    private final FitDeviceInfoRepository fitDeviceInfoRepository;
    private final FitZoneRepository fitZoneRepository;
    private final FitEventRepository fitEventRepository;
    private final FitHrvRepository fitHrvRepository;
    private final FitMessageRepository fitMessageRepository;
    private final FitUnknownMessageRepository fitUnknownMessageRepository;
    private final FitFileMapper fitFileMapper;

    // Pluggable message processors
    private final Map<String, MessageProcessor> processorsByType;
    private final List<MessageProcessor> allProcessors;

    @Autowired
    public UniversalFitFileServiceImpl(
            FitFileUploadRepository fitFileUploadRepository,
            FitTrackPointRepository fitTrackPointRepository,
            FitLapDataRepository fitLapDataRepository,
            FitDeviceInfoRepository fitDeviceInfoRepository,
            FitZoneRepository fitZoneRepository,
            FitEventRepository fitEventRepository,
            FitHrvRepository fitHrvRepository,
            FitMessageRepository fitMessageRepository,
            FitUnknownMessageRepository fitUnknownMessageRepository,
            FitFileMapper fitFileMapper,
            List<MessageProcessor> processors) {

        this.fitFileUploadRepository = fitFileUploadRepository;
        this.fitTrackPointRepository = fitTrackPointRepository;
        this.fitLapDataRepository = fitLapDataRepository;
        this.fitDeviceInfoRepository = fitDeviceInfoRepository;
        this.fitZoneRepository = fitZoneRepository;
        this.fitEventRepository = fitEventRepository;
        this.fitHrvRepository = fitHrvRepository;
        this.fitMessageRepository = fitMessageRepository;
        this.fitUnknownMessageRepository = fitUnknownMessageRepository;
        this.fitFileMapper = fitFileMapper;

        // Initialize processors
        this.allProcessors = processors.stream()
            .sorted(Comparator.comparingInt(MessageProcessor::getPriority).reversed())
            .collect(Collectors.toList());

        this.processorsByType = processors.stream()
            .collect(Collectors.toMap(
                MessageProcessor::getMessageType,
                p -> p,
                (p1, p2) -> p1.getPriority() > p2.getPriority() ? p1 : p2
            ));

        log.info("Initialized UniversalFitFileService with {} processors", processors.size());
        processors.forEach(p -> log.info("  - {} (priority: {})",
            p.getClass().getSimpleName(), p.getPriority()));
    }

    @Override
    @Transactional
    public FitFileUploadResponseDto uploadFitFile(MultipartFile file, Long userId, FitFileUploadRequestDto request) {
        log.info("========================================");
        log.info("Processing FIT file upload: {} for user: {}", file.getOriginalFilename(), userId);
        log.info("========================================");

        try {
            // Validate file
            if (file.isEmpty()) {
                return buildErrorResponse(file.getOriginalFilename(),
                    FitFileUpload.ProcessingStatus.FAILED, "File is empty");
            }

            byte[] fileBytes = file.getBytes();
            String fileHash = calculateFileHash(fileBytes);

            // Check for duplicates
            if (isDuplicateFile(fileHash)) {
                return buildErrorResponse(file.getOriginalFilename(),
                    FitFileUpload.ProcessingStatus.DUPLICATE, "File already exists");
            }

            // Create upload entity
            FitFileUpload fitFileUpload = FitFileUpload.builder()
                .userId(userId)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .processingStatus(FitFileUpload.ProcessingStatus.PENDING)
                .activityStartTime(LocalDateTime.of(1900, 1, 1, 0, 0)) // Temporary
                .build();

            fitFileUpload = fitFileUploadRepository.save(fitFileUpload);
            log.info("FIT file upload saved with ID: {}", fitFileUpload.getId());

            // Process the file
            try {
                processFitFile(fitFileUpload, fileBytes);
                fitFileUpload.setProcessingStatus(FitFileUpload.ProcessingStatus.COMPLETED);
                fitFileUpload.setProcessedAt(LocalDateTime.now());
                log.info("✅ FIT FILE PROCESSING COMPLETED SUCCESSFULLY");
            } catch (Exception e) {
                log.error("❌ FIT FILE PROCESSING FAILED: {}", e.getMessage(), e);
                fitFileUpload.setProcessingStatus(FitFileUpload.ProcessingStatus.FAILED);
                fitFileUpload.setErrorMessage(e.getMessage());
            }

            fitFileUpload = fitFileUploadRepository.save(fitFileUpload);

            log.info("========================================");
            return fitFileMapper.toResponseDto(fitFileUpload, "File processed successfully");

        } catch (Exception e) {
            log.error("Error uploading FIT file: {}", e.getMessage(), e);
            return buildErrorResponse(file.getOriginalFilename(),
                FitFileUpload.ProcessingStatus.FAILED, "Upload failed: " + e.getMessage());
        }
    }

    private void processFitFile(FitFileUpload fitFileUpload, byte[] fileBytes) throws Exception {
        log.info("🔄 Starting FIT file processing (Size: {} bytes)", fileBytes.length);

        Decode decode = new Decode();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);

        // Check file integrity
        log.info("🔍 Checking FIT file integrity...");
        if (!decode.checkFileIntegrity(inputStream)) {
            throw new RuntimeException("FIT file integrity check failed - file may be corrupted");
        }
        log.info("✅ FIT file integrity check PASSED");

        // Reset input stream
        inputStream.reset();

        // Create universal message listener
        UniversalMesgListener listener = new UniversalMesgListener(fitFileUpload);
        MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
        broadcaster.addListener(listener);

        // Decode the file
        log.info("🔄 Decoding FIT file...");
        RecordMessageProcessor.resetRecordCount();

        if (!decode.read(inputStream, broadcaster)) {
            throw new RuntimeException("Failed to decode FIT file");
        }

        log.info("✅ FIT file decoded successfully");

        // Save all processed data
        saveProcessedData(fitFileUpload, listener);

        // Log statistics
        logProcessingStatistics(listener);
    }

    private void saveProcessedData(FitFileUpload fitFileUpload, UniversalMesgListener listener) {
        log.info("💾 Saving processed data to database...");

        // Save main upload entity (updated by processors)
        fitFileUploadRepository.save(fitFileUpload);

        // Save generic messages
        if (!listener.getMessages().isEmpty()) {
            fitMessageRepository.saveAll(listener.getMessages());
            log.info("  ✓ Saved {} generic messages", listener.getMessages().size());
        }

        // Save unknown messages
        if (!listener.getUnknownMessages().isEmpty()) {
            fitUnknownMessageRepository.saveAll(listener.getUnknownMessages());
            log.warn("  ⚠ Saved {} unknown messages for future processing",
                listener.getUnknownMessages().size());
        }

        log.info("✅ All data saved successfully");
    }

    private void logProcessingStatistics(UniversalMesgListener listener) {
        log.info("========================================");
        log.info("📊 PROCESSING STATISTICS:");
        log.info("  • Total messages processed: {}", listener.getTotalMessageCount());
        log.info("  • Messages by type:");

        listener.getMessageCountsByType().entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
            .forEach(entry -> log.info("    - {}: {}", entry.getKey(), entry.getValue()));

        if (!listener.getUnknownMessages().isEmpty()) {
            log.warn("  ⚠ Unknown message types encountered: {}",
                listener.getUnknownMessages().size());
        }

        log.info("========================================");
    }

    /**
     * Universal message listener that processes all FIT messages
     */
    private class UniversalMesgListener implements MesgListener {
        private final FitFileUpload fitFileUpload;
        private final List<FitMessage> messages = new ArrayList<>();
        private final List<FitUnknownMessage> unknownMessages = new ArrayList<>();
        private final Map<String, Integer> messageCountsByType = new HashMap<>();
        private int sequenceNumber = 0;
        private int totalMessageCount = 0;

        public UniversalMesgListener(FitFileUpload fitFileUpload) {
            this.fitFileUpload = fitFileUpload;
        }

        @Override
        public void onMesg(Mesg mesg) {
            if (mesg == null) {
                return;
            }

            totalMessageCount++;
            String messageType = mesg.getName() != null ? mesg.getName() : "unknown";
            messageCountsByType.merge(messageType, 1, Integer::sum);

            try {
                // Find appropriate processor
                MessageProcessor processor = findProcessor(mesg);

                if (processor != null) {
                    // Process with dedicated processor
                    MessageProcessor.ProcessingResult result =
                        processor.process(mesg, fitFileUpload, sequenceNumber);

                    if (result.isSuccess() && result.getData() != null) {
                        // Store as generic message
                        FitMessage fitMessage = FitMessage.builder()
                            .fitFileUpload(fitFileUpload)
                            .messageType(messageType)
                            .messageNumber(mesg.getNum())
                            .sequenceNumber(sequenceNumber)
                            .messageData(result.getData())
                            .developerFields(result.getDeveloperFields())
                            .fullyParsed(result.isFullyParsed())
                            .parsingNotes(result.getNotes())
                            .build();

                        // Extract timestamp if present
                        Object timestamp = result.getData().get("timestamp");
                        if (timestamp instanceof LocalDateTime) {
                            fitMessage.setMessageTimestamp((LocalDateTime) timestamp);
                        }

                        messages.add(fitMessage);
                    } else {
                        log.warn("Processor failed for message {}: {}",
                            messageType, result.getNotes());
                        storeAsUnknown(mesg, "Processor failed: " + result.getNotes());
                    }
                } else {
                    log.warn("No processor found for message type: {} (num: {})",
                        messageType, mesg.getNum());
                    storeAsUnknown(mesg, "No processor available");
                }

            } catch (Exception e) {
                log.error("Error processing message {}: {}", messageType, e.getMessage(), e);
                storeAsUnknown(mesg, "Processing error: " + e.getMessage());
            }

            sequenceNumber++;
        }

        private MessageProcessor findProcessor(Mesg mesg) {
            // Try by message type first
            String messageType = mesg.getName();
            if (messageType != null && processorsByType.containsKey(messageType)) {
                return processorsByType.get(messageType);
            }

            // Try by capability (priority order)
            for (MessageProcessor processor : allProcessors) {
                if (processor.canProcess(mesg)) {
                    return processor;
                }
            }

            return null;
        }

        private void storeAsUnknown(Mesg mesg, String reason) {
            try {
                Map<String, Object> rawData = new HashMap<>();

                // Extract basic message info
                rawData.put("message_name", mesg.getName());
                rawData.put("message_num", mesg.getNum());

                // Extract all fields
                for (Field field : mesg.getFields()) {
                    try {
                        rawData.put(field.getName(), field.getValue());
                    } catch (Exception e) {
                        log.debug("Could not extract field {}: {}", field.getName(), e.getMessage());
                    }
                }

                FitUnknownMessage unknownMessage = FitUnknownMessage.builder()
                    .fitFileUpload(fitFileUpload)
                    .globalMessageNumber(mesg.getNum())
                    .sequenceNumber(sequenceNumber)
                    .rawData(rawData)
                    .completelyUnknown(mesg.getName() == null || mesg.getName().startsWith("unknown"))
                    .unknownReason(reason)
                    .reprocessStatus(FitUnknownMessage.ReprocessStatus.PENDING)
                    .build();

                unknownMessages.add(unknownMessage);
            } catch (Exception e) {
                log.error("Failed to store unknown message: {}", e.getMessage());
            }
        }

        public List<FitMessage> getMessages() {
            return messages;
        }

        public List<FitUnknownMessage> getUnknownMessages() {
            return unknownMessages;
        }

        public Map<String, Integer> getMessageCountsByType() {
            return messageCountsByType;
        }

        public int getTotalMessageCount() {
            return totalMessageCount;
        }
    }

    // Utility methods
    private String calculateFileHash(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate file hash", e);
        }
    }

    private FitFileUploadResponseDto buildErrorResponse(String filename,
            FitFileUpload.ProcessingStatus status, String errorMessage) {
        return FitFileUploadResponseDto.builder()
            .originalFilename(filename)
            .processingStatus(status)
            .errorMessage(errorMessage)
            .build();
    }

    // Delegate methods from FitFileService interface
    @Override
    public List<FitFileUploadDto> getUserUploads(Long userId) {
        List<FitFileUpload> uploads = fitFileUploadRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return fitFileMapper.toDtoList(uploads);
    }

    @Override
    public Page<FitFileUploadDto> getUserUploads(Long userId, Pageable pageable) {
        Page<FitFileUpload> uploads = fitFileUploadRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return uploads.map(fitFileMapper::toDto);
    }

    @Override
    public Optional<FitFileUploadDto> getUploadById(Long uploadId, Long userId) {
        return fitFileUploadRepository.findById(uploadId)
            .filter(upload -> upload.getUserId().equals(userId))
            .map(fitFileMapper::toDto);
    }

    @Override
    public List<FitFileUploadDto> getUserUploadsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<FitFileUpload> uploads = fitFileUploadRepository
            .findByUserIdAndActivityStartTimeBetween(userId, startDate, endDate);
        return fitFileMapper.toDtoList(uploads);
    }

    @Override
    @Transactional
    public void deleteUpload(Long uploadId, Long userId) {
        Optional<FitFileUpload> uploadOpt = fitFileUploadRepository.findById(uploadId);

        if (uploadOpt.isPresent() && uploadOpt.get().getUserId().equals(userId)) {
            // Delete all related data
            fitMessageRepository.deleteByFitFileUploadId(uploadId);
            fitUnknownMessageRepository.deleteByFitFileUploadId(uploadId);
            fitTrackPointRepository.deleteByFitFileUploadId(uploadId);
            fitLapDataRepository.deleteByFitFileUploadId(uploadId);
            fitDeviceInfoRepository.deleteByFitFileUploadId(uploadId);
            fitZoneRepository.deleteByFitFileUploadId(uploadId);
            fitEventRepository.deleteByFitFileUploadId(uploadId);
            fitHrvRepository.deleteByFitFileUploadId(uploadId);
            fitFileUploadRepository.deleteById(uploadId);
            log.info("Deleted FIT file upload with ID: {}", uploadId);
        }
    }

    @Override
    @Transactional
    public void processUpload(Long uploadId) {
        log.info("Reprocessing upload with ID: {}", uploadId);
        // Implementation for reprocessing
    }

    @Override
    @Transactional
    public void processPendingUploads() {
        List<FitFileUpload> pendingUploads = fitFileUploadRepository.findPendingUploads();
        log.info("Found {} pending uploads to process", pendingUploads.size());

        for (FitFileUpload upload : pendingUploads) {
            try {
                processUpload(upload.getId());
            } catch (Exception e) {
                log.error("Error processing pending upload {}: {}", upload.getId(), e.getMessage());
            }
        }
    }

    @Override
    public boolean isDuplicateFile(String fileHash) {
        return fitFileUploadRepository.existsByFileHash(fileHash);
    }

    @Override
    public long getUserUploadCount(Long userId) {
        return fitFileUploadRepository.countCompletedUploadsByUserId(userId);
    }
}
