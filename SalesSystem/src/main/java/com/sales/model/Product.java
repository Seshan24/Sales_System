package com.sales.model; // package for data objects

import java.math.BigDecimal; // exact money type

public class Product { // one product row
    private final int id; // primary key
    private final String code; // business code
    private final String name; // display name
    private final int categoryId; // category key
    private final String categoryName; // category text
    private final BigDecimal unitPrice; // selling price
    private final BigDecimal costPrice; // purchase cost
    private final int stockQty; // quantity on hand
    private final int reorderLevel; // restock threshold

    public Product(int id, String code, String name, int categoryId, String categoryName, // constructor sets every field
                   BigDecimal unitPrice, BigDecimal costPrice, int stockQty, int reorderLevel) { // rest of the parameters
        this.id = id; // store id
        this.code = code; // store code
        this.name = name; // store name
        this.categoryId = categoryId; // store category key
        this.categoryName = categoryName; // store category text
        this.unitPrice = unitPrice; // store price
        this.costPrice = costPrice; // store cost
        this.stockQty = stockQty; // store stock
        this.reorderLevel = reorderLevel; // store reorder level
    } // end constructor

    public int getId() { return id; } // id getter
    public String getCode() { return code; } // code getter
    public String getName() { return name; } // name getter
    public int getCategoryId() { return categoryId; } // category key getter
    public String getCategoryName() { return categoryName; } // category text getter
    public BigDecimal getUnitPrice() { return unitPrice; } // price getter
    public BigDecimal getCostPrice() { return costPrice; } // cost getter
    public int getStockQty() { return stockQty; } // stock getter
    public int getReorderLevel() { return reorderLevel; } // reorder getter

    @Override public String toString() { return code + " - " + name + " (stock " + stockQty + ")"; } // text shown in combo boxes
} // end class
