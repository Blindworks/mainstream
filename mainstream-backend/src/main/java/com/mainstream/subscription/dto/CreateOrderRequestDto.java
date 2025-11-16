package com.mainstream.subscription.dto;

import com.mainstream.subscription.entity.Order;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDto {

    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String billingFirstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String billingLastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String billingEmail;

    @NotBlank(message = "Street is required")
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String billingStreet;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String billingPostalCode;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String billingCity;

    @NotBlank(message = "Country is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    private String billingCountry;

    @NotNull(message = "Payment method is required")
    private Order.PaymentMethod paymentMethod;

    @NotNull(message = "Terms acceptance is required")
    private Boolean acceptTerms;
}
