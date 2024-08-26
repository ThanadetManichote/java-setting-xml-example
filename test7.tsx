import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class ExcelToDatabaseProcessor {

    private static final int BATCH_SIZE = 1000;

    public void processLargeExcelFileAndWriteToDatabase() throws Exception {
        OPCPackage pkg = OPCPackage.open(getClass().getClassLoader().getResourceAsStream("your-file.xlsx"));
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();

        XMLReader parser = XMLReaderFactory.createXMLReader();
        SheetHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);

        try (InputStream sheet = r.getSheetsData().next()) {
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
        }
    }

    private class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;

        private List<String[]> batchRecords = new ArrayList<>();
        private Connection connection;

        public SheetHandler(SharedStringsTable sst) throws Exception {
            this.sst = sst;
            // Establish the database connection
            connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password");
            connection.setAutoCommit(false);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("c")) { // c => cell
                String cellType = attributes.getValue("t");
                nextIsString = "s".equals(cellType);
            }
            lastContents = "";
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (nextIsString) {
                int idx = Integer.parseInt(lastContents);
                lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
                nextIsString = false;
            }

            if (qName.equals("v")) { // v => value within a cell
                // Collect cell data (adjust as needed)
                batchRecords.add(new String[]{lastContents}); // Example for single column

                if (batchRecords.size() == BATCH_SIZE) {
                    writeBatchToDatabase();
                    batchRecords.clear();
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        private void writeBatchToDatabase() {
            String sql = "INSERT INTO your_table_name (column_name) VALUES (?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (String[] record : batchRecords) {
                    pstmt.setString(1, record[0]); // Adjust based on number of columns
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void endDocument() {
            // Write any remaining records to the database
            if (!batchRecords.isEmpty()) {
                writeBatchToDatabase();
            }
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ExcelToDatabaseProcessor processor = new ExcelToDatabaseProcessor();
        try {
            processor.processLargeExcelFileAndWriteToDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
