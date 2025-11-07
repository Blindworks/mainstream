package com.mainstream.activity.repository;

import com.mainstream.activity.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByUserIdOrderByActivityStartTimeDesc(Long userId);

    List<UserActivity> findByUserIdAndActivityStartTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<UserActivity> findByFitFileUploadId(Long fitFileUploadId);

    Optional<UserActivity> findByRunId(Long runId);

    @Query("SELECT COUNT(DISTINCT ua.matchedRoute.id) FROM UserActivity ua WHERE ua.user.id = :userId AND ua.matchedRoute IS NOT NULL")
    long countDistinctRoutesForUser(@Param("userId") Long userId);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId AND ua.activityStartTime >= :startDate ORDER BY ua.activityStartTime ASC")
    List<UserActivity> findUserActivitiesSince(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT ua FROM UserActivity ua WHERE DATE(ua.activityStartTime) = CURRENT_DATE ORDER BY ua.activityStartTime ASC")
    List<UserActivity> findTodaysActivities();

    @Query("SELECT SUM(ua.distanceMeters) FROM UserActivity ua WHERE ua.user.id = :userId")
    Long getTotalDistanceForUser(@Param("userId") Long userId);

    // Route statistics queries
    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.matchedRoute.id = :routeId AND ua.activityStartTime >= :startTime")
    long countByRouteAndTimeRange(@Param("routeId") Long routeId, @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.matchedRoute.id = :routeId")
    long countByRoute(@Param("routeId") Long routeId);
}
