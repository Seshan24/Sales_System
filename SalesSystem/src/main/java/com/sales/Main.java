package com.sales; // root package of the application

import com.sales.ui.LoginForm; // first window shown
import com.sales.util.UI; // shared helper (central exception handler)
import javax.swing.SwingUtilities; // starts the GUI on the Event Dispatch Thread
import javax.swing.UIManager; // look and feel manager

public class Main { // application entry point
    public static void main(String[] args) { // the JVM starts here
        try { // try to apply the modern Nimbus theme
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) { // loop over installed themes
                if ("Nimbus".equals(info.getName())) { // find Nimbus
                    UIManager.setLookAndFeel(info.getClassName()); // activate it
                    break; // stop searching
                } // end if
            } // end for
        } catch (Exception ex) { // theme failed: the default theme is used
            System.err.println("Look and feel not applied: " + ex.getMessage()); // log to console
        } // end try
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> UI.handle(null, e)); // last-resort handler for unexpected errors
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true)); // open the login window
    } // end main
} // end class
