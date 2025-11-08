package com.mainstream.activity.repository;

import com.mainstream.activity.entity.Trophy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrophyRepository extends JpaRepository<Trophy, Long> {

    Optional<Trophy> findByCode(String code);

    List<Trophy> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Trophy> findByTypeAndIsActiveTrue(Trophy.TrophyType type);

    /**
     * Find all active location-based trophies that are currently valid.
     * A trophy is valid if:
     * - It's active (isActive = true)
     * - It's a LOCATION_BASED type
     * - It has location data (latitude, longitude, collectionRadiusMeters)
     * - The current time is within validFrom and validUntil (if specified)
     */
    @Query("SELECT t FROM Trophy t WHERE t.isActive = true " +
           "AND t.type = 'LOCATION_BASED' " +
           "AND t.latitude IS NOT NULL " +
           "AND t.longitude IS NOT NULL " +
           "AND t.collectionRadiusMeters IS NOT NULL " +
           "AND (t.validFrom IS NULL OR t.validFrom <= :now) " +
           "AND (t.validUntil IS NULL OR t.validUntil >= :now)")
    List<Trophy> findActiveLocationBasedTrophies(@Param("now") LocalDateTime now);
}
