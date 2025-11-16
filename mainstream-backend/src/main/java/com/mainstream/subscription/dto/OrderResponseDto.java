package com.mainstream.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {

    private boolean success;
    private String message;
    private OrderDto order;
    private SubscriptionDto subscription;
}
