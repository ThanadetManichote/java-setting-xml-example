import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;

public class ExcelStreamingReader {

    public void excelReaderColumnJ1(String filePath) {
        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();

            // Use SAXParserFactory to create XMLReader
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();
            FirstRowColumnJHandler handler = new FirstRowColumnJHandler(sharedStringsTable);
            parser.setContentHandler(handler);

            // Parse the first sheet (only the first sheet in this example)
            InputStream sheetData = reader.getSheetsData().next();
            InputSource sheetSource = new InputSource(sheetData);
            parser.parse(sheetSource);

            if (!handler.isValueFound()) {
                System.out.println("Value in Column J, Row 1 not found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class FirstRowColumnJHandler extends DefaultHandler {
        private final SharedStringsTable sharedStringsTable;
        private boolean isProcessingFirstRow = true;
        private boolean isProcessingColumnJ = false;
        private boolean valueFound = false;
        private StringBuilder cellValue = new StringBuilder();

        public FirstRowColumnJHandler(SharedStringsTable sharedStringsTable) {
            this.sharedStringsTable = sharedStringsTable;
        }

        public boolean isValueFound() {
            return valueFound;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("row".equals(qName) && isProcessingFirstRow) {
                // Ensure we're processing only the first row
                String rowNum = attributes.getValue("r");
                if (!"1".equals(rowNum)) {
                    isProcessingFirstRow = false; // Stop after the first row
                }
            } else if ("c".equals(qName) && isProcessingFirstRow) {
                // Check if the cell is in Column J (e.g., J1)
                String cellReference = attributes.getValue("r"); // e.g., "J1"
                if (cellReference != null && cellReference.startsWith("J")) {
                    isProcessingColumnJ = true;

                    // Clear previous value (if any)
                    cellValue.setLength(0);
                }
            } else if ("v".equals(qName) && isProcessingColumnJ) {
                // This is where we get the value
                cellValue.setLength(0); // Clear previous value
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (isProcessingColumnJ) {
                cellValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("v".equals(qName) && isProcessingColumnJ) {
                String value = cellValue.toString().trim();

                // Check if it's a shared string (i.e., a text value stored in the shared strings table)
                if (!value.isEmpty() && sharedStringsTable != null && value.matches("\\d+")) {
                    int sharedStringIndex = Integer.parseInt(value);
                    value = sharedStringsTable.getItemAt(sharedStringIndex).toString();
                }

                System.out.println("Value in Column J, Row 1: " + value); // Log the result
                valueFound = true;

                // Stop parsing once the value is found
                throw new SAXException("Value Found - Stopping Parsing");
            }
        }
    }

    public static void main(String[] args) {
        ExcelStreamingReader reader = new ExcelStreamingReader();
        reader.excelReaderColumnJ1("path/to/your/excel-file.xlsx");
    }
}