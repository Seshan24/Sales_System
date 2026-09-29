package com.sales.model; // package for data objects

public class Customer { // one customer row
    private final int id; // primary key
    private final String name; // customer name
    private final String phone; // phone number
    private final String email; // email (may be null)
    private final String address; // address (may be null)
    private final String type; // RETAIL or WHOLESALE

    public Customer(int id, String name, String phone, String email, String address, String type) { // constructor sets every field
        this.id = id; // store id
        this.name = name; // store name
        this.phone = phone; // store phone
        this.email = email; // store email
        this.address = address; // store address
        this.type = type; // store type
    } // end constructor

    public int getId() { return id; } // id getter
    public String getName() { return name; } // name getter
    public String getPhone() { return phone; } // phone getter
    public String getEmail() { return email; } // email getter
    public String getAddress() { return address; } // address getter
    public String getType() { return type; } // type getter

    @Override public String toString() { return name + " [" + type + "]"; } // text shown in combo boxes
} // end class
