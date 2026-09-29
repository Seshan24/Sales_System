package com.sales.dao; // package for the Data Access Objects

import com.sales.util.DBConnection; // connection singleton
import java.sql.Connection; // JDBC connection
import java.sql.ResultSet; // query result
import java.sql.SQLException; // JDBC error
import java.sql.Statement; // plain statement
import java.util.ArrayList; // list implementation
import java.util.LinkedHashMap; // ordered map
import java.util.List; // list interface
import java.util.Map; // map interface

public class DashboardDAO { // read-only queries for the dashboard
    public double todaySales() throws SQLException { // revenue since midnight
        return scalar("SELECT COALESCE(SUM(grand_total),0) FROM sales WHERE DATE(sale_date) = CURDATE()"); // one number
    } // end todaySales

    public double last30DaysSales() throws SQLException { // revenue in the last 30 days
        return scalar("SELECT COALESCE(SUM(grand_total),0) FROM sales WHERE sale_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)"); // one number
    } // end last30DaysSales

    public double last30DaysProfit() throws SQLException { // profit = revenue - cost of goods
        return scalar("SELECT COALESCE(SUM(si.line_total - si.quantity * p.cost_price),0) FROM sale_items si " // margin per line
                    + "JOIN products p ON p.product_id = si.product_id JOIN sales s ON s.sale_id = si.sale_id " // needed tables
                    + "WHERE s.sale_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)"); // last 30 days
    } // end last30DaysProfit

    public double lowStockCount() throws SQLException { // number of products at or below reorder level
        return scalar("SELECT COUNT(*) FROM products WHERE stock_qty <= reorder_level"); // one number
    } // end lowStockCount

    public Map<String, Double> topProducts() throws SQLException { // top 5 products by revenue (last 30 days)
        Map<String, Double> map = new LinkedHashMap<>(); // keeps rank order
        String sql = "SELECT p.product_code, SUM(si.line_total) AS rev FROM sale_items si " // revenue per product
                   + "JOIN products p ON p.product_id = si.product_id JOIN sales s ON s.sale_id = si.sale_id " // needed tables
                   + "WHERE s.sale_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) GROUP BY p.product_code ORDER BY rev DESC LIMIT 5"; // top 5
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { // run query
            while (rs.next()) { // for every row
                map.put(rs.getString(1), rs.getDouble(2)); // code -> revenue
            } // end while
        } // end try
        return map; // return ranking
    } // end topProducts

    public List<String[]> lowStockItems() throws SQLException { // rows for the low-stock table
        List<String[]> rows = new ArrayList<>(); // result rows
        String sql = "SELECT product_code, product_name, stock_qty, reorder_level FROM products " // columns
                   + "WHERE stock_qty <= reorder_level ORDER BY stock_qty"; // most urgent first
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { // run query
            while (rs.next()) { // for every row
                rows.add(new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)}); // store as text
            } // end while
        } // end try
        return rows; // return rows
    } // end lowStockItems

    private double scalar(String sql) throws SQLException { // helper: run a query that returns one number
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) { // run query
            rs.next(); // move to the only row
            return rs.getDouble(1); // read the number
        } // end try
    } // end scalar
} // end class
