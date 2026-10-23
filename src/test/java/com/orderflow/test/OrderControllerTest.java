
package com.orderflow.test;

import com.orderflow.controller.OrderController;
import com.orderflow.dto.CreateOrderRequest;
import com.orderflow.dto.OrderResponse;
import com.orderflow.dto.UpdateOrderRequest;
import com.orderflow.entity.OrderStatus;
import com.orderflow.exception.OrderNotFoundException;
import com.orderflow.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    // 1. Blank customer name
    @Test
    void shouldReturnBadRequestWhenCustomerNameIsBlank()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": 500.00
                }
                """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.customerName").exists());

        verifyNoInteractions(orderService);
    }

    // 2. Blank product name
    @Test
    void shouldReturnBadRequestWhenProductNameIsBlank()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "   ",
                    "quantity": 2,
                    "unitPrice": 500.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 3. Customer name exceeds 100 characters
    @Test
    void shouldReturnBadRequestWhenCustomerNameExceedsMaxLength()
            throws Exception {

        String longCustomerName = "a".repeat(101);
        String invalidJson = """
                {
                    "customerName": "%s",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": 500.00
                }
                """.formatted(longCustomerName);

        assertBadRequest(invalidJson);
    }

    // 4. Product name exceeds 150 characters
    @Test
    void shouldReturnBadRequestWhenProductNameExceedsMaxLength()
            throws Exception {

        String longProductName = "a".repeat(151);
        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "%s",
                    "quantity": 2,
                    "unitPrice": 500.00
                }
                """.formatted(longProductName);

        assertBadRequest(invalidJson);
    }

    // 5. Quantity is missing
    @Test
    void shouldReturnBadRequestWhenQuantityIsMissing()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "unitPrice": 500.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 6. Quantity is zero
    @Test
    void shouldReturnBadRequestWhenQuantityIsZero()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 0,
                    "unitPrice": 500.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 7. Quantity is negative
    @Test
    void shouldReturnBadRequestWhenQuantityIsNegative()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": -1,
                    "unitPrice": 500.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 8. Quantity exceeds maximum 100000
    @Test
    void shouldReturnBadRequestWhenQuantityExceedsMaximum()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 100001,
                    "unitPrice": 500.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 9. Unit price is missing
    @Test
    void shouldReturnBadRequestWhenUnitPriceIsMissing()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 2
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 10. Unit price is null
    @Test
    void shouldReturnBadRequestWhenUnitPriceIsNull()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": null
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 11. Unit price is zero
    @Test
    void shouldReturnBadRequestWhenUnitPriceIsZero()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": 0.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 12. Unit price is negative
    @Test
    void shouldReturnBadRequestWhenUnitPriceIsNegative()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": -50.00
                }
                """;

        assertBadRequest(invalidJson);
    }

    // 13. Unit price has more than 2 decimal places
    @Test
    void shouldReturnBadRequestWhenUnitPriceHasMoreThanTwoDecimalPlaces()
            throws Exception {

        String invalidJson = """
                {
                    "customerName": "Irshad",
                    "productName": "Keyboard",
                    "quantity": 2,
                    "unitPrice": 12.9999
                }
                """;

        assertBadRequest(invalidJson);
    }

    // Shared helper for invalid requests
    private void assertBadRequest(String invalidJson) throws Exception {

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldCreateOrderSuccessfully() throws Exception {

        OrderResponse response = new OrderResponse(
                1L,
                "Irshad",
                "Biryani",
                2,
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(response);

        String validJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Irshad"))
                .andExpect(jsonPath("$.productName").value("Biryani"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(1000.00));

        verify(orderService).createOrder(any(CreateOrderRequest.class));
    }

    @Test
    public void shouldReturnBadRequestWhenUpdatingOrderWithBlankProductName() throws Exception{

        String validJson = """
            {
                "customerName": "Irshad",
                "productName": "",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType("application/json")
                        .content(validJson))
                .andExpect(status().isBadRequest());

        verifyNoMoreInteractions(orderService);

    }


    @Test
    void shouldReturnBadRequestWhenUpdatingOrderWithNegativeQuantity()
            throws Exception {

        String invalidJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": -3,
                "unitPrice": 500.00
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldReturnBadRequestWhenUpdatingOrderWithZeroUnitPrice()
            throws Exception {

        String invalidJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": 10,
                "unitPrice": 0.00
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldReturnBadRequestWhenUpdatingOrderWithNullUnitPrice()
            throws Exception {

        String invalidJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": 10,
                "unitPrice":null
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldReturnUpdatedOrderWhenSuccess()
            throws Exception {

        UpdateOrderRequest request= new UpdateOrderRequest("Irshad", "Biryani",
                10, new BigDecimal(10.00));



        OrderResponse response = new OrderResponse(
                1L,
                request.customerName(),
                request.productName(),
                request.quantity(),
                request.unitPrice(),
                request.unitPrice().multiply(
                        BigDecimal.valueOf(request.quantity())
                ),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()



        );

        when(orderService.updateOrder(
                eq(1L),
                any(UpdateOrderRequest.class)
        )).thenReturn(response);

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "customerName": "Irshad",
                    "productName": "Biryani",
                    "quantity": 10,
                    "unitPrice": 10.00
                }
                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Irshad"))
                .andExpect(jsonPath("$.productName").value("Biryani"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.unitPrice").value(10.00))
                .andExpect(jsonPath("$.totalPrice").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).updateOrder(
                eq(1L),
                any(UpdateOrderRequest.class)
        );
    }

    @Test
    public void shouldReturnOrderWhenOrderExists() throws Exception
    {
        OrderResponse response = new OrderResponse(
                1L,
                "Irshad",
                "Biryani",
                2,
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        //Mock getOrderById(1L) to return response
        when(orderService.getOrderById(1L))
                .thenReturn(response);

        //Use mockMvc.perform(get(...))
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Irshad"))
                .andExpect(jsonPath("$.totalPrice").value(1000.00));


        //Verify service was called with 1L
        verify(orderService).getOrderById(1L);

    }



    @Test
    void shouldReturnOrdersWithPaginationMetadata() throws Exception {

        OrderResponse response1 = new OrderResponse(
                2L,
                "Alex",
                "Biryani",
                2,
                new BigDecimal("200.00"),
                new BigDecimal("400.00"),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        OrderResponse response2 = new OrderResponse(
                1L,
                "Irshad",
                "Biryani",
                2,
                new BigDecimal("500.00"),
                new BigDecimal("1000.00"),
                OrderStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        List<OrderResponse> responseList = List.of(
                response1,
                response2
        );

        Pageable pageable = PageRequest.of(
                0,
                10,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<OrderResponse> responsePage = new PageImpl<>(
                responseList,
                pageable,
                2
        );

        when(orderService.getAllOrders(
                any(Pageable.class),
                isNull()
        )).thenReturn(responsePage);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].customerName").value("Alex"))
                .andExpect(jsonPath("$.content[1].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));

        verify(orderService).getAllOrders(
                any(Pageable.class),
                isNull()
        );
    }


    @Test
    public void shouldReturnOrderStatus() throws Exception {

        // 1. Define the status we want to set for this order
        OrderStatus created = OrderStatus.CREATED;

        // 2. Create the response that our mocked service will return
        OrderResponse response = new OrderResponse(
                1L,                         // Order ID
                "Irshad",                   // Customer name
                "Biryani",                  // Product name
                2,                          // Quantity
                new BigDecimal("500.00"),   // Unit price
                new BigDecimal("1000.00"),  // Total price: 500 * 2
                created,                    // Expected order status
                LocalDateTime.now(),        // Creation timestamp
                LocalDateTime.now()         // Last updated timestamp
        );

        // 3. Configure Mockito:
        // When updateOrderStatus() is called with ID 1 and CREATED,
        // return the response object created above
        when(orderService.updateOrderStatus(1L, created))
                .thenReturn(response);

        // 4. Send a PATCH request to update order 1's status
        // .param() sends the status query parameter
        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", created.name()))

                // 5. Verify the API returns HTTP 200 OK
                .andExpect(status().isOk())

                // 6. Print request and response details for debugging
                .andDo(print())

                // 7. Verify the returned order ID
                .andExpect(jsonPath("$.id").value(1))

                // 8. Verify the returned status
                .andExpect(jsonPath("$.status").value("CREATED"));

        // 9. Verify the service was called with the expected ID and status
        verify(orderService).updateOrderStatus(
                eq(1L),
                eq(OrderStatus.CREATED)
        );
    }

    @Test
    public void shouldDeleteOrderSuccessfully() throws Exception
    {

        //when(...).thenReturn(...) [its used to return value when method call so in delete method its
        // return type is void so we dont required this here

        mockMvc.perform(delete("/api/v1/orders/1"))
                .andExpect(status().isNoContent())
                .andDo(print());


        verify(orderService).deleteOrder(eq(1L));

    }

    @Test
    public void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception
    {

        when(orderService.getOrderById(999L))
                .thenThrow(new OrderNotFoundException(999L));


        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found with id: 999"))
                .andExpect(jsonPath("$.validationErrors").isEmpty());

        //Yeh verify karega ki controller ne service ko 999L ID ke saath call kiya.
        verify(orderService).getOrderById(
                eq(999L)
        );
    }


    @Test
    void shouldRejectNegativePageNumber() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectPageSizeZero() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectPageSizeGreaterThanMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("size", "51"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("sortBy", "invalidField"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldRejectInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("direction", "sideways"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldAcceptValidPaginationSortingAndStatusParameters() throws Exception {
        Pageable pageable = PageRequest.of(
                1,
                5,
                Sort.by(Sort.Direction.ASC, "customerName")
        );

        Page<OrderResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(orderService.getAllOrders(any(Pageable.class), eq(OrderStatus.CREATED)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "customerName")
                        .param("direction", "asc")
                        .param("status", "CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(orderService).getAllOrders(any(Pageable.class), eq(OrderStatus.CREATED));
    }



    @Test
    void shouldReturnStandardErrorBodyForPaginationValidation() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("size", "51"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 50"))
                .andExpect(jsonPath("$.validationErrors").isEmpty());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturnStandardErrorBodyForInvalidStatusParameter() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter: status"))
                .andExpect(jsonPath("$.validationErrors").isEmpty());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturnStandardErrorBodyForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed or invalid request body"))
                .andExpect(jsonPath("$.validationErrors").isEmpty());

        verifyNoInteractions(orderService);
    }


    @Test
    void shouldReturnBadRequestWhenUpdatingOrderWithNegativeUnitPrice()
            throws Exception {

        String invalidJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": -50.00
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingOrderWithZeroQuantity()
            throws Exception {

        String invalidJson = """
            {
                "customerName": "Irshad",
                "productName": "Biryani",
                "quantity": 0,
                "unitPrice": 500.00
            }
            """;

        mockMvc.perform(put("/api/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }



}