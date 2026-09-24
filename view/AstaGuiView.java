package view;

import control.AstaController;
import model.FantaSquadra;
import model.Giocatore;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AstaGuiView extends JFrame {
    private AstaController controller;
    private boolean setupCompletato;

    private JTextField txtRicerca;
    private JTable tabellaGiocatori;
    private DefaultTableModel tableModel;
    
    private JTabbedPane tabbedPane;
    private JTable tabellaTabellone;
    private DefaultTableModel tableModelTabellone;
    private JComboBox<FantaSquadra> comboVisualizzaRosa;

    private JTable tabellaRosaDettaglio;
    private DefaultTableModel tableModelRosaDettaglio;
    private JLabel lblInfoRosa;
    private JLabel lblFotoProfilo;
    private JLabel lblTitoloRosa;
    private JLabel lblNomeSquadra;

    // Palette Dark High Contrast
    private final Color COLOR_BG_DARK = new Color(14, 14, 22);
    private final Color COLOR_CARD_DARK = new Color(24, 24, 36);
    private final Color COLOR_HEADER_DARK = new Color(36, 38, 58);
    private final Color COLOR_BORDER = new Color(60, 63, 95);
    private final Color COLOR_TEXT_WHITE = new Color(240, 243, 246);
    private final Color COLOR_TEXT_MUTED = new Color(158, 166, 188);

    private final Color COLOR_ACCENT = new Color(137, 180, 250);
    private final Color COLOR_SUCCESS = new Color(166, 227, 161);
    private final Color COLOR_DANGER = new Color(243, 139, 168);

    // Colori Ruoli
    private final Color COLOR_P = new Color(130, 226, 128);
    private final Color COLOR_D = new Color(130, 200, 255);
    private final Color COLOR_C = new Color(255, 215, 110);
    private final Color COLOR_A = new Color(255, 120, 140);

    // Tipografia ingrandita per una UI più leggibile
    private final Font FONT_HEADER_TITLE = new Font("SansSerif", Font.BOLD, 38);
    private final Font FONT_TABLE = new Font("SansSerif", Font.BOLD, 22);
    private final Font FONT_TABLE_HEADER = new Font("SansSerif", Font.BOLD, 20);
    private final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 20);
    private final Font FONT_COMBO = new Font("SansSerif", Font.BOLD, 22);
    private final Font FONT_INFO = new Font("SansSerif", Font.BOLD, 22);

    // Dimensioni foto
    private static final int AVATAR_TABELLONE_SIZE = 125; 
    private static final int AVATAR_PROFILO_SIZE = 300;

    public AstaGuiView(AstaController controller) {
        // Uso del LookAndFeel di sistema/cross-platform standard per evitare bug grafici su Linux
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback silenzioso
        }

        this.controller = controller;
        controller.caricaListoneCSV("data/giocatori.csv");

        setTitle("⚡ COMPAGNI DI MERENDE - ASTA FANTACALCIO");
        setSize(1700, 1000);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_DARK);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mostraMessaggioChiusura();
                dispose();
                System.exit(0);
            }
        });

        if (!configuraPartecipanti()) {
            dispose();
            return;
        }
        inizializzaComponenti();
        aggiornaVista();
        setupCompletato = true;
    }

    public boolean isSetupCompletato() {
        return setupCompletato;
    }

    private void mostraMessaggioChiusura() {
        String messaggio = "Questa app è stata sviluppata nel mio tempo libero.\n"
                + "Se ti è utile, un caffè o una birra può sempre fare piacere.\n"
                + "Grazie per averla usata!";
        System.out.println(messaggio);
        JOptionPane.showMessageDialog(this, messaggio, "Grazie!", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean configuraPartecipanti() {
        if (controller.esisteSalvataggio()) {
            int sceltaRipristino = mostraDialogRipristino();
            if (sceltaRipristino == JOptionPane.YES_OPTION && controller.caricaStatoSalvato()) {
                return true;
            }
            if (sceltaRipristino == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }

        ConfigurazioneAsta configurazione = mostraDialogConfigurazioneAsta();
        if (configurazione == null) {
            return false;
        }
        int numeroPartecipanti = configurazione.numeroPartecipanti;

        List<JTextField> campiAllenatore = new ArrayList<>();
        List<JTextField> campiSquadra = new ArrayList<>();
        JPanel righe = new JPanel();
        righe.setLayout(new BoxLayout(righe, BoxLayout.Y_AXIS));
        righe.setBackground(COLOR_BG_DARK);

        for (int i = 1; i <= numeroPartecipanti; i++) {
            JPanel riga = new JPanel(new BorderLayout(12, 8));
            riga.setBackground(COLOR_CARD_DARK);
            riga.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true),
                    new EmptyBorder(12, 14, 14, 14)
            ));
            riga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

            JLabel indice = new JLabel(String.format("PARTECIPANTE %02d", i));
            indice.setFont(new Font("SansSerif", Font.BOLD, 13));
            indice.setForeground(COLOR_ACCENT);
            riga.add(indice, BorderLayout.NORTH);

            JPanel campi = new JPanel(new GridLayout(1, 2, 12, 0));
            campi.setOpaque(false);
            JTextField campoAllenatore = creaCampoSetup();
            JTextField campoSquadra = creaCampoSetup();
            campiAllenatore.add(campoAllenatore);
            campiSquadra.add(campoSquadra);
            campi.add(creaGruppoCampo("Nome partecipante", campoAllenatore));
            campi.add(creaGruppoCampo("Nome squadra", campoSquadra));
            riga.add(campi, BorderLayout.CENTER);
            righe.add(riga);
            righe.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(righe);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(COLOR_BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        JDialog dialog = creaDialogSetup("Nomi partecipanti e squadre");
        JPanel contenuto = pannelloSetup("Completa la tua lega", "I campi sono vuoti: inserisci un nome per ogni partecipante e per ogni squadra.");
        contenuto.add(scroll, BorderLayout.CENTER);

        JLabel errore = new JLabel(" ");
        errore.setFont(new Font("SansSerif", Font.PLAIN, 14));
        errore.setForeground(COLOR_DANGER);
        JButton annulla = creaBottone("Annulla", COLOR_HEADER_DARK, COLOR_TEXT_WHITE);
        JButton conferma = creaBottone("Crea asta", COLOR_ACCENT, Color.BLACK);
        JPanel azioni = new JPanel(new BorderLayout(12, 8));
        azioni.setOpaque(false);
        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pulsanti.setOpaque(false);
        pulsanti.add(annulla);
        pulsanti.add(conferma);
        azioni.add(errore, BorderLayout.CENTER);
        azioni.add(pulsanti, BorderLayout.EAST);
        contenuto.add(azioni, BorderLayout.SOUTH);
        dialog.setContentPane(contenuto);
        dialog.setSize(900, 700);
        dialog.setLocationRelativeTo(this);

        List<String> nomiAllenatori = new ArrayList<>();
        List<String> nomiSquadre = new ArrayList<>();
        boolean[] confermato = {false};
        annulla.addActionListener(e -> dialog.dispose());
        conferma.addActionListener(e -> {
            nomiAllenatori.clear();
            nomiSquadre.clear();
            for (int i = 0; i < numeroPartecipanti; i++) {
                String allenatore = campiAllenatore.get(i).getText().trim();
                String squadra = campiSquadra.get(i).getText().trim();
                if (allenatore.isEmpty() || squadra.isEmpty()) {
                    errore.setText("Compila tutti i campi prima di continuare.");
                    (allenatore.isEmpty() ? campiAllenatore.get(i) : campiSquadra.get(i)).requestFocusInWindow();
                    return;
                }
                for (int j = 0; j < i; j++) {
                    if (nomiAllenatori.get(j).equalsIgnoreCase(allenatore)
                            || nomiSquadre.get(j).equalsIgnoreCase(squadra)) {
                        errore.setText("I nomi dei partecipanti e delle squadre devono essere univoci.");
                        campiAllenatore.get(i).requestFocusInWindow();
                        return;
                    }
                }
                nomiAllenatori.add(allenatore);
                nomiSquadre.add(squadra);
            }
            confermato[0] = true;
            dialog.dispose();
        });
        dialog.setVisible(true);

        if (!confermato[0]) {
            return false;
        }
        for (int i = 0; i < numeroPartecipanti; i++) {
            controller.aggiungiPartecipante(nomiAllenatori.get(i), nomiSquadre.get(i), configurazione.creditiIniziali);
        }
        return true;
    }

    private int mostraDialogRipristino() {
        JDialog dialog = creaDialogSetup("Sessione salvata");
        JPanel contenuto = pannelloSetup("Bentornato", "È disponibile una sessione salvata.");
        JLabel nota = new JLabel("Riprendendo, ritroverai partecipanti, squadre e rose.");
        nota.setForeground(COLOR_TEXT_MUTED);
        nota.setFont(new Font("SansSerif", Font.PLAIN, 15));
        contenuto.add(nota, BorderLayout.CENTER);

        int[] scelta = {JOptionPane.CANCEL_OPTION};
        JButton nuova = creaBottone("Nuova asta", COLOR_HEADER_DARK, COLOR_TEXT_WHITE);
        JButton riprendi = creaBottone("Riprendi asta", COLOR_ACCENT, Color.BLACK);
        JPanel azioni = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        azioni.setOpaque(false);
        azioni.add(nuova);
        azioni.add(riprendi);
        contenuto.add(azioni, BorderLayout.SOUTH);
        dialog.setContentPane(contenuto);
        dialog.setSize(560, 300);
        dialog.setLocationRelativeTo(this);
        nuova.addActionListener(e -> { scelta[0] = JOptionPane.NO_OPTION; dialog.dispose(); });
        riprendi.addActionListener(e -> { scelta[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
        dialog.setVisible(true);
        return scelta[0];
    }

    private ConfigurazioneAsta mostraDialogConfigurazioneAsta() {
        JDialog dialog = creaDialogSetup("Nuova asta");
        JPanel contenuto = pannelloSetup("Imposta la tua lega", "Scegli il numero di partecipanti e i crediti uguali per tutte le squadre.");

        JSpinner spinnerPartecipanti = new JSpinner(new SpinnerNumberModel(8, 2, 20, 1));
        JSpinner spinnerCrediti = new JSpinner(new SpinnerNumberModel(500, 1, 100000, 50));
        configuraSpinnerSetup(spinnerPartecipanti, 24);
        configuraSpinnerSetup(spinnerCrediti, 24);

        JPanel selezione = new JPanel(new GridLayout(2, 2, 14, 14));
        selezione.setOpaque(false);
        selezione.add(creaEtichettaSetup("Numero di partecipanti"));
        selezione.add(spinnerPartecipanti);
        selezione.add(creaEtichettaSetup("Crediti iniziali per squadra"));
        selezione.add(spinnerCrediti);
        contenuto.add(selezione, BorderLayout.CENTER);

        ConfigurazioneAsta[] configurazione = {null};
        JButton annulla = creaBottone("Annulla", COLOR_HEADER_DARK, COLOR_TEXT_WHITE);
        JButton continua = creaBottone("Continua", COLOR_ACCENT, Color.BLACK);
        JPanel azioni = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        azioni.setOpaque(false);
        azioni.add(annulla);
        azioni.add(continua);
        contenuto.add(azioni, BorderLayout.SOUTH);
        dialog.setContentPane(contenuto);
        dialog.setSize(620, 400);
        dialog.setLocationRelativeTo(this);
        annulla.addActionListener(e -> dialog.dispose());
        continua.addActionListener(e -> {
            configurazione[0] = new ConfigurazioneAsta(
                    (Integer) spinnerPartecipanti.getValue(),
                    (Integer) spinnerCrediti.getValue()
            );
            dialog.dispose();
        });
        dialog.setVisible(true);
        return configurazione[0];
    }

    private void configuraSpinnerSetup(JSpinner spinner, int dimensioneFont) {
        spinner.setFont(new Font("SansSerif", Font.BOLD, dimensioneFont));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField campo = ((JSpinner.DefaultEditor) editor).getTextField();
            campo.setHorizontalAlignment(JTextField.CENTER);
            campo.setBackground(COLOR_HEADER_DARK);
            campo.setForeground(COLOR_TEXT_WHITE);
            campo.setCaretColor(COLOR_ACCENT);
            campo.setBorder(new CompoundBorder(
                    new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        }
    }

    private JLabel creaEtichettaSetup(String testo) {
        JLabel etichetta = new JLabel(testo);
        etichetta.setFont(new Font("SansSerif", Font.BOLD, 17));
        etichetta.setForeground(COLOR_TEXT_WHITE);
        return etichetta;
    }

    private static class ConfigurazioneAsta {
        private final int numeroPartecipanti;
        private final int creditiIniziali;

        private ConfigurazioneAsta(int numeroPartecipanti, int creditiIniziali) {
            this.numeroPartecipanti = numeroPartecipanti;
            this.creditiIniziali = creditiIniziali;
        }
    }

    private JDialog creaDialogSetup(String titolo) {
        JDialog dialog = new JDialog(this, titolo, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(COLOR_BG_DARK);
        return dialog;
    }

    private JPanel pannelloSetup(String titolo, String descrizione) {
        JPanel pannello = new JPanel(new BorderLayout(0, 18));
        pannello.setBackground(COLOR_BG_DARK);
        pannello.setBorder(new EmptyBorder(24, 26, 22, 26));

        JPanel intestazione = new JPanel();
        intestazione.setLayout(new BoxLayout(intestazione, BoxLayout.Y_AXIS));
        intestazione.setOpaque(false);
        JLabel titoloLabel = new JLabel(titolo);
        titoloLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        titoloLabel.setForeground(COLOR_ACCENT);
        JLabel descrizioneLabel = new JLabel(descrizione);
        descrizioneLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descrizioneLabel.setForeground(COLOR_TEXT_MUTED);
        intestazione.add(titoloLabel);
        intestazione.add(Box.createVerticalStrut(5));
        intestazione.add(descrizioneLabel);
        pannello.add(intestazione, BorderLayout.NORTH);
        return pannello;
    }

    private JPanel creaGruppoCampo(String etichetta, JTextField campo) {
        JPanel gruppo = new JPanel(new BorderLayout(0, 5));
        gruppo.setOpaque(false);
        JLabel label = new JLabel(etichetta);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(COLOR_TEXT_MUTED);
        gruppo.add(label, BorderLayout.NORTH);
        gruppo.add(campo, BorderLayout.CENTER);
        return gruppo;
    }

    private JTextField creaCampoSetup() {
        JTextField campo = new JTextField(18);
        campo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        campo.setBackground(COLOR_HEADER_DARK);
        campo.setForeground(COLOR_TEXT_WHITE);
        campo.setCaretColor(COLOR_ACCENT);
        campo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true), new EmptyBorder(8, 10, 8, 10)));
        return campo;
    }

    private void inizializzaComponenti() {
        setLayout(new BorderLayout(16, 16));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(14, 14, 14, 14));

        // --- HEADER SUPERIORE ---
        JPanel panelHeader = new JPanel(new GridBagLayout());
        panelHeader.setBackground(COLOR_CARD_DARK);
        panelHeader.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(14, 18, 14, 18)));

        JLabel lblTitle = new JLabel("COMPAGNI DI MERENDE");
        lblTitle.setFont(FONT_HEADER_TITLE);
        lblTitle.setForeground(COLOR_ACCENT);

        panelHeader.add(lblTitle);
        add(panelHeader, BorderLayout.NORTH);

        // --- PANNELLO SINISTRO: LISTONE MINIMIZZATO ---
        JPanel panelSinistra = new JPanel(new BorderLayout(12, 12));
        panelSinistra.setBackground(COLOR_CARD_DARK);
        panelSinistra.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(12, 12, 12, 12)));

        txtRicerca = new JTextField();
        txtRicerca.setFont(FONT_TABLE);
        txtRicerca.setBackground(COLOR_HEADER_DARK);
        txtRicerca.setForeground(COLOR_TEXT_WHITE);
        txtRicerca.setCaretColor(COLOR_TEXT_WHITE);
        txtRicerca.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(8, 10, 8, 10)));

        JButton btnCerca = creaBottone("🔍 Cerca", COLOR_ACCENT, Color.BLACK);

        JPanel panelRicerca = new JPanel(new BorderLayout(10, 10));
        panelRicerca.setOpaque(false);
        panelRicerca.add(txtRicerca, BorderLayout.CENTER);
        panelRicerca.add(btnCerca, BorderLayout.EAST);

        String[] colonne = {"Nome", "Ruolo"};
        tableModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabellaGiocatori = new JTable(tableModel);
        tabellaGiocatori.setFont(FONT_TABLE);
        tabellaGiocatori.setRowHeight(44);
        tabellaGiocatori.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabellaGiocatori.setBackground(COLOR_CARD_DARK);
        tabellaGiocatori.setForeground(COLOR_TEXT_WHITE);
        tabellaGiocatori.setShowGrid(false);
        tabellaGiocatori.getTableHeader().setBackground(COLOR_HEADER_DARK);
        tabellaGiocatori.getTableHeader().setForeground(COLOR_ACCENT);
        tabellaGiocatori.getTableHeader().setFont(FONT_TABLE_HEADER);
        tabellaGiocatori.getTableHeader().setPreferredSize(new Dimension(0, 44));
        
        tabellaGiocatori.getColumnModel().getColumn(0).setPreferredWidth(260); 
        tabellaGiocatori.getColumnModel().getColumn(1).setPreferredWidth(60);  

        tabellaGiocatori.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String ruolo = String.valueOf(table.getValueAt(row, 1));
                setBorder(new EmptyBorder(0, 10, 0, 10));

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_CARD_DARK : COLOR_HEADER_DARK.darker());
                    if (column == 1) { 
                        switch (ruolo) {
                            case "P": c.setForeground(COLOR_P); break;
                            case "D": c.setForeground(COLOR_D); break;
                            case "C": c.setForeground(COLOR_C); break;
                            case "A": c.setForeground(COLOR_A); break;
                            default: c.setForeground(COLOR_TEXT_WHITE); break;
                        }
                    } else {
                        c.setForeground(COLOR_TEXT_WHITE);
                    }
                } else {
                    c.setBackground(COLOR_ACCENT);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tabellaGiocatori);
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        scrollTable.getViewport().setBackground(COLOR_CARD_DARK);
        panelSinistra.add(panelRicerca, BorderLayout.NORTH);
        panelSinistra.add(scrollTable, BorderLayout.CENTER);

        // --- PANNELLO DESTRO: TABELLONE & DETTAGLIO ROSE ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_TABLE_HEADER);
        tabbedPane.setBackground(COLOR_CARD_DARK);
        tabbedPane.setForeground(COLOR_TEXT_WHITE);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());

        // 1. Tabellone Generale
        String[] colTabellone = {"Foto", "Allenatore", "Crediti", "P", "D", "C", "A", "Tot"};
        tableModelTabellone = new DefaultTableModel(colTabellone, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return ImageIcon.class;
                return Object.class;
            }
        };

        tabellaTabellone = new JTable(tableModelTabellone);
        tabellaTabellone.setFont(FONT_TABLE);
        tabellaTabellone.setRowHeight(150); 
        tabellaTabellone.setBackground(COLOR_CARD_DARK);
        tabellaTabellone.setForeground(COLOR_TEXT_WHITE);
        tabellaTabellone.setShowGrid(false);
        tabellaTabellone.getTableHeader().setBackground(COLOR_HEADER_DARK);
        tabellaTabellone.getTableHeader().setForeground(COLOR_ACCENT);
        tabellaTabellone.getTableHeader().setFont(FONT_TABLE_HEADER);
        tabellaTabellone.getTableHeader().setPreferredSize(new Dimension(0, 42));
        
        tabellaTabellone.getColumnModel().getColumn(0).setPreferredWidth(AVATAR_TABELLONE_SIZE + 45);

        DefaultTableCellRenderer renderTestoTabellone = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(0, 15, 0, 15));
                setHorizontalAlignment(column >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                c.setFont(FONT_TABLE);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_CARD_DARK : COLOR_HEADER_DARK.darker());
                    c.setForeground(COLOR_TEXT_WHITE);
                } else {
                    c.setBackground(COLOR_ACCENT);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };

        DefaultTableCellRenderer renderFotoTabellone = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setText("");
                if (value instanceof ImageIcon) {
                    lbl.setIcon((ImageIcon) value);
                } else {
                    lbl.setIcon(null);
                }
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? COLOR_CARD_DARK : COLOR_HEADER_DARK.darker());
                } else {
                    lbl.setBackground(COLOR_ACCENT);
                }
                return lbl;
            }
        };

        tabellaTabellone.getColumnModel().getColumn(0).setCellRenderer(renderFotoTabellone);
        for(int i = 1; i < tabellaTabellone.getColumnCount(); i++) {
            tabellaTabellone.getColumnModel().getColumn(i).setCellRenderer(renderTestoTabellone);
        }

        JScrollPane scrollTabellone = new JScrollPane(tabellaTabellone);
        scrollTabellone.setBorder(BorderFactory.createEmptyBorder());
        scrollTabellone.getViewport().setBackground(COLOR_CARD_DARK);
        tabbedPane.addTab("📊 Tabellone Generale", scrollTabellone);

        // 2. Dettaglio Rosa Ottimizzato
        JPanel panelDettaglioRosa = new JPanel(new BorderLayout(14, 14));
        panelDettaglioRosa.setBackground(COLOR_CARD_DARK);
        panelDettaglioRosa.setBorder(new EmptyBorder(16, 16, 16, 16));

        comboVisualizzaRosa = new JComboBox<>();
        comboVisualizzaRosa.setFont(FONT_COMBO);
        comboVisualizzaRosa.setBackground(COLOR_HEADER_DARK);
        comboVisualizzaRosa.setForeground(COLOR_ACCENT);
        comboVisualizzaRosa.setPreferredSize(new Dimension(300, 42));
        comboVisualizzaRosa.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(4, 8, 4, 8)));
        
        comboVisualizzaRosa.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_COMBO);
                setBorder(new EmptyBorder(8, 12, 8, 12));
                if (isSelected) {
                    setBackground(COLOR_ACCENT);
                    setForeground(Color.BLACK);
                } else {
                    setBackground(COLOR_HEADER_DARK);
                    setForeground(COLOR_TEXT_WHITE);
                }
                return c;
            }
        });

        JPanel panelTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelTopRight.setOpaque(false);
        JButton btnModificaNomi = creaBottone("✎ Modifica nomi", COLOR_HEADER_DARK, COLOR_TEXT_WHITE);
        panelTopRight.add(btnModificaNomi);
        panelTopRight.add(comboVisualizzaRosa);
        btnModificaNomi.addActionListener(e -> apriDialogRinominaPartecipante());

        lblFotoProfilo = new JLabel();
        lblFotoProfilo.setPreferredSize(new Dimension(AVATAR_PROFILO_SIZE, AVATAR_PROFILO_SIZE));
        lblFotoProfilo.setHorizontalAlignment(JLabel.CENTER);
        lblFotoProfilo.setVerticalAlignment(JLabel.CENTER);

        JPanel panelInfoDestra = new JPanel();
        panelInfoDestra.setLayout(new BoxLayout(panelInfoDestra, BoxLayout.Y_AXIS));
        panelInfoDestra.setOpaque(false);
        panelInfoDestra.setBorder(new EmptyBorder(10, 24, 10, 10));

        lblTitoloRosa = new JLabel("NOME SQUADRA");
        lblTitoloRosa.setFont(new Font("SansSerif", Font.BOLD, 42));
        lblTitoloRosa.setForeground(COLOR_ACCENT);

        lblNomeSquadra = new JLabel();
        lblNomeSquadra.setFont(new Font("SansSerif", Font.BOLD, 30));
        lblNomeSquadra.setForeground(new Color(167, 224, 255));
        lblNomeSquadra.setBorder(new EmptyBorder(6, 0, 0, 0));

        lblInfoRosa = new JLabel("Resoconto ruoli e crediti...");
        lblInfoRosa.setFont(FONT_INFO);
        lblInfoRosa.setForeground(COLOR_TEXT_WHITE);
        lblInfoRosa.setBorder(new EmptyBorder(18, 0, 0, 0));

        panelInfoDestra.add(lblTitoloRosa);
        panelInfoDestra.add(lblNomeSquadra);
        panelInfoDestra.add(lblInfoRosa);

        JPanel panelHeaderCentro = new JPanel(new BorderLayout(20, 0));
        panelHeaderCentro.setOpaque(false);
        panelHeaderCentro.add(lblFotoProfilo, BorderLayout.WEST);
        panelHeaderCentro.add(panelInfoDestra, BorderLayout.CENTER);

        JPanel panelTopRosa = new JPanel(new BorderLayout(0, 12));
        panelTopRosa.setOpaque(false);
        panelTopRosa.setBorder(new EmptyBorder(0, 0, 16, 0));
        panelTopRosa.add(panelTopRight, BorderLayout.NORTH);
        panelTopRosa.add(panelHeaderCentro, BorderLayout.CENTER);

        String[] colRosa = {"Nome", "R", "Squadra", "Prezzo"};
        tableModelRosaDettaglio = new DefaultTableModel(colRosa, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabellaRosaDettaglio = new JTable(tableModelRosaDettaglio);
        tabellaRosaDettaglio.setFont(FONT_TABLE);
        tabellaRosaDettaglio.setRowHeight(38);
        tabellaRosaDettaglio.setBackground(COLOR_CARD_DARK);
        tabellaRosaDettaglio.setForeground(COLOR_TEXT_WHITE);
        tabellaRosaDettaglio.setShowGrid(false);
        tabellaRosaDettaglio.getTableHeader().setBackground(COLOR_HEADER_DARK);
        tabellaRosaDettaglio.getTableHeader().setForeground(COLOR_ACCENT);
        tabellaRosaDettaglio.getTableHeader().setFont(FONT_TABLE_HEADER);
        tabellaRosaDettaglio.getTableHeader().setPreferredSize(new Dimension(0, 38));

        tabellaRosaDettaglio.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String ruolo = String.valueOf(table.getValueAt(row, 1));
                setBorder(new EmptyBorder(0, 15, 0, 15));
                c.setFont(FONT_TABLE);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_CARD_DARK : COLOR_HEADER_DARK.darker());
                    if (column == 1) {
                        switch (ruolo) {
                            case "P": c.setForeground(COLOR_P); break;
                            case "D": c.setForeground(COLOR_D); break;
                            case "C": c.setForeground(COLOR_C); break;
                            case "A": c.setForeground(COLOR_A); break;
                        }
                    } else if (column == 3) {
                        c.setForeground(COLOR_SUCCESS);
                    } else {
                        c.setForeground(COLOR_TEXT_WHITE);
                    }
                } else {
                    c.setBackground(COLOR_ACCENT);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scrollRosa = new JScrollPane(tabellaRosaDettaglio);
        scrollRosa.setBorder(BorderFactory.createEmptyBorder());
        scrollRosa.getViewport().setBackground(COLOR_CARD_DARK);

        panelDettaglioRosa.add(panelTopRosa, BorderLayout.NORTH);
        panelDettaglioRosa.add(scrollRosa, BorderLayout.CENTER);

        tabbedPane.addTab("📋 Dettaglio Rose", panelDettaglioRosa);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelSinistra, tabbedPane);
        mainSplit.setDividerLocation(400); 
        mainSplit.setBorder(null);
        mainSplit.setBackground(COLOR_BG_DARK);
        mainSplit.setDividerSize(16);

        add(mainSplit, BorderLayout.CENTER);

        // LISTENERS ED EVENTI
        btnCerca.addActionListener(e -> cercaGiocatori());
        txtRicerca.addActionListener(e -> cercaGiocatori());
        comboVisualizzaRosa.addActionListener(e -> mostraRosaDettagliata());

        tabellaTabellone.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabellaTabellone.getSelectedRow() != -1) {
                    String nomeAllen = (String) tableModelTabellone.getValueAt(tabellaTabellone.getSelectedRow(), 1);
                    for (int i = 0; i < comboVisualizzaRosa.getItemCount(); i++) {
                        FantaSquadra sq = comboVisualizzaRosa.getItemAt(i);
                        if (sq.getNomeAllenatore().equals(nomeAllen)) {
                            comboVisualizzaRosa.setSelectedItem(sq);
                            tabbedPane.setSelectedIndex(1);
                            break;
                        }
                    }
                }
            }
        });

        tabellaGiocatori.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    apriDialogAssegnazione();
                }
            }
        });

        tabellaGiocatori.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabellaGiocatori.getSelectedRow() != -1) {
                    apriDialogAssegnazione();
                }
            }
        });

        tabellaRosaDettaglio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    apriDialogGestioneRosa();
                }
            }
        });

        tabellaRosaDettaglio.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabellaRosaDettaglio.getSelectedRow() != -1) {
                    apriDialogGestioneRosa();
                }
            }
        });
    }

    private ImageIcon caricaFotoRidimensionata(String nomeAllenatore, int larghezza, int altezza) {
        String baseDir = "data/foto/";
        File dir = new File(baseDir);
        
        if (!dir.exists() || !dir.isDirectory()) {
            return null;
        }

        File f = null;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String nomeFile = file.getName();
                int punto = nomeFile.lastIndexOf('.');
                if (punto > 0) {
                    String nomeSenzaExt = nomeFile.substring(0, punto);
                    if (nomeSenzaExt.equalsIgnoreCase(nomeAllenatore.trim())) {
                        f = file;
                        break;
                    }
                }
            }
        }

        if (f == null || !f.exists()) {
            return null;
        }

        try {
            BufferedImage originale = ImageIO.read(f);
            if (originale == null) return null;

            int dim = Math.min(larghezza, altezza);
            BufferedImage circolare = creaAvatarCircolare(originale, dim, nomeAllenatore);
            return new ImageIcon(circolare);
        } catch (IOException ex) {
            return null;
        }
    }
private BufferedImage creaAvatarCircolare(
        BufferedImage sorgente,
        int dimensione,
        String nomeAllenatore) {

    int w = sorgente.getWidth();
    int h = sorgente.getHeight();

    int lato = Math.min(w, h);

    // Crop personalizzato
    int cropX = (w - lato) / 2;
    int cropY = (h - lato) / 2;

    if (nomeAllenatore.equalsIgnoreCase("Luigi")) {
        // Luigi: sposta l'inquadratura leggermente verso l'alto
        cropY = 80;
    }
    else if (nomeAllenatore.equalsIgnoreCase("Simone G")) {
        // Simone G: sposta l'inquadratura verso l'alto
        cropY = 60;
    }
    else if (nomeAllenatore.equalsIgnoreCase("luca")) {
        // Luca: inquadratura personalizzata
        cropY = 250;
    }

    // Sicurezza
    cropY = Math.max(0, Math.min(cropY, h - lato));

    BufferedImage quadrata =
            sorgente.getSubimage(cropX, cropY, lato, lato);

    BufferedImage risultato = new BufferedImage(
            dimensione,
            dimensione,
            BufferedImage.TYPE_INT_ARGB
    );

    Graphics2D g2 = risultato.createGraphics();

    g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
    );

    g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR
    );

    g2.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
    );

    int anello = Math.max(3, dimensione / 30);

    Ellipse2D clip = new Ellipse2D.Float(
            anello,
            anello,
            dimensione - anello * 2f,
            dimensione - anello * 2f
    );

    // Mantiene il ritratto circolare
    g2.setClip(clip);

    g2.drawImage(
            quadrata,
            anello,
            anello,
            dimensione - anello,
            dimensione - anello,
            null
    );

    g2.setClip(null);

    // Bordo circolare
    g2.setStroke(new BasicStroke(anello));
    g2.setColor(COLOR_ACCENT);

    g2.draw(new Ellipse2D.Float(
            anello / 2f,
            anello / 2f,
            dimensione - anello,
            dimensione - anello
    ));

    g2.dispose();

    return risultato;
}

    private JButton creaBottone(String testo, Color colBg, Color colFg) {
        JButton b = new JButton(testo);
        b.setFont(FONT_BUTTON);
        b.setBackground(colBg);
        b.setForeground(colFg);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }

    private void cercaGiocatori() {
        tableModel.setRowCount(0);
        String query = txtRicerca.getText().trim();
        List<Giocatore> trovati = controller.cercaGiocatori(query);
        for (Giocatore g : trovati) {
            tableModel.addRow(new Object[]{g.getNome(), g.getRuolo()});
        }
        if (tabellaGiocatori.getRowCount() > 0) {
            tabellaGiocatori.setRowSelectionInterval(0, 0);
        }
    }

    private void apriDialogAssegnazione() {
        int row = tabellaGiocatori.getSelectedRow();
        if (row == -1) return;

        String nomeGiocatore = (String) tableModel.getValueAt(row, 0);
        Giocatore gSel = controller.getListone().stream()
                .filter(g -> g.getNome().equalsIgnoreCase(nomeGiocatore))
                .findFirst()
                .orElse(null);

        if (gSel == null) return;

        JDialog dialog = new JDialog(this, "Assegna calciatore", true);
        dialog.setSize(540, 380);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(COLOR_CARD_DARK);
        ((JPanel) dialog.getContentPane()).setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(COLOR_CARD_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);

        JLabel lblGiocatore = new JLabel(gSel.getNome());
        lblGiocatore.setForeground(COLOR_ACCENT);
        lblGiocatore.setHorizontalAlignment(SwingConstants.CENTER);
        lblGiocatore.setFont(new Font(FONT_HEADER_TITLE.getFamily(), Font.BOLD, 32));
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.gridy = 0;
        content.add(lblGiocatore, gbc);

        JLabel lblSq = new JLabel("Seleziona Fantasquadra:");
        lblSq.setForeground(COLOR_TEXT_MUTED);
        lblSq.setFont(FONT_TABLE);
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        content.add(lblSq, gbc);

        JComboBox<FantaSquadra> comboDialog = new JComboBox<>();
        comboDialog.setFont(FONT_COMBO);
        comboDialog.setBackground(COLOR_HEADER_DARK);
        comboDialog.setForeground(COLOR_TEXT_WHITE);
        comboDialog.setBorder(new LineBorder(COLOR_BORDER, 1));
        for (FantaSquadra s : controller.getPartecipanti()) {
            comboDialog.addItem(s);
        }
        gbc.gridx = 1;
        gbc.weightx = 1;
        content.add(comboDialog, gbc);

        JLabel lblPr = new JLabel("Prezzo d'acquisto (cr):");
        lblPr.setForeground(COLOR_TEXT_MUTED);
        lblPr.setFont(FONT_TABLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        content.add(lblPr, gbc);

        JTextField txtDialogPrezzo = new JTextField();
        txtDialogPrezzo.setFont(new Font(FONT_TABLE.getFamily(), Font.BOLD, 30));
        txtDialogPrezzo.setHorizontalAlignment(JTextField.CENTER);
        txtDialogPrezzo.setBackground(COLOR_HEADER_DARK);
        txtDialogPrezzo.setForeground(COLOR_SUCCESS);
        txtDialogPrezzo.setCaretColor(COLOR_SUCCESS);
        txtDialogPrezzo.setBorder(new CompoundBorder(new LineBorder(COLOR_BORDER, 1), new EmptyBorder(10, 12, 10, 12)));
        txtDialogPrezzo.setPreferredSize(new Dimension(140, 52));
        gbc.gridx = 1;
        gbc.weightx = 1;
        content.add(txtDialogPrezzo, gbc);

        JButton btnConferma = creaBottone("✔ ASSEGNA", COLOR_SUCCESS, Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(btnConferma, gbc);

        dialog.setLayout(new BorderLayout(10, 10));
        dialog.add(content, BorderLayout.CENTER);

        Runnable azionAssegna = () -> {
            FantaSquadra sq = (FantaSquadra) comboDialog.getSelectedItem();
            String tPrezzo = txtDialogPrezzo.getText().trim();
            if (sq == null || tPrezzo.isEmpty()) return;

            if (sq.haRaggiuntoLimite(gSel.getRuolo())) {
                JOptionPane.showMessageDialog(dialog, sq.getNomeAllenatore() + " ha già il reparto completo per [" + gSel.getRuolo() + "]!", "Limite Raggiunto", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int prezzo = Integer.parseInt(tPrezzo);
                if (controller.assegnaGiocatore(gSel, sq, prezzo)) {
                    dialog.dispose();
                    cercaGiocatori();
                    aggiornaVista();
                    mostraAnimazioneAssegnazione(sq, gSel); 
                } else {
                    JOptionPane.showMessageDialog(dialog, "Crediti insufficienti!", "Errore Crediti", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Prezzo non valido!", "Errore Input", JOptionPane.ERROR_MESSAGE);
            }
        };

        btnConferma.addActionListener(e -> azionAssegna.run());
        txtDialogPrezzo.addActionListener(e -> azionAssegna.run());

        SwingUtilities.invokeLater(txtDialogPrezzo::requestFocusInWindow);
        dialog.setVisible(true);
    }

private void mostraAnimazioneAssegnazione(FantaSquadra sq, Giocatore giocatore) {

    // Riproduce l'audio personalizzato dell'allenatore
    riproduciAudioAllenatore(sq.getNomeAllenatore());

    JDialog popupTransizione = new JDialog(this, "", true);
    popupTransizione.setUndecorated(true);

    // Finestra ingrandita
    popupTransizione.setSize(1000, 1000);
    popupTransizione.setLocationRelativeTo(this);

    JPanel panelTrans = new JPanel(new BorderLayout(15, 15));
    panelTrans.setBackground(COLOR_CARD_DARK);
    panelTrans.setBorder(
        new CompoundBorder(
            new LineBorder(COLOR_ACCENT, 3),
            new EmptyBorder(30, 35, 35, 35)
        )
    );

    JLabel lblTitolo = new JLabel(
        "🎉 ASSEGNATO!",
        SwingConstants.CENTER
    );

    lblTitolo.setFont(
        new Font(
            FONT_HEADER_TITLE.getFamily(),
            Font.BOLD,
            48
        )
    );

    lblTitolo.setForeground(COLOR_SUCCESS);

    JLabel lblImg = new JLabel("", SwingConstants.CENTER);

    // Foto ingrandita mantenendo le proporzioni
    ImageIcon fotoGrande = caricaFotoRidimensionata(
        sq.getNomeAllenatore(),
        450,
        450
    );

    if (fotoGrande != null) {
        lblImg.setIcon(fotoGrande);
    }

    JLabel lblAssegnazione = new JLabel(
        sq.getNomeAllenatore().toUpperCase() + " HA PRESO " + giocatore.getNome().toUpperCase(),
        SwingConstants.CENTER
    );
    lblAssegnazione.setFont(
        new Font(
            FONT_HEADER_TITLE.getFamily(),
            Font.BOLD,
            42
        )
    );
    lblAssegnazione.setForeground(COLOR_ACCENT);

    panelTrans.add(lblTitolo, BorderLayout.NORTH);
    panelTrans.add(lblImg, BorderLayout.CENTER);
    panelTrans.add(lblAssegnazione, BorderLayout.SOUTH);

    popupTransizione.add(panelTrans);

    Timer timer = new Timer(2000, e -> {
        popupTransizione.dispose();
        comboVisualizzaRosa.setSelectedItem(sq);
        tabbedPane.setSelectedIndex(1);
    });

    timer.setRepeats(false);
    timer.start();

    popupTransizione.setVisible(true);
}
private void riproduciAudioAllenatore(String nomeAllenatore) {
    try {
        File audioFile = new File("data/audio/" + nomeAllenatore + ".wav");

        if (!audioFile.exists()) {
            System.out.println("Audio non trovato: " + audioFile.getPath());
            return;
        }

        AudioInputStream audioStream =
                AudioSystem.getAudioInputStream(audioFile);

        Clip clip = AudioSystem.getClip();
        clip.open(audioStream);

        // Volume specifico per ogni allenatore
        float volume = 0.0f;

        if (nomeAllenatore.equalsIgnoreCase("Luca")) {
            volume = -20.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Luigi")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Manolo")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Zampa")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Leonardo")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Simone G")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Simone M")) {
            volume = -0.0f;
        } else if (nomeAllenatore.equalsIgnoreCase("Mattia")) {
            volume = -0.0f;
        }

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            gainControl.setValue(volume);
        }

        clip.start();

    } catch (Exception e) {
        System.out.println("Errore nella riproduzione dell'audio: "
                + e.getMessage());
    }
}

    private void apriDialogGestioneRosa() {
        int row = tabellaRosaDettaglio.getSelectedRow();
        if (row == -1) return;

        FantaSquadra squadraSel = (FantaSquadra) comboVisualizzaRosa.getSelectedItem();
        if (squadraSel == null) return;

        String nomeGiocatore = String.valueOf(tableModelRosaDettaglio.getValueAt(row, 0));
        Giocatore giocatoreSel = squadraSel.getRosa().stream()
                .filter(g -> g.getNome().equals(nomeGiocatore))
                .findFirst()
                .orElse(null);

        if (giocatoreSel == null) return;

        String[] opzioni = {"Modifica Prezzo", "Rimuovi / Svincola", "Annulla"};
        int scelta = JOptionPane.showOptionDialog(
                this,
                "Gestione " + giocatoreSel.getNome() + " (" + giocatoreSel.getRuolo() + ") - Pagato: " + giocatoreSel.getPrezzoAcquisto() + " cr",
                "Gestione Calciatore Rosa",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opzioni,
                opzioni[0]
        );

        if (scelta == 0) {
            String nuovoPrezzoStr = JOptionPane.showInputDialog(
                    this,
                    "Inserisci il nuovo prezzo per " + giocatoreSel.getNome() + ":",
                    giocatoreSel.getPrezzoAcquisto()
            );

            if (nuovoPrezzoStr != null && !nuovoPrezzoStr.trim().isEmpty()) {
                try {
                    int nuovoPrezzo = Integer.parseInt(nuovoPrezzoStr.trim());
                    int differenza = nuovoPrezzo - giocatoreSel.getPrezzoAcquisto();

                    if (differenza > squadraSel.getCreditiRimanenti()) {
                        JOptionPane.showMessageDialog(this, "Crediti insufficienti per questa modifica!", "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    squadraSel.setCreditiRimanenti(squadraSel.getCreditiRimanenti() - differenza);
                    giocatoreSel.setPrezzoAcquisto(nuovoPrezzo);

                    controller.assegnaGiocatore(null, squadraSel, 0);
                    aggiornaVista();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Prezzo non valido!", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (scelta == 1) {
            int conferma = JOptionPane.showConfirmDialog(
                    this,
                    "Vuoi davvero svincolare " + giocatoreSel.getNome() + " e rimborsare " + giocatoreSel.getPrezzoAcquisto() + " cr a " + squadraSel.getNomeAllenatore() + "?",
                    "Conferma Rimozione",
                    JOptionPane.YES_NO_OPTION
            );

            if (conferma == JOptionPane.YES_OPTION) {
                controller.rimuoviGiocatoreDaSquadra(squadraSel, giocatoreSel);
                cercaGiocatori();
                aggiornaVista();
            }
        }
    }

    private void aggiornaVista() {
        comboVisualizzaRosa.removeAllItems();

        for (FantaSquadra s : controller.getPartecipanti()) {
            comboVisualizzaRosa.addItem(s);
        }

        tableModelTabellone.setRowCount(0);
        for (FantaSquadra s : controller.getPartecipanti()) {
            ImageIcon avatar = caricaFotoRidimensionata(s.getNomeAllenatore(), AVATAR_TABELLONE_SIZE, AVATAR_TABELLONE_SIZE);
            tableModelTabellone.addRow(new Object[]{
                avatar,
                s.getNomeAllenatore(),
                s.getCreditiRimanenti() + " cr",
                formattaRuolo(s, "P"),
                formattaRuolo(s, "D"),
                formattaRuolo(s, "C"),
                formattaRuolo(s, "A"),
                s.getRosa().size() + "/25"
            });
        }

        mostraRosaDettagliata();
    }

    private String formattaRuolo(FantaSquadra s, String ruolo) {
        long attuali = s.getConteggioRuolo(ruolo);
        int max = s.getLimiteRuolo(ruolo);
        return attuali >= max ? attuali + "/" + max + " [OK]" : attuali + "/" + max;
    }

    private int getPrioritaRuolo(String ruolo) {
        switch (ruolo) {
            case "P": return 1;
            case "D": return 2;
            case "C": return 3;
            case "A": return 4;
            default: return 99;
        }
    }

    private void apriDialogRinominaPartecipante() {
        FantaSquadra squadra = (FantaSquadra) comboVisualizzaRosa.getSelectedItem();
        if (squadra == null) return;

        JDialog dialog = creaDialogSetup("Modifica nomi");
        JPanel contenuto = pannelloSetup(
                "Rinomina partecipante",
                "La modifica viene salvata insieme alla sessione dell’asta."
        );

        JTextField campoAllenatore = creaCampoSetup();
        campoAllenatore.setText(squadra.getNomeAllenatore());
        JTextField campoSquadra = creaCampoSetup();
        campoSquadra.setText(squadra.getNomeSquadra());

        JPanel campi = new JPanel(new GridLayout(1, 2, 14, 0));
        campi.setOpaque(false);
        campi.add(creaGruppoCampo("Nome partecipante", campoAllenatore));
        campi.add(creaGruppoCampo("Nome squadra", campoSquadra));
        contenuto.add(campi, BorderLayout.CENTER);

        JLabel errore = new JLabel(" ");
        errore.setFont(new Font("SansSerif", Font.PLAIN, 14));
        errore.setForeground(COLOR_DANGER);
        JButton annulla = creaBottone("Annulla", COLOR_HEADER_DARK, COLOR_TEXT_WHITE);
        JButton salva = creaBottone("Salva modifiche", COLOR_ACCENT, Color.BLACK);
        JPanel azioni = new JPanel(new BorderLayout(12, 8));
        azioni.setOpaque(false);
        JPanel pulsanti = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pulsanti.setOpaque(false);
        pulsanti.add(annulla);
        pulsanti.add(salva);
        azioni.add(errore, BorderLayout.CENTER);
        azioni.add(pulsanti, BorderLayout.EAST);
        contenuto.add(azioni, BorderLayout.SOUTH);

        dialog.setContentPane(contenuto);
        dialog.setSize(760, 300);
        dialog.setLocationRelativeTo(this);
        annulla.addActionListener(e -> dialog.dispose());
        salva.addActionListener(e -> {
            String allenatore = campoAllenatore.getText().trim();
            String nomeSquadra = campoSquadra.getText().trim();
            if (allenatore.isEmpty() || nomeSquadra.isEmpty()) {
                errore.setText("Compila entrambi i campi.");
                return;
            }
            if (!controller.rinominaPartecipante(squadra, allenatore, nomeSquadra)) {
                errore.setText("Uno dei due nomi è già usato da un altro partecipante.");
                return;
            }
            dialog.dispose();
            aggiornaVista();
            comboVisualizzaRosa.setSelectedItem(squadra);
        });
        dialog.setVisible(true);
        campoAllenatore.requestFocusInWindow();
    }

    private void mostraRosaDettagliata() {
        FantaSquadra sel = (FantaSquadra) comboVisualizzaRosa.getSelectedItem();
        if (sel == null) return;

        lblTitoloRosa.setText(sel.getNomeAllenatore().toUpperCase());

        String nomeSquadra = sel.getNomeSquadra();
        lblNomeSquadra.setText(nomeSquadra != null && !nomeSquadra.trim().isEmpty() ? nomeSquadra : "");

        String resocontoRuoli = String.format("<html><b>Crediti Rimanenti:</b> <font color='#A6E3A1'>%d cr</font><br><br>" +
                "<b>Portieri (P):</b> %d/%d &nbsp;&nbsp;|&nbsp;&nbsp; " +
                "<b>Difensori (D):</b> %d/%d<br>" +
                "<b>Centrocampisti (C):</b> %d/%d &nbsp;&nbsp;|&nbsp;&nbsp; " +
                "<b>Attaccanti (A):</b> %d/%d</html>",
                sel.getCreditiRimanenti(),
                sel.getConteggioRuolo("P"), sel.getLimiteRuolo("P"),
                sel.getConteggioRuolo("D"), sel.getLimiteRuolo("D"),
                sel.getConteggioRuolo("C"), sel.getLimiteRuolo("C"),
                sel.getConteggioRuolo("A"), sel.getLimiteRuolo("A")
        );

        lblInfoRosa.setText(resocontoRuoli);

        ImageIcon fotoGrande = caricaFotoRidimensionata(sel.getNomeAllenatore(), AVATAR_PROFILO_SIZE, AVATAR_PROFILO_SIZE);
        lblFotoProfilo.setIcon(fotoGrande);

        List<Giocatore> rosaOrdinata = new ArrayList<>(sel.getRosa());

        rosaOrdinata.sort((g1, g2) -> {
            int r1 = getPrioritaRuolo(g1.getRuolo());
            int r2 = getPrioritaRuolo(g2.getRuolo());

            if (r1 != r2) {
                return Integer.compare(r1, r2);
            }
            return Integer.compare(g2.getPrezzoAcquisto(), g1.getPrezzoAcquisto());
        });

        tableModelRosaDettaglio.setRowCount(0);
        for (Giocatore g : rosaOrdinata) {
            tableModelRosaDettaglio.addRow(new Object[]{
                g.getNome(),
                g.getRuolo(),
                g.getSquadra(),
                g.getPrezzoAcquisto() + " cr"
            });
        }
    }
}
