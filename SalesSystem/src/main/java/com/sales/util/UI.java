package com.sales.util; // package for shared helpers

import java.awt.Color; // colours
import java.awt.Component; // parent for dialogs
import java.awt.Cursor; // mouse cursor
import java.awt.Font; // fonts
import java.sql.SQLException; // database errors
import java.util.logging.Level; // log levels
import java.util.logging.Logger; // logging
import javax.swing.BorderFactory; // borders
import javax.swing.JButton; // button
import javax.swing.JComponent; // base component
import javax.swing.JLabel; // label
import javax.swing.JOptionPane; // dialogs
import javax.swing.JPanel; // panel
import javax.swing.plaf.basic.BasicButtonUI; // simple button painting so colours work in every theme
import javax.swing.table.DefaultTableModel; // table data model

public final class UI { // shared Swing helpers and the central exception handler
    public static final Color PRIMARY = new Color(21, 101, 192); // main blue
    public static final Color SUCCESS = new Color(46, 125, 50); // green
    public static final Color DANGER = new Color(198, 40, 40); // red
    public static final Color GREY = new Color(96, 125, 139); // neutral grey
    private static final Logger LOG = Logger.getLogger("SalesApp"); // application log

    private UI() { } // utility class

    public static DefaultTableModel model(String... columns) { // read-only table model
        return new DefaultTableModel(columns, 0) { // start with zero rows
            @Override public boolean isCellEditable(int row, int col) { return false; } // users cannot edit cells directly
        }; // end anonymous class
    } // end model

    public static JButton button(String text, Color background) { // coloured flat button
        JButton b = new JButton(text); // create the button
        b.setUI(new BasicButtonUI()); // use the basic painter so background colour is respected
        b.setOpaque(true); // paint the background
        b.setBackground(background); // set the colour
        b.setForeground(Color.WHITE); // white text
        b.setFont(b.getFont().deriveFont(Font.BOLD)); // bold text
        b.setFocusPainted(false); // no dotted focus box
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16)); // inner padding
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // hand cursor on hover
        return b; // return the styled button
    } // end button

    public static void addRow(JPanel form, String label, JComponent field) { // add "label + field" to a grid form
        form.add(new JLabel(label)); // caption
        form.add(field); // input control
    } // end addRow

    public static void info(Component parent, String message) { // information dialog
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE); // show it
    } // end info

    public static void error(Component parent, String message) { // error dialog
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE); // show it
    } // end error

    public static boolean confirm(Component parent, String message) { // yes/no dialog
        return JOptionPane.showConfirmDialog(parent, message, "Please confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION; // true when Yes
    } // end confirm

    /** CENTRAL EXCEPTION HANDLER: every screen sends its exceptions here. */ // handler note
    public static void handle(Component parent, Throwable ex) { // one place that turns exceptions into friendly messages
        if (ex instanceof ValidationException) { // user mistake, not a program fault
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE); // warn only
            return; // no logging needed
        } // end if
        LOG.log(Level.SEVERE, "Application error", ex); // log the technical detail for developers
        if (ex instanceof SQLException) { // database problem
            SQLException s = (SQLException) ex; // cast to read the code
            String msg; // message for the user
            switch (s.getErrorCode()) { // MySQL error numbers
                case 1062: msg = "This record already exists (duplicate code, phone or invoice)."; break; // unique key violated
                case 1451: msg = "This record is used by other data (for example past sales) and cannot be deleted."; break; // foreign key blocks delete
                case 1452: msg = "A related record was not found."; break; // foreign key missing
                default: // anything else
                    msg = (s.getSQLState() != null && s.getSQLState().startsWith("08")) // SQLSTATE 08 = connection problem
                            ? "Cannot connect to the database. Check that MySQL is running and db.properties is correct." // connection message
                            : "Database error: " + s.getMessage(); // generic database message
            } // end switch
            error(parent, msg); // show it
            return; // done
        } // end if
        error(parent, "Unexpected error: " + ex.getMessage()); // any other exception
    } // end handle
} // end class
