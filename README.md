# Guida all’asta di fantacalcio

[![Sostieni il progetto (offrimi una caffè)](https://img.shields.io/badge/Sostieni%20il%20progetto-offrimi%20una%20birra-FFB000?style=for-the-badge&logo=coffee)](buymeacoffee.com/lucayu763a)

Questa guida spiega come usare il programma, un passaggio alla volta.

## Avviare il programma

Apri un terminale nella cartella del progetto e lancia questi comandi:

```sh
mkdir -p out
javac -d out Main.java control/*.java model/*.java view/*.java
java -cp out Main
```

Serve il JDK Java installato. È importante avviare il programma dalla cartella principale del progetto, quella che contiene `Main.java`, perché il programma cerca lì i file della cartella `data`.

## Iniziare un’asta

Se esiste già un’asta salvata, il programma ti chiede se vuoi riprenderla o iniziarne una nuova. Scegli **Riprendi asta** per continuare la sessione precedente. Scegli **Nuova asta** per crearne una: i nuovi dati sostituiranno quelli dell’asta salvata.

Per una nuova asta:

1. Scegli il numero dei partecipanti, da 2 a 20.
2. Scegli i crediti iniziali. Questo numero vale per tutte le squadre.
3. Scrivi il nome di ogni partecipante e quello della sua squadra. I campi sono vuoti all’inizio: non dimenticarne nessuno.
4. Premi **Crea asta**.

I nomi dei partecipanti devono essere tutti diversi tra loro. Anche i nomi delle squadre devono essere tutti diversi tra loro. Scrivi per ogni squadra lo stesso nome scelto dai partecipanti nell’app ufficiale del Fantacalcio, facendo attenzione a spazi e grafia: serve per riconoscerla quando importerai i risultati.

### Se due partecipanti hanno lo stesso nome

Aggiungi l’iniziale del cognome per distinguerli. Per esempio:

- Marco R. (Marco Rossi)
- Marco B. (Marco Bianchi)

Se anche le iniziali sono uguali, aggiungi altre lettere del cognome, per esempio **Marco Ro.** e **Marco Ru.**. Così saprai sempre a chi appartiene ogni squadra.

## Comprare un calciatore

1. Scrivi tutto o una parte del nome nella casella di ricerca a sinistra.
2. Premi **Cerca**.
3. Fai doppio clic sul calciatore che vuoi comprare. Puoi anche selezionarlo e premere Invio.
4. Scegli la squadra che lo compra.
5. Scrivi il prezzo in crediti e premi **Assegna**.

Il programma aggiorna i crediti e la rosa. Se la squadra non ha abbastanza crediti o ha già completato quel ruolo, il programma te lo segnala.

## Guardare e gestire le squadre

- Apri **Tabellone Generale** per vedere partecipanti, crediti e giocatori di ogni ruolo.
- Apri **Dettaglio Rose** e scegli una squadra dal menu per vedere i suoi calciatori.
- Per cambiare il nome del partecipante o della squadra selezionata, premi **Modifica nomi**. I nomi devono restare unici.
- Per svincolare un calciatore, selezionalo nella rosa e premi Invio. Poi scegli **Rimuovi / Svincola** e conferma.

## Foto e audio dei partecipanti

Foto e audio sono facoltativi. Se vuoi usarli, salva i file con lo stesso nome scritto per il partecipante:

- Foto: mettila in `data/foto/`, per esempio `data/foto/Marco R.png`.
- Audio: mettilo in `data/audio/` e usa il formato WAV, per esempio `data/audio/Marco R.wav`.

Se cambi il nome del partecipante durante l’asta, rinomina anche i suoi file foto e audio. Se un file manca, l’asta continua comunque; semplicemente non vedrai la foto o non sentirai l’audio.

## Salvataggio e chiusura

Il programma salva automaticamente i nomi, le squadre e gli acquisti. Quando lo riapri, puoi riprendere l’asta salvata.

Quando l’asta è terminata, usa il file data/rose_import.csv per importare i risultati nell’app ufficiale del Fantacalcio. L’app può usare questo file per assegnare automaticamente i calciatori alle squadre. Per farlo funzionare, i nomi delle squadre nel setup devono essere uguali a quelli già presenti nell’app ufficiale.

Per chiudere il programma, chiudi la finestra principale.
