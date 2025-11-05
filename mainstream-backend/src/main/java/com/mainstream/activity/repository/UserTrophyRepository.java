package com.mainstream.activity.repository;

import com.mainstream.activity.entity.UserTrophy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTrophyRepository extends JpaRepository<UserTrophy, Long> {

    List<UserTrophy> findByUserIdOrderByEarnedAtDesc(Long userId);

    boolean existsByUserIdAndTrophyId(Long userId, Long trophyId);

    @Query("SELECT COUNT(ut) FROM UserTrophy ut WHERE ut.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ut) FROM UserTrophy ut WHERE ut.trophy.id = :trophyId")
    long countByTrophyId(@Param("trophyId") Long trophyId);

    @Query("SELECT ut FROM UserTrophy ut WHERE ut.trophy.id = :trophyId ORDER BY ut.earnedAt ASC")
    List<UserTrophy> findByTrophyIdOrderByEarnedAtAsc(@Param("trophyId") Long trophyId);

    @Query("SELECT ut FROM UserTrophy ut WHERE ut.activity.id = :activityId AND ut.user.id = :userId ORDER BY ut.earnedAt DESC")
    List<UserTrophy> findByActivityIdAndUserIdOrderByEarnedAtDesc(@Param("activityId") Long activityId, @Param("userId") Long userId);
}
