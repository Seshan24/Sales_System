package com.sales.ui; // package for screens

import com.sales.report.ReportService; // Jasper wrapper
import com.sales.util.UI; // shared helpers
import java.awt.BorderLayout; // layout manager
import java.awt.Cursor; // mouse cursor
import java.awt.FlowLayout; // flow layout
import java.io.File; // file handling
import java.sql.Timestamp; // used to convert a Date safely
import java.time.LocalDate; // date without time
import java.time.ZoneId; // time zone
import java.util.Calendar; // calendar constants
import java.util.Date; // date type
import javax.swing.BorderFactory; // borders
import javax.swing.JFileChooser; // save dialog
import javax.swing.JLabel; // label
import javax.swing.JPanel; // panel
import javax.swing.JSpinner; // date picker control
import javax.swing.SpinnerDateModel; // spinner model for dates
import net.sf.jasperreports.engine.JasperPrint; // filled report

public class ReportPanel extends JPanel { // REPORT screen: choose a period and open the decision report
    private final ReportService service = new ReportService(); // report engine
    private final JSpinner from = dateSpinner(LocalDate.now().minusDays(30)); // default: last 30 days
    private final JSpinner to = dateSpinner(LocalDate.now()); // default: today

    public ReportPanel() { // build the screen
        setLayout(new BorderLayout(10, 10)); // main layout
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // outer padding
        JLabel info = new JLabel("<html><h2>Sales Performance &amp; Restock Decision Report</h2>" // heading
                + "<p>Combines Products, Categories, Sales, Sale Items and Customers to show what sold, what it earned,<br>" // description line 1
                + "and which items management should <b>restock now</b> or <b>review (no sales)</b>.</p></html>"); // description line 2
        add(info, BorderLayout.NORTH); // description on top

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // input row
        controls.add(new JLabel("From:")); // caption
        controls.add(from); // start date
        controls.add(new JLabel("To:")); // caption
        controls.add(to); // end date
        var preview = UI.button("Preview Report", UI.PRIMARY); // preview button
        var pdf = UI.button("Export PDF", UI.SUCCESS); // export button
        preview.addActionListener(e -> run(false)); // preview on screen
        pdf.addActionListener(e -> run(true)); // export to file
        controls.add(preview); // add button
        controls.add(pdf); // add button
        add(controls, BorderLayout.CENTER); // inputs in the middle
    } // end constructor

    private static JSpinner dateSpinner(LocalDate d) { // create a date input control
        Date start = Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()); // convert to java.util.Date
        JSpinner s = new JSpinner(new SpinnerDateModel(start, null, null, Calendar.DAY_OF_MONTH)); // date model
        s.setEditor(new JSpinner.DateEditor(s, "yyyy-MM-dd")); // show as 2026-09-24
        return s; // return the control
    } // end dateSpinner

    private LocalDate value(JSpinner s) { // read a spinner as LocalDate
        return new Timestamp(((Date) s.getValue()).getTime()).toLocalDateTime().toLocalDate(); // safe for any Date subclass
    } // end value

    private void run(boolean toPdf) { // fill the report then preview or export it
        try { // any problem goes to the central handler
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // show the busy cursor
            JasperPrint print = service.salesDecision(value(from), value(to)); // VALIDATED range, runs the SQL
            if (!toPdf) { service.preview(print); return; } // on-screen preview
            JFileChooser fc = new JFileChooser(); // save dialog
            fc.setSelectedFile(new File("SalesDecisionReport.pdf")); // default file name
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { // user chose a file
                String path = fc.getSelectedFile().getAbsolutePath(); // chosen path
                if (!path.toLowerCase().endsWith(".pdf")) { path += ".pdf"; } // force the extension
                service.exportPdf(print, path); // write the PDF
                UI.info(this, "Report exported to:\n" + path); // confirmation
            } // end if
        } catch (Exception ex) { // validation, database or Jasper problem
            UI.handle(this, ex); // show a friendly message
        } finally { // always runs
            setCursor(Cursor.getDefaultCursor()); // restore the cursor
        } // end try
    } // end run
} // end class
