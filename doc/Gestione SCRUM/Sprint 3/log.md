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

Al termine del terzo Sprint, l'obiettivo è avere un server capace di gestire molteplici partite con un max ipotetico di 100 giocatori in coda. Inoltre lo dovrà essere visibile ed utilizzabile. In particolare:

- consentire più partite contemporaneamente (single player), con un numero massimo di giocatori in coda
- serializzazione sparo (proiettile)
- le entità devono riportare tipo dell'entità ed entity id
- implementare meccanismi di vita
- refactor game engine

## 📋 Task Assegnati (Sprint Points)

Suddivisione Item scelti dal Product Backlog:

| ID Obiettivo | Requisito / Area               | Task Associato                                    |
| :----------- | :----------------------------- | :------------------------------------------------ |
| **RFU5**     | Sparo                          | Serializzazione proiettile                        |
| **RFS2**     | Gestione Multi-partita         | Implementazione multi-partita single player       |
| **RFS2**     | Gestione Multi-partita         | Limitare giocatori in coda                        |
| **RFS3**     | Confini e Ostacoli             | Rilevamento Scontri con Arena (recupero sprint 2) |
| **RFS3**     | Confini e Ostacoli             | Implementazione muri                              |
| **RFS4**     | Rilevamento Collisioni e Danni | Implementazione della logica di vita              |
| **RNF4**     | Estensibilità e Modularità     | Refactor game engine                              |
| **RFS4**     | Rilevamento Collisioni e Danni | (Opzionale) implementazione muri                  |
| **RFS3**     | Confini e Ostacoli             | Analisi realizzazione mappa di gioco              |

| Task ID                                       | Descrizione                                                                                                                     | Assegnatario                 | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :-------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------ | :--------------------------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| *Implementazione multi-partita single player* | Supporto all'esecuzione concorrente di più partite single-player isolate gestite contemporaneamente dal server                  | Martini                      |       -       |     8      |     -      |   **8**   | `To-Do` | Alta     |
| *Limitare giocatori in coda*                  | Impostazione di un limite massimo di capienza per la lobby e gestione del rifiuto/notifica per i giocatori in eccesso           | Martini                      |       -       |     3      |            |   **3**   | `To-Do` | Normale  |
| *Serializzazione proiettile*                  | Modellazione del DTO e serializzazione JSON dei proiettili per trasmetterne lo stato ai client via WebSocket                    | Torelli                      |       -       |     -      |     3      |   **3**   | `To-Do` | Alta     |
| *Implementazione della logica di vita*        | Implementazione del concetto di vita per le entità spaceship, con relativi danni subiti in caso di collisione con un proiettile | Torelli                      |       -       |     -      |     4      |   **4**   | `To-Do` | Normale  |
| *Implementazione muri*                        | Definizione dell'entità statica per gli ostacoli e inserimento dei blocchi all'interno del mondo di gioco                       | Torelli                      |       -       |     -      |     4      |     4     | `To-Do` | Normale  |
| *Refactor game engine*                        | Riorganizzazione architetturale del motore per separare pipeline ed esecuzione, migliorando la modularità                       | Diotallevi                   |       8       |     -      |     -      |   **8**   | `To-Do` | Normale  |
| *Scontri con l'Arena*                         | Applicazione della geometria per impedire alle entità di uscire dai bordi o attraversare i muri                                 | Diotallevi                   |       3       |     -      |     -      |    *3*    | `To-Do` | Alta     |
| *Analisi realizzazione mappa di gioco*        | Analisi di gruppo per la modalità e posizionamento degli ostacoli all'interno del campo di gioco                                | Diotallevi, Martini, Torelli |       2       |     2      |     2      |     6     | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**                      |                                                                                                                                 |                              |    **13**     |   **13**   |   **13**   |  **39**   |         |          |

## 💬 Note & Decisioni

- Aggiunta l'analisi della realizzazione della mappa, in caso di tempi sovrastimati si procederà con la realizzazione della mappa.
- Lo Sprint si concluderà inderogabilmente alla data prestabilita. Nessuna estensione verrà concessa.
- Inizialmente si pensava di mantenere opzionale l'implementazione dei muri, resa "obbligatoria per uniformare il carico di lavoro" (demandabile in caso di sottostima al prossimo sprint).
