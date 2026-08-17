
## Requisiti di business

L'obiettivo chiave del progetto è la realizzazione di Scala Party: un videogioco multiplayer online di tipo arcade, fortemente ispirato al celebre _Astro Party_. In particolare:

- Il gioco deve essere fruibile direttamente tramite un'interfaccia web che permetta di accedere ad una partita e giocare con altri utenti.
    
- Il sistema deve consentire a più utenti di connettersi a una partita online e competere in tempo reale, assicurando una sincronizzazione corretta dello stato di gioco.
    
- Le meccaniche di gioco devono rispecchiare fedelmente il modello di _Astro Party_.

## Modello del dominio

ScalaParty è un videogioco multigiocatore ambientato nello spazio. Ogni partita comprende fino a quattro giocatori, ognuno dei quali controlla una propria navicella spaziale all'interno di un'arena delimitata. Le navicelle si muovono costantemente nello spazio di gioco, poiché una navicella non può mai rimanere ferma: il giocatore può esclusivamente modificarne la direzione di rotazione. Durante la partita, le navicelle possono sparare proiettili per colpire gli avversari. Lo spazio di gioco è caratterizzato dalla presenza di muri fissi, che costituiscono ostacoli impenetrabili sia per le navicelle che per i proiettili.

Ogni navicella dispone di un livello di vita che diminuisce a ogni danno subito, che può essere provocato dall'impatto diretto con un proiettile avversario o dallo scontro con un'altra navicella. Quando i punti vita si azzerano, la navicella viene distrutta ed è eliminata dalla partita. L'ultimo giocatore a rimanere in vita si aggiudica la vittoria.

Gli elementi concettuali e le entità che compongono il dominio di gioco sono i seguenti:

-  **Partita (Match):** Rappresenta l'istanza di gioco attiva, caratterizzata da un'arena con confini definiti, una durata e un insieme di giocatori partecipanti.
- **Giocatore (Player):** L'utente collegato alla sessione, che detiene il controllo di una navicella all'interno dell'arena.
- **Navicella (Spaceship)**: La navicella controllata dal giocatore che naviga all'interno dell'arena.
- **Muro (Wall)**: Ostacolo che impedisce il passaggio di navicelle e proiettili attraverso di esso.
- **Proiettile (Bullet)**: Colpo sparato da una nevicella che causa danno da contatto ad altre navicelle avversarie.
- **Arena**: Spazio di gioco limitato in cui si sfidano le navicelle spaziali.

--- Da inserire un bello schema UML ---

## Requisiti funzionali

In questa sezione vengono riportate le interazioni consentite agli utenti e i comportamenti che il sistema deve garantire per soddisfare le regole di gioco di **Scala Party**. Sono riportati, per ogni sezione, due tipi di requisiti:
- Requisiti obbligatori: condizioni da soddisfare necessariamente per garantire l'uscita del videogioco.
- Requisiti opzionali: obiettivi previsti per release future, non strettamente necessari per una prima release.
### Requisiti Utente

I requisiti utente descrivono le azioni che i giocatori possono compiere interagendo con l'applicazione.

##### Requisiti Obbligatori

- **Accesso alla Piattaforma (RFU1):** L'utente deve potersi connettere al sistema di gioco tramite un comune browser web.
- **Partecipazione alla Partita (RFU 2):** L'utente deve potersi unire a una sessione di gioco online, condividendo la sessione con un numero massimo di quattro partecipanti complessivi.
- - **Controllo della Navicella (RFU3):** Durante la partita, l'utente deve poter variare in tempo reale la direzione di rotazione della propria navicella spaziale.
- **Permanenza nell'Arena:** L'utente deve avere pieno accesso alla visione dell'arena e di tutti gli elementi che ne fanno parte per l'intera durata della partita.
- **Sparo:** L'utente deve poter azionare il comando di sparo per rilasciare un proiettile nella direzione corrente della navicella da lui controllata.

##### Requisiti Opzionali

- **Visione Statistiche del Giocatore:** Durante e dopo la partita, ciascun utente deve poter vedere statistiche riportanti: precisione, danni inflitti, colpi sparati nemici eliminati.
- **Visione Replay:** Al termine della partita, l'utente può vedere la simulazione della stessa fino al momento a cui vi ha partecipato.

### Requisiti di Sistema

I requisiti di sistema descrivono le risposte automatiche, le regole di simulazione e la gestione dello stato eseguite dal software.

##### Requisiti Obbligatori

- **Movimento Continuo (RFS1):** Il sistema deve garantire che le navicelle si muovano costantemente nello spazio di gioco a una velocità prestabilita, impedendo che un'unità rimanga ferma.
- **Gestione dei Confini e degli Ostacoli:** Il sistema deve impedire alle navicelle e ai proiettili di superare i confini dell'arena o di attraversare i muri presenti al suo interno.
- **Rilevamento Collisioni e Danni:** Il sistema deve rilevare gli impatti tra navicelle e proiettili o tra navicelle stesse, applicando la conseguente riduzione dei punti vita all'unità colpita.
- **Eliminazione Navicelle:** Il sistema deve rimuovere permanentemente dall'arena le navicelle i cui punti vita si azzerano, decretando l'eliminazione del giocatore corrispondente.
- **Condizione di Vittoria:** Il sistema deve monitorare continuamente lo stato della partita e dichiarare vincitore l'ultimo giocatore la cui navicella rimane in vita.
- **Sincronizzazione in Tempo Reale:** Il sistema deve aggiornare e sincronizzare lo stato della partita in tempo reale tra tutti i client connessi, garantendo coerenza visiva e interattiva durante lo svolgimento del match.

##### Requisiti Opzionali

- **Generazione di Bonus:** Il sistema deve poter generare casualmente all'interno dell'arena elementi bonus temporanei in grado di conferire vantaggi speciali alle navicelle che li raccolgono.
- **Gestione Multi-partita:** Il sistema deve essere in grado di ospitare, isolare e gestire simultaneamente più sessioni di gioco distinte e indipendenti tra loro.

### Requisiti Non Funzionali

I requisiti non funzionali definiscono le qualità qualitative, le prestazioni e i vincoli operativi che il sistema deve soddisfare, focalizzandosi sul _come_ il software si comporta piuttosto che sulle specifiche funzioni di gioco.
