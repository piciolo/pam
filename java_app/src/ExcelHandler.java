import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Gestisce la lettura e scrittura dei file Excel
 */
public class ExcelHandler {
    
    private String excelFilePath;
    private Workbook workbook;
    
    public ExcelHandler(String filePath) throws IOException {
        this.excelFilePath = filePath;
        System.out.println("[DEBUG ExcelHandler] Apertura file: " + filePath);
        
        File f = new File(filePath);
        if (!f.exists()) {
            throw new FileNotFoundException("File non trovato: " + filePath);
        }
        
        System.out.println("[DEBUG ExcelHandler] File esiste, dimensione: " + f.length() + " bytes");
        System.out.println("[DEBUG ExcelHandler] Permessi - lettura: " + f.canRead() + ", scrittura: " + f.canWrite());
        System.out.println("[DEBUG ExcelHandler] Ultima modifica: " + new java.util.Date(f.lastModified()));
        
        try {
            long readStartMs = System.currentTimeMillis();
            System.out.println("[DEBUG ExcelHandler] Lettura file in memoria...");
            byte[] fileBytes = java.nio.file.Files.readAllBytes(new File(filePath).toPath());
            long readElapsedMs = System.currentTimeMillis() - readStartMs;
            System.out.println("[DEBUG ExcelHandler] Letti " + fileBytes.length + " bytes in " + readElapsedMs + " ms");

            long parseStartMs = System.currentTimeMillis();
            System.out.println("[DEBUG ExcelHandler] Creando WorkbookFactory...");
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<Workbook> future = executor.submit(() -> {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(fileBytes)) {
                        return WorkbookFactory.create(bais);
                    }
                });
                this.workbook = future.get(15, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new IOException("Timeout durante apertura Excel (15s). Il file potrebbe essere corrotto o molto lento da leggere.", e);
            } catch (ExecutionException e) {
                throw new IOException("Errore durante parsing Excel: " + e.getCause().getMessage(), e.getCause());
            } finally {
                executor.shutdownNow();
            }
            long parseElapsedMs = System.currentTimeMillis() - parseStartMs;
            System.out.println("[DEBUG ExcelHandler] Workbook creato in " + parseElapsedMs + " ms");
            
            System.out.println("[DEBUG ExcelHandler] Workbook caricato con successo! Sheet totali: " + workbook.getNumberOfSheets());
        } catch (Exception e) {
            System.out.println("[ERROR ExcelHandler] Errore caricamento workbook: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Errore durante caricamento Excel", e);
        }
    }
    
    /**
     * Popola i dati estratti da Bolla_Consegna.pdf
     * Colonna A: Q.C. da Bolla
     * Colonna D: Lordo da Bolla
     */
    public void populateFromBolla(PDFDataExtractor.ExtractedData bollData) throws Exception {
        // Ricerca il sheet day-of-week (Lun, Mar, Mer, ecc.)
        String daySheet = bollData.dayOfWeek;
        System.out.println("[DEBUG ExcelHandler] Ricercando sheet: '" + daySheet + "'");
        System.out.println("[DEBUG ExcelHandler] Sheet disponibili: ");
        
        int numSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numSheets; i++) {
            String sheetName = workbook.getSheetName(i);
            System.out.println("  - " + sheetName);
        }
        
        Sheet sheet = workbook.getSheet(daySheet);
        if (sheet == null) {
            throw new Exception("Sheet '" + daySheet + "' non trovato. Disponibili: " + numSheets + " sheet");
        }
        
        System.out.println("[DEBUG ExcelHandler] Sheet '" + daySheet + "' trovato!");
        
        // Popola i prodotti INVIO
        int currentRow = 8; // Riga 9 (0-based)
        System.out.println("[DEBUG ExcelHandler] Inizio popolazione Bolla - " + bollData.invioProdotti.size() + " prodotti");
        
        for (PDFDataExtractor.ProductRow product : bollData.invioProdotti) {
            if (currentRow > 32) break; // Max riga 33
            
            Row row = sheet.getRow(currentRow);
            if (row == null) row = sheet.createRow(currentRow);
            
            // Colonna A: Q.C. Bolla
            Cell cellQC = row.getCell(0);
            if (cellQC == null) cellQC = row.createCell(0);
            cellQC.setCellValue(product.quantityInvio);
            
            // Colonna D: Lordo Bolla
            Cell cellLordo = row.getCell(3);
            if (cellLordo == null) cellLordo = row.createCell(3);
            cellLordo.setCellValue(product.lordo);
            
            System.out.println("[DEBUG ExcelHandler] Riga " + (currentRow+1) + " - " + product.title + ": A=" + product.quantityInvio + ", D=" + product.lordo);
            
            currentRow++;
        }
        System.out.println("[DEBUG ExcelHandler] Popolazione Bolla completata");
    }
    
    /**
     * Popola i dati estratti da Distinta.pdf
     * Colonna B: Q.C. da Distinta
     * Colonna E: Lordo da Distinta
     */
    public void populateFromDistinta(PDFDataExtractor.ExtractedData distinaData) throws Exception {
        // Riccerca il sheet day-of-week (Lun, Mar, Mer, ecc.)
        String daySheet = distinaData.dayOfWeek;
        Sheet sheet = workbook.getSheet(daySheet);
        if (sheet == null) {
            throw new Exception("Sheet '" + daySheet + "' non trovato per " + distinaData.dayOfWeek);
        }
        
        // Popola i prodotti INVIO
        int currentRow = 8; // Riga 9 (0-based)
        for (PDFDataExtractor.ProductRow product : distinaData.invioProdotti) {
            if (currentRow > 32) break; // Max riga 33
            
            Row row = sheet.getRow(currentRow);
            if (row == null) row = sheet.createRow(currentRow);
            
            // Colonna B: Q.C. Distinta
            Cell cellQC = row.getCell(1);
            if (cellQC == null) cellQC = row.createCell(1);
            cellQC.setCellValue(product.quantityInvio);
            
            // Colonna E: Lordo Distinta
            Cell cellLordo = row.getCell(4);
            if (cellLordo == null) cellLordo = row.createCell(4);
            cellLordo.setCellValue(product.lordo);
            
            currentRow++;
        }
    }
    
    
    /**
     * Popola una sezione (INVIO o RESO) di QUOTIDIANI
     * startRow: riga iniziale dati (0-based, normalmente 8 per riga 9)
     * startCol: colonna iniziale (0 per INVIO, 3 per RESO)
     */
    private void populateSection(Sheet sheet, List<PDFDataExtractor.ProductRow> products,
                                 String sectionType, int startRow, int startCol) {
        int currentRow = startRow;
        int maxRows = 25; // Righe disponibili per quotidiani (9-33)
        
        for (PDFDataExtractor.ProductRow product : products) {
            if (currentRow - startRow >= maxRows) {
                break; // Non ci sono più righe disponibili
            }
            
            Row row = sheet.getRow(currentRow);
            if (row == null) {
                row = sheet.createRow(currentRow);
            }
            
            // Colonna A (Pezzi)
            Cell cellPezzi = row.getCell(startCol);
            if (cellPezzi == null) {
                cellPezzi = row.createCell(startCol);
            }
            cellPezzi.setCellValue(product.quantityInvio);
            
            // Colonna B (Prezzo)
            Cell cellPrezzo = row.getCell(startCol + 1);
            if (cellPrezzo == null) {
                cellPrezzo = row.createCell(startCol + 1);
            }
            cellPrezzo.setCellValue(product.lordo);
            
            // Colonna C (Totale) - Formula
            Cell cellTotale = row.getCell(startCol + 2);
            if (cellTotale == null) {
                cellTotale = row.createCell(startCol + 2);
            }
            String formula = String.format("=%s%d*%s%d",
                cellLetterFromIndex(startCol),
                currentRow + 1,
                cellLetterFromIndex(startCol + 1),
                currentRow + 1
            );
            cellTotale.setCellFormula(formula);
            
            currentRow++;
        }
    }
    
    /**
     * Valida che il sheet abbia la struttura attesa
     */
    private void validateSheetStructure(Sheet sheet) throws Exception {
        // Verifica che ci sia la riga "INVIO QUOTIDIANI" nella cella A7
        Row row7 = sheet.getRow(6); // 0-based, quindi riga 7 è indice 6
        if (row7 == null || row7.getCell(0) == null) {
            throw new Exception("Sheet non ha la struttura attesa. Riga 7 non trovata.");
        }
        
        Cell cell = row7.getCell(0);
        String cellValue = cell.getStringCellValue();
        if (!cellValue.contains("INVIO QUOTIDIANI")) {
            throw new Exception("Sheet non ha la struttura attesa. 'INVIO QUOTIDIANI' non trovato in A7");
        }
    }
    
    /**
     * Converte indice colonna a lettera (0=A, 1=B, ecc.)
     */
    private String cellLetterFromIndex(int index) {
        StringBuilder sb = new StringBuilder();
        while (index >= 0) {
            sb.insert(0, (char) ('A' + (index % 26)));
            index = index / 26 - 1;
        }
        return sb.toString();
    }
    
    /**
     * Salva il workbook
     */
    public void save() throws IOException {
        System.out.println("[DEBUG ExcelHandler] Salvataggio file: " + excelFilePath);
        workbook.setForceFormulaRecalculation(true);
        Path targetPath = Path.of(excelFilePath);
        Path parentDir = targetPath.getParent();
        if (parentDir == null) {
            throw new IOException("Percorso Excel non valido: " + excelFilePath);
        }
        Path tempFile = Files.createTempFile(parentDir, "pam_excel_", ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            workbook.write(fos);
        }
        try {
            Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Impossibile salvare il file Excel. Chiudi il file se è aperto: " + excelFilePath, e);
        }
        System.out.println("[DEBUG ExcelHandler] File salvato correttamente!");
    }
    
    /**
     * Chiude il workbook
     */
    public void close() throws IOException {
        workbook.close();
    }
}
