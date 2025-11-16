package com.mainstream.subscription.repository;

import com.mainstream.subscription.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findAllByUserIdList(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findAllByStatus(@Param("status") Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt < :cutoffDate")
    List<Order> findStalePendingOrders(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId AND o.status = 'COMPLETED'")
    long countCompletedOrdersByUserId(@Param("userId") Long userId);

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
