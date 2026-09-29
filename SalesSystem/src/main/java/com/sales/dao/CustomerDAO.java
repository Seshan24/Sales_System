package com.sales.dao; // package for the Data Access Objects

import com.sales.model.Customer; // customer model
import com.sales.util.DBConnection; // connection singleton
import java.sql.Connection; // JDBC connection
import java.sql.PreparedStatement; // parameterised SQL
import java.sql.ResultSet; // query result
import java.sql.SQLException; // JDBC error
import java.util.ArrayList; // list implementation
import java.util.List; // list interface

public class CustomerDAO { // data access for customers
    public List<Customer> search(String keyword) throws SQLException { // list customers matching a name or phone
        String sql = "SELECT * FROM customers WHERE customer_name LIKE ? OR phone LIKE ? ORDER BY customer_name"; // filter and sort
        List<Customer> list = new ArrayList<>(); // result list
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement(sql)) { // prepare statement
            ps.setString(1, "%" + keyword + "%"); // bind name filter
            ps.setString(2, "%" + keyword + "%"); // bind phone filter
            try (ResultSet rs = ps.executeQuery()) { // run query
                while (rs.next()) { // for every row
                    list.add(new Customer(rs.getInt("customer_id"), rs.getString("customer_name"), rs.getString("phone"), // map columns
                            rs.getString("email"), rs.getString("address"), rs.getString("customer_type"))); // more columns
                } // end while
            } // end inner try
        } // end outer try
        return list; // return all matches
    } // end search

    public void insert(Customer c) throws SQLException { // add a customer
        execute("INSERT INTO customers (customer_name, phone, email, address, customer_type) VALUES (?,?,?,?,?)", c, false); // SQL
    } // end insert

    public void update(Customer c) throws SQLException { // change a customer
        execute("UPDATE customers SET customer_name=?, phone=?, email=?, address=?, customer_type=? WHERE customer_id=?", c, true); // SQL
    } // end update

    public void delete(int id) throws SQLException { // remove a customer (fails if they have sales)
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement("DELETE FROM customers WHERE customer_id = ?")) { // prepare statement
            ps.setInt(1, id); // bind id
            ps.executeUpdate(); // run delete
        } // end try
    } // end delete

    private void execute(String sql, Customer c, boolean withId) throws SQLException { // shared insert/update runner
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection
             PreparedStatement ps = con.prepareStatement(sql)) { // prepare statement
            ps.setString(1, c.getName()); // bind name
            ps.setString(2, c.getPhone()); // bind phone
            ps.setString(3, c.getEmail()); // bind email (null allowed)
            ps.setString(4, c.getAddress()); // bind address (null allowed)
            ps.setString(5, c.getType()); // bind type
            if (withId) { // only for update
                ps.setInt(6, c.getId()); // bind id for the WHERE clause
            } // end if
            ps.executeUpdate(); // run the statement
        } // end try
    } // end execute
} // end class
