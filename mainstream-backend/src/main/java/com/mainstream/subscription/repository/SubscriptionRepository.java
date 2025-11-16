package com.mainstream.subscription.repository;

import com.mainstream.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Subscription> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status")
    List<Subscription> findAllByStatus(@Param("status") Subscription.SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate IS NOT NULL AND s.endDate < :date")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.autoRenew = true AND s.endDate IS NOT NULL AND s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findSubscriptionsToRenew(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    boolean hasActiveSubscription(@Param("userId") Long userId);

    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.plan.price > 0")
    boolean hasPremiumSubscription(@Param("userId") Long userId);
}
