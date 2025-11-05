package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitTrackPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitTrackPointRepository extends JpaRepository<FitTrackPoint, Long> {

    List<FitTrackPoint> findByFitFileUploadIdOrderBySequenceNumber(Long fitFileUploadId);

    @Query("SELECT tp FROM FitTrackPoint tp WHERE tp.fitFileUpload.id = :fitFileUploadId AND tp.positionLat IS NOT NULL AND tp.positionLong IS NOT NULL ORDER BY tp.sequenceNumber")
    List<FitTrackPoint> findByFitFileUploadIdWithGpsData(@Param("fitFileUploadId") Long fitFileUploadId);

    @Query("SELECT COUNT(tp) FROM FitTrackPoint tp WHERE tp.fitFileUpload.id = :fitFileUploadId")
    long countByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Query("SELECT tp FROM FitTrackPoint tp WHERE tp.fitFileUpload.id = :fitFileUploadId AND tp.heartRate IS NOT NULL ORDER BY tp.sequenceNumber")
    List<FitTrackPoint> findByFitFileUploadIdWithHeartRate(@Param("fitFileUploadId") Long fitFileUploadId);

    void deleteByFitFileUploadId(Long fitFileUploadId);
}