package view;

import control.AstaController;
import model.FantaSquadra;
import model.Giocatore;

import java.util.List;
import java.util.Scanner;

public class AstaView {
    private AstaController controller;
    private Scanner scanner;

    public AstaView(AstaController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void avvia() {
        if (!controller.caricaListoneCSV("data/giocatori.csv")) {
            System.out.println("Errore durante la lettura del file CSV.");
            return;
        }

        System.out.println("-> Listone caricato con successo! Calciatori totali: " + controller.getListone().size());

        configuraAsta();
        gestisciMenu();
    }

    private void configuraAsta() {
        System.out.println("\n=== CONFIGURAZIONE ASTA FANTACALCIO ===");
        System.out.print("Inserisci il numero di partecipanti: ");
        int numPartecipanti = Integer.parseInt(scanner.nextLine());

        System.out.print("Inserisci il budget iniziale per squadra: ");
        int budgetIniziale = Integer.parseInt(scanner.nextLine());

        for (int i = 1; i <= numPartecipanti; i++) {
            System.out.print("Nome Fantallenatore " + i + ": ");
            String nome = scanner.nextLine();
            controller.aggiungiPartecipante(nome, budgetIniziale);
        }
    }

    private void gestisciMenu() {
        boolean inCorso = true;
        while (inCorso) {
            System.out.println("\n-------------------------------------------");
            System.out.println("MENU ASTA:");
            System.out.println("1. Cerca e Assegna Giocatore");
            System.out.println("2. Mostra Tabellone Squadre e Crediti");
            System.out.println("3. Mostra Rosa Partecipante");
            System.out.println("4. Esci");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    assegnaGiocatoreMenu();
                    break;
                case "2":
                    mostraTabellone();
                    break;
                case "3":
                    mostraRosaPartecipante();
                    break;
                case "4":
                    inCorso = false;
                    System.out.println("Asta terminata. Buona fortuna!");
                    break;
                default:
                    System.out.println("Opzione non valida!");
            }
        }
    }

    private void assegnaGiocatoreMenu() {
        System.out.print("Inserisci il nome (o parte del nome) del calciatore: ");
        String query = scanner.nextLine();

        List<Giocatore> trovati = controller.cercaGiocatori(query);

        if (trovati.isEmpty()) {
            System.out.println("Nessun giocatore trovato.");
            return;
        }

        System.out.println("\nRisultati della ricerca:");
        for (int i = 0; i < trovati.size(); i++) {
            System.out.println((i + 1) + ". " + trovati.get(i));
        }

        System.out.print("Seleziona il numero del giocatore (0 per annullare): ");
        int selG = Integer.parseInt(scanner.nextLine());
        if (selG <= 0 || selG > trovati.size()) return;

        Giocatore scelto = trovati.get(selG - 1);
        List<FantaSquadra> partecipanti = controller.getPartecipanti();

        System.out.println("\nSeleziona l'acquirente:");
        for (int i = 0; i < partecipanti.size(); i++) {
            FantaSquadra fs = partecipanti.get(i);
            System.out.println((i + 1) + ". " + fs.getNomeAllenatore() + " (Crediti residui: " + fs.getCreditiRimanenti() + ")");
        }

        System.out.print("Scegli squadra (1-" + partecipanti.size() + "): ");
        int selAcquirente = Integer.parseInt(scanner.nextLine());
        if (selAcquirente <= 0 || selAcquirente > partecipanti.size()) return;

        FantaSquadra acquirente = partecipanti.get(selAcquirente - 1);

        System.out.print("Inserisci prezzo d'acquisto per " + scelto.getNome() + ": ");
        int prezzo = Integer.parseInt(scanner.nextLine());

        if (controller.assegnaGiocatore(scelto, acquirente, prezzo)) {
            System.out.println("-> SUCCESS: " + scelto.getNome() + " assegnato a " + acquirente.getNomeAllenatore() + "!");
        } else {
            System.out.println("-> ERRORE: Crediti insufficienti per questa operazione!");
        }
    }

    private void mostraTabellone() {
        System.out.println("\n=== TABELLONE SQUADRE ===");
        for (FantaSquadra s : controller.getPartecipanti()) {
            System.out.printf("%-15s | Crediti: %-4d | P:%d D:%d C:%d A:%d | Tot: %d/25%n",
                    s.getNomeAllenatore(),
                    s.getCreditiRimanenti(),
                    s.getConteggioRuolo("P"),
                    s.getConteggioRuolo("D"),
                    s.getConteggioRuolo("C"),
                    s.getConteggioRuolo("A"),
                    s.getRosa().size());
        }
    }

    private void mostraRosaPartecipante() {
        List<FantaSquadra> partecipanti = controller.getPartecipanti();
        System.out.println("\nSeleziona il partecipante:");
        for (int i = 0; i < partecipanti.size(); i++) {
            System.out.println((i + 1) + ". " + partecipanti.get(i).getNomeAllenatore());
        }
        int sel = Integer.parseInt(scanner.nextLine());
        if (sel > 0 && sel <= partecipanti.size()) {
            FantaSquadra s = partecipanti.get(sel - 1);
            System.out.println("\n--- Rosa di " + s.getNomeAllenatore() + " ---");
            for (Giocatore g : s.getRosa()) {
                System.out.println(g + " - " + g.getPrezzoAcquisto() + " crediti");
            }
        }
    }
}