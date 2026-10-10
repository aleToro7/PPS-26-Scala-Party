---
tipo: meeting
sprint: "1"
data: 2026-08-30
---

# 🔄 Sprint 1 - Review & Retrospective

- **Data:** 2026-08-30
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli
---

## 🎯 Sprint Review (Esito dello Sprint)

> **Obiettivo iniziale**:
> Rilasciare il prototipo fondamentale del sistema: con accesso alla piattaforma ed assegnazione di una navicella in grado di muoversi liberamente sullo schermo, ruotando a comando.

> **Esito**:
> L'obiettivo è stato parzialmente raggiunto: 
> Il team ha completato con successo l'analisi dell'architettura e la configurazione dell'infrastruttura di base.
> Attualmente, la pagina web del gioco è raggiungibile e le rotte di accesso al server sono operative.
> Tuttavia, la partita non viene effettivamente iniziata una volta riempita la lobby, pertanto il giocatore non può interagire con la piattaforma.


## 📋 Task Assegnati (Sprint Points)


| Task ID                              | Descrizione                                                                                                   | Assegnatario                 | SP Diotallevi | SP Martini | SP Torelli | SP Totali | Stato  | Priorità |
| :----------------------------------- | :------------------------------------------------------------------------------------------------------------ | :--------------------------- | :-----------: | :--------: | :--------: | :-------: | :----: | :------- |
| **Analisi architettura**             | Definizione della struttura generale del server                                                               | Diotallevi, Martini, Torelli |      2/2      |    2/2     |    2/2     |  **6/6**  | `Done` | Alta     |
| **Analisi rotte di accesso**         | Analisi della costruzione del server e delle rotte                                                            | Martini, Torelli             |       -       |    2/2     |    2/2     |  **4/4**  | `Done` | Alta     |
| **Implementazione rotte di accesso** | Sviluppo degli endpoint di rete e gestione dei canali di comunicazione                                        | Martini, Torelli             |       -       |    4/4     |    5/4     |  **9/8**  | `Done` | Normale  |
| **Implementazione invio comandi**    | Realizzazione del meccanismo di invio e gestione asincrona dei comandi utente da client a server              | Martini, Torelli             |       -       |    0/4     |    12/4    | **12/8**  | `Done` | Normale  |
| **Implementazione navicella**        | Modellazione dell'entità navicella all'interno del dominio di gioco                                           | Diotallevi                   |      8/5      |     -      |     -      |  **8/5**  | `Done` | Normale  |
| **Implementazione movimento**        | Realizzazione della logica di movimento continuo della navicella gestita dal ciclo di simulazione             | Diotallevi                   |      5/4      |     -      |     -      |  **5/4**  | `Done` | Normale  |
| **Implementazione rotazione**        | Sviluppo del controllo di rotazione in tempo reale dell'entità navicella in risposta agli input del giocatore | Diotallevi                   |      2/3      |     -      |     -      |  **2/3**  | `Done` | Normale  |
| **TOTALE SPRINT POINTS**             |                                                                                                               |                              |   **17/13**   |  **8/11**  | **21/11**  | **46/38** |        |          |

## 📊 Analisi del Workload e Metriche

Durante l'esecuzione dello Sprint è emersa una discrepanza significativa tra le stime in Sprint Points e le ore effettive lavorate, dovuta a imprevisti organizzativi:

- **Alessandro Martini**: A causa di problemi personali, ha potuto lavorare solo in parte al progetto, dedicandogli 8SP invece dei 15SP previsti per lo Sprint.
- **Alessandro Torelli**: Per compensare l'assenza e non bloccare lo sviluppo dell'infrastruttura, ha assorbito gran parte dei task di Martini, registrando un surplus di circa +10SP rispetto al proprio effort stimato.
- **Federico Diotallevi**: Ha completato tutti i task assegnati, con un effort totale di 16SP, superando di 3SP le stime iniziali.

L'impatto principale di questa riassegnazione è stato l'estensione temporale dello Sprint oltre la data di chiusura prestabilita.
Nonostante ciò, le considerazioni finali sulle stime sono positive.
Infatti il team si è appena formato e ritardi potenziali dovuti a stime errate ed imprevisti erano già pienamente considerati nel carico dii lavoro iniziale.
Dei 45SP totali previsti per lo Sprint, ne erano stati allocati soltanto 38SP, lasciando un margine di SP per eventuali imprevisti.

## 🔍 Sprint Retrospective

###  🟢Cosa ha funzionato

- La divisione tra lo sviluppo del dominio (Diotallevi) e lo sviluppo infrastrutturale/di rete (Torelli/Martini) ha permesso di lavorare in parallelo creando conflitti minori e del tutto trascurabili durante il merge.
- La suddivisione di task in branch dedicati e la richiesta di revisione per le pull request ha portato ad un'ottima collaborazione del team e all'individuazione di potenziali punti critici.

### 🔴 Cosa migliorare

- L'estensione della durata dello Sprint ha compromesso l'agilità del team. È preferibile chiudere lo Sprint alla data prefissata, accettando che alcuni task slittino al ciclo successivo, piuttosto che allungare le scadenze. Allungare lo Sprint ha portato a "deadlock" di alcuni membri.

