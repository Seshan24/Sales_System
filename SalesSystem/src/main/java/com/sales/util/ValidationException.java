package com.sales.util; // package for shared helpers

public class ValidationException extends Exception { // checked exception used for invalid user input or broken business rules
    public ValidationException(String message) { // constructor receives the text shown to the user
        super(message); // pass the message to the parent Exception
    } // end constructor
} // end class
