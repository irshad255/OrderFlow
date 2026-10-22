package com.orderflow.test;

import jakarta.persistence.EntityManager;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.orderflow.entity.Order;
import com.orderflow.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateOrderSaveInDatabaseAndRetrieveIt() throws Exception {

        // Arrange: Prepare a valid order request
        String requestJson = """
                {
                    "customerName": "Integration Test Customer",
                    "productName": "Biryani",
                    "quantity": 2,
                    "unitPrice": 500.00
                }
                """;

        // Act: Create an order through the real HTTP endpoint
        MvcResult postResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName")
                        .value("Integration Test Customer"))
                .andExpect(jsonPath("$.productName")
                        .value("Biryani"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(1000.0))
                .andReturn();

        // Assert: Read the generated ID from the API response
        JsonNode responseJson = objectMapper.readTree(
                postResult.getResponse().getContentAsString());

        long orderId = responseJson.get("id").asLong();

        entityManager.flush();
        entityManager.clear();
        // Assert: Verify that the order exists in PostgreSQL
        Optional<Order> savedOrder = orderRepository.findById(orderId);

        assertTrue(savedOrder.isPresent(),
                "Order should be saved in the database");

        assertEquals("Integration Test Customer",
                savedOrder.get().getCustomerName());

        assertEquals("Biryani",
                savedOrder.get().getProductName());

        // Act + Assert: Retrieve the same order through GET API
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerName")
                        .value("Integration Test Customer"))
                .andExpect(jsonPath("$.productName").value("Biryani"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(1000.0));
//                .andDo(print());
    }

    @Test
    void shouldDeleteOrderAndVerifyDatabaseRemoval() throws Exception {

        // Arrange: Create an order through the API
        String requestJson = """
            {
                "customerName": "Delete Test Customer",
                "productName": "Test Product",
                "quantity": 2,
                "unitPrice": 100.00
            }
            """;

        MvcResult result = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(
                result.getResponse().getContentAsString());

        long orderId = responseJson.get("id").asLong();

        // Assert: Verify order exists before deletion
        assertTrue(orderRepository.findById(orderId).isPresent());

        // Act: Delete order through the API
        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        // Assert: Verify order no longer exists
        assertFalse(orderRepository.findById(orderId).isPresent(),
                "Order should be removed from the database");
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {

        // Arrange
        long nonExistingOrderId = 999999L;

        // Act + Assert
        mockMvc.perform(get("/api/v1/orders/{id}", nonExistingOrderId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
 /*   @Test
    void shouldReturnAllOrdersFromDatabase() throws Exception {

        // Arrange: Create an order through the real API
        String req1 = """
            {
                "customerName": "Integration Test Customer",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        String req2 = """
            {
                "customerName": "Integration Test Customer",
                "productName": "Tahari",
                "quantity": 2,
                "unitPrice": 300.00
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req1))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req2))
                .andExpect(status().isCreated());

        // Act: Retrieve all orders
        mockMvc.perform(get("/api/v1/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))

                // First order
                .andExpect(jsonPath("$[0].customerName")
                        .value("Integration Test Customer"))
                .andExpect(jsonPath("$[0].productName")
                        .value("Biryani"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].totalPrice").value(1000.0))

                // Second order
                .andExpect(jsonPath("$[1].customerName")
                        .value("Integration Test Customer"))
                .andExpect(jsonPath("$[1].productName")
                        .value("Tahari"))
                .andExpect(jsonPath("$[1].quantity").value(2))
                .andExpect(jsonPath("$[1].totalPrice").value(600.0));
    }
*/

/*

    @Test
    void shouldReturnAllOrdersFromDatabase() throws Exception {

        // Arrange: Create two orders through the real API
        String req1 = """
            {
                "customerName": "Isolation Test Customer",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        String req2 = """
            {
                "customerName": "Isolation Test Customer",
                "productName": "Tahari",
                "quantity": 2,
                "unitPrice": 300.00
            }
            """;

        MvcResult result1 = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req1))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req2))
                .andExpect(status().isCreated())
                .andReturn();

        long biryaniId = objectMapper.readTree(
                        result1.getResponse().getContentAsString())
                .get("id").asLong();

        long tahariId = objectMapper.readTree(
                        result2.getResponse().getContentAsString())
                .get("id").asLong();

        // Act: Retrieve all orders
        MvcResult getResult = mockMvc.perform(get("/api/v1/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        // Assert: Verify our own orders, regardless of other records
        JsonNode orders = objectMapper.readTree(
                getResult.getResponse().getContentAsString());

        JsonNode biryaniOrder = null;
        JsonNode tahariOrder = null;

        for (JsonNode order : orders) {
            if (order.path("id").asLong(-1) == biryaniId) {
                biryaniOrder = order;
            }

            if (order.path("id").asLong(-1) == tahariId) {
                tahariOrder = order;
            }
        }

        assertNotNull(biryaniOrder, "Biryani order should appear in the response");
        assertNotNull(tahariOrder, "Tahari order should appear in the response");

        assertEquals("Biryani", biryaniOrder.get("productName").asString());
        assertEquals(2, biryaniOrder.get("quantity").asInt());
        assertEquals(1000.0, biryaniOrder.get("totalPrice").asDouble());

        assertEquals("Tahari", tahariOrder.get("productName").asString());
        assertEquals(2, tahariOrder.get("quantity").asInt());
        assertEquals(600.0, tahariOrder.get("totalPrice").asDouble());
    }

*/

    @Test
    void shouldReturnAllOrdersFromDatabase() throws Exception {

        // Arrange: Create two orders through the real API
        String req1 = """
        {
            "customerName": "Isolation Test Customer",
            "productName": "Biryani",
            "quantity": 2,
            "unitPrice": 500.00
        }
        """;

        String req2 = """
        {
            "customerName": "Isolation Test Customer",
            "productName": "Tahari",
            "quantity": 2,
            "unitPrice": 300.00
        }
        """;

        MvcResult result1 = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req1))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult result2 = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(req2))
                .andExpect(status().isCreated())
                .andReturn();

        long biryaniId = objectMapper.readTree(
                        result1.getResponse().getContentAsString())
                .get("id").asLong();

        long tahariId = objectMapper.readTree(
                        result2.getResponse().getContentAsString())
                .get("id").asLong();

        // Act: Retrieve the first page of orders
        MvcResult getResult = mockMvc.perform(get("/api/v1/orders"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andReturn();

        // Assert: Read orders from the paginated content array
        JsonNode response = objectMapper.readTree(
                getResult.getResponse().getContentAsString());

        JsonNode orders = response.path("content");

        JsonNode biryaniOrder = null;
        JsonNode tahariOrder = null;

        for (JsonNode order : orders) {
            if (order.path("id").asLong(-1) == biryaniId) {
                biryaniOrder = order;
            }

            if (order.path("id").asLong(-1) == tahariId) {
                tahariOrder = order;
            }
        }

        assertNotNull(
                biryaniOrder,
                "Biryani order should appear in the first page"
        );

        assertNotNull(
                tahariOrder,
                "Tahari order should appear in the first page"
        );

        assertEquals("Biryani", biryaniOrder.get("productName").asString());
        assertEquals(2, biryaniOrder.get("quantity").asInt());
        assertEquals(1000.0, biryaniOrder.get("totalPrice").asDouble());

        assertEquals("Tahari", tahariOrder.get("productName").asString());
        assertEquals(2, tahariOrder.get("quantity").asInt());
        assertEquals(600.0, tahariOrder.get("totalPrice").asDouble());
    }

    @Test
    void shouldUpdateOrderAndVerifyDatabaseChanges() throws Exception {

        // Arrange: Create an order through the real API
        String createRequest = """
            {
                "customerName": "Original Customer",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdOrder = objectMapper.readTree(
                createResult.getResponse().getContentAsString());

        long orderId = createdOrder.get("id").asLong();

        // Act: Update the existing order
        String updateRequest = """
            {
                "customerName": "Updated Customer",
                "productName": "Tahari",
                "quantity": 3,
                "unitPrice": 300.00
            }
            """;

        mockMvc.perform(
                        put("/api/v1/orders/{id}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.customerName").value("Updated Customer"))
                .andExpect(jsonPath("$.productName").value("Tahari"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(900.0));

        // Assert: Verify updated values in the database
        entityManager.flush();
        entityManager.clear();

        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Updated order not found"));

        assertEquals("Updated Customer", updatedOrder.getCustomerName());
        assertEquals("Tahari", updatedOrder.getProductName());
        assertEquals(3, updatedOrder.getQuantity());
        assertEquals(0, updatedOrder.getUnitPrice()
                .compareTo(new java.math.BigDecimal("300.00")));
    }


    @Test
    void shouldUpdateOrderStatusAndVerifyDatabase() throws Exception {

        // Arrange: Create an order
        String requestJson = """
            {
                "customerName": "Status Test Customer",
                "productName": "Biryani",
                "quantity": 2,
                "unitPrice": 500.00
            }
            """;

        MvcResult result = mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn();


        // extract the order id
        JsonNode responseJson = objectMapper.readTree(
                result.getResponse().getContentAsString());

        long orderId = responseJson.get("id").asLong();

        // Act: Change status from CREATED to CONFIRMED
        mockMvc.perform(
                        patch("/api/v1/orders/{id}/status", orderId)
                                .param("status", "CONFIRMED"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        // Assert: Verify status in database
        entityManager.flush();
        entityManager.clear();

        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AssertionError("Order not found"));

        assertEquals(
                com.orderflow.entity.OrderStatus.CONFIRMED,
                updatedOrder.getStatus()
        );
    }


    @Test
    void shouldReturnBadRequestWhenQuantityIsZero() throws Exception {

        // Arrange: Create an invalid order request
        String invalidRequest = """
            {
                "customerName": "Invalid Customer",
                "productName": "Biryani",
                "quantity": 0,
                "unitPrice": 500.00
            }
            """;

        // Act & Assert
        mockMvc.perform(
                        post("/api/v1/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingOrder() throws Exception {

        long nonExistingOrderId = 999999999L;

        String updateRequest = """
            {
                "customerName": "Updated Customer",
                "productName": "Tahari",
                "quantity": 3,
                "unitPrice": 300.00
            }
            """;

        mockMvc.perform(
                        put("/api/v1/orders/{id}", nonExistingOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateRequest))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingOrder() throws Exception {

        // Arrange
        long nonExistingOrderId = 999999999L;

        // Act & Assert
        mockMvc.perform(
                        delete("/api/v1/orders/{id}", nonExistingOrderId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldUseFallbackMessageWhenValidationMessageIsNull() throws Exception {

        org.springframework.validation.FieldError fieldError =
                new org.springframework.validation.FieldError(
                        "createOrderRequest",
                        "customerName",
                        null,
                        false,
                        null,
                        null,
                        null
                );

        org.springframework.validation.BeanPropertyBindingResult bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(),
                        "createOrderRequest"
                );

        bindingResult.addError(fieldError);

        org.springframework.web.bind.MethodArgumentNotValidException exception =
                new org.springframework.web.bind.MethodArgumentNotValidException(
                        null,
                        bindingResult
                );

        org.springframework.http.ResponseEntity<com.orderflow.exception.ApiError> response =
                new com.orderflow.exception.GlobalExceptionHandler()
                        .handleValidation(exception);

        assertEquals(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
    }





}