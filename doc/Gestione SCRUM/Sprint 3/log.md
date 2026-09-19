---
tipo: meeting
sprint: "3"
data: 2026-09-17
---

# 📅 Sprint Planning - 3

- **Data:** 2026-09-17
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Obiettivo dello Sprint

Al termine del terzo Sprint, l'obiettivo è avere un server capace di gestire molteplici partite detenendo al contempo una giocatori in coda in attesa di una stanza libera.
Lato model, invece, l'obiettivo è ulteriormente estendere la logica di gioco, con collisioni e sistemi di danno.
Sinteticamente:

- consentire più partite contemporaneamente (single player per il momento)
- detenere una coda limitata di giocatori in attesa e rifiutare i nuovi
- serializzazione e visualizzazione dello sparo
- la serializzazione delle entità deve permettere di identificare le entità ed il loro tipo (attualmente era possibile solo una navicella indistinguibile dalle altre)
- implementare meccanismi di vita e danno

## 📋 Task Assegnati (Sprint Points)

Suddivisione Item scelti dal Product Backlog:

| ID Obiettivo | Requisito / Area               | Task Associato                              |
| :----------- | :----------------------------- | :------------------------------------------ |
| **RFU5**     | Sparo                          | Serializzazione proiettile                  |
| **RFS2**     | Gestione Multi-partita         | Implementazione multi-partita single player |
| **RFS2**     | Gestione Multi-partita         | Limitare giocatori in coda                  |
| **RFS3**     | Confini e Ostacoli             | Rilevamento Scontri con Arena (da Sprint 2) |
| **RFS4**     | Rilevamento Collisioni e Danni | Implementazione della logica di vita        |
| **RNF4**     | Estensibilità e Modularità     | Refactor game engine                        |
| **RFS3**     | Confini e Ostacoli             | Implementazione mappa di gioco              |
| **RFS4**     | Rilevamento Collisioni e Danni | Implementazione Collision System            |
| **RFS3**     | Confini e Ostacoli             | (opzionale) Implementazione muri            |


| Task ID                                       | Descrizione                                                                                                                     | Assegnatario | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :-------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------ | :----------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| *Implementazione multi-partita single player* | Supporto all'esecuzione concorrente di più partite single-player isolate gestite contemporaneamente dal server                  | Martini      |       -       |     8      |     -      |   **8**   | `To-Do` | Alta     |
| *Serializzazione proiettile*                  | Modellazione del DTO e serializzazione JSON dei proiettili per trasmetterne lo stato ai client via WebSocket                    | Torelli      |       -       |     -      |     3      |   **3**   | `To-Do` | Alta     |
| *Implementazione Collision System*            | Implementazione sistema di collisione per la risoluzione degli urti e la propagazione degli eventi di collisione                | Diotallevi   |       7       |     -      |     -      |    *7*    | `To-Do` | Alta     |
| *Scontri con l'Arena*                         | Applicazione della geometria per impedire alle entità di uscire dai bordi o attraversare i muri                                 | Diotallevi   |       3       |     -      |     -      |    *3*    | `To-Do` | Normale  |
| *Limitare giocatori in coda*                  | Impostazione di un limite massimo di capienza per la lobby e gestione del rifiuto/notifica per i giocatori in eccesso           | Martini      |       -       |     3      |            |   **3**   | `To-Do` | Normale  |
| *Implementazione della logica di vita*        | Implementazione del concetto di vita per le entità spaceship, con relativi danni subiti in caso di collisione con un proiettile | Torelli      |       -       |     -      |     5      |   **4**   | `To-Do` | Normale  |
| *Implementazione mappa di gioco*              | Creazione di un sistema per definire mappe di gioco                                                                             | Torelli      |       -       |     -      |     6      |     4     | `To-Do` | Bassa    |
| *Refactor game engine*                        | Riorganizzazione architetturale del motore per separare pipeline ed esecuzione, migliorando la modularità                       | Diotallevi   |       3       |     -      |     -      |   **3**   | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**                      |                                                                                                                                 |              |    **13**     |   **11**   |   **14**   |  **38**   |         |          |

## 💬 Note & Decisioni

- Aggiunta l'analisi della realizzazione della mappa, in caso di tempi sovrastimati si procederà con la realizzazione della mappa.
- Lo Sprint si concluderà inderogabilmente alla data prestabilita. Nessuna estensione verrà concessa.
- Inizialmente si pensava di mantenere opzionale l'implementazione dei muri, resa "obbligatoria per uniformare il carico di lavoro" (demandabile in caso di sottostima al prossimo sprint).
- L'implementazione del danno è dipendente dall'implementazione degli eventi di collisione, che dovranno essere implementati ancor prima del sistema di collisione.
