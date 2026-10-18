package com.orderflow.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderCalculationTest {

    @Test // this tells the junit that this is test method
    void shouldCalculateTotalPrice() {
        int quantity = 3;
        int unitPrice = 500;

        int actual = quantity * unitPrice; // actual results given by code
        int expected = 1500; // expected result

        assertEquals(expected, actual); // it compares both are equal or not
    }
}
