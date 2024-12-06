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

public class FastExcelReader {

    public void readColumnJFirstRow(String filePath) {
        long startTime = System.currentTimeMillis();

        try (OPCPackage pkg = OPCPackage.open(filePath)) {
            XSSFReader reader = new XSSFReader(pkg);
            SharedStringsTable sharedStringsTable = reader.getSharedStringsTable();

            // Get the first sheet input stream
            InputStream sheetData = reader.getSheetsData().next();

            // Create SAX parser for optimized reading
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            XMLReader parser = saxParser.getXMLReader();

            parser.setContentHandler(new FastRowHandler(sharedStringsTable));
            parser.parse(new InputSource(sheetData));

        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution Time: " + (endTime - startTime) + " ms");
    }

    static class FastRowHandler extends DefaultHandler {
        private final SharedStringsTable sharedStringsTable;
        private boolean isFirstRow = true;
        private boolean isTargetCell = false;
        private StringBuilder cellValue = new StringBuilder();

        public FastRowHandler(SharedStringsTable sharedStringsTable) {
            this.sharedStringsTable = sharedStringsTable;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("row".equals(qName) && isFirstRow) {
                // Ensure we process only the first row
                String rowNum = attributes.getValue("r");
                if (!"1".equals(rowNum)) {
                    isFirstRow = false;
                }
            } else if ("c".equals(qName) && isFirstRow) {
                // Check if this is column J
                String cellReference = attributes.getValue("r");
                if (cellReference != null && cellReference.startsWith("J")) {
                    isTargetCell = true;
                    cellValue.setLength(0); // Clear previous value
                }
            } else if ("v".equals(qName) && isTargetCell) {
                cellValue.setLength(0); // Prepare to read the value
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (isTargetCell) {
                cellValue.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("v".equals(qName) && isTargetCell) {
                String value = cellValue.toString().trim();

                // Handle shared strings
                if (!value.isEmpty() && value.matches("\\d+")) {
                    int sharedStringIndex = Integer.parseInt(value);
                    value = new XSSFRichTextString(sharedStringsTable.getEntryAt(sharedStringIndex)).toString();
                }

                System.out.println("Value in Column J, Row 1: " + value);

                // Stop parsing once we get the value
                throw new SAXException("Target cell value found - stopping parsing");
            }
        }
    }

    public static void main(String[] args) {
        FastExcelReader reader = new FastExcelReader();
        reader.readColumnJFirstRow("path/to/your/excel-file.xlsx");
    }
}
