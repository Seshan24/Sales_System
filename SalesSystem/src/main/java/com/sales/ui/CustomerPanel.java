package com.sales.ui; // package for screens

import com.sales.dao.CustomerDAO; // customer data access
import com.sales.model.Customer; // customer model
import com.sales.util.UI; // shared helpers
import com.sales.util.ValidationException; // business rule error
import com.sales.util.Validator; // input validation
import java.awt.BorderLayout; // layout manager
import java.awt.FlowLayout; // flow layout
import java.awt.GridLayout; // grid layout
import java.util.List; // list interface
import javax.swing.BorderFactory; // borders
import javax.swing.JComboBox; // drop-down
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JScrollPane; // scroll container
import javax.swing.JTable; // table
import javax.swing.JTextField; // text input
import javax.swing.table.DefaultTableModel; // table model

public class CustomerPanel extends JPanel { // INPUT UI: customer maintenance
    private final CustomerDAO dao = new CustomerDAO(); // data source
    private final JTextField txtName = new JTextField(); // name input
    private final JTextField txtPhone = new JTextField(); // phone input
    private final JTextField txtEmail = new JTextField(); // email input
    private final JTextField txtAddress = new JTextField(); // address input
    private final JComboBox<String> cboType = new JComboBox<>(new String[]{"RETAIL", "WHOLESALE"}); // customer type picker
    private final JTextField txtSearch = new JTextField(15); // search box
    private final DefaultTableModel model = UI.model("ID", "Name", "Phone", "Email", "Address", "Type"); // grid model
    private final JTable table = new JTable(model); // customer grid
    private List<Customer> current; // customers currently shown
    private int selectedId = 0; // 0 means "new customer"

    public CustomerPanel() { // build the screen
        setLayout(new BorderLayout(10, 10)); // main layout
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // outer padding
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8)); // label/field pairs, two per row
        form.setBorder(BorderFactory.createTitledBorder("Customer Details")); // panel title
        UI.addRow(form, "Name *", txtName); // field
        UI.addRow(form, "Phone *", txtPhone); // field
        UI.addRow(form, "Email", txtEmail); // field
        UI.addRow(form, "Type *", cboType); // field
        UI.addRow(form, "Address", txtAddress); // field
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
        load(); // first load of the grid
    } // end constructor

    private void load() { // refill the grid from the database
        try { // any problem goes to the central handler
            current = dao.search(txtSearch.getText().trim()); // query with the search text
            model.setRowCount(0); // clear the grid
            for (Customer c : current) { // for every customer
                model.addRow(new Object[]{c.getId(), c.getName(), c.getPhone(), c.getEmail(), c.getAddress(), c.getType()}); // one row
            } // end for
        } catch (Exception ex) { UI.handle(this, ex); } // database problem
    } // end load

    private void fillForm(Customer c) { // copy a customer into the inputs
        selectedId = c.getId(); // remember which row is being edited
        txtName.setText(c.getName()); // name
        txtPhone.setText(c.getPhone()); // phone
        txtEmail.setText(c.getEmail() == null ? "" : c.getEmail()); // email (null becomes empty)
        txtAddress.setText(c.getAddress() == null ? "" : c.getAddress()); // address (null becomes empty)
        cboType.setSelectedItem(c.getType()); // type
    } // end fillForm

    private void clearForm() { // reset the inputs
        selectedId = 0; // back to "new customer" mode
        txtName.setText(""); txtPhone.setText(""); txtEmail.setText(""); txtAddress.setText(""); // clear text
        cboType.setSelectedIndex(0); // default type
        table.clearSelection(); // unselect the grid row
    } // end clearForm

    private Customer readForm(int id) throws ValidationException { // VALIDATION: build a customer from the inputs
        String name = Validator.requireText(txtName.getText(), "Name", 80); // mandatory, max 80
        String phone = Validator.requirePhone(txtPhone.getText()); // mandatory, digit format
        String email = Validator.optionalEmail(txtEmail.getText()); // optional, format checked
        String address = txtAddress.getText().trim().isEmpty() ? null : Validator.requireText(txtAddress.getText(), "Address", 150); // optional, max 150
        return new Customer(id, name, phone, email, address, (String) cboType.getSelectedItem()); // valid customer
    } // end readForm

    private void save(boolean isNew) { // insert or update
        try { // any problem goes to the central handler
            if (!isNew && selectedId == 0) { throw new ValidationException("Select a customer from the grid to update."); } // need a selection
            Customer c = readForm(isNew ? 0 : selectedId); // validate the inputs
            if (isNew) { dao.insert(c); } else { dao.update(c); } // write to the database
            UI.info(this, isNew ? "Customer added." : "Customer updated."); // confirmation
            clearForm(); // reset inputs
            load(); // refresh grid
        } catch (Exception ex) { UI.handle(this, ex); } // validation or database problem (e.g. duplicate phone)
    } // end save

    private void delete() { // delete the selected customer
        try { // any problem goes to the central handler
            if (selectedId == 0) { throw new ValidationException("Select a customer from the grid to delete."); } // need a selection
            if (!UI.confirm(this, "Delete this customer permanently?")) { return; } // ask first
            dao.delete(selectedId); // remove from the database
            clearForm(); // reset inputs
            load(); // refresh grid
        } catch (Exception ex) { UI.handle(this, ex); } // validation or database problem (e.g. customer has sales)
    } // end delete
} // end class
