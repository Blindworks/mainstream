package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitFileUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FitFileUploadRepository extends JpaRepository<FitFileUpload, Long> {

    List<FitFileUpload> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<FitFileUpload> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<FitFileUpload> findByFileHash(String fileHash);

    boolean existsByFileHash(String fileHash);

    @Query("SELECT f FROM FitFileUpload f WHERE f.userId = :userId AND f.processingStatus = :status")
    List<FitFileUpload> findByUserIdAndProcessingStatus(@Param("userId") Long userId, 
                                                        @Param("status") FitFileUpload.ProcessingStatus status);

    @Query("SELECT f FROM FitFileUpload f WHERE f.userId = :userId AND f.activityStartTime BETWEEN :startDate AND :endDate ORDER BY f.activityStartTime DESC")
    List<FitFileUpload> findByUserIdAndActivityStartTimeBetween(@Param("userId") Long userId, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(f) FROM FitFileUpload f WHERE f.userId = :userId AND f.processingStatus = 'COMPLETED'")
    long countCompletedUploadsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM FitFileUpload f WHERE f.processingStatus = 'PENDING' OR f.processingStatus = 'PROCESSING' ORDER BY f.createdAt ASC")
    List<FitFileUpload> findPendingUploads();

    List<FitFileUpload> findByProcessingStatus(FitFileUpload.ProcessingStatus status);

    // Additional methods for RunService integration
    List<FitFileUpload> findByUserIdAndProcessingStatusOrderByActivityStartTimeDesc(
        Long userId, FitFileUpload.ProcessingStatus status);

    @Query("SELECT f FROM FitFileUpload f WHERE f.userId = :userId AND f.processingStatus = :status AND f.activityStartTime BETWEEN :startDate AND :endDate ORDER BY f.activityStartTime DESC")
    List<FitFileUpload> findByUserIdAndProcessingStatusAndActivityStartTimeBetweenOrderByActivityStartTimeDesc(
        @Param("userId") Long userId, 
        @Param("status") FitFileUpload.ProcessingStatus status,
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);

    Optional<FitFileUpload> findByIdAndUserId(Long id, Long userId);

    // Methods with JOIN FETCH to load track points for speed calculation
    @Query("SELECT DISTINCT f FROM FitFileUpload f LEFT JOIN FETCH f.trackPoints WHERE f.userId = :userId AND f.processingStatus = :status ORDER BY f.activityStartTime DESC")
    List<FitFileUpload> findByUserIdAndProcessingStatusWithTrackPoints(
        @Param("userId") Long userId,
        @Param("status") FitFileUpload.ProcessingStatus status);

    @Query("SELECT DISTINCT f FROM FitFileUpload f LEFT JOIN FETCH f.trackPoints WHERE f.id = :id AND f.userId = :userId")
    Optional<FitFileUpload> findByIdAndUserIdWithTrackPoints(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT f FROM FitFileUpload f LEFT JOIN FETCH f.trackPoints WHERE f.userId = :userId AND f.processingStatus = :status AND f.activityStartTime BETWEEN :startDate AND :endDate ORDER BY f.activityStartTime DESC")
    List<FitFileUpload> findByUserIdAndProcessingStatusAndActivityStartTimeBetweenWithTrackPoints(
        @Param("userId") Long userId,
        @Param("status") FitFileUpload.ProcessingStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}