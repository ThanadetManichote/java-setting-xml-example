package com.example.demo.service;

import net.sf.jasperreports.engine.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    public void generateReport() throws Exception {
        // Path to the .jasper file in resources
        String reportPath = new ClassPathResource("template.jasper").getFile().getAbsolutePath();

        // Parameters to fill the report
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("Param1", "Value1");
        parameters.put("Param2", 123);

        // Data source (can be empty or use a data source for your report)
        JRDataSource dataSource = new JREmptyDataSource();

        // Compile and fill the report
        JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export the report to a PDF file in the "resources/download" folder
        File downloadDir = new File(new ClassPathResource("download").getFile().getAbsolutePath());
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        File pdfFile = new File(downloadDir, "report.pdf");
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            JasperExportManager.exportReportToPdfStream(jasperPrint, fos);
        }
    }
}
