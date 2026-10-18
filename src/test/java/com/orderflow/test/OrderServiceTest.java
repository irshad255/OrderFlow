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

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        // 1. Test ke liye sample order banao
        Order order = new Order(
                "Irshad",
                "Keyboard",
                2,
                new BigDecimal("1000.00")
        );

        // 2. Mock repository ka behaviour define karo
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        // 3. Service ko call karo ( here we are calling actual service method)
        OrderResponse response = orderService.getOrderById(1L);

        // 4. Result verify karo
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

        // mock repo behaviour configured over here
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // actual service call hui
        OrderResponse response = orderService.createOrder(request);

        // compare actual and expected values
        assertEquals("Irshad", response.customerName());
        assertEquals("Keyboard", response.productName());
        assertEquals(new BigDecimal("2000.00"), response.totalPrice());


        // it verifies whether the save method is called before or not
        verify(orderRepository).save(any(Order.class));

    }


   /* @Test
    public void shouldUpdateOrderSuccessfully()
    {

        // Arrange: Existing order
        Order existingOrder = new Order(
                "Irshad",
                "pizza",
                1,
                new BigDecimal(499.00)
        );

        UpdateOrderRequest request = new UpdateOrderRequest(
                "Irshad",
                "cheez pizza",
                2,
                new BigDecimal(500)
        );

        // Mock behaviour: Existing order mil jayega
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        // Mock behaviour: Save kiya hua Order hi return hoga
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation-> invocation.getArgument(0));


        // Act: Service method call
        OrderResponse response = orderService.updateOrder(1l, request);

        // Assert: Updated values check
        assertEquals("Irshad", response.customerName());
        assertEquals("cheez pizza", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("1000"), response.totalPrice());

        // Verify: Repository save call hua
        verify(orderRepository).save(any(Order.class));

    }*/


    @Test
    void shouldUpdateOrderSuccessfully() {

        // Arrange: Existing order
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

        // Mock: Existing order mil jayega
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(existingOrder));

        // Mock: Save kiya hua Order hi return hoga
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Service method call
        OrderResponse response = orderService.updateOrder(1L, request);

        // Assert: Response verify karo
        assertEquals("Irshad", response.customerName());
        assertEquals("cheez pizza", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("1000.00"), response.totalPrice());

        // Capture: save() ko pass hua Order object capture karo
        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();

        // Assert: Captured Order ki updated details verify karo
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

        // TODO 1: assertThrows() use karke
        // OrderNotFoundException verify karo
        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(999L, request)
        );

        // TODO 2: verify() use karke confirm karo
        // orderRepository.save() ek baar bhi call  hua tha ki nhi
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



}