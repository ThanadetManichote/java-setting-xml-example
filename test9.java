import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelBatchProcessor {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private YourEntityRepository yourEntityRepository;

    public void processExcelFile() throws Exception {
        try (FileInputStream fis = new FileInputStream("src/main/resources/your-file.xlsx");
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            List<YourEntity> batchRecords = new ArrayList<>();

            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                YourEntity entity = new YourEntity();

                // Assuming your entity has fields corresponding to columns in the Excel sheet
                entity.setField1(row.getCell(0).toString());
                entity.setField2(row.getCell(1).toString());
                entity.setField3(row.getCell(2).toString());

                batchRecords.add(entity);

                if (batchRecords.size() == BATCH_SIZE) {
                    sendBatchToRepository(batchRecords);
                    batchRecords.clear();
                }
            }

            // Send remaining records to repository
            if (!batchRecords.isEmpty()) {
                sendBatchToRepository(batchRecords);
            }
        }
    }

    private void sendBatchToRepository(List<YourEntity> batchRecords) {
        yourEntityRepository.saveAll(batchRecords);
    }

    
}

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class YourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String field1;
    private String field2;
    private String field3;

    // Getters and Setters
}
