package com.sales.ui; // package for screens

import com.sales.dao.CustomerDAO; // customer data access
import com.sales.dao.ProductDAO; // product data access
import com.sales.dao.SaleDAO; // sale data access
import com.sales.model.CartItem; // cart line
import com.sales.model.Customer; // customer model
import com.sales.model.Product; // product model
import com.sales.model.User; // logged-in user
import com.sales.strategy.DiscountFactory; // Factory pattern
import com.sales.strategy.DiscountStrategy; // Strategy pattern
import com.sales.util.UI; // shared helpers
import com.sales.util.ValidationException; // business rule error
import com.sales.util.Validator; // input validation
import java.awt.BorderLayout; // layout manager
import java.awt.FlowLayout; // flow layout
import java.awt.Font; // fonts
import java.awt.GridLayout; // grid layout
import java.math.BigDecimal; // exact money type
import java.util.ArrayList; // list implementation
import java.util.List; // list interface
import javax.swing.BorderFactory; // borders
import javax.swing.JComboBox; // drop-down
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JScrollPane; // scroll container
import javax.swing.JTable; // table
import javax.swing.JTextField; // text input
import javax.swing.table.DefaultTableModel; // table model

public class SalesPanel extends JPanel { // TRANSACTION UI: create a sales invoice
    private final User user; // cashier making the sale
    private final CustomerDAO customerDAO = new CustomerDAO(); // customers source
    private final ProductDAO productDAO = new ProductDAO(); // products source
    private final SaleDAO saleDAO = new SaleDAO(); // saves the sale
    private final JComboBox<Customer> cboCustomer = new JComboBox<>(); // customer picker
    private final JComboBox<Product> cboProduct = new JComboBox<>(); // product picker
    private final JTextField txtQty = new JTextField("1", 5); // quantity input
    private final DefaultTableModel model = UI.model("Code", "Product", "Qty", "Unit Price", "Line Total"); // cart grid model
    private final JTable table = new JTable(model); // cart grid
    private final JLabel lblSub = new JLabel("0.00"); // sub total display
    private final JLabel lblDiscLabel = new JLabel("Discount"); // discount caption (changes with the strategy)
    private final JLabel lblDisc = new JLabel("0.00"); // discount display
    private final JLabel lblGrand = new JLabel("0.00"); // grand total display
    private final List<CartItem> cart = new ArrayList<>(); // items in the current sale
    private BigDecimal subTotal = BigDecimal.ZERO; // current sub total
    private BigDecimal discount = BigDecimal.ZERO; // current discount

    public SalesPanel(User user) { // build the screen
        this.user = user; // remember the cashier
        setLayout(new BorderLayout(10, 10)); // main layout
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // outer padding

        JPanel top = new JPanel(new GridLayout(2, 1, 6, 6)); // two input rows
        JPanel r1 = new JPanel(new FlowLayout(FlowLayout.LEFT)); // row 1: customer
        r1.add(new JLabel("Customer:")); // caption
        cboCustomer.setPrototypeDisplayValue(new Customer(0, "XXXXXXXXXXXXXXXXXXXXXXXX", "", "", "", "WHOLESALE")); // fixed width
        r1.add(cboCustomer); // customer picker
        JPanel r2 = new JPanel(new FlowLayout(FlowLayout.LEFT)); // row 2: product
        r2.add(new JLabel("Product:")); // caption
        r2.add(cboProduct); // product picker
        r2.add(new JLabel("Qty:")); // caption
        r2.add(txtQty); // quantity input
        var add = UI.button("Add to Cart", UI.PRIMARY); // add button
        add.addActionListener(e -> addItem()); // click adds the item
        r2.add(add); // add the button
        top.add(r1); // first row
        top.add(r2); // second row
        add(top, BorderLayout.NORTH); // inputs on top
        add(new JScrollPane(table), BorderLayout.CENTER); // cart grid in the middle

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT)); // bottom-left buttons
        var remove = UI.button("Remove Selected", UI.GREY); // remove button
        remove.addActionListener(e -> removeItem()); // click removes the row
        var clear = UI.button("Clear Cart", UI.GREY); // clear button
        clear.addActionListener(e -> clearCart()); // click empties the cart
        left.add(remove); // add button
        left.add(clear); // add button

        JPanel totals = new JPanel(new GridLayout(3, 2, 6, 4)); // totals block
        totals.add(new JLabel("Sub Total:")); // caption
        totals.add(lblSub); // value
        totals.add(lblDiscLabel); // caption
        totals.add(lblDisc); // value
        totals.add(new JLabel("GRAND TOTAL:")); // caption
        lblGrand.setFont(lblGrand.getFont().deriveFont(Font.BOLD, 18f)); // emphasise the total
        totals.add(lblGrand); // value
        var complete = UI.button("Complete Sale", UI.SUCCESS); // main action button
        complete.addActionListener(e -> completeSale()); // click saves the sale

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0)); // bottom-right block
        right.add(totals); // totals
        right.add(complete); // complete button
        JPanel bottom = new JPanel(new BorderLayout()); // bottom bar
        bottom.add(left, BorderLayout.WEST); // buttons on the left
        bottom.add(right, BorderLayout.EAST); // totals on the right
        add(bottom, BorderLayout.SOUTH); // bottom bar

        cboCustomer.addActionListener(e -> recalc()); // customer change can change the discount
        reload(); // load drop-downs
    } // end constructor

    public void reload() { // refresh customers and products (fresh stock levels)
        try { // any problem goes to the central handler
            Customer keep = (Customer) cboCustomer.getSelectedItem(); // remember the selection
            cboCustomer.removeAllItems(); // clear the list
            for (Customer c : customerDAO.search("")) { cboCustomer.addItem(c); } // reload customers
            if (keep != null) { // restore the previous selection
                for (int i = 0; i < cboCustomer.getItemCount(); i++) { // search the list
                    if (cboCustomer.getItemAt(i).getId() == keep.getId()) { cboCustomer.setSelectedIndex(i); } // same id = same customer
                } // end for
            } // end if
            cboProduct.removeAllItems(); // clear the list
            for (Product p : productDAO.search("")) { cboProduct.addItem(p); } // reload products
        } catch (Exception ex) { // database problem
            UI.handle(this, ex); // show a friendly message
        } // end try
    } // end reload

    private void addItem() { // validate and add a line to the cart
        try { // any problem goes to the central handler
            Product p = (Product) cboProduct.getSelectedItem(); // chosen product
            if (p == null) { throw new ValidationException("Select a product."); } // must choose one
            int qty = Validator.requirePositiveInt(txtQty.getText(), "Quantity"); // quantity must be a positive whole number
            CartItem existing = null; // line already in the cart?
            for (CartItem c : cart) { if (c.getProduct().getId() == p.getId()) { existing = c; } } // look for the same product
            int already = existing == null ? 0 : existing.getQty(); // quantity already reserved
            if (qty + already > p.getStockQty()) { // stock rule
                throw new ValidationException("Only " + p.getStockQty() + " units of " + p.getName() + " are in stock."); // explain
            } // end if
            if (existing == null) { cart.add(new CartItem(p, qty)); } else { existing.addQty(qty); } // add or merge
            txtQty.setText("1"); // reset quantity
            refreshCart(); // redraw grid and totals
        } catch (Exception ex) { // validation problem
            UI.handle(this, ex); // show a friendly message
        } // end try
    } // end addItem

    private void removeItem() { // remove the selected line
        int row = table.getSelectedRow(); // selected row index
        if (row < 0) { UI.info(this, "Select a cart line first."); return; } // nothing selected
        cart.remove(row); // remove from the list
        refreshCart(); // redraw
    } // end removeItem

    private void clearCart() { // empty the cart
        cart.clear(); // remove every line
        refreshCart(); // redraw
    } // end clearCart

    private void refreshCart() { // rebuild the grid from the cart list
        model.setRowCount(0); // clear the grid
        for (CartItem c : cart) { // for every line
            model.addRow(new Object[]{c.getProduct().getCode(), c.getProduct().getName(), c.getQty(), // code, name, quantity
                    c.getProduct().getUnitPrice(), c.getLineTotal()}); // price and total
        } // end for
        recalc(); // update totals
    } // end refreshCart

    private void recalc() { // compute sub total, discount and grand total
        subTotal = BigDecimal.ZERO; // start from zero
        for (CartItem c : cart) { subTotal = subTotal.add(c.getLineTotal()); } // add every line
        Customer cu = (Customer) cboCustomer.getSelectedItem(); // current customer
        DiscountStrategy s = DiscountFactory.forCustomer(cu == null ? "RETAIL" : cu.getType(), subTotal); // pick the rule
        discount = s.calculate(subTotal); // apply the rule
        lblSub.setText(String.format("%,.2f", subTotal)); // show sub total
        lblDiscLabel.setText("Discount (" + s.label() + "):"); // show which rule applied
        lblDisc.setText(String.format("%,.2f", discount)); // show discount
        lblGrand.setText(String.format("%,.2f", subTotal.subtract(discount))); // show grand total
    } // end recalc

    private void completeSale() { // save the invoice
        try { // any problem goes to the central handler
            Customer cu = (Customer) cboCustomer.getSelectedItem(); // chosen customer
            if (cu == null) { throw new ValidationException("Select a customer."); } // customer is mandatory
            if (cart.isEmpty()) { throw new ValidationException("The cart is empty."); } // need at least one line
            if (!UI.confirm(this, "Save this sale for " + cu.getName() + "?\nGrand total: " + lblGrand.getText())) { return; } // ask first
            String invoice = saleDAO.saveSale(cu.getId(), user.getId(), cart, subTotal, discount); // one DB transaction
            UI.info(this, "Sale saved successfully.\nInvoice No: " + invoice); // confirmation
            cart.clear(); // start a new sale
            reload(); // refresh stock levels
            refreshCart(); // reset the grid
        } catch (Exception ex) { // validation or database problem
            UI.handle(this, ex); // show a friendly message (transaction was rolled back)
        } // end try
    } // end completeSale
} // end class
