package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitLapData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitLapDataRepository extends JpaRepository<FitLapData, Long> {

    List<FitLapData> findByFitFileUploadIdOrderByLapNumber(Long fitFileUploadId);

    @Query("SELECT COUNT(ld) FROM FitLapData ld WHERE ld.fitFileUpload.id = :fitFileUploadId")
    long countByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Query("SELECT ld FROM FitLapData ld WHERE ld.fitFileUpload.id = :fitFileUploadId AND ld.sport = 'RUNNING' ORDER BY ld.lapNumber")
    List<FitLapData> findRunningLapsByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    void deleteByFitFileUploadId(Long fitFileUploadId);
}