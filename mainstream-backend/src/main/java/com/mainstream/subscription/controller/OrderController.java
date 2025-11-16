package com.mainstream.subscription.controller;

import com.mainstream.subscription.dto.CreateOrderRequestDto;
import com.mainstream.subscription.dto.OrderDto;
import com.mainstream.subscription.dto.OrderResponseDto;
import com.mainstream.subscription.service.OrderService;
import com.mainstream.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody CreateOrderRequestDto request) {
        log.info("Creating order for user: {}", email);

        Long userId = getUserIdByEmail(email);
        OrderResponseDto response = orderService.createOrder(userId, request);

        if (response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getUserOrders(
            @RequestHeader("X-User-Email") String email,
            Pageable pageable) {
        log.debug("Getting orders for user: {}", email);

        Long userId = getUserIdByEmail(email);
        Page<OrderDto> orders = orderService.getOrdersByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllUserOrders(@RequestHeader("X-User-Email") String email) {
        log.debug("Getting all orders for user: {}", email);

        Long userId = getUserIdByEmail(email);
        List<OrderDto> orders = orderService.getAllOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long orderId) {
        log.debug("Getting order {} for user: {}", orderId, email);

        Long userId = getUserIdByEmail(email);
        OrderDto order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/by-number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(
            @RequestHeader("X-User-Email") String email,
            @PathVariable String orderNumber) {
        log.debug("Getting order {} for user: {}", orderNumber, email);

        Long userId = getUserIdByEmail(email);
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with number: " + orderNumber));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @RequestHeader("X-User-Email") String email,
            @PathVariable Long orderId) {
        log.info("Canceling order {} for user: {}", orderId, email);

        Long userId = getUserIdByEmail(email);
        OrderDto order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        // Verify order belongs to user
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        OrderDto canceledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(canceledOrder);
    }

    private Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email))
                .getId();
    }
}
