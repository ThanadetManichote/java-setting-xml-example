import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JasperImageExample {

    public static void main(String[] args) {
        try {
            // Load the JasperReport template (.jrxml or .jasper file)
            String reportPath = "path/to/your/report.jasper";
            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

            // Create a JasperPrint object by filling the report with data
            Map<String, Object> parameters = new HashMap<>();
            // Add any parameters needed by your report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // Export the report to an image file (e.g., PNG)
            String imagePath = "path/to/save/output.png";
            File imageFile = new File(imagePath);

            JRPdfExporter exporter = new JRPdfExporter();  // Use JRPdfExporter for image formats
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(imageFile));
            exporter.exportReport();

            System.out.println("Image generated successfully at: " + imageFile.getAbsolutePath());

        } catch (JRException e) {
            e.printStackTrace();
        }
    }
}
