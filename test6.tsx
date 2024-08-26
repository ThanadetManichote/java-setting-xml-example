public class ExcelProcessor {

    public void processLargeFile(String inputFilePath, String outputFilePath) throws IOException {
        // Open the existing Excel file using XSSFWorkbook
        try (FileInputStream fis = new FileInputStream(inputFilePath);
             XSSFWorkbook xssfWorkbook = new XSSFWorkbook(fis)) {

            // Create an SXSSFWorkbook from the XSSFWorkbook to handle large files
            try (SXSSFWorkbook workbook = new SXSSFWorkbook(xssfWorkbook, 100)) {
                SXSSFSheet sheet = workbook.getSheetAt(0);

                // Process each row
                for (Row row : sheet) {
                    // Example: Print each cell value
                    for (Cell cell : row) {
                        System.out.print(cell.toString() + "\t");
                    }
                    System.out.println();
                }

                // Optionally, modify the file and write it to an output file
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    workbook.write(fos);
                }

                // Dispose of temporary files created by SXSSF
                workbook.dispose();
            }
        }
    }
    
    public static void main(String[] args) {
        ExcelProcessor processor = new ExcelProcessor();
        try {
            processor.processLargeFile("path/to/your/input.xlsx", "path/to/your/output.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}