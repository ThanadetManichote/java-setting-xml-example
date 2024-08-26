import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ExcelToCsvConverter {

    public static void main(String[] args) {
        String excelPath = "src/main/resources/your-file.xlsx";
        String csvPath = "src/main/resources/your-file.csv";
        
        try (FileInputStream fis = new FileInputStream(new File(excelPath));
             Workbook workbook = new XSSFWorkbook(fis);
             FileWriter writer = new FileWriter(new File(csvPath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Column1", "Column2", "Column3"))) { // Adjust header as needed
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                for (Cell cell : row) {
                    csvPrinter.print(cell.toString());
                }
                csvPrinter.println();
            }

            csvPrinter.flush();
            System.out.println("Excel to CSV conversion completed!");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
