package com.sales.ui; // package for screens

import com.sales.model.User; // logged-in user
import com.sales.util.UI; // shared helpers
import java.awt.BorderLayout; // layout manager
import java.awt.Color; // colours
import java.awt.Component; // base component
import java.awt.Font; // fonts
import javax.swing.BorderFactory; // borders
import javax.swing.JFrame; // window
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JTabbedPane; // tab container

public class MainFrame extends JFrame { // main window that hosts every module
    public MainFrame(User user) { // build the window for the logged-in user
        setTitle("Sales Management System"); // window title
        setDefaultCloseOperation(EXIT_ON_CLOSE); // closing ends the program
        setSize(1100, 700); // window size
        setLocationRelativeTo(null); // centre on screen

        DashboardPanel dashboard = new DashboardPanel(); // dashboard module
        SalesPanel sales = new SalesPanel(user); // sales transaction module
        JTabbedPane tabs = new JTabbedPane(); // tab container
        tabs.addTab("Dashboard", dashboard); // tab 1
        tabs.addTab("New Sale", sales); // tab 2
        tabs.addTab("Customers", new CustomerPanel()); // tab 3
        if (user.isAdmin()) { // ROLE-BASED ACCESS: only admins see the next tabs
            tabs.addTab("Products", new ProductPanel()); // tab 4 (admin only)
            tabs.addTab("Reports", new ReportPanel()); // tab 5 (admin only)
        } // end if
        tabs.addChangeListener(e -> { // refresh data whenever the user switches tab
            Component c = tabs.getSelectedComponent(); // current tab
            if (c == dashboard) { dashboard.refresh(); } // reload KPIs
            else if (c == sales) { sales.reload(); } // reload customers and stock
        }); // end listener

        JPanel header = new JPanel(new BorderLayout()); // top bar
        header.setBackground(UI.PRIMARY); // blue background
        header.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14)); // padding
        JLabel who = new JLabel("Logged in: " + user.getUsername() + " (" + user.getRole() + ")"); // user info
        who.setForeground(Color.WHITE); // white text
        who.setFont(who.getFont().deriveFont(Font.BOLD)); // bold text
        var logout = UI.button("Logout", UI.DANGER); // logout button
        logout.addActionListener(e -> { new LoginForm().setVisible(true); dispose(); }); // back to login
        header.add(who, BorderLayout.WEST); // user info on the left
        header.add(logout, BorderLayout.EAST); // logout on the right

        setLayout(new BorderLayout()); // window layout
        add(header, BorderLayout.NORTH); // top bar
        add(tabs, BorderLayout.CENTER); // tabs fill the rest
        dashboard.refresh(); // first load of the dashboard
    } // end constructor
} // end class
