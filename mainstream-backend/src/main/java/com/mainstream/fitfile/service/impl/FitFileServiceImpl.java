package com.mainstream.fitfile.service.impl;

import com.garmin.fit.*;
import com.mainstream.fitfile.dto.FitFileUploadDto;
import com.mainstream.fitfile.dto.FitFileUploadRequestDto;
import com.mainstream.fitfile.dto.FitFileUploadResponseDto;
import com.mainstream.fitfile.entity.FitFileUpload;
import com.mainstream.fitfile.entity.FitLapData;
import com.mainstream.fitfile.entity.FitTrackPoint;
import com.mainstream.fitfile.mapper.FitFileMapper;
import com.mainstream.fitfile.repository.FitFileUploadRepository;
import com.mainstream.fitfile.repository.FitLapDataRepository;
import com.mainstream.fitfile.repository.FitTrackPointRepository;
import com.mainstream.fitfile.service.FitFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FitFileServiceImpl implements FitFileService {

    private final FitFileUploadRepository fitFileUploadRepository;
    private final FitTrackPointRepository fitTrackPointRepository;
    private final FitLapDataRepository fitLapDataRepository;
    private final FitFileMapper fitFileMapper;

    @Override
    @Transactional
    public FitFileUploadResponseDto uploadFitFile(MultipartFile file, Long userId, FitFileUploadRequestDto request) {
        log.debug("Processing FIT file upload: {} for user: {}", file.getOriginalFilename(), userId);

        try {
            if (file.isEmpty()) {
                return FitFileUploadResponseDto.builder()
                    .originalFilename(file.getOriginalFilename())
                    .processingStatus(FitFileUpload.ProcessingStatus.FAILED)
                    .errorMessage("File is empty")
                    .build();
            }

            byte[] fileBytes = file.getBytes();
            String fileHash = calculateFileHash(fileBytes);

            if (isDuplicateFile(fileHash)) {
                return FitFileUploadResponseDto.builder()
                    .originalFilename(file.getOriginalFilename())
                    .processingStatus(FitFileUpload.ProcessingStatus.DUPLICATE)
                    .errorMessage("File already exists")
                    .build();
            }

            FitFileUpload fitFileUpload = FitFileUpload.builder()
                .userId(userId)
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .fileHash(fileHash)
                .processingStatus(FitFileUpload.ProcessingStatus.PENDING)
                .activityStartTime(LocalDateTime.of(1900, 1, 1, 0, 0)) // Temporary placeholder
                .build();

            fitFileUpload = fitFileUploadRepository.save(fitFileUpload);
            log.info("FIT file upload saved with ID: {}", fitFileUpload.getId());

            try {
                log.info("=== STARTING FIT FILE PROCESSING FOR: {} ===", file.getOriginalFilename());
                processFitFile(fitFileUpload, fileBytes);
                fitFileUpload.setProcessingStatus(FitFileUpload.ProcessingStatus.COMPLETED);
                fitFileUpload.setProcessedAt(LocalDateTime.now());
                log.info("=== FIT FILE PROCESSING COMPLETED FOR: {} ===", file.getOriginalFilename());
            } catch (Exception e) {
                log.error("=== FIT FILE PROCESSING FAILED FOR: {} ===", file.getOriginalFilename());
                log.error("Error processing FIT file: {}", e.getMessage(), e);
                fitFileUpload.setProcessingStatus(FitFileUpload.ProcessingStatus.FAILED);
                fitFileUpload.setErrorMessage(e.getMessage());
            }

            fitFileUpload = fitFileUploadRepository.save(fitFileUpload);

            return fitFileMapper.toResponseDto(fitFileUpload, "File uploaded and processed successfully");

        } catch (Exception e) {
            log.error("Error uploading FIT file: {}", e.getMessage(), e);
            return FitFileUploadResponseDto.builder()
                .originalFilename(file.getOriginalFilename())
                .processingStatus(FitFileUpload.ProcessingStatus.FAILED)
                .errorMessage("Upload failed: " + e.getMessage())
                .build();
        }
    }

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
        List<FitFileUpload> uploads = fitFileUploadRepository.findByUserIdAndActivityStartTimeBetween(userId, startDate, endDate);
        return fitFileMapper.toDtoList(uploads);
    }

    @Override
    @Transactional
    public void deleteUpload(Long uploadId, Long userId) {
        Optional<FitFileUpload> uploadOpt = fitFileUploadRepository.findById(uploadId);
        
        if (uploadOpt.isPresent() && uploadOpt.get().getUserId().equals(userId)) {
            fitTrackPointRepository.deleteByFitFileUploadId(uploadId);
            fitLapDataRepository.deleteByFitFileUploadId(uploadId);
            fitFileUploadRepository.deleteById(uploadId);
            log.info("Deleted FIT file upload with ID: {}", uploadId);
        }
    }

    @Override
    @Transactional
    public void processUpload(Long uploadId) {
        // This method is for reprocessing failed uploads
        log.info("Reprocessing upload with ID: {}", uploadId);
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

    private void processFitFile(FitFileUpload fitFileUpload, byte[] fileBytes) throws Exception {
        log.info("Processing FIT file with ID: {} (Size: {} bytes)", fitFileUpload.getId(), fileBytes.length);

        Decode decode = new Decode();
        MesgBroadcaster broadcaster = new MesgBroadcaster(decode);
        FitFileListener listener = new FitFileListener(fitFileUpload);
        
        broadcaster.addListener((FileIdMesgListener) listener);
        broadcaster.addListener((ActivityMesgListener) listener);
        broadcaster.addListener((SessionMesgListener) listener);
        broadcaster.addListener((RecordMesgListener) listener);
        broadcaster.addListener((LapMesgListener) listener);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        
        log.info("=== CHECKING FIT FILE INTEGRITY ===");
        if (!decode.checkFileIntegrity(inputStream)) {
            throw new RuntimeException("FIT file integrity check failed");
        }
        log.info("=== FIT FILE INTEGRITY CHECK PASSED ===");

        inputStream.reset();
        
        log.info("=== STARTING FIT FILE DECODING ===");
        if (!decode.read(inputStream, broadcaster)) {
            throw new RuntimeException("Failed to decode FIT file");
        }
        log.info("=== FIT FILE DECODING COMPLETED ===");

        fitFileUploadRepository.save(fitFileUpload);
        
        if (!listener.getTrackPoints().isEmpty()) {
            fitTrackPointRepository.saveAll(listener.getTrackPoints());
        }
        
        if (!listener.getLapData().isEmpty()) {
            fitLapDataRepository.saveAll(listener.getLapData());
        }

        log.info("Successfully processed FIT file with {} track points and {} laps", 
                listener.getTrackPoints().size(), listener.getLapData().size());
    }

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

    private static class FitFileListener implements FileIdMesgListener, SessionMesgListener, RecordMesgListener, LapMesgListener {
        
        private final FitFileUpload fitFileUpload;
        private final List<FitTrackPoint> trackPoints = new ArrayList<>();
        private final List<FitLapData> lapData = new ArrayList<>();
        private int sequenceNumber = 0;

        public FitFileListener(FitFileUpload fitFileUpload) {
            this.fitFileUpload = fitFileUpload;
        }

        @Override
        public void onMesg(FileIdMesg mesg) {
            log.info("=== FILE ID MESSAGE ===");
            log.info("Type: {}", mesg.getType());
            log.info("Manufacturer: {}", mesg.getManufacturer());
            log.info("Product: {}", mesg.getProduct());
            log.info("Serial Number: {}", mesg.getSerialNumber());
            log.info("Time Created: {}", mesg.getTimeCreated());
            log.info("Number: {}", mesg.getNumber());
            
            if (mesg.getManufacturer() != null) {
                fitFileUpload.setFitManufacturer(mesg.getManufacturer().toString());
            }
            if (mesg.getProduct() != null) {
                fitFileUpload.setFitProduct(mesg.getProduct().toString());
            }
            if (mesg.getSerialNumber() != null) {
                fitFileUpload.setFitDeviceSerial(mesg.getSerialNumber().toString());
            }
            log.info("=== END FILE ID MESSAGE ===");
        }

        @Override
        public void onMesg(SessionMesg mesg) {
            log.info("=== SESSION MESSAGE ===");
            log.info("Start Time: {}", mesg.getStartTime());
            log.info("Timestamp: {}", mesg.getTimestamp());
            log.info("Total Elapsed Time: {} seconds", mesg.getTotalElapsedTime());
            log.info("Total Timer Time: {} seconds", mesg.getTotalTimerTime());
            log.info("Total Distance: {} meters", mesg.getTotalDistance());
            log.info("Total Calories: {}", mesg.getTotalCalories());
            log.info("Total Work: {}", mesg.getTotalWork());
            log.info("Total Ascent: {} meters", mesg.getTotalAscent());
            log.info("Total Descent: {} meters", mesg.getTotalDescent());
            log.info("Average Speed: {} m/s", mesg.getAvgSpeed());
            log.info("Max Speed: {} m/s", mesg.getMaxSpeed());
            log.info("Average Heart Rate: {} bpm", mesg.getAvgHeartRate());
            log.info("Max Heart Rate: {} bpm", mesg.getMaxHeartRate());
            log.info("Average Cadence: {} steps/min", mesg.getAvgCadence());
            log.info("Max Cadence: {} steps/min", mesg.getMaxCadence());
            log.info("Average Power: {} watts", mesg.getAvgPower());
            log.info("Max Power: {} watts", mesg.getMaxPower());
            log.info("Average Temperature: {} celsius", mesg.getAvgTemperature());
            log.info("Max Temperature: {} celsius", mesg.getMaxTemperature());
            log.info("Normalized Power: {} watts", mesg.getNormalizedPower());
            log.info("Training Stress Score: {}", mesg.getTrainingStressScore());
            log.info("Intensity Factor: {}", mesg.getIntensityFactor());
            log.info("Left Right Balance: {}", mesg.getLeftRightBalance());
            log.info("Average Stroke Count: {}", mesg.getAvgStrokeCount());
            log.info("Total Cycles: {}", mesg.getTotalCycles());
            log.info("First Lap Index: {}", mesg.getFirstLapIndex());
            log.info("Num Laps: {}", mesg.getNumLaps());
            log.info("Event: {}", mesg.getEvent());
            log.info("Event Type: {}", mesg.getEventType());
            log.info("Sport: {}", mesg.getSport());
            log.info("Sub Sport: {}", mesg.getSubSport());
            log.info("Average Running Cadence: {}", mesg.getAvgRunningCadence());
            log.info("Max Running Cadence: {}", mesg.getMaxRunningCadence());
            log.info("Average Stride Length: {} meters", mesg.getAvgStanceTime());
            log.info("Average Vertical Oscillation: {} mm", mesg.getAvgVerticalOscillation());
            log.info("Average Ground Contact Time: {} ms", mesg.getAvgStanceTime());
            log.info("Training Load Peak: {}", mesg.getTrainingLoadPeak());
            //log.info("Recovery Time: {} hours", mesg.getR());
            log.info("Total Training Effect: {}", mesg.getTotalTrainingEffect());
            log.info("Total Anaerobic Training Effect: {}", mesg.getTotalAnaerobicTrainingEffect());
            
            if (mesg.getStartTime() != null) {
                fitFileUpload.setActivityStartTime(convertToLocalDateTime(mesg.getStartTime()));
            }
            if (mesg.getTotalElapsedTime() != null) {
                fitFileUpload.setTotalElapsedTime(mesg.getTotalElapsedTime().intValue());
            }
            if (mesg.getTotalTimerTime() != null) {
                fitFileUpload.setTotalTimerTime(mesg.getTotalTimerTime().intValue());
            }
            if (mesg.getTotalDistance() != null) {
                fitFileUpload.setTotalDistance(new BigDecimal(mesg.getTotalDistance().toString()));
            }
            if (mesg.getTotalCalories() != null) {
                fitFileUpload.setTotalCalories(mesg.getTotalCalories().intValue());
            }
            if (mesg.getAvgSpeed() != null) {
                fitFileUpload.setAvgSpeed(new BigDecimal(mesg.getAvgSpeed().toString()));
            }
            if (mesg.getMaxSpeed() != null) {
                fitFileUpload.setMaxSpeed(new BigDecimal(mesg.getMaxSpeed().toString()));
            }
            if (mesg.getAvgHeartRate() != null) {
                fitFileUpload.setAvgHeartRate(mesg.getAvgHeartRate().intValue());
            }
            if (mesg.getMaxHeartRate() != null) {
                fitFileUpload.setMaxHeartRate(mesg.getMaxHeartRate().intValue());
            }
            if (mesg.getAvgCadence() != null) {
                fitFileUpload.setAvgCadence(mesg.getAvgCadence().intValue());
            }
            if (mesg.getMaxCadence() != null) {
                fitFileUpload.setMaxCadence(mesg.getMaxCadence().intValue());
            }
            if (mesg.getTotalAscent() != null) {
                fitFileUpload.setTotalAscent(new BigDecimal(mesg.getTotalAscent().toString()));
            }
            if (mesg.getTotalDescent() != null) {
                fitFileUpload.setTotalDescent(new BigDecimal(mesg.getTotalDescent().toString()));
            }

            // Calculate end time
            if (fitFileUpload.getActivityStartTime() != null && fitFileUpload.getTotalElapsedTime() != null) {
                fitFileUpload.setActivityEndTime(
                    fitFileUpload.getActivityStartTime().plusSeconds(fitFileUpload.getTotalElapsedTime())
                );
            }
            log.info("=== END SESSION MESSAGE ===");
        }

        @Override
        public void onMesg(RecordMesg mesg) {
            if (sequenceNumber < 5) { // Log only first 5 track points to avoid log spam
                log.info("=== RECORD MESSAGE #{} ===", sequenceNumber);
                log.info("Timestamp: {}", mesg.getTimestamp());
                log.info("Position Lat: {} (semicircles)", mesg.getPositionLat());
                log.info("Position Long: {} (semicircles)", mesg.getPositionLong());
                log.info("Altitude: {} meters", mesg.getAltitude());
                log.info("Distance: {} meters", mesg.getDistance());
                log.info("Speed: {} m/s", mesg.getSpeed());
                log.info("Heart Rate: {} bpm", mesg.getHeartRate());
                log.info("Cadence: {} steps/min", mesg.getCadence());
                log.info("Power: {} watts", mesg.getPower());
                log.info("Temperature: {} celsius", mesg.getTemperature());
                log.info("Calories: {}", mesg.getCalories());
                log.info("Compressed Speed Distance: {}", mesg.getCompressedSpeedDistance());
                log.info("Grade: {}%", mesg.getGrade());
                log.info("Resistance: {}", mesg.getResistance());
                log.info("Time From Course: {} seconds", mesg.getTimeFromCourse());
                log.info("Cycle Length: {} meters", mesg.getCycleLength());
                log.info("Activity Type: {}", mesg.getActivityType());
                log.info("Left Right Balance: {}", mesg.getLeftRightBalance());
                log.info("GPS Accuracy: {} meters", mesg.getGpsAccuracy());
                log.info("Vertical Speed: {} m/s", mesg.getVerticalSpeed());
                log.info("=== END RECORD MESSAGE #{} ===", sequenceNumber);
            } else if (sequenceNumber == 5) {
                log.info("=== RECORD MESSAGE LOGGING STOPPED (too many records) ===");
            }

            FitTrackPoint trackPoint = FitTrackPoint.builder()
                .fitFileUpload(fitFileUpload)
                .sequenceNumber(sequenceNumber++)
                .build();

            if (mesg.getTimestamp() != null) {
                trackPoint.setTimestamp(convertToLocalDateTime(mesg.getTimestamp()));
            }
            if (mesg.getPositionLat() != null && mesg.getPositionLong() != null) {
                trackPoint.setPositionLat(convertSemicirclesToDegrees(mesg.getPositionLat()));
                trackPoint.setPositionLong(convertSemicirclesToDegrees(mesg.getPositionLong()));
            }
            if (mesg.getAltitude() != null) {
                trackPoint.setAltitude(Double.valueOf(mesg.getAltitude().toString()));
            }
            if (mesg.getDistance() != null) {
                trackPoint.setDistance(Double.valueOf(mesg.getDistance().toString()));
            }
            if (mesg.getSpeed() != null) {
                trackPoint.setSpeed(Double.valueOf(mesg.getSpeed().toString()));
            }
            if (mesg.getHeartRate() != null) {
                trackPoint.setHeartRate(mesg.getHeartRate().intValue());
            }
            if (mesg.getCadence() != null) {
                trackPoint.setCadence(mesg.getCadence().intValue());
            }
            if (mesg.getTemperature() != null) {
                trackPoint.setTemperature(mesg.getTemperature().intValue());
            }

            trackPoints.add(trackPoint);
        }

        @Override
        public void onMesg(LapMesg mesg) {
            log.info("=== LAP MESSAGE ===");
            log.info("Message Index (Lap #): {}", mesg.getMessageIndex());
            log.info("Start Time: {}", mesg.getStartTime());
            log.info("Timestamp: {}", mesg.getTimestamp());
            log.info("Event: {}", mesg.getEvent());
            log.info("Event Type: {}", mesg.getEventType());
            log.info("Total Elapsed Time: {} seconds", mesg.getTotalElapsedTime());
            log.info("Total Timer Time: {} seconds", mesg.getTotalTimerTime());
            log.info("Total Distance: {} meters", mesg.getTotalDistance());
            log.info("Total Calories: {}", mesg.getTotalCalories());
            log.info("Total Fat Calories: {}", mesg.getTotalFatCalories());
            log.info("Average Speed: {} m/s", mesg.getAvgSpeed());
            log.info("Max Speed: {} m/s", mesg.getMaxSpeed());
            log.info("Average Heart Rate: {} bpm", mesg.getAvgHeartRate());
            log.info("Max Heart Rate: {} bpm", mesg.getMaxHeartRate());
            log.info("Average Cadence: {} steps/min", mesg.getAvgCadence());
            log.info("Max Cadence: {} steps/min", mesg.getMaxCadence());
            log.info("Average Power: {} watts", mesg.getAvgPower());
            log.info("Max Power: {} watts", mesg.getMaxPower());
            log.info("Total Ascent: {} meters", mesg.getTotalAscent());
            log.info("Total Descent: {} meters", mesg.getTotalDescent());
            log.info("Intensity: {}", mesg.getIntensity());
            log.info("Lap Trigger: {}", mesg.getLapTrigger());
            log.info("Sport: {}", mesg.getSport());
            log.info("Sub Sport: {}", mesg.getSubSport());
            log.info("Start Position Lat: {} (semicircles)", mesg.getStartPositionLat());
            log.info("Start Position Long: {} (semicircles)", mesg.getStartPositionLong());
            log.info("End Position Lat: {} (semicircles)", mesg.getEndPositionLat());
            log.info("End Position Long: {} (semicircles)", mesg.getEndPositionLong());
            log.info("Normalized Power: {} watts", mesg.getNormalizedPower());
            log.info("Left Right Balance: {}", mesg.getLeftRightBalance());
            log.info("First Length Index: {}", mesg.getFirstLengthIndex());
            log.info("Num Lengths: {}", mesg.getNumLengths());
            log.info("Enhanced Average Speed: {} m/s", mesg.getEnhancedAvgSpeed());
            log.info("Enhanced Max Speed: {} m/s", mesg.getEnhancedMaxSpeed());
            log.info("Average Temperature: {} celsius", mesg.getAvgTemperature());
            log.info("Max Temperature: {} celsius", mesg.getMaxTemperature());

            FitLapData lap = FitLapData.builder()
                .fitFileUpload(fitFileUpload)
                .build();

            if (mesg.getMessageIndex() != null) {
                lap.setLapNumber(mesg.getMessageIndex().intValue());
            }
            if (mesg.getStartTime() != null) {
                lap.setStartTime(convertToLocalDateTime(mesg.getStartTime()));
            }
            if (mesg.getTotalElapsedTime() != null) {
                lap.setTotalElapsedTime(mesg.getTotalElapsedTime().intValue());
            }
            if (mesg.getTotalTimerTime() != null) {
                lap.setTotalTimerTime(mesg.getTotalTimerTime().intValue());
            }
            if (mesg.getTotalDistance() != null) {
                lap.setTotalDistance(new BigDecimal(mesg.getTotalDistance().toString()));
            }
            if (mesg.getAvgSpeed() != null) {
                lap.setAvgSpeed(new BigDecimal(mesg.getAvgSpeed().toString()));
            }
            if (mesg.getMaxSpeed() != null) {
                lap.setMaxSpeed(new BigDecimal(mesg.getMaxSpeed().toString()));
            }
            if (mesg.getAvgHeartRate() != null) {
                lap.setAvgHeartRate(mesg.getAvgHeartRate().intValue());
            }
            if (mesg.getMaxHeartRate() != null) {
                lap.setMaxHeartRate(mesg.getMaxHeartRate().intValue());
            }
            if (mesg.getTotalCalories() != null) {
                lap.setTotalCalories(mesg.getTotalCalories().intValue());
            }

            // Calculate end time for lap
            if (lap.getStartTime() != null && lap.getTotalElapsedTime() != null) {
                lap.setEndTime(lap.getStartTime().plusSeconds(lap.getTotalElapsedTime()));
            }

            lapData.add(lap);
            log.info("=== END LAP MESSAGE ===");
        }

        private LocalDateTime convertToLocalDateTime(DateTime dateTime) {
            return LocalDateTime.ofEpochSecond(
                dateTime.getTimestamp() + 631065600L, // FIT epoch is 1989-12-31T00:00:00Z
                0,
                ZoneOffset.UTC
            );
        }

        private BigDecimal convertSemicirclesToDegrees(Integer semicircles) {
            if (semicircles == null) return null;
            double degrees = semicircles * (180.0 / Math.pow(2, 31));
            return new BigDecimal(degrees).setScale(8, BigDecimal.ROUND_HALF_UP);
        }

        public List<FitTrackPoint> getTrackPoints() {
            return trackPoints;
        }

        public List<FitLapData> getLapData() {
            return lapData;
        }
    }
}