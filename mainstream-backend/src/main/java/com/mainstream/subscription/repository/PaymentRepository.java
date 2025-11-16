package com.mainstream.subscription.repository;

import com.mainstream.subscription.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findAllByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findAllByStatus(@Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' OR p.status = 'PROCESSING'")
    List<Payment> findAllPendingPayments();

    boolean existsByPaymentReference(String paymentReference);
}
