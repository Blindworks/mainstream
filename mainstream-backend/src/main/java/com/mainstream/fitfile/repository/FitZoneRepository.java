package com.mainstream.fitfile.repository;

import com.mainstream.fitfile.entity.FitZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FitZoneRepository extends JpaRepository<FitZone, Long> {

    List<FitZone> findByFitFileUploadId(Long fitFileUploadId);

    List<FitZone> findByFitFileUploadIdAndZoneType(Long fitFileUploadId, FitZone.ZoneType zoneType);

    @Query("SELECT z FROM FitZone z WHERE z.fitFileUpload.id = :fitFileUploadId AND z.zoneType = :zoneType ORDER BY z.zoneNumber ASC")
    List<FitZone> findByFitFileUploadIdAndZoneTypeOrderByZoneNumber(
        @Param("fitFileUploadId") Long fitFileUploadId, 
        @Param("zoneType") FitZone.ZoneType zoneType);

    @Query("SELECT z FROM FitZone z WHERE z.fitFileUpload.id = :fitFileUploadId AND z.zoneType = :zoneType AND z.zoneNumber = :zoneNumber")
    FitZone findByFitFileUploadIdAndZoneTypeAndZoneNumber(
        @Param("fitFileUploadId") Long fitFileUploadId,
        @Param("zoneType") FitZone.ZoneType zoneType,
        @Param("zoneNumber") Integer zoneNumber);

    @Modifying
    @Query("DELETE FROM FitZone z WHERE z.fitFileUpload.id = :fitFileUploadId")
    void deleteByFitFileUploadId(@Param("fitFileUploadId") Long fitFileUploadId);

    @Modifying
    @Query("DELETE FROM FitZone z WHERE z.fitFileUpload.id = :fitFileUploadId AND z.zoneType = :zoneType")
    void deleteByFitFileUploadIdAndZoneType(@Param("fitFileUploadId") Long fitFileUploadId, 
                                           @Param("zoneType") FitZone.ZoneType zoneType);
}