package com.orderflow.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateOrderRequest(

        @NotBlank
        @Size(max = 100)
        String customerName,

        @NotBlank
        @Size(max = 150)
        String productName,

        @NotNull
        @Min(1)
        @Max(100000)
        Integer quantity,

        @NotNull
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal unitPrice

) {
}