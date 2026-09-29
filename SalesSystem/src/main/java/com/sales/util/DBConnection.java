package com.sales.util; // package for shared helpers

import java.io.File; // to look for an external db.properties
import java.io.FileInputStream; // to read that file
import java.io.InputStream; // generic input stream
import java.sql.Connection; // JDBC connection
import java.sql.DriverManager; // JDBC connection factory
import java.sql.SQLException; // JDBC error type
import java.util.Properties; // key/value configuration

/** DESIGN PATTERN: Singleton - one shared object holds the database settings. */ // pattern note
public final class DBConnection { // singleton class
    private static DBConnection instance; // the single shared instance
    private final String url; // JDBC url
    private final String user; // database user
    private final String password; // database password

    private DBConnection() { // private constructor: nobody else can create it
        Properties p = new Properties(); // holds the settings
        File external = new File("db.properties"); // settings file next to the jar (used after deployment)
        try (InputStream in = external.exists() ? new FileInputStream(external) // prefer the external file
                : DBConnection.class.getResourceAsStream("/db.properties")) { // otherwise use the bundled default
            if (in == null) { // no configuration found at all
                throw new IllegalStateException("db.properties not found."); // stop with a clear message
            } // end if
            p.load(in); // read key/value pairs
        } catch (java.io.IOException ex) { // file could not be read
            throw new IllegalStateException("Cannot read db.properties: " + ex.getMessage()); // stop with a clear message
        } // end try
        url = p.getProperty("db.url"); // read url
        user = p.getProperty("db.user"); // read user
        password = p.getProperty("db.password"); // read password
    } // end constructor

    public static synchronized DBConnection getInstance() { // global access point (thread safe)
        if (instance == null) { // first call
            instance = new DBConnection(); // create the only instance
        } // end if
        return instance; // return the shared instance
    } // end getInstance

    public Connection getConnection() throws SQLException { // hand out a fresh connection (callers close it)
        return DriverManager.getConnection(url, user, password); // open the connection
    } // end getConnection
} // end class
