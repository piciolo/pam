import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * Estrae dati dai PDF Bolla_Consegna e Distinta
 */
public class PDFDataExtractor {
    
    public static class ExtractedData {
        public String documentType; // BOLLA o DISTINTA
        public String documentDate; // Data del documento
        public String dayOfWeek;    // Giorno della settimana
        public List<ProductRow> invioProdotti;      // Prodotti invio
        public List<ProductRow> resoProdotti;       // Prodotti reso
        
        public ExtractedData() {
            invioProdotti = new ArrayList<>();
            resoProdotti = new ArrayList<>();
        }
    }
    
    public static class ProductRow {
        public String title;        // Titolo prodotto (es: GAZZETTINO PD SAB)
        public int quantityInvio;   // Q.C. Invio
        public double lordo;        // Prezzo lordo
        public int quantityReso;    // Q.C. Reso (per Distinta)
        
        public ProductRow(String title, int qtyInvio, double lordo, int qtyReso) {
            this.title = title;
            this.quantityInvio = qtyInvio;
            this.lordo = lordo;
            this.quantityReso = qtyReso;
        }
    }
    
    /**
     * Estrae dati dal PDF usando Python script
     */
    public static ExtractedData extractFromPDF(String pdfFilePath) throws Exception {
        ExtractedData data = new ExtractedData();
        
        // Leggi il PDF come testo (usando Apache PDFBox tramite Python)
        String pdfText = extractPDFText(pdfFilePath);
        
        // Parse il testo
        parsePDFText(pdfText, data);
        
        return data;
    }
    
    /**
     * Estrae testo dal PDF usando uno script Python (pdfplumber)
     */
    private static String extractPDFText(String pdfFilePath) throws Exception {
        // Crea uno script Python temporaneo
        File tempScript = File.createTempFile("extract_pdf", ".py");
        tempScript.deleteOnExit();
        
        String pythonCode = "import pdfplumber\n" +
            "import json\n" +
            "import sys\n" +
            "\n" +
            "pdf_path = r'" + pdfFilePath + "'\n" +
            "\n" +
            "try:\n" +
            "    with pdfplumber.open(pdf_path) as pdf:\n" +
            "        page = pdf.pages[0]\n" +
            "        text = page.extract_text()\n" +
            "        print(text)\n" +
            "except Exception as e:\n" +
            "    print('ERROR: ' + str(e), file=sys.stderr)\n" +
            "    sys.exit(1)\n";
        
        Files.write(tempScript.toPath(), pythonCode.getBytes());
        
        // Esegui Python
        ProcessBuilder pb = new ProcessBuilder(
            "f:\\PAM\\.venv\\Scripts\\python.exe",
            tempScript.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Errore nell'estrazione PDF: " + output.toString());
        }
        
        return output.toString();
    }
    
    /**
     * Parsing del testo estratto dal PDF
     */
    private static void parsePDFText(String text, ExtractedData data) {
        String[] lines = text.split("\n");
        
        // DEBUG: Log il primo 500 caratteri del testo estratto
        System.out.println("[DEBUG] Testo estratto dal PDF (primi 500 char):");
        System.out.println(text.substring(0, Math.min(500, text.length())));
        System.out.println("[DEBUG] Numero di righe: " + lines.length);
        
        // Determina il tipo di documento
        data.documentType = text.contains("BOLLA CONSEGNA") ? "BOLLA" : "DISTINTA";
        System.out.println("[DEBUG] Tipo documento: " + data.documentType);
        
        // Estrai data documento
        Pattern datePattern = Pattern.compile("DEL ([A-Z]+)\\s+(\\d{1,2})/(\\d{2})/(\\d{4})");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            String dayName = dateMatcher.group(1);
            String day = dateMatcher.group(2);
            String month = dateMatcher.group(3);
            String year = dateMatcher.group(4);
            data.documentDate = String.format("%s/%s/%s", day, month, year);
            data.dayOfWeek = italianToDayOfWeek(dayName);
            System.out.println("[DEBUG] Data: " + data.documentDate + ", Giorno: " + data.dayOfWeek);
        } else {
            System.out.println("[DEBUG] Data non trovata nel testo");
        }
        
        // Parsing delle righe prodotto
        boolean inDataSection = false;
        for (String line : lines) {
            line = line.trim();
            
            // Salta la riga di intestazione
            if (line.contains("Cd.Pub") && line.contains("Q.C.") && line.contains("Lordo")) {
                inDataSection = true;
                System.out.println("[DEBUG] Intestazione trovata, inizio parsing prodotti");
                continue;
            }
            
            if (!inDataSection || line.isEmpty()) {
                continue;
            }
            
            System.out.println("[DEBUG] Parsing riga: " + line);
            
            // Parse riga prodotto
            ProductRow row = parseProductLine(line);
            if (row != null) {
                data.invioProdotti.add(row);
                System.out.println("[DEBUG] Prodotto aggiunto: " + row.title + " x" + row.quantityInvio);
            }
        }
        
        System.out.println("[DEBUG] Prodotti trovati: " + data.invioProdotti.size());
    }
    
    /**
     * Parsing di una riga prodotto
     * Formato: 15386 GAZZETTINO PD SAB 60124 17 1,20 ? 20,40 ? 60124 17 1,20 ?
     */
    private static ProductRow parseProductLine(String line) {
        try {
            // Pattern più semplice per estrarre i dati
            // Formato: CODICE TITOLO NCOPIE Q.C. LORDO ...
            String[] parts = line.split("\\s+");
            
            if (parts.length < 5) {
                return null;
            }
            
            // Salta se la riga è un totale o altra etichetta
            if (line.contains("TOTALE") || line.contains("NUMERO") || line.contains("Page")) {
                return null;
            }
            
            try {
                int codice = Integer.parseInt(parts[0]);
                // Il titolo è da parts[1] fino a quando trovo il numero di copie
                // Cerco il primo numero dopo il codice che non sia parte del titolo
                
                int titleEndIdx = 1;
                int nCopie = 0;
                for (int i = 1; i < parts.length; i++) {
                    try {
                        nCopie = Integer.parseInt(parts[i]);
                        titleEndIdx = i;
                        break;
                    } catch (NumberFormatException e) {
                        // Continue - still part of title
                    }
                }
                
                if (titleEndIdx <= 1) {
                    return null; // Couldn't find number of copies
                }
                
                String title = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, titleEndIdx));
                
                // Q.C. è il prossimo numero dopo N° Cp.
                if (titleEndIdx + 1 >= parts.length) {
                    return null;
                }
                
                int qc = Integer.parseInt(parts[titleEndIdx + 1]);
                
                // Lordo è il prossimo numero (può contenere virgola)
                if (titleEndIdx + 2 >= parts.length) {
                    return null;
                }
                
                String lordoStr = parts[titleEndIdx + 2];
                double lordoVal = Double.parseDouble(lordoStr.replace(",", "."));
                
                System.out.println("[DEBUG] Estratto: " + codice + " | " + title + " | Q.C.=" + qc + " | Lordo=" + lordoVal);
                
                return new ProductRow(title, qc, lordoVal, 0);
            } catch (NumberFormatException e) {
                return null;
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Errore parsing: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Converte nome giorno italiano a short (SAB -> Sab)
     */
    private static String italianToDayOfWeek(String italianDay) {
        Map<String, String> dayMap = new HashMap<>();
        dayMap.put("LUNEDI", "Lun");
        dayMap.put("MARTEDI", "Mar");
        dayMap.put("MERCOLEDI", "Mer");
        dayMap.put("GIOVEDI", "Gio");
        dayMap.put("VENERDI", "Ven");
        dayMap.put("SABATO", "Sab");
        dayMap.put("DOMENICA", "Dom");
        dayMap.put("SAB", "Sab");
        dayMap.put("LUN", "Lun");
        dayMap.put("MAR", "Mar");
        dayMap.put("MER", "Mer");
        dayMap.put("GIO", "Gio");
        dayMap.put("VEN", "Ven");
        dayMap.put("DOM", "Dom");
        
        return dayMap.getOrDefault(italianDay.toUpperCase(), "Lun");
    }
}
