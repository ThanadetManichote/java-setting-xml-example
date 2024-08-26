import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ExcelBatchProcessor {

    private static final int BATCH_SIZE = 1000;

    public void processExcelFile() throws Exception {
        try (FileInputStream fis = new FileInputStream("src/main/resources/your-file.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(fis);
             Connection connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password")) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String[]> batchRecords = new ArrayList<>();
            connection.setAutoCommit(false);

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                String[] record = new String[row.getLastCellNum()];

                for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    record[cellIndex] = cell.toString();
                }

                batchRecords.add(record);

                if (batchRecords.size() == BATCH_SIZE) {
                    writeBatchToDatabase(batchRecords, connection);
                    batchRecords.clear();
                }
            }

            // Write remaining records to the database
            if (!batchRecords.isEmpty()) {
                writeBatchToDatabase(batchRecords, connection);
            }

            connection.commit();
        }
    }

    private void writeBatchToDatabase(List<String[]> batchRecords, Connection connection) throws Exception {
        String sql = "INSERT INTO your_table_name (column1, column2, column3) VALUES (?, ?, ?)"; // Adjust based on columns
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String[] record : batchRecords) {
                pstmt.setString(1, record[0]);
                pstmt.setString(2, record[1]);
                pstmt.setString(3, record[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public static void main(String[] args) {
        ExcelBatchProcessor processor = new ExcelBatchProcessor();
        try {
            processor.processExcelFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
