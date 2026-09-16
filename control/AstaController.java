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
import java.util.List;

public class AstaController {
    private List<Giocatore> listone;
    private List<FantaSquadra> partecipanti;
    private final String PATH_SALVATAGGIO = "data/stato_asta.txt";

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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_SALVATAGGIO))) {
            for (FantaSquadra fs : partecipanti) {
                bw.write("SQUADRA:" + fs.getNomeAllenatore() + ";" + fs.getCreditiRimanenti());
                bw.newLine();
                for (Giocatore g : fs.getRosa()) {
                    bw.write("GIOCATORE:" + g.getId() + ";" + g.getRuolo() + ";" + g.getNome() + ";" + g.getSquadra() + ";" + g.getPrezzoAcquisto());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio automatico: " + e.getMessage());
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
    File file = new File(PATH_SALVATAGGIO);
    return file.exists() && file.length() > 0;
}

// Carica lo stato delle squadre e dei calciatori dal file salvato
public boolean caricaStatoSalvato() {
    if (!esisteSalvataggio()) return false;

    try (BufferedReader br = new BufferedReader(new FileReader(PATH_SALVATAGGIO))) {
        String riga;
        FantaSquadra squadraCorrente = null;
        
        // 1. Svuotiamo la lista per evitare duplicati
        partecipanti.clear();

        while ((riga = br.readLine()) != null) {
            String[] parti = riga.split(":");
            if (parti.length < 2) continue;

            String tipo = parti[0];
            String[] dati = parti[1].split(";");

            if (tipo.equals("SQUADRA")) {
                String nome = dati[0];
                int creditiResiduiSalvati = Integer.parseInt(dati[1]);
                
                // Ricreiamo la squadra impostando direttamente i crediti presi dal file
                squadraCorrente = new FantaSquadra(nome, creditiResiduiSalvati);
                partecipanti.add(squadraCorrente);

            } else if (tipo.equals("GIOCATORE") && squadraCorrente != null) {
                int id = Integer.parseInt(dati[0]);
                int prezzo = Integer.parseInt(dati[4]);

                // Cerchiamo il giocatore nel listone generale
                Giocatore g = listone.stream()
                        .filter(calc -> calc.getId() == id)
                        .findFirst()
                        .orElse(null);

                if (g != null) {
                    g.setPrezzoAcquisto(prezzo);
                    
                    // IMPORTANTE: Aggiungiamo alla rosa SENZA chiamare compraGiocatore()
                    // per evitare di scalare di nuovo i crediti!
                    squadraCorrente.getRosa().add(g);
                    
                    // Rimuoviamo il giocatore dal listone dei disponibili
                    listone.remove(g);
                }
            }
        }
        return true;
    } catch (IOException e) {
        System.err.println("Errore nel ripristino: " + e.getMessage());
        return false;
    }
}
}