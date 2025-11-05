package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitDeviceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitDeviceInfoRepository extends JpaRepository<FitDeviceInfo, Long> {

    List<FitDeviceInfo> findByFitFileUploadId(Long fitFileUploadId);

    List<FitDeviceInfo> findByFitFileUploadIdOrderByDeviceIndexAsc(Long fitFileUploadId);

    @Query("SELECT d FROM FitDeviceInfo d WHERE d.fitFileUpload.id = :fitFileUploadId AND d.deviceIndex = 0")
    FitDeviceInfo findPrimaryDeviceByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Query("SELECT COUNT(d) FROM FitDeviceInfo d WHERE d.fitFileUpload.id = :fitFileUploadId")
    long countDevicesByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Modifying
    @Query("DELETE FROM FitDeviceInfo d WHERE d.fitFileUpload.id = :fitFileUploadId")
    void deleteByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);
}