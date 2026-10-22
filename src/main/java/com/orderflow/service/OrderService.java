
package com.orderflow.service;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.UpdateOrderRequest;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.repository.OrderRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
    public Page<OrderResponse> getAllOrders(
            Pageable pageable,
            OrderStatus status
    ) {
        Page<Order> orders;

        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::toResponse);
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

/*Is change se kya hua?
- Page<OrderResponse> order records ke saath pagination metadata bhi return karta hai.
- pageable decide karega ki kaunsa page, kitne records aur kis sorting order mein chahiye.
- status null hua toh saare statuses ke orders milenge; status diya toh sirf matching orders milenge.
- Page.map() entities ko OrderResponse mein convert karta hai, bina pagination metadata lose kiye.*/
