import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;

/**
 * GUI principale per l'estrazione dati da PDF a Excel
 * Accetta Bolla_Consegna.pdf e Distinta.pdf
 */
public class MainWindow extends JFrame {
    
    private JLabel bollaLabel;
    private JLabel distinaLabel;
    private JLabel excelFileLabel;
    private JButton selectBollaButton;
    private JButton selectDistintaButton;
    private JButton selectExcelButton;
    private JButton processButton;
    private JTextArea statusArea;
    private JProgressBar progressBar;
    
    private String selectedBollaFile;
    private String selectedDistintaFile;
    private String selectedExcelFile = "F:\\PAM\\0289 PAM PD ASPETTI TOTALIZZATORI S2.xlsx";
    
    public MainWindow() {
        // Log di avvio
        try {
            PrintWriter logWriter = new PrintWriter(new FileWriter("app_debug.log", true));
            logWriter.println("[" + java.time.LocalDateTime.now() + "] MainWindow avviato");
            logWriter.close();
        } catch (Exception e) {
            System.err.println("Errore nel log: " + e.getMessage());
        }
        
        setTitle("PAM PDF to Excel - Estrazione Automatica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Layout principale
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Titolo
        JLabel titleLabel = new JLabel("Estrazione Automatica PDF → Excel");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Panel centrale con i file
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        filePanel.setBorder(BorderFactory.createTitledBorder("File da elaborare"));
        
        // Selezione Bolla_Consegna.pdf
        JPanel bollaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bollaPanel.add(new JLabel("Bolla_Consegna.pdf:"));
        bollaLabel = new JLabel("Nessun file selezionato");
        bollaLabel.setPreferredSize(new Dimension(350, 25));
        bollaLabel.setBorder(BorderFactory.createEtchedBorder());
        bollaPanel.add(bollaLabel);
        selectBollaButton = new JButton("Seleziona Bolla");
        selectBollaButton.addActionListener(e -> selectBollaFile());
        bollaPanel.add(selectBollaButton);
        filePanel.add(bollaPanel);
        
        // Selezione Distinta.pdf
        JPanel distinaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        distinaPanel.add(new JLabel("Distinta.pdf:"));
        distinaLabel = new JLabel("Nessun file selezionato");
        distinaLabel.setPreferredSize(new Dimension(350, 25));
        distinaLabel.setBorder(BorderFactory.createEtchedBorder());
        distinaPanel.add(distinaLabel);
        selectDistintaButton = new JButton("Seleziona Distinta");
        selectDistintaButton.addActionListener(e -> selectDistintaFile());
        distinaPanel.add(selectDistintaButton);
        filePanel.add(distinaPanel);
        
        // Selezione Excel
        JPanel excelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        excelPanel.add(new JLabel("Excel:"));
        excelFileLabel = new JLabel(selectedExcelFile);
        excelFileLabel.setPreferredSize(new Dimension(350, 25));
        excelFileLabel.setBorder(BorderFactory.createEtchedBorder());
        excelPanel.add(excelFileLabel);
        selectExcelButton = new JButton("Seleziona Excel");
        selectExcelButton.addActionListener(e -> selectExcelFile());
        excelPanel.add(selectExcelButton);
        filePanel.add(excelPanel);
        
        mainPanel.add(filePanel, BorderLayout.NORTH);
        
        // Panel inferiore: processo + status
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        // Panel per il pulsante di elaborazione e progress bar
        JPanel processPanel = new JPanel(new BorderLayout(10, 10));
        JPanel buttonPanel = new JPanel();
        processButton = new JButton("Elabora");
        processButton.setFont(new Font("Arial", Font.BOLD, 14));
        processButton.setPreferredSize(new Dimension(150, 40));
        processButton.addActionListener(e -> processFiles());
        buttonPanel.add(processButton);
        processPanel.add(buttonPanel, BorderLayout.WEST);
        
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        processPanel.add(progressBar, BorderLayout.CENTER);
        
        bottomPanel.add(processPanel, BorderLayout.NORTH);
        
        // Panel stato con scrollable area
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Stato Elaborazione"));
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setPreferredSize(new Dimension(650, 200));
        statusPanel.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.CENTER);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
        
        addStatus("Applicazione avviata");
        addStatus("Seleziona i 2 file PDF e il file Excel");
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
    private void selectBollaFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("F:\\PAM"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PDF Files (*.pdf)", "pdf"
        );
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedBollaFile = chooser.getSelectedFile().getAbsolutePath();
            bollaLabel.setText(chooser.getSelectedFile().getName());
            addStatus("✓ Bolla selezionata: " + chooser.getSelectedFile().getName());
        }
    }
    
    private void selectDistintaFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("F:\\PAM"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PDF Files (*.pdf)", "pdf"
        );
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDistintaFile = chooser.getSelectedFile().getAbsolutePath();
            distinaLabel.setText(chooser.getSelectedFile().getName());
            addStatus("✓ Distinta selezionata: " + chooser.getSelectedFile().getName());
        }
    }
    
    private void selectExcelFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("F:\\PAM"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Excel Files (*.xlsx)", "xlsx"
        );
        chooser.setFileFilter(filter);
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedExcelFile = chooser.getSelectedFile().getAbsolutePath();
            excelFileLabel.setText(selectedExcelFile);
            addStatus("Excel selezionato: " + selectedExcelFile);
        }
    }
    
    private void processFiles() {
        if (selectedBollaFile == null || selectedBollaFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona Bolla_Consegna.pdf", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        if (selectedDistintaFile == null || selectedDistintaFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona Distinta.pdf", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        if (selectedExcelFile == null || selectedExcelFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Seleziona un file Excel", 
                "Errore", 
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        // Disabilita i pulsanti durante l'elaborazione
        processButton.setEnabled(false);
        selectBollaButton.setEnabled(false);
        selectDistintaButton.setEnabled(false);
        selectExcelButton.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        
        // Esegui in thread separato per non bloccare la GUI
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    addStatus("Inizio elaborazione...");
                    progressBar.setValue(10);
                    
                    // Estrai dati da Bolla
                    addStatus("Lettura Bolla_Consegna.pdf...");
                    PDFDataExtractor.ExtractedData bollData = 
                        PDFDataExtractor.extractFromPDF(selectedBollaFile);
                    progressBar.setValue(30);
                    addStatus("  → Trovati " + bollData.invioProdotti.size() + " prodotti (Invio)");
                    
                    // Estrai dati da Distinta
                    addStatus("Lettura Distinta.pdf...");
                    PDFDataExtractor.ExtractedData distinaData = 
                        PDFDataExtractor.extractFromPDF(selectedDistintaFile);
                    progressBar.setValue(50);
                    addStatus("  → Trovati " + distinaData.invioProdotti.size() + " prodotti (Invio)");
                    
                    // Popola Excel
                    addStatus("Aggiornamento file Excel...");
                    ExcelHandler excel = new ExcelHandler(selectedExcelFile);
                    
                    // Popola colonne A (Q.C. Bolla), D (Lordo Bolla)
                    excel.populateFromBolla(bollData);
                    progressBar.setValue(70);
                    
                    // Popola colonne B (Q.C. Distinta), E (Lordo Distinta)
                    excel.populateFromDistinta(distinaData);
                    progressBar.setValue(90);
                    
                    excel.save();
                    excel.close();
                    progressBar.setValue(100);
                    
                    addStatus("✓ Elaborazione completata con successo!");
                    addStatus("File salvato: " + selectedExcelFile);
                    
                    // Mostra messaggio di successo
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Elaborazione completata!\n\n" +
                        "Bolla: " + bollData.invioProdotti.size() + " prodotti\n" +
                        "Distinta: " + distinaData.invioProdotti.size() + " prodotti\n\n" +
                        "File salvato con successo.",
                        "Successo",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                } catch (Exception ex) {
                    addStatus("✗ ERRORE: " + ex.getMessage());
                    addStatus("✗ Tipo errore: " + ex.getClass().getName());
                    addStatus("✗ Stack trace:");
                    for (StackTraceElement ste : ex.getStackTrace()) {
                        addStatus("    at " + ste);
                    }
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.this,
                        "Errore durante l'elaborazione:\n" + ex.getMessage() + "\n\nVedi il log per dettagli",
                        "Errore",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                return null;
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                progressBar.setIndeterminate(false);
                processButton.setEnabled(true);
                selectBollaButton.setEnabled(true);
                selectDistintaButton.setEnabled(true);
                selectExcelButton.setEnabled(true);
            }
        };
        
        worker.execute();
    }
    
    private void addStatus(String message) {
        String timestamp = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
        );
        statusArea.append("[" + timestamp + "] " + message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }
}
