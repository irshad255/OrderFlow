package com.orderflow.test;

import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.UpdateOrderRequest;
import com.orderflow.entity.Order;
import com.orderflow.entity.OrderStatus;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.repository.OrderRepository;
import com.orderflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldReturnOrderWhenOrderExists() {
        Order order = new Order(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals("Irshad", response.customerName());
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(999L)
        );
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertEquals("Irshad", response.customerName());
        assertEquals("Keyboard", response.productName());
        assertEquals(new BigDecimal("2000.00"), response.totalPrice());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldUpdateOrderSuccessfully() {
        Order existingOrder = new Order(
                "Irshad",
                "pizza",
                1,
                new BigDecimal("499.00")
        );

        UpdateOrderRequest request = new UpdateOrderRequest(
                "Irshad",
                "cheez pizza",
                2,
                new BigDecimal("500.00")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.updateOrder(1L, request);

        assertEquals("Irshad", response.customerName());
        assertEquals("cheez pizza", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("1000.00"), response.totalPrice());

        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();

        assertEquals("Irshad", capturedOrder.getCustomerName());
        assertEquals("cheez pizza", capturedOrder.getProductName());
        assertEquals(2, capturedOrder.getQuantity());
        assertEquals(
                new BigDecimal("500.00"),
                capturedOrder.getUnitPrice()
        );
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingOrder() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        UpdateOrderRequest request = new UpdateOrderRequest(
                "Irshad Hussain",
                "Mouse",
                3,
                new BigDecimal("500.00")
        );

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(999L, request)
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldDeleteOrderSuccessfully() {
        Order existingOrder = new Order(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(existingOrder);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingOrder() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.deleteOrder(999L)
        );

        verify(orderRepository, never())
                .delete(any(Order.class));
    }

    @Test
    void shouldReturnOrdersFilteredByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        OrderStatus status = OrderStatus.CREATED;

        Order order = new Order(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findByStatus(status, pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> result =
                orderService.getAllOrders(pageable, status);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Irshad", result.getContent().get(0).customerName());
        assertEquals("Keyboard", result.getContent().get(0).productName());

        verify(orderRepository).findByStatus(status, pageable);
        verify(orderRepository, never()).findAll(pageable);
    }

    @Test
    void shouldReturnAllOrdersWhenStatusIsNull() {
        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Order order = new Order(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        Page<Order> orderPage = new PageImpl<>(
                List.of(order),
                pageable,
                1
        );

        when(orderRepository.findAll(pageable))
                .thenReturn(orderPage);

        Page<OrderResponse> result =
                orderService.getAllOrders(pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Irshad", result.getContent().get(0).customerName());

        verify(orderRepository).findAll(pageable);
        verify(orderRepository, never())
                .findByStatus(any(OrderStatus.class), any(Pageable.class));
    }
}
