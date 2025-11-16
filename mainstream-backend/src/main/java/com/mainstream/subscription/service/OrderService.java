package com.mainstream.subscription.service;

import com.mainstream.email.service.EmailService;
import com.mainstream.subscription.dto.*;
import com.mainstream.subscription.entity.Order;
import com.mainstream.subscription.entity.Payment;
import com.mainstream.subscription.entity.SubscriptionPlan;
import com.mainstream.subscription.repository.OrderRepository;
import com.mainstream.subscription.repository.SubscriptionPlanRepository;
import com.mainstream.subscription.repository.SubscriptionRepository;
import com.mainstream.user.entity.User;
import com.mainstream.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;
    private final SubscriptionPlanService planService;
    private final EmailService emailService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final String DASHBOARD_URL = "http://localhost:4200/dashboard";

    @Transactional
    public OrderResponseDto createOrder(Long userId, CreateOrderRequestDto request) {
        log.info("Creating order for user {} with plan {}", userId, request.getPlanId());

        if (!request.getAcceptTerms()) {
            throw new IllegalArgumentException("Terms must be accepted to create an order");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found with id: " + request.getPlanId()));

        if (!plan.getIsActive()) {
            throw new IllegalArgumentException("Selected plan is not active");
        }

        String orderNumber = generateOrderNumber();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .plan(plan)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(plan.getPrice())
                .currency(plan.getCurrency())
                .billingFirstName(request.getBillingFirstName())
                .billingLastName(request.getBillingLastName())
                .billingEmail(request.getBillingEmail())
                .billingStreet(request.getBillingStreet())
                .billingPostalCode(request.getBillingPostalCode())
                .billingCity(request.getBillingCity())
                .billingCountry(request.getBillingCountry())
                .paymentMethod(request.getPaymentMethod())
                .build();

        order = orderRepository.save(order);
        log.info("Created order {} for user {}", orderNumber, userId);

        // Create and process payment
        PaymentDto paymentDto = paymentService.createPayment(order);
        paymentDto = paymentService.processPayment(paymentDto.getPaymentReference());

        SubscriptionDto subscriptionDto = null;

        if (paymentDto.getStatus() == Payment.PaymentStatus.COMPLETED) {
            // Update order status
            order.setStatus(Order.OrderStatus.COMPLETED);
            order = orderRepository.save(order);

            // Create subscription
            subscriptionDto = subscriptionService.createSubscription(userId, plan.getId());

            // Link subscription to order
            order.setSubscription(subscriptionRepository.findActiveSubscriptionByUserId(userId)
                    .orElse(null));

            log.info("Order {} completed successfully, subscription created for user {}", orderNumber, userId);

            // Send confirmation emails
            sendOrderConfirmationEmail(order, user, plan);
            sendPremiumActivatedEmail(user, plan, subscriptionDto);

            return OrderResponseDto.builder()
                    .success(true)
                    .message("Premium-Abonnement erfolgreich abgeschlossen!")
                    .order(toDto(order))
                    .subscription(subscriptionDto)
                    .build();
        } else {
            // Payment failed
            order.setStatus(Order.OrderStatus.FAILED);
            order = orderRepository.save(order);

            log.warn("Order {} failed due to payment failure", orderNumber);

            return OrderResponseDto.builder()
                    .success(false)
                    .message("Zahlung fehlgeschlagen. Bitte versuchen Sie es erneut.")
                    .order(toDto(order))
                    .subscription(null)
                    .build();
        }
    }

    public Optional<OrderDto> getOrderById(Long orderId) {
        log.debug("Fetching order with id: {}", orderId);
        return orderRepository.findById(orderId).map(this::toDto);
    }

    public Optional<OrderDto> getOrderByOrderNumber(String orderNumber) {
        log.debug("Fetching order with number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber).map(this::toDto);
    }

    public Page<OrderDto> getOrdersByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching orders for user: {}", userId);
        return orderRepository.findAllByUserId(userId, pageable).map(this::toDto);
    }

    public List<OrderDto> getAllOrdersByUserId(Long userId) {
        log.debug("Fetching all orders for user: {}", userId);
        return orderRepository.findAllByUserIdList(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        log.info("Canceling order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new IllegalStateException("Completed orders cannot be canceled");
        }

        order.setStatus(Order.OrderStatus.CANCELED);
        order = orderRepository.save(order);

        log.info("Order {} canceled", orderId);
        return toDto(order);
    }

    @Transactional
    public void cleanupStalePendingOrders() {
        log.info("Cleaning up stale pending orders");
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24);
        List<Order> staleOrders = orderRepository.findStalePendingOrders(cutoffDate);

        for (Order order : staleOrders) {
            log.info("Canceling stale order {}", order.getOrderNumber());
            order.setStatus(Order.OrderStatus.CANCELED);
            orderRepository.save(order);
        }

        log.info("Cleaned up {} stale pending orders", staleOrders.size());
    }

    private String generateOrderNumber() {
        String orderNumber;
        do {
            orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        } while (orderRepository.existsByOrderNumber(orderNumber));
        return orderNumber;
    }

    public OrderDto toDto(Order order) {
        List<PaymentDto> paymentDtos = order.getPayments().stream()
                .map(paymentService::toDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .subscriptionId(order.getSubscription() != null ? order.getSubscription().getId() : null)
                .plan(planService.toDto(order.getPlan()))
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .billingFirstName(order.getBillingFirstName())
                .billingLastName(order.getBillingLastName())
                .billingEmail(order.getBillingEmail())
                .billingStreet(order.getBillingStreet())
                .billingPostalCode(order.getBillingPostalCode())
                .billingCity(order.getBillingCity())
                .billingCountry(order.getBillingCountry())
                .paymentMethod(order.getPaymentMethod())
                .payments(paymentDtos)
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void sendOrderConfirmationEmail(Order order, User user, SubscriptionPlan plan) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", user.getFullName());
            variables.put("orderNumber", order.getOrderNumber());
            variables.put("orderDate", order.getCreatedAt().format(DATE_FORMATTER));
            variables.put("planName", plan.getName());
            variables.put("paymentMethod", formatPaymentMethod(order.getPaymentMethod()));
            variables.put("totalAmount", plan.getPrice().toString());
            variables.put("currency", plan.getCurrency());
            variables.put("billingFirstName", order.getBillingFirstName());
            variables.put("billingLastName", order.getBillingLastName());
            variables.put("billingStreet", order.getBillingStreet());
            variables.put("billingPostalCode", order.getBillingPostalCode());
            variables.put("billingCity", order.getBillingCity());
            variables.put("billingCountry", formatCountryCode(order.getBillingCountry()));
            variables.put("dashboardUrl", DASHBOARD_URL);

            emailService.sendTemplatedEmail(
                    order.getBillingEmail(),
                    "Bestellbestätigung - MainStream Premium #" + order.getOrderNumber(),
                    "order-confirmation",
                    variables
            );
            log.info("Order confirmation email sent to {}", order.getBillingEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}", order.getOrderNumber(), e);
        }
    }

    private void sendPremiumActivatedEmail(User user, SubscriptionPlan plan, SubscriptionDto subscription) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("username", user.getFullName());
            variables.put("planName", plan.getName());
            variables.put("price", plan.getPrice().toString());
            variables.put("currency", plan.getCurrency());
            variables.put("startDate", subscription.getStartDate().format(DATE_FORMATTER));
            variables.put("endDate", subscription.getEndDate() != null ?
                    subscription.getEndDate().format(DATE_FORMATTER) : "Unbegrenzt");
            variables.put("autoRenew", subscription.getAutoRenew() ? "Aktiv" : "Inaktiv");
            variables.put("dashboardUrl", DASHBOARD_URL);

            emailService.sendTemplatedEmail(
                    user.getEmail(),
                    "Willkommen bei MainStream Premium!",
                    "premium-activated",
                    variables
            );
            log.info("Premium activated email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send premium activated email for user {}", user.getId(), e);
        }
    }

    private String formatPaymentMethod(Order.PaymentMethod method) {
        return switch (method) {
            case CREDIT_CARD -> "Kreditkarte";
            case PAYPAL -> "PayPal";
            case SEPA -> "SEPA-Lastschrift";
            case BANK_TRANSFER -> "Banküberweisung";
        };
    }

    private String formatCountryCode(String code) {
        return switch (code) {
            case "DE" -> "Deutschland";
            case "AT" -> "Österreich";
            case "CH" -> "Schweiz";
            default -> code;
        };
    }
}
