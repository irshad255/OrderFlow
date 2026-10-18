package com.orderflow.service;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.UpdateOrderRequest;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.repository.OrderRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(
                request.customerName(),
                request.productName(),
                request.quantity(),
                request.unitPrice()
        );

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderOrThrow(id);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateOrder(
            Long id,
            UpdateOrderRequest request
    ) {
        Order order = findOrderOrThrow(id);

        order.updateDetails(
                request.customerName(),
                request.productName(),
                request.quantity(),
                request.unitPrice()
        );

        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);
    }

    @Transactional
    public OrderResponse updateOrderStatus(
            Long id,
            OrderStatus status
    ) {
        Order order = findOrderOrThrow(id);
        order.updateStatus(status);

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = findOrderOrThrow(id);
        orderRepository.delete(order);
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponse toResponse(Order order) {
        BigDecimal totalPrice = order.getUnitPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()));

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getProductName(),
                order.getQuantity(),
                order.getUnitPrice(),
                totalPrice,
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}