package com.sales.ui; // package for screens

import com.sales.dao.ProductDAO; // product data access
import com.sales.model.Product; // product model
import com.sales.util.UI; // shared helpers
import com.sales.util.ValidationException; // business rule error
import com.sales.util.Validator; // input validation
import java.awt.BorderLayout; // layout manager
import java.awt.FlowLayout; // flow layout
import java.awt.GridLayout; // grid layout
import java.math.BigDecimal; // exact money type
import java.util.List; // list interface
import java.util.Map; // map interface
import javax.swing.BorderFactory; // borders
import javax.swing.JComboBox; // drop-down
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JScrollPane; // scroll container
import javax.swing.JTable; // table
import javax.swing.JTextField; // text input
import javax.swing.table.DefaultTableModel; // table model

public class ProductPanel extends JPanel { // INPUT UI: product maintenance (admin only)
    private final ProductDAO dao = new ProductDAO(); // data source
    private final JTextField txtCode = new JTextField(); // product code input
    private final JTextField txtName = new JTextField(); // product name input
    private final JComboBox<String> cboCategory = new JComboBox<>(); // category picker
    private final JTextField txtPrice = new JTextField(); // unit price input
    private final JTextField txtCost = new JTextField(); // cost price input
    private final JTextField txtStock = new JTextField(); // stock input
    private final JTextField txtReorder = new JTextField(); // reorder level input
    private final JTextField txtSearch = new JTextField(15); // search box
    private final DefaultTableModel model = UI.model("ID", "Code", "Name", "Category", "Price", "Cost", "Stock", "Reorder"); // grid model
    private final JTable table = new JTable(model); // product grid
    private Map<String, Integer> categories; // category name -> id
    private List<Product> current; // products currently shown
    private int selectedId = 0; // 0 means "new product"

    public ProductPanel() { // build the screen
        setLayout(new BorderLayout(10, 10)); // main layout
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // outer padding
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8)); // label/field pairs, two per row
        form.setBorder(BorderFactory.createTitledBorder("Product Details")); // panel title
        UI.addRow(form, "Code *", txtCode); // field
        UI.addRow(form, "Name *", txtName); // field
        UI.addRow(form, "Category *", cboCategory); // field
        UI.addRow(form, "Unit Price *", txtPrice); // field
        UI.addRow(form, "Cost Price *", txtCost); // field
        UI.addRow(form, "Stock Qty *", txtStock); // field
        UI.addRow(form, "Reorder Level *", txtReorder); // field
        form.add(new JLabel("")); // filler cell
        form.add(new JLabel("")); // filler cell

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT)); // button row
        var bAdd = UI.button("Add", UI.SUCCESS); // add button
        var bUpd = UI.button("Update", UI.PRIMARY); // update button
        var bDel = UI.button("Delete", UI.DANGER); // delete button
        var bClr = UI.button("Clear", UI.GREY); // clear button
        var bSea = UI.button("Search", UI.GREY); // search button
        bAdd.addActionListener(e -> save(true)); // insert
        bUpd.addActionListener(e -> save(false)); // update
        bDel.addActionListener(e -> delete()); // delete
        bClr.addActionListener(e -> clearForm()); // reset form
        bSea.addActionListener(e -> load()); // reload with filter
        buttons.add(bAdd); buttons.add(bUpd); buttons.add(bDel); buttons.add(bClr); // add buttons
        buttons.add(new JLabel("   Search:")); buttons.add(txtSearch); buttons.add(bSea); // add search controls

        JPanel north = new JPanel(new BorderLayout()); // top block
        north.add(form, BorderLayout.CENTER); // form
        north.add(buttons, BorderLayout.SOUTH); // buttons
        add(north, BorderLayout.NORTH); // top
        add(new JScrollPane(table), BorderLayout.CENTER); // grid

        table.getSelectionModel().addListSelectionListener(e -> { // clicking a row fills the form
            int row = table.getSelectedRow(); // selected row
            if (!e.getValueIsAdjusting() && row >= 0 && row < current.size()) { fillForm(current.get(row)); } // copy values
        }); // end listener
        try { // load categories once
            categories = dao.categories(); // read from the database
            for (String name : categories.keySet()) { cboCategory.addItem(name); } // fill the drop-down
        } catch (Exception ex) { UI.handle(this, ex); } // database problem
        load(); // first load of the grid
    } // end constructor

    private void load() { // refill the grid from the database
        try { // any problem goes to the central handler
            current = dao.search(txtSearch.getText().trim()); // query with the search text
            model.setRowCount(0); // clear the grid
            for (Product p : current) { // for every product
                model.addRow(new Object[]{p.getId(), p.getCode(), p.getName(), p.getCategoryName(), // columns 1-4
                        p.getUnitPrice(), p.getCostPrice(), p.getStockQty(), p.getReorderLevel()}); // columns 5-8
            } // end for
        } catch (Exception ex) { UI.handle(this, ex); } // database problem
    } // end load

    private void fillForm(Product p) { // copy a product into the inputs
        selectedId = p.getId(); // remember which row is being edited
        txtCode.setText(p.getCode()); // code
        txtName.setText(p.getName()); // name
        cboCategory.setSelectedItem(p.getCategoryName()); // category
        txtPrice.setText(p.getUnitPrice().toPlainString()); // price
        txtCost.setText(p.getCostPrice().toPlainString()); // cost
        txtStock.setText(String.valueOf(p.getStockQty())); // stock
        txtReorder.setText(String.valueOf(p.getReorderLevel())); // reorder level
    } // end fillForm

    private void clearForm() { // reset the inputs
        selectedId = 0; // back to "new product" mode
        txtCode.setText(""); txtName.setText(""); txtPrice.setText(""); // clear text
        txtCost.setText(""); txtStock.setText(""); txtReorder.setText(""); // clear text
        table.clearSelection(); // unselect the grid row
    } // end clearForm

    private Product readForm(int id) throws ValidationException { // VALIDATION: build a product from the inputs
        String code = Validator.requireText(txtCode.getText(), "Product code", 20); // mandatory, max 20
        String name = Validator.requireText(txtName.getText(), "Product name", 80); // mandatory, max 80
        String cat = (String) cboCategory.getSelectedItem(); // chosen category
        if (cat == null) { throw new ValidationException("Select a category."); } // mandatory
        BigDecimal price = Validator.requirePositiveMoney(txtPrice.getText(), "Unit price"); // positive money
        BigDecimal cost = Validator.requirePositiveMoney(txtCost.getText(), "Cost price"); // positive money
        if (cost.compareTo(price) > 0) { throw new ValidationException("Cost price cannot be higher than the unit price."); } // business rule
        int stock = Validator.requireNonNegativeInt(txtStock.getText(), "Stock quantity"); // zero or more
        int reorder = Validator.requireNonNegativeInt(txtReorder.getText(), "Reorder level"); // zero or more
        return new Product(id, code, name, categories.get(cat), cat, price, cost, stock, reorder); // valid product
    } // end readForm

    private void save(boolean isNew) { // insert or update
        try { // any problem goes to the central handler
            if (!isNew && selectedId == 0) { throw new ValidationException("Select a product from the grid to update."); } // need a selection
            Product p = readForm(isNew ? 0 : selectedId); // validate the inputs
            if (isNew) { dao.insert(p); } else { dao.update(p); } // write to the database
            UI.info(this, isNew ? "Product added." : "Product updated."); // confirmation
            clearForm(); // reset inputs
            load(); // refresh grid
        } catch (Exception ex) { UI.handle(this, ex); } // validation or database problem
    } // end save

    private void delete() { // delete the selected product
        try { // any problem goes to the central handler
            if (selectedId == 0) { throw new ValidationException("Select a product from the grid to delete."); } // need a selection
            if (!UI.confirm(this, "Delete this product permanently?")) { return; } // ask first
            dao.delete(selectedId); // remove from the database
            clearForm(); // reset inputs
            load(); // refresh grid
        } catch (Exception ex) { UI.handle(this, ex); } // validation or database problem (e.g. product already sold)
    } // end delete
} // end class
