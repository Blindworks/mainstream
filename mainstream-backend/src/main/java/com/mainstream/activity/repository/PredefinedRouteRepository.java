package com.mainstream.activity.repository;

import com.mainstream.activity.entity.PredefinedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredefinedRouteRepository extends JpaRepository<PredefinedRoute, Long> {

    List<PredefinedRoute> findByIsActiveTrue();

    Optional<PredefinedRoute> findByName(String name);

    boolean existsByName(String name);

    /**
     * Find all routes with trackPoints eagerly loaded to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT r FROM PredefinedRoute r LEFT JOIN FETCH r.trackPoints")
    List<PredefinedRoute> findAllWithTrackPoints();

    /**
     * Find active routes with trackPoints eagerly loaded to avoid LazyInitializationException.
     */
    @Query("SELECT DISTINCT r FROM PredefinedRoute r LEFT JOIN FETCH r.trackPoints WHERE r.isActive = true")
    List<PredefinedRoute> findByIsActiveTrueWithTrackPoints();

    /**
     * Find a route by ID with trackPoints eagerly loaded to avoid LazyInitializationException.
     */
    @Query("SELECT r FROM PredefinedRoute r LEFT JOIN FETCH r.trackPoints WHERE r.id = :id")
    Optional<PredefinedRoute> findByIdWithTrackPoints(@Param("id") Long id);
}
