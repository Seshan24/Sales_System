package com.sales.strategy; // package for the Strategy pattern

import java.math.BigDecimal; // exact money type
import java.math.RoundingMode; // rounding rule

/** DESIGN PATTERN: Factory - chooses the right DiscountStrategy for the customer and basket. */ // pattern note
public final class DiscountFactory { // factory class
    private static final BigDecimal BULK_LIMIT = new BigDecimal("50000"); // retail bulk threshold

    private DiscountFactory() { } // utility class

    public static DiscountStrategy forCustomer(String customerType, BigDecimal subTotal) { // pick a strategy
        if ("WHOLESALE".equals(customerType)) { // wholesale customers
            return new Percent("Wholesale 5%", "0.05"); // 5 percent off
        } // end if
        if (subTotal.compareTo(BULK_LIMIT) >= 0) { // big retail basket
            return new Percent("Bulk purchase 10%", "0.10"); // 10 percent off
        } // end if
        return new Percent("No discount", "0"); // otherwise nothing
    } // end forCustomer

    private static final class Percent implements DiscountStrategy { // one concrete strategy driven by a rate
        private final String label; // description
        private final BigDecimal rate; // percentage as a fraction

        Percent(String label, String rate) { // constructor
            this.label = label; // store description
            this.rate = new BigDecimal(rate); // store rate
        } // end constructor

        @Override public BigDecimal calculate(BigDecimal subTotal) { // discount amount
            return subTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP); // sub total x rate
        } // end calculate

        @Override public String label() { return label; } // description getter
    } // end Percent
} // end class
