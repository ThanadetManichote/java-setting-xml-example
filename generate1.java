import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JRException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ReportController {

    @GetMapping("/generateReport")
    public ModelAndView generateReport(HttpServletResponse response) {
        try {
            // Fill the report (you need to implement this part based on your data source)
            JasperPrint jasperPrint = /* Logic to fill the report */;

            // Export to PDF
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            response.setContentType("application/pdf");
            response.getOutputStream().write(pdfBytes);

            // Optionally, you can also generate an image (e.g., PNG)
            // byte[] imageBytes = JasperPrintManager.printPageToImage(jasperPrint, 0, 1.0f);
            // response.setContentType("image/png");
            // response.getOutputStream().write(imageBytes);

        } catch (JRException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
