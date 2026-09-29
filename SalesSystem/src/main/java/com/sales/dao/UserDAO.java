package com.sales.dao; // package for the Data Access Objects

import com.sales.model.User; // logged-in user model
import com.sales.util.DBConnection; // connection singleton
import java.sql.Connection; // JDBC connection
import java.sql.PreparedStatement; // parameterised SQL (prevents SQL injection)
import java.sql.ResultSet; // query result
import java.sql.SQLException; // JDBC error

/** DESIGN PATTERN: DAO - all SQL for users lives here, screens never write SQL. */ // pattern note
public class UserDAO { // data access for users
    public User authenticate(String username, String password) throws SQLException { // returns null when login fails
        String sql = "SELECT user_id, username, role FROM users WHERE username = ? AND password_hash = SHA2(?, 256)"; // hash compared in the database
        try (Connection con = DBConnection.getInstance().getConnection(); // open connection (auto closed)
             PreparedStatement ps = con.prepareStatement(sql)) { // prepare the statement
            ps.setString(1, username); // bind username
            ps.setString(2, password); // bind plain password (hashed by SHA2 in SQL)
            try (ResultSet rs = ps.executeQuery()) { // run the query
                return rs.next() ? new User(rs.getInt(1), rs.getString(2), rs.getString(3)) : null; // user or null
            } // end inner try
        } // end outer try
    } // end authenticate
} // end class
