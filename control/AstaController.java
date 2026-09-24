package control;

import model.Giocatore;
import model.FantaSquadra;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class AstaController {
    private List<Giocatore> listone;
    private List<FantaSquadra> partecipanti;
    private final String PATH_ROSE_IMPORT = "data/rose_import.csv";
    private final String PATH_SETUP_ASTA = "data/setup_asta.properties";

    public AstaController() {
        this.listone = new ArrayList<>();
        this.partecipanti = new ArrayList<>();
    }

    // Caricamento dati da CSV
    public boolean caricaListoneCSV(String percorsoFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(percorsoFile))) {
            String riga;
            boolean primaRiga = true;

            while ((riga = br.readLine()) != null) {
                if (primaRiga) {
                    primaRiga = false;
                    continue;
                }

                String[] campi = riga.split(",");
                if (campi.length >= 5) {
                    try {
                        int id = Integer.parseInt(campi[0].trim());
                        String ruolo = campi[1].trim();
                        String nome = campi[3].trim();
                        String squadra = campi[4].trim();

                        listone.add(new Giocatore(id, ruolo, nome, squadra));
                    } catch (NumberFormatException e) {
                        // Ignora eventuali righe malformate
                    }
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Aggiungi partecipante + AUTOSAVE
    public void aggiungiPartecipante(String nome, int budgetIniziale) {
        aggiungiPartecipante(nome, getNomeSquadraFantacalcio(nome), budgetIniziale);
    }

    public void aggiungiPartecipante(String nomeAllenatore, String nomeSquadra, int budgetIniziale) {
        partecipanti.add(new FantaSquadra(nomeAllenatore, nomeSquadra, budgetIniziale));
        salvaStato();
    }

    // Ricerca calciatori
    public List<Giocatore> cercaGiocatori(String query) {
        List<Giocatore> trovati = new ArrayList<>();
        String queryLower = query.toLowerCase();
        for (Giocatore g : listone) {
            if (g.getNome().toLowerCase().contains(queryLower)) {
                trovati.add(g);
            }
        }
        return trovati;
    }

    // Assegnazione giocatore + AUTOSAVE
    public boolean assegnaGiocatore(Giocatore giocatore, FantaSquadra acquirente, int prezzo) {
        boolean successo = acquirente.compraGiocatore(giocatore, prezzo);
        if (successo) {
            listone.remove(giocatore);
            salvaStato(); // Salvataggio automatico dopo acquisto
        }
        return successo;
    }

    public boolean rinominaPartecipante(FantaSquadra squadra, String nuovoAllenatore, String nuovaSquadra) {
        if (squadra == null || nuovoAllenatore == null || nuovaSquadra == null) return false;
        String allenatore = nuovoAllenatore.trim();
        String nomeSquadra = nuovaSquadra.trim();
        if (allenatore.isEmpty() || nomeSquadra.isEmpty()) return false;

        for (FantaSquadra partecipante : partecipanti) {
            if (partecipante == squadra) continue;
            if (partecipante.getNomeAllenatore().equalsIgnoreCase(allenatore)
                    || partecipante.getNomeSquadra().equalsIgnoreCase(nomeSquadra)) {
                return false;
            }
        }

        squadra.setNomeAllenatore(allenatore);
        squadra.setNomeSquadra(nomeSquadra);
        salvaStato();
        return true;
    }

    // Metodo di Salvataggio su File in tempo reale
    private void salvaStato() {
        salvaSetupAsta();
        salvaRoseImport();
    }

    private void salvaSetupAsta() {
        Properties setup = new Properties();
        setup.setProperty("numeroPartecipanti", Integer.toString(partecipanti.size()));
        for (int i = 0; i < partecipanti.size(); i++) {
            FantaSquadra partecipante = partecipanti.get(i);
            String prefisso = "partecipante." + i + ".";
            setup.setProperty(prefisso + "allenatore", partecipante.getNomeAllenatore());
            setup.setProperty(prefisso + "squadra", partecipante.getNomeSquadra());
            setup.setProperty(prefisso + "budget", Integer.toString(partecipante.getCreditiRimanenti()
                    + partecipante.getRosa().stream().mapToInt(Giocatore::getPrezzoAcquisto).sum()));
        }
        try (java.io.OutputStream out = new java.io.FileOutputStream(PATH_SETUP_ASTA)) {
            setup.store(out, "Configurazione asta");
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio della configurazione: " + e.getMessage());
        }
    }

    public String getNomeSquadraFantacalcio(String nomeAllenatore) {
        if (nomeAllenatore == null) return "";
        for (FantaSquadra partecipante : partecipanti) {
            if (partecipante.getNomeAllenatore().equalsIgnoreCase(nomeAllenatore.trim())) {
                return partecipante.getNomeSquadra();
            }
        }
        String n = nomeAllenatore.trim();

        switch (n.toLowerCase()) {
            case "manolo": return "AC TUA";
            case "simone m": return "FUTURO INTER-NAZIONALE";
            case "simone p": return "FUTURO INTER-NAZIONALE";
            case "simone g": return "lager mania";
            case "luigi": return "Locatelli amministrami casa";
            case "leonardo": return "MacLautaro";
            case "mattia": return "Reggina Celik";
            case "zampa": return "Sporting GC";
            case "luca": return "Tua madre";
            case "ac tua": return "AC TUA";
            case "futuro inter-nazionale": return "FUTURO INTER-NAZIONALE";
            case "lager mania": return "lager mania";
            case "locatelli amministrami casa": return "Locatelli amministrami casa";
            case "locatelli amministrami casa gigi": return "Locatelli amministrami casa";
            case "maclautaro": return "MacLautaro";
            case "reggina celik": return "Reggina Celik";
            case "sporting gc": return "Sporting GC";
            case "tua madre": return "Tua madre";
            default: return n;
        }
    }

    public String getAllenatoreDaNomeSquadra(String nomeSquadra) {
        if (nomeSquadra == null) return "";
        String n = nomeSquadra.trim();

        switch (n.toLowerCase()) {
            case "ac tua": return "Manolo";
            case "futuro inter-nazionale": return "Simone M";
            case "lager mania": return "Simone G";
            case "locatelli amministrami casa": return "Luigi";
            case "maclautaro": return "Leonardo";
            case "reggina celik": return "Mattia";
            case "sporting gc": return "Zampa";
            case "tua madre": return "Luca";
            default: return n;
        }
    }

    private String normalizzaNomeSquadra(String nome) {
        if (nome == null) return "";
        String n = nome.trim();

        switch (n.toLowerCase()) {
            case "manolo": return "AC TUA";
            case "simone m": return "FUTURO INTER-NAZIONALE";
            case "simone p": return "FUTURO INTER-NAZIONALE";
            case "simone g": return "lager mania";
            case "luigi": return "Locatelli amministrami casa";
            case "leonardo": return "MacLautaro";
            case "mattia": return "Reggina Celik";
            case "zampa": return "Sporting GC";
            case "luca": return "Tua madre";
            case "ac tua": return "AC TUA";
            case "futuro inter-nazionale": return "FUTURO INTER-NAZIONALE";
            case "lager mania": return "lager mania";
            case "locatelli amministrami casa": return "Locatelli amministrami casa";
            case "locatelli amministrami casa gigi": return "Locatelli amministrami casa";
            case "maclautaro": return "MacLautaro";
            case "reggina celik": return "Reggina Celik";
            case "sporting gc": return "Sporting GC";
            case "tua madre": return "Tua madre";
            default: return n;
        }
    }

    private void salvaRoseImport() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_ROSE_IMPORT))) {
            for (FantaSquadra fs : partecipanti) {
                String nomeSquadra = fs.getNomeSquadra();
                for (Giocatore g : fs.getRosa()) {
                    bw.write(nomeSquadra + "," + g.getId() + "," + g.getPrezzoAcquisto());
                    bw.newLine();
                }
            }
            System.out.println("CSV esportato. Questa app è stata sviluppata nel tempo libero: grazie per averla usata!");
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio del CSV rose: " + e.getMessage());
        }
    }

    // Getters
    public List<Giocatore> getListone() { return listone; }
    public List<FantaSquadra> getPartecipanti() { return partecipanti; }


    // Rimuove un giocatore da una squadra, gli restituisce i crediti e lo riaggiunge al listone
public boolean rimuoviGiocatoreDaSquadra(FantaSquadra squadra, Giocatore giocatore) {
    if (squadra.getRosa().contains(giocatore)) {
        squadra.getRosa().remove(giocatore);
        // Restituisce i crediti spesi alla squadra
        squadra.setCreditiRimanenti(squadra.getCreditiRimanenti() + giocatore.getPrezzoAcquisto());
        giocatore.setPrezzoAcquisto(0);
        
        // Riaggiunge il calciatore al listone disponibile
        listone.add(giocatore);
        
        // Salvataggio in tempo reale
        salvaStato();
        return true;
    }
    return false;
}

// Verifica se esiste un file di salvataggio valido
public boolean esisteSalvataggio() {
    File setup = new File(PATH_SETUP_ASTA);
    File rose = new File(PATH_ROSE_IMPORT);
    return (setup.exists() && setup.length() > 0) || (rose.exists() && rose.length() > 0);
}

// Carica lo stato delle squadre e dei calciatori dal file salvato
public boolean caricaStatoSalvato() {
    if (!esisteSalvataggio()) return false;

    try {
        partecipanti.clear();
        Map<String, FantaSquadra> squadrePerNome = new HashMap<>();
        File fileSetup = new File(PATH_SETUP_ASTA);
        if (fileSetup.exists()) {
            Properties setup = new Properties();
            try (java.io.InputStream in = new java.io.FileInputStream(fileSetup)) {
                setup.load(in);
            }
            int numeroPartecipanti = Integer.parseInt(setup.getProperty("numeroPartecipanti", "0"));
            for (int i = 0; i < numeroPartecipanti; i++) {
                String prefisso = "partecipante." + i + ".";
                String allenatore = setup.getProperty(prefisso + "allenatore", "").trim();
                String nomeSquadra = setup.getProperty(prefisso + "squadra", "").trim();
                int budget = Integer.parseInt(setup.getProperty(prefisso + "budget", "500"));
                if (!allenatore.isEmpty() && !nomeSquadra.isEmpty()) {
                    FantaSquadra squadra = new FantaSquadra(allenatore, nomeSquadra, budget);
                    squadrePerNome.put(nomeSquadra, squadra);
                    partecipanti.add(squadra);
                }
            }
        }

        File fileRose = new File(PATH_ROSE_IMPORT);
        if (!fileRose.exists()) return !partecipanti.isEmpty();
        try (BufferedReader br = new BufferedReader(new FileReader(fileRose))) {
        String riga;
        while ((riga = br.readLine()) != null) {
            if (riga.trim().isEmpty()) continue;

            String[] dati = riga.split(",");
            if (dati.length < 3) continue;

            String nomeSquadraCsv = dati[0].trim();
            String nomeSquadraLegacy = normalizzaNomeSquadra(nomeSquadraCsv);
            String nomeAllenatore = getAllenatoreDaNomeSquadra(nomeSquadraLegacy);
            int idGiocatore = Integer.parseInt(dati[1].trim());
            int prezzo = Integer.parseInt(dati[2].trim());

            FantaSquadra squadra = squadrePerNome.get(nomeSquadraCsv);
            if (squadra == null) {
                squadra = squadrePerNome.get(nomeAllenatore);
            }
            if (squadra == null) {
                squadra = new FantaSquadra(nomeAllenatore, nomeSquadraLegacy, 500);
                squadrePerNome.put(nomeSquadraCsv, squadra);
                squadrePerNome.put(nomeAllenatore, squadra);
                partecipanti.add(squadra);
            }

            Giocatore g = listone.stream()
                    .filter(calc -> calc.getId() == idGiocatore)
                    .findFirst()
                    .orElse(null);

            if (g != null) {
                g.setPrezzoAcquisto(prezzo);
                squadra.getRosa().add(g);
                listone.remove(g);
                squadra.setCreditiRimanenti(squadra.getCreditiRimanenti() - prezzo);
            }
        }
        }
        return true;
    } catch (IOException e) {
        System.err.println("Errore nel ripristino: " + e.getMessage());
        return false;
    } catch (NumberFormatException e) {
        System.err.println("Formato CSV non valido per il ripristino: " + e.getMessage());
        return false;
    }
}
}