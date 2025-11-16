package com.mainstream.subscription.dto;

import com.mainstream.subscription.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;
    private String orderNumber;
    private Long userId;
    private Long subscriptionId;
    private SubscriptionPlanDto plan;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String currency;
    private String billingFirstName;
    private String billingLastName;
    private String billingEmail;
    private String billingStreet;
    private String billingPostalCode;
    private String billingCity;
    private String billingCountry;
    private Order.PaymentMethod paymentMethod;
    private List<PaymentDto> payments;
    private LocalDateTime createdAt;
}
