package model;

public class Giocatore {
    private int id;
    private String ruolo; // P, D, C, A
    private String nome;
    private String squadra;
    private int prezzoAcquisto;

    public Giocatore(int id, String ruolo, String nome, String squadra) {
        this.id = id;
        this.ruolo = ruolo;
        this.nome = nome;
        this.squadra = squadra;
        this.prezzoAcquisto = 0;
    }

    public int getId() { return id; }
    public String getRuolo() { return ruolo; }
    public String getNome() { return nome; }
    public String getSquadra() { return squadra; }
    public int getPrezzoAcquisto() { return prezzoAcquisto; }
    public void setPrezzoAcquisto(int prezzoAcquisto) { this.prezzoAcquisto = prezzoAcquisto; }

    @Override
    public String toString() {
        return "[" + ruolo + "] " + nome + " (" + squadra + ")";
    }
}