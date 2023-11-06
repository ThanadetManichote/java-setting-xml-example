import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPngExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class JasperPNGExample {

    public static void main(String[] args) {
        try {
            // Load the JasperReport template (.jrxml or .jasper file)
            String reportPath = "path/to/your/SimpleReport.jasper";
            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

            // Create a JasperPrint object by filling the report with data
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "My Simple Report"); // Pass any parameters needed by your report

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());

            // Export the JasperPrint to a PNG file
            String pngPath = "path/to/save/SimpleReport.png";
            File pngFile = new File(pngPath);

            JRPngExporter exporter = new JRPngExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(pngFile));
            exporter.exportReport();

            System.out.println("PNG generated successfully at: " + pngFile.getAbsolutePath());

        } catch (JRException e) {
            e.printStackTrace();
        }
    }
}
