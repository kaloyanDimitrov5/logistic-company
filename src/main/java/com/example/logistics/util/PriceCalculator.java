package com.example.logistics.util;

import com.example.logistics.domain.type.DeliveryType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceCalculator {
    private PriceCalculator() {}

    public static BigDecimal calculate(BigDecimal weightKg, DeliveryType type) {
        BigDecimal base;
        if (weightKg.compareTo(BigDecimal.valueOf(1)) <= 0) {
            base = new BigDecimal("5.00");
        } else if (weightKg.compareTo(BigDecimal.valueOf(5)) <= 0) {
            base = new BigDecimal("8.00");
        } else if (weightKg.compareTo(BigDecimal.valueOf(10)) <= 0) {
            base = new BigDecimal("12.00");
        } else {
            base = new BigDecimal("12.00")
                    .add(weightKg.subtract(BigDecimal.TEN).multiply(new BigDecimal("1.00")));
        }
        if (type == DeliveryType.TO_OFFICE) {
            base = base.multiply(new BigDecimal("0.80")); // 20% cheaper to office
        }
        return base.setScale(2, RoundingMode.HALF_UP);
    }
}