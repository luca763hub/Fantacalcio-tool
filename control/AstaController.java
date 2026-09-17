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

public class AstaController {
    private List<Giocatore> listone;
    private List<FantaSquadra> partecipanti;
    private final String PATH_ROSE_IMPORT = "data/rose_import.csv";

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
        partecipanti.add(new FantaSquadra(nome, budgetIniziale));
        salvaStato(); // Salvataggio automatico
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

    // Metodo di Salvataggio su File in tempo reale
    private void salvaStato() {
        salvaRoseImport();
    }

    public String getNomeSquadraFantacalcio(String nomeAllenatore) {
        if (nomeAllenatore == null) return "";
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
                String nomeSquadra = getNomeSquadraFantacalcio(fs.getNomeAllenatore());
                for (Giocatore g : fs.getRosa()) {
                    bw.write(nomeSquadra + "," + g.getId() + "," + g.getPrezzoAcquisto());
                    bw.newLine();
                }
            }
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
    File file = new File(PATH_ROSE_IMPORT);
    return file.exists() && file.length() > 0;
}

// Carica lo stato delle squadre e dei calciatori dal file salvato
public boolean caricaStatoSalvato() {
    if (!esisteSalvataggio()) return false;

    try (BufferedReader br = new BufferedReader(new FileReader(PATH_ROSE_IMPORT))) {
        String riga;
        Map<String, FantaSquadra> squadrePerNome = new HashMap<>();

        partecipanti.clear();

        while ((riga = br.readLine()) != null) {
            if (riga.trim().isEmpty()) continue;

            String[] dati = riga.split(",");
            if (dati.length < 3) continue;

            String nomeSquadraCsv = normalizzaNomeSquadra(dati[0].trim());
            String nomeAllenatore = getAllenatoreDaNomeSquadra(nomeSquadraCsv);
            int idGiocatore = Integer.parseInt(dati[1].trim());
            int prezzo = Integer.parseInt(dati[2].trim());

            FantaSquadra squadra = squadrePerNome.get(nomeAllenatore);
            if (squadra == null) {
                squadra = new FantaSquadra(nomeAllenatore, 500);
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
            }

            int creditiDisponibili = 500 - squadra.getRosa().stream().mapToInt(Giocatore::getPrezzoAcquisto).sum();
            squadra.setCreditiRimanenti(creditiDisponibili);
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