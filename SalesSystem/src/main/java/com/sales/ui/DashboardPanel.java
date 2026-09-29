package com.sales.ui; // package for screens

import com.sales.dao.DashboardDAO; // dashboard queries
import com.sales.util.UI; // shared helpers
import java.awt.BorderLayout; // layout manager
import java.awt.Color; // colours
import java.awt.Font; // fonts
import java.awt.Graphics; // painting
import java.awt.Graphics2D; // advanced painting
import java.awt.GridLayout; // grid layout
import java.awt.RenderingHints; // smooth drawing
import java.util.Collections; // helper for max value
import java.util.LinkedHashMap; // ordered map
import java.util.Map; // map interface
import javax.swing.BorderFactory; // borders
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JScrollPane; // scroll container
import javax.swing.JTable; // table
import javax.swing.table.DefaultTableModel; // table model

public class DashboardPanel extends JPanel { // DASHBOARD: KPI cards + chart + alert table
    private final DashboardDAO dao = new DashboardDAO(); // data source
    private final JLabel lblToday = new JLabel("-"); // KPI value: today's sales
    private final JLabel lblMonth = new JLabel("-"); // KPI value: 30 day sales
    private final JLabel lblProfit = new JLabel("-"); // KPI value: 30 day profit
    private final JLabel lblLow = new JLabel("-"); // KPI value: low stock count
    private final BarChart chart = new BarChart(); // custom bar chart
    private final DefaultTableModel lowModel = UI.model("Code", "Product", "Stock", "Reorder Level"); // low stock rows

    public DashboardPanel() { // build the layout
        setLayout(new BorderLayout(12, 12)); // main layout
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // outer padding
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 12)); // four KPI cards side by side
        cards.add(card("Today's Sales", lblToday, UI.PRIMARY)); // card 1
        cards.add(card("Sales (30 days)", lblMonth, UI.SUCCESS)); // card 2
        cards.add(card("Profit (30 days)", lblProfit, new Color(106, 27, 154))); // card 3
        cards.add(card("Low Stock Items", lblLow, UI.DANGER)); // card 4
        add(cards, BorderLayout.NORTH); // cards on top

        JPanel centre = new JPanel(new GridLayout(1, 2, 12, 12)); // chart and table side by side
        chart.setBorder(BorderFactory.createTitledBorder("Top 5 Products by Revenue (30 days)")); // chart title
        JScrollPane sp = new JScrollPane(new JTable(lowModel)); // low stock table in a scroll pane
        sp.setBorder(BorderFactory.createTitledBorder("Restock Alerts")); // table title
        centre.add(chart); // left: chart
        centre.add(sp); // right: table
        add(centre, BorderLayout.CENTER); // fill the rest
    } // end constructor

    private JPanel card(String title, JLabel value, Color colour) { // build one KPI card
        JPanel p = new JPanel(new BorderLayout()); // card layout
        p.setBackground(colour); // card colour
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16)); // padding
        JLabel t = new JLabel(title); // card caption
        t.setForeground(Color.WHITE); // white text
        value.setForeground(Color.WHITE); // white text
        value.setFont(value.getFont().deriveFont(Font.BOLD, 26f)); // big number
        p.add(t, BorderLayout.NORTH); // caption on top
        p.add(value, BorderLayout.CENTER); // number below
        return p; // return the card
    } // end card

    public void refresh() { // reload every figure from the database
        try { // any problem goes to the central handler
            lblToday.setText(String.format("%,.2f", dao.todaySales())); // today's revenue
            lblMonth.setText(String.format("%,.2f", dao.last30DaysSales())); // 30 day revenue
            lblProfit.setText(String.format("%,.2f", dao.last30DaysProfit())); // 30 day profit
            lblLow.setText(String.valueOf((int) dao.lowStockCount())); // number of low stock items
            chart.setData(dao.topProducts()); // update the chart
            lowModel.setRowCount(0); // clear the table
            for (String[] r : dao.lowStockItems()) { lowModel.addRow(r); } // add every alert row
        } catch (Exception ex) { // database problem
            UI.handle(this, ex); // show a friendly message
        } // end try
    } // end refresh

    private static class BarChart extends JPanel { // simple bar chart drawn with Java2D
        private Map<String, Double> data = new LinkedHashMap<>(); // code -> revenue

        void setData(Map<String, Double> d) { data = d; repaint(); } // replace data and redraw

        @Override protected void paintComponent(Graphics g) { // draw the chart
            super.paintComponent(g); // clear the background
            Graphics2D g2 = (Graphics2D) g; // advanced drawing API
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth edges
            if (data.isEmpty()) { g2.drawString("No sales in the last 30 days", 20, 40); return; } // nothing to draw
            double max = Collections.max(data.values()); // tallest bar value
            int w = getWidth(), h = getHeight(); // panel size
            int barW = (w - 40) / data.size(); // width of each bar slot
            int i = 0; // bar index
            for (Map.Entry<String, Double> e : data.entrySet()) { // for every product
                int bh = (int) ((h - 90) * e.getValue() / max); // bar height scaled to the maximum
                int x = 20 + i * barW + 6; // left edge
                int y = h - 30 - bh; // top edge
                g2.setColor(UI.PRIMARY); // bar colour
                g2.fillRect(x, y, barW - 12, bh); // draw the bar
                g2.setColor(Color.DARK_GRAY); // text colour
                g2.drawString(e.getKey(), x, h - 12); // product code under the bar
                g2.drawString(String.format("%,.0f", e.getValue()), x, y - 4); // value above the bar
                i++; // next bar
            } // end for
        } // end paintComponent
    } // end BarChart
} // end class
