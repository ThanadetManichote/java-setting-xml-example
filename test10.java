import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelSAXProcessor {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private YourEntityRepository yourEntityRepository;

    public void processLargeExcelFile() throws Exception {
        OPCPackage pkg = OPCPackage.open(getClass().getClassLoader().getResourceAsStream("your-file.xlsx"));
        XSSFReader reader = new XSSFReader(pkg);
        SharedStringsTable sst = reader.getSharedStringsTable();

        XMLReader parser = XMLReaderFactory.createXMLReader();
        SheetHandler handler = new SheetHandler(sst);
        parser.setContentHandler(handler);

        try (InputStream sheet = reader.getSheetsData().next()) {
            InputSource sheetSource = new InputSource(sheet);
            parser.parse(sheetSource);
        }
    }

    private class SheetHandler extends DefaultHandler {
        private SharedStringsTable sst;
        private String lastContents;
        private boolean nextIsString;

        private List<YourEntity> batchRecords = new ArrayList<>();

        public SheetHandler(SharedStringsTable sst) {
            this.sst = sst;
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
                // Process cell data
                // Example for single-column entity
                YourEntity entity = new YourEntity();
                entity.setField1(lastContents); // Adjust according to your entity structure
                batchRecords.add(entity);

                if (batchRecords.size() == BATCH_SIZE) {
                    sendBatchToRepository(batchRecords);
                    batchRecords.clear();
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        private void sendBatchToRepository(List<YourEntity> batchRecords) {
            yourEntityRepository.saveAll(batchRecords);
        }

        @Override
        public void endDocument() {
            // Send any remaining records to the repository
            if (!batchRecords.isEmpty()) {
                sendBatchToRepository(batchRecords);
            }
        }
    }
}
