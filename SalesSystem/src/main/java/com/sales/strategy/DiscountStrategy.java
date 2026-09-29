package com.sales.strategy; // package for the Strategy pattern

import java.math.BigDecimal; // exact money type

/** DESIGN PATTERN: Strategy - each discount rule is a swappable algorithm. */ // pattern note
public interface DiscountStrategy { // common contract for all discount rules
    BigDecimal calculate(BigDecimal subTotal); // returns the discount amount
    String label(); // text shown on the screen
} // end interface
