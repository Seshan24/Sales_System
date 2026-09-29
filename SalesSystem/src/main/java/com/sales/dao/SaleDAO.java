package com.sales.dao; // package for the Data Access Objects

import com.sales.model.CartItem; // cart line model
import com.sales.util.DBConnection; // connection singleton
import com.sales.util.ValidationException; // business rule error
import java.math.BigDecimal; // exact money type
import java.sql.Connection; // JDBC connection
import java.sql.PreparedStatement; // parameterised SQL
import java.sql.ResultSet; // query result
import java.sql.SQLException; // JDBC error
import java.sql.Statement; // constant for generated keys
import java.text.SimpleDateFormat; // invoice number format
import java.util.Date; // current time
import java.util.List; // list interface

public class SaleDAO { // data access for the sales transaction
    /** Saves invoice + lines + stock deduction as ONE database transaction (all or nothing). */ // transaction note
    public String saveSale(int customerId, int userId, List<CartItem> cart, // arguments: who and what
                           BigDecimal subTotal, BigDecimal discount) throws SQLException, ValidationException { // money values
        String invoice = "INV-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()); // unique invoice number
        try (Connection con = DBConnection.getInstance().getConnection()) { // open connection
            con.setAutoCommit(false); // start a manual transaction
            try { // everything below must succeed together
                int saleId; // key of the new invoice
                String head = "INSERT INTO sales (invoice_no, customer_id, user_id, sub_total, discount, grand_total) VALUES (?,?,?,?,?,?)"; // header SQL
                try (PreparedStatement ps = con.prepareStatement(head, Statement.RETURN_GENERATED_KEYS)) { // ask for the new key
                    ps.setString(1, invoice); // bind invoice number
                    ps.setInt(2, customerId); // bind customer
                    ps.setInt(3, userId); // bind cashier
                    ps.setBigDecimal(4, subTotal); // bind sub total
                    ps.setBigDecimal(5, discount); // bind discount
                    ps.setBigDecimal(6, subTotal.subtract(discount)); // bind grand total
                    ps.executeUpdate(); // insert the header
                    try (ResultSet keys = ps.getGeneratedKeys()) { // read the generated key
                        keys.next(); // move to the row
                        saleId = keys.getInt(1); // store the invoice id
                    } // end keys
                } // end header statement
                String line = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, line_total) VALUES (?,?,?,?,?)"; // line SQL
                String stock = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ? AND stock_qty >= ?"; // safe stock deduction
                try (PreparedStatement psLine = con.prepareStatement(line); // prepare line insert
                     PreparedStatement psStock = con.prepareStatement(stock)) { // prepare stock update
                    for (CartItem c : cart) { // for every cart line
                        psStock.setInt(1, c.getQty()); // quantity to remove
                        psStock.setInt(2, c.getProduct().getId()); // which product
                        psStock.setInt(3, c.getQty()); // guard: only if enough stock
                        if (psStock.executeUpdate() == 0) { // no row changed = not enough stock
                            throw new ValidationException("Insufficient stock for " + c.getProduct().getName() + "."); // abort the sale
                        } // end if
                        psLine.setInt(1, saleId); // bind invoice id
                        psLine.setInt(2, c.getProduct().getId()); // bind product
                        psLine.setInt(3, c.getQty()); // bind quantity
                        psLine.setBigDecimal(4, c.getProduct().getUnitPrice()); // bind unit price
                        psLine.setBigDecimal(5, c.getLineTotal()); // bind line total
                        psLine.executeUpdate(); // insert the line
                    } // end for
                } // end line statements
                con.commit(); // everything worked: make it permanent
            } catch (SQLException | ValidationException ex) { // any failure
                con.rollback(); // undo header, lines and stock changes
                throw ex; // report the problem to the screen
            } // end inner try
        } // end outer try
        return invoice; // give the invoice number back
    } // end saveSale
} // end class
