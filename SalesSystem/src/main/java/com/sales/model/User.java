package com.sales.model; // package for data objects

public class User { // the logged-in user
    private final int id; // primary key
    private final String username; // login name
    private final String role; // ADMIN or CASHIER

    public User(int id, String username, String role) { // constructor
        this.id = id; // store id
        this.username = username; // store name
        this.role = role; // store role
    } // end constructor

    public int getId() { return id; } // id getter
    public String getUsername() { return username; } // name getter
    public String getRole() { return role; } // role getter
    public boolean isAdmin() { return "ADMIN".equals(role); } // true for administrators
} // end class
