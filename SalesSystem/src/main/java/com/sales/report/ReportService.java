package com.sales.report; // package for reporting

import com.sales.util.DBConnection; // connection singleton
import com.sales.util.ValidationException; // business rule error
import java.io.InputStream; // template stream
import java.sql.Connection; // JDBC connection
import java.sql.Timestamp; // date-time type used by the report parameters
import java.time.LocalDate; // date without time
import java.util.HashMap; // parameter map
import java.util.Map; // map interface
import net.sf.jasperreports.engine.JRException; // Jasper error
import net.sf.jasperreports.engine.JasperCompileManager; // compiles .jrxml
import net.sf.jasperreports.engine.JasperExportManager; // exports PDF
import net.sf.jasperreports.engine.JasperFillManager; // fills the report with data
import net.sf.jasperreports.engine.JasperPrint; // filled report
import net.sf.jasperreports.engine.JasperReport; // compiled report
import net.sf.jasperreports.view.JasperViewer; // preview window

public class ReportService { // wraps JasperReports so screens stay simple
    public JasperPrint salesDecision(LocalDate from, LocalDate to) throws Exception { // build the decision report
        if (from.isAfter(to)) { throw new ValidationException("'From' date cannot be after 'To' date."); } // date range rule
        try (InputStream in = getClass().getResourceAsStream("/reports/SalesDecisionReport.jrxml"); // load the template from the jar
             Connection con = DBConnection.getInstance().getConnection()) { // open the database
            if (in == null) { throw new IllegalStateException("Report template not found."); } // packaging problem
            JasperReport report = JasperCompileManager.compileReport(in); // compile the template
            Map<String, Object> params = new HashMap<>(); // report parameters
            params.put("FROM_DATE", Timestamp.valueOf(from.atStartOfDay())); // start of the first day
            params.put("TO_DATE", Timestamp.valueOf(to.atTime(23, 59, 59))); // end of the last day
            return JasperFillManager.fillReport(report, params, con); // run the SQL and lay out the pages
        } // end try
    } // end salesDecision

    public void preview(JasperPrint print) { // show the report on screen
        JasperViewer.viewReport(print, false); // false = do not exit the application when closed
    } // end preview

    public void exportPdf(JasperPrint print, String path) throws JRException { // save the report as PDF
        JasperExportManager.exportReportToPdfFile(print, path); // write the file
    } // end exportPdf
} // end class
