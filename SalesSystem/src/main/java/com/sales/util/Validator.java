package com.sales.util; // package for shared helpers

import java.math.BigDecimal; // exact decimal type for money
import java.math.RoundingMode; // rounding rule for money
import java.util.regex.Pattern; // regular expressions for format checks

public final class Validator { // static input validation rules used by every form
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"); // simple email format
    private static final Pattern PHONE = Pattern.compile("^\\+?\\d{9,15}$"); // 9-15 digits with optional leading +

    private Validator() { } // utility class: prevent object creation

    public static String requireText(String value, String field, int max) throws ValidationException { // mandatory text with a length limit
        if (value == null || value.trim().isEmpty()) { // empty or only spaces
            throw new ValidationException(field + " is required."); // tell the user which field is missing
        } // end if
        if (value.trim().length() > max) { // longer than the database column
            throw new ValidationException(field + " must be at most " + max + " characters."); // report the limit
        } // end if
        return value.trim(); // return the cleaned text
    } // end requireText

    public static int requirePositiveInt(String value, String field) throws ValidationException { // whole number greater than zero
        int n = parseInt(value, field); // convert text to int (throws if not a number)
        if (n <= 0) { // zero or negative
            throw new ValidationException(field + " must be greater than zero."); // explain the rule
        } // end if
        return n; // valid number
    } // end requirePositiveInt

    public static int requireNonNegativeInt(String value, String field) throws ValidationException { // whole number, zero allowed
        int n = parseInt(value, field); // convert text to int
        if (n < 0) { // negative
            throw new ValidationException(field + " cannot be negative."); // explain the rule
        } // end if
        return n; // valid number
    } // end requireNonNegativeInt

    public static BigDecimal requirePositiveMoney(String value, String field) throws ValidationException { // money greater than zero
        try { // parsing may fail
            BigDecimal m = new BigDecimal(value == null ? "" : value.trim()); // convert text to decimal
            if (m.signum() <= 0) { // zero or negative
                throw new ValidationException(field + " must be greater than zero."); // explain the rule
            } // end if
            return m.setScale(2, RoundingMode.HALF_UP); // keep two decimal places
        } catch (NumberFormatException ex) { // text was not a number
            throw new ValidationException(field + " must be a valid amount."); // friendly message
        } // end try
    } // end requirePositiveMoney

    public static String requirePhone(String value) throws ValidationException { // mandatory phone number
        String v = requireText(value, "Phone", 15); // must not be empty
        if (!PHONE.matcher(v).matches()) { // must match the digit pattern
            throw new ValidationException("Phone must contain 9 to 15 digits (optional leading +)."); // explain the format
        } // end if
        return v; // valid phone
    } // end requirePhone

    public static String optionalEmail(String value) throws ValidationException { // email may be left empty
        if (value == null || value.trim().isEmpty()) { // nothing entered
            return null; // store NULL in the database
        } // end if
        if (!EMAIL.matcher(value.trim()).matches()) { // entered but wrong format
            throw new ValidationException("Email format is invalid."); // explain the problem
        } // end if
        return value.trim(); // valid email
    } // end optionalEmail

    private static int parseInt(String value, String field) throws ValidationException { // shared integer parser
        try { // parsing may fail
            return Integer.parseInt(value == null ? "" : value.trim()); // convert text to int
        } catch (NumberFormatException ex) { // not a whole number
            throw new ValidationException(field + " must be a whole number."); // friendly message
        } // end try
    } // end parseInt
} // end class
