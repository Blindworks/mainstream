package com.mainstream.subscription.service;

import com.mainstream.subscription.dto.PaymentDto;
import com.mainstream.subscription.entity.Order;
import com.mainstream.subscription.entity.Payment;
import com.mainstream.subscription.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentDto createPayment(Order order) {
        log.info("Creating payment for order {}", order.getOrderNumber());

        String paymentReference = generatePaymentReference();

        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
                .order(order)
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(order.getPaymentMethod())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Created payment {} for order {}", paymentReference, order.getOrderNumber());

        return toDto(payment);
    }

    @Transactional
    public PaymentDto processPayment(String paymentReference) {
        log.info("Processing payment {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentReference));

        payment.setStatus(Payment.PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // Simulate payment processing (in production, this would call external payment provider)
        boolean paymentSuccessful = simulatePaymentProcessing(payment);

        if (paymentSuccessful) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setProvider("INTERNAL");
            payment.setProviderTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
            payment.setProviderResponse("{\"status\": \"success\", \"message\": \"Payment processed successfully\"}");
            log.info("Payment {} completed successfully", paymentReference);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setProcessedAt(LocalDateTime.now());
            payment.setProviderResponse("{\"status\": \"failed\", \"message\": \"Payment processing failed\"}");
            log.warn("Payment {} failed", paymentReference);
        }

        payment = paymentRepository.save(payment);
        return toDto(payment);
    }

    public Optional<PaymentDto> getPaymentByReference(String paymentReference) {
        log.debug("Fetching payment by reference: {}", paymentReference);
        return paymentRepository.findByPaymentReference(paymentReference).map(this::toDto);
    }

    public List<PaymentDto> getPaymentsByOrderId(Long orderId) {
        log.debug("Fetching payments for order: {}", orderId);
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<PaymentDto> getPaymentsByUserId(Long userId) {
        log.debug("Fetching payments for user: {}", userId);
        return paymentRepository.findAllByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentDto refundPayment(String paymentReference) {
        log.info("Refunding payment {}", paymentReference);

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentReference));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setProviderResponse(payment.getProviderResponse() + ", {\"refund\": \"processed\"}");
        payment = paymentRepository.save(payment);

        log.info("Payment {} refunded", paymentReference);
        return toDto(payment);
    }

    private String generatePaymentReference() {
        String reference;
        do {
            reference = "PAY-" + UUID.randomUUID().toString().substring(0, 16).toUpperCase();
        } while (paymentRepository.existsByPaymentReference(reference));
        return reference;
    }

    private boolean simulatePaymentProcessing(Payment payment) {
        // In production, this would integrate with Stripe, PayPal, or other payment providers
        // For now, we simulate a successful payment
        log.debug("Simulating payment processing for {} (amount: {} {})",
                payment.getPaymentReference(), payment.getAmount(), payment.getCurrency());

        // Simulate some processing time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate 95% success rate
        return Math.random() > 0.05;
    }

    public PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .providerTransactionId(payment.getProviderTransactionId())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
