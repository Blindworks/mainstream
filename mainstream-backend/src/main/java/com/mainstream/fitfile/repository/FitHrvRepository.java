package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitHrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FitHrvRepository extends JpaRepository<FitHrv, Long> {

    List<FitHrv> findByFitFileUploadId(Long fitFileUploadId);

    List<FitHrv> findByFitFileUploadIdOrderByTimestampAsc(Long fitFileUploadId);

    @Query("SELECT h FROM FitHrv h WHERE h.fitFileUpload.id = :fitFileUploadId AND h.timestamp BETWEEN :startTime AND :endTime ORDER BY h.timestamp ASC")
    List<FitHrv> findByFitFileUploadIdAndTimestampBetween(
        @Param("fitFileUploadId") Long fitFileUploadId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(h) FROM FitHrv h WHERE h.fitFileUpload.id = :fitFileUploadId")
    long countByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Modifying
    @Query("DELETE FROM FitHrv h WHERE h.fitFileUpload.id = :fitFileUploadId")
    void deleteByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);
}