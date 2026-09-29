package com.sales.dao; // package for the Data Access Objects

import com.sales.model.Product; // product model
import com.sales.util.DBConnection; // connection singleton
import java.sql.Connection; // JDBC connection
import java.sql.PreparedStatement; // parameterised SQL
import java.sql.ResultSet; // query result
import java.sql.SQLException; // JDBC error
import java.sql.Statement; // plain statement for the category list
import java.util.ArrayList; // list implementation
import java.util.LinkedHashMap; // ordered map
import java.util.List; // list interface
import java.util.Map; // map interface

public class ProductDAO { // data access for products and categories
    public List<Product> search(String keyword) throws SQLException { // list products matching a code or name
        String sql = "SELECT p.*, c.category_name FROM products p JOIN categories c ON c.category_id = p.category_id " // join for category text
                   + "WHERE p.product_name LIKE ? OR p.product_code LIKE ? ORDER BY p.product_name"; // filter and sort
        List<Product> list = new ArrayList<>(); // result list
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement(sql)) { // prepare statement
            ps.setString(1, "%" + keyword + "%"); // bind name filter
            ps.setString(2, "%" + keyword + "%"); // bind code filter
            try (ResultSet rs = ps.executeQuery()) { // run query
                while (rs.next()) { // for every row
                    list.add(new Product(rs.getInt("product_id"), rs.getString("product_code"), rs.getString("product_name"), // map columns to object
                            rs.getInt("category_id"), rs.getString("category_name"), rs.getBigDecimal("unit_price"), // more columns
                            rs.getBigDecimal("cost_price"), rs.getInt("stock_qty"), rs.getInt("reorder_level"))); // last columns
                } // end while
            } // end inner try
        } // end outer try
        return list; // return all matches
    } // end search

    public void insert(Product p) throws SQLException { // add a new product
        execute("INSERT INTO products (product_code, product_name, category_id, unit_price, cost_price, stock_qty, reorder_level) " // columns
              + "VALUES (?,?,?,?,?,?,?)", p, false); // placeholders
    } // end insert

    public void update(Product p) throws SQLException { // change an existing product
        execute("UPDATE products SET product_code=?, product_name=?, category_id=?, unit_price=?, cost_price=?, " // assignments
              + "stock_qty=?, reorder_level=? WHERE product_id=?", p, true); // filter by id
    } // end update

    public void delete(int id) throws SQLException { // remove a product (fails if it was sold)
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE product_id = ?")) { // prepare statement
            ps.setInt(1, id); // bind id
            ps.executeUpdate(); // run delete
        } // end try
    } // end delete

    public Map<String, Integer> categories() throws SQLException { // category name -> id
        Map<String, Integer> map = new LinkedHashMap<>(); // keeps insertion order
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             Statement st = con.createStatement(); // plain statement (no parameters)
             ResultSet rs = st.executeQuery("SELECT category_id, category_name FROM categories ORDER BY category_name")) { // run query
            while (rs.next()) { // for every row
                map.put(rs.getString(2), rs.getInt(1)); // store name and id
            } // end while
        } // end try
        return map; // return the map
    } // end categories

    private void execute(String sql, Product p, boolean withId) throws SQLException { // shared insert/update runner
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement(sql)) { // prepare statement
            ps.setString(1, p.getCode()); // bind code
            ps.setString(2, p.getName()); // bind name
            ps.setInt(3, p.getCategoryId()); // bind category
            ps.setBigDecimal(4, p.getUnitPrice()); // bind price
            ps.setBigDecimal(5, p.getCostPrice()); // bind cost
            ps.setInt(6, p.getStockQty()); // bind stock
            ps.setInt(7, p.getReorderLevel()); // bind reorder level
            if (withId) { // only for update
                ps.setInt(8, p.getId()); // bind id for the WHERE clause
            } // end if
            ps.executeUpdate(); // run the statement
        } // end try
    } // end execute
} // end class
