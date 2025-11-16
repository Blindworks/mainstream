package com.mainstream.subscription.dto;

import com.mainstream.subscription.entity.Order;
import com.mainstream.subscription.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {

    private Long id;
    private String paymentReference;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private Order.PaymentMethod paymentMethod;
    private String provider;
    private String providerTransactionId;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
