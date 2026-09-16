package model;

import java.util.ArrayList;
import java.util.List;

public class FantaSquadra {
    private String nomeAllenatore;
    private int creditiRimanenti;
    private List<Giocatore> rosa;

    public FantaSquadra(String nomeAllenatore, int creditiIniziali) {
        this.nomeAllenatore = nomeAllenatore;
        this.creditiRimanenti = creditiIniziali;
        this.rosa = new ArrayList<>();
    }

    public String getNomeAllenatore() { return nomeAllenatore; }
    public int getCreditiRimanenti() { return creditiRimanenti; }
    public List<Giocatore> getRosa() { return rosa; }
    public boolean compraGiocatore(Giocatore g, int prezzo) {
        if (prezzo > creditiRimanenti) {
            return false;
        }
        g.setPrezzoAcquisto(prezzo);
        rosa.add(g);
        creditiRimanenti -= prezzo; // <-- Verifica che non ci siano altre detrazioni
        return true;
    }

        public long getConteggioRuolo(String ruolo) {
            return rosa.stream().filter(g -> g.getRuolo().equalsIgnoreCase(ruolo)).count();
        }
        public void setCreditiRimanenti(int creditiRimanenti) {
        this.creditiRimanenti = creditiRimanenti;
    }
    @Override
    public String toString() {
        return nomeAllenatore + " (" + creditiRimanenti + " cr)";
    }
    // Limiti standard Fantacalcio: 3P, 8D, 8C, 6A
public int getLimiteRuolo(String ruolo) {
    switch (ruolo.toUpperCase()) {
        case "P": return 3;
        case "D": return 8;
        case "C": return 8;
        case "A": return 6;
        default: return 0;
    }
}

public boolean haRaggiuntoLimite(String ruolo) {
    return getConteggioRuolo(ruolo) >= getLimiteRuolo(ruolo);
}
}