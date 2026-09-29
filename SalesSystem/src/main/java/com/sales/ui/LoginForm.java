package com.sales.ui; // package for screens

import com.sales.dao.UserDAO; // user data access
import com.sales.model.User; // user model
import com.sales.util.UI; // shared helpers
import com.sales.util.Validator; // input validation
import java.awt.BorderLayout; // layout manager
import java.awt.Font; // fonts
import java.awt.GridLayout; // grid layout
import javax.swing.BorderFactory; // borders
import javax.swing.JFrame; // window
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JPasswordField; // hidden text input
import javax.swing.JTextField; // text input
import javax.swing.SwingConstants; // alignment constants

public class LoginForm extends JFrame { // INPUT UI: login window
    private final JTextField txtUser = new JTextField(); // username input
    private final JPasswordField txtPass = new JPasswordField(); // password input (characters hidden)

    public LoginForm() { // build the window
        setTitle("Sales Management System - Login"); // window title
        setDefaultCloseOperation(EXIT_ON_CLOSE); // closing the login ends the program
        setSize(380, 300); // window size
        setLocationRelativeTo(null); // centre on screen
        setResizable(false); // fixed size

        JLabel title = new JLabel("SALES MANAGEMENT SYSTEM", SwingConstants.CENTER); // heading
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f)); // bigger bold font
        title.setForeground(UI.PRIMARY); // brand colour

        JPanel form = new JPanel(new GridLayout(4, 1, 6, 6)); // 4 stacked rows
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // side padding
        form.add(new JLabel("Username")); // caption
        form.add(txtUser); // username field
        form.add(new JLabel("Password")); // caption
        form.add(txtPass); // password field

        JPanel south = new JPanel(); // holds the button
        south.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0)); // bottom padding
        var btn = UI.button("Login", UI.PRIMARY); // styled button
        btn.addActionListener(e -> doLogin()); // click runs the login
        south.add(btn); // add button
        getRootPane().setDefaultButton(btn); // pressing Enter also logs in

        setLayout(new BorderLayout(0, 10)); // window layout
        add(title, BorderLayout.NORTH); // heading on top
        add(form, BorderLayout.CENTER); // fields in the middle
        add(south, BorderLayout.SOUTH); // button at the bottom
    } // end constructor

    private void doLogin() { // validate input then check the database
        try { // any problem goes to the central handler
            String u = Validator.requireText(txtUser.getText(), "Username", 30); // username is mandatory
            String p = Validator.requireText(new String(txtPass.getPassword()), "Password", 50); // password is mandatory
            User user = new UserDAO().authenticate(u, p); // ask the database
            if (user == null) { // no matching row
                UI.error(this, "Invalid username or password."); // tell the user
                txtPass.setText(""); // clear the password box
                return; // stay on the login screen
            } // end if
            new MainFrame(user).setVisible(true); // open the main application
            dispose(); // close the login window
        } catch (Exception ex) { // validation or database problem
            UI.handle(this, ex); // show a friendly message
        } // end try
    } // end doLogin
} // end class
