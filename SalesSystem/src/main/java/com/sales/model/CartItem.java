package com.sales.model; // package for data objects

import java.math.BigDecimal; // exact money type

public class CartItem { // one line of the sale being built
    private final Product product; // the product
    private int qty; // quantity (can grow when the same product is added again)

    public CartItem(Product product, int qty) { // constructor
        this.product = product; // store product
        this.qty = qty; // store quantity
    } // end constructor

    public Product getProduct() { return product; } // product getter
    public int getQty() { return qty; } // quantity getter
    public void addQty(int extra) { this.qty += extra; } // increase quantity
    public BigDecimal getLineTotal() { return product.getUnitPrice().multiply(BigDecimal.valueOf(qty)); } // price x quantity
} // end class
