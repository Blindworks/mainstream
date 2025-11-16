package com.mainstream.subscription.entity;

import com.mainstream.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @NotNull(message = "Plan is required")
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    @NotBlank(message = "Currency is required")
    @Builder.Default
    private String currency = "EUR";

    @Column(name = "billing_first_name", nullable = false, length = 50)
    @NotBlank(message = "Billing first name is required")
    @Size(max = 50)
    private String billingFirstName;

    @Column(name = "billing_last_name", nullable = false, length = 50)
    @NotBlank(message = "Billing last name is required")
    @Size(max = 50)
    private String billingLastName;

    @Column(name = "billing_email", nullable = false, length = 255)
    @NotBlank(message = "Billing email is required")
    @Email(message = "Billing email should be valid")
    private String billingEmail;

    @Column(name = "billing_street", nullable = false, length = 255)
    @NotBlank(message = "Billing street is required")
    private String billingStreet;

    @Column(name = "billing_postal_code", nullable = false, length = 10)
    @NotBlank(message = "Billing postal code is required")
    private String billingPostalCode;

    @Column(name = "billing_city", nullable = false, length = 100)
    @NotBlank(message = "Billing city is required")
    private String billingCity;

    @Column(name = "billing_country", nullable = false, length = 2)
    @NotBlank(message = "Billing country is required")
    private String billingCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELED,
        REFUNDED
    }

    public enum PaymentMethod {
        CREDIT_CARD,
        PAYPAL,
        SEPA,
        BANK_TRANSFER
    }

    public String getBillingFullName() {
        return billingFirstName + " " + billingLastName;
    }

    public boolean isPaid() {
        return status == OrderStatus.COMPLETED;
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setOrder(this);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment);
        payment.setOrder(null);
    }
}
