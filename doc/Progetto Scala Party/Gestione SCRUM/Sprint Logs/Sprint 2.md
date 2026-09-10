---
tipo: meeting
sprint: "2"
data: 2026-09-09
---

# 📅 Sprint Planning - Sprint 2

- *Data:* 2026-09-09
- *Presenti:* Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Obiettivo dello Sprint

Lo Sprint 2 sposta il focus sulla gestione del ciclo di vita della partita e sullo sviluppo della fisica di gioco.
Lato server, l'obiettivo è permettere di far iniziare una partita partita per volta e accodare i giocatori in modo che al termine della partita corrente possano iniziare la loro.
Il server informerà i giocatori della loro situazione in coda mentre si gioca una partita.
Lato engine, verrà introdotta la possibilità di sparare, inoltre l'astronave non potrà più uscire dall'arena e si inizierà lo sviluppo della logica e dei componenti di collisione.

## 📋 Task Assegnati (Sprint Points)

Suddivisione degli Item pescati dal Product Backlog:

| ID Obiettivo | Requisito                               | Task Associato                                    |
| :----------- | :-------------------------------------- | :------------------------------------------------ |
| *RFU2*       | Accesso Partita                         | Gestione Coda e Ciclo di Vita Lobby               |
| *RFU2*       | Accesso Partita                         | Configurazione dei parametri della partita        |
| *RFS2*       | Sync Multiplayer                        | Gestione Disconnessioni                           |
| *RFS2*       | Sync Multiplayer                        | Broadcast Stato Lobby (WebSocket)                 |
| *RFS2*       | Sync Multiplayer                        | Analisi gestione multi-lobby                      |
| *RFU5*       | Sparo                                   | Implementazione Entità Proiettile                 |
| *RFS3*       | Rilevamento Collisioni                  | Geometria delle Collisioni (Forme e intersezioni) |
| *RFS2*       | Confini e Ostacoli                      | Rilevamento Scontri con Muri/Arena                |
| *RFS3*       | Rilevamento Collisioni (Task di backup) | Collision System (Risoluzione Urti)               |

| Task ID                                      | Descrizione                                                                                                    | Assegnatario     | SP Diotallevi | SP Martini | SP Torelli | SP Totali | Stato | Priorità |
| :------------------------------------------- | :------------------------------------------------------------------------------------------------------------- | :--------------- | :-----------: | :--------: | :--------: | :-------: | :---: | :------- |
| *Gestione Coda e Lobby*                      | Logica server per mantenere la coda e istanziare le partite in sequenza                                        | Martini, Torelli |       -       |     5      |     5      |   *10*    | To-Do | Alta     |
| *Gestione Disconnessioni*                    | Rilevamento disconnessione dei client e pulizia degli slot occupati in coda o in lobby                         | Martini, Torelli |       -       |     2      |     2      |    *4*    | To-Do | Alta     |
| *Implementazione Sparo*                      | Creazione proiettili ereditando la direzione della navicella e gestione del loro ciclo di vita                 | Diotallevi       |       5       |     -      |     -      |    *5*    | To-Do | Alta     |
| *Scontri con l'Arena*                        | Applicazione della geometria per impedire alle entità di uscire dai bordi o attraversare i muri                | Diotallevi       |       3       |     -      |     -      |    *3*    | To-Do | Normale  |
| *Geometria Collisioni*                       | Implementazione delle funzioni matematiche per il rilevamento intersezioni tra forme                           | Diotallevi       |       5       |     -      |     -      |    *5*    | To-Do | Alta     |
| *Configurazione dei parametri della partita* | Gestione unificata dei parametri (single/multi, mappa) condivisi tra gioco ed engine                           | Martini, Torelli |       -       |     3      |     3      |    *6*    | To-Do | Normale  |
| *Broadcast Stato Lobby*                      | Trasmissione ai client via socket degli aggiornamenti di stato della lobby ("ci sono 3 giocatori prima di te") | Martini, Torelli |       -       |     3      |     3      |    *6*    | To-Do | Normale  |
| *TOTALE SPRINT POINTS*                       |                                                                                                                |                  |     *13*      |    *13*    |    *13*    |   *39*    |       |          |

## 💬 Note & Decisioni

- Il rendering è completamente demandato alla generazione automatica tramite AI essendo scritto in HTML e JS e dunque fuori scope.
- Lo Sprint si concluderà inderogabilmente alla data prestabilita. Nessuna estensione verrà concessa.
- Si è valutato durante il corso dello Sprint 2 una implementazione secondaria del LobbyManager mirata a gestire unicamente una lobby funzionante.
- Aggiunti task opzionali in caso il carico degli SP risultasse sovrastimato.