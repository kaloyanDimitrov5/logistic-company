package com.example.logistics.util;

import com.example.logistics.domain.type.DeliveryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculatorTest {

    @Test
    void officeIsCheaperThanAddressForSameWeight() {
        BigDecimal address = PriceCalculator.calculate(new BigDecimal("2.00"), DeliveryType.TO_ADDRESS);
        BigDecimal office  = PriceCalculator.calculate(new BigDecimal("2.00"), DeliveryType.TO_OFFICE);
        assertTrue(office.compareTo(address) < 0, "OFFICE should be cheaper than ADDRESS");
    }

    @Test
    void bracketsWorkForUnder1kg() {
        BigDecimal price = PriceCalculator.calculate(new BigDecimal("0.50"), DeliveryType.TO_ADDRESS);
        assertEquals(new BigDecimal("5.00"), price);
    }

    @Test
    void over10kgAddsPerKg() {
        // base 12.00 + (15 - 10) * 1.00 = 17.00; OFFICE 20% cheaper -> 13.60
        BigDecimal price = PriceCalculator.calculate(new BigDecimal("15.00"), DeliveryType.TO_OFFICE);
        assertEquals(new BigDecimal("13.60"), price);
    }
}