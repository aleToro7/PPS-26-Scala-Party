---
tipo: meeting
sprint: "4"
data: 2026-09-26
---

# 📅 Sprint Planning - 4

- **Data:** 2026-09-26
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Obiettivo dello Sprint

Al termine del quarto Sprint, l'obiettivo è implementare tutte le restanti parti del gioco, per consegnare una versione effettivamente giocabile. In particolare, al termine dello Sprint dovrebbe essere possibile giocare anche più di una partita multi-player in contemporanea, in un'arena con muri e (opzionalmente) bonus randomici che si generano ogni tanto.

## 📋 Task Assegnati (Sprint Points)

Suddivisione Item scelti dal Product Backlog:

| ID Requisito | Requisito / Area              | Task Associato                   |
| :----------- | :---------------------------- | :------------------------------- |
| **RFS2**     | Gestione Multi-partita        | Multiplayer (server)             |
|              | Bug Networking                | Fix errore disconnessione client |
| **RFS4**     | Eventi di Gioco               | Implementazione Death Event      |
| **RFS4**     | Eventi di Gioco               | Implementazione Match-End        |
| **RNF4**     | Estensibilità e Modularità    | Refactor GameEngine              |
| **RFS3**     | Confini e Ostacoli            | Implementazione mappa di gioco   |
| **RFS8**     | (Opzionale) Entità di Gioco   | Generazione power-up             |
|              | (Opzionale) Benchmarking core | Integrazione benchmark di update |

| Task ID                            | Descrizione                                                                         | Assegnatario | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :--------------------------------- | :---------------------------------------------------------------------------------- | :----------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| _Multiplayer (server)_             | Implementazione della logica server-side per la gestione delle sessioni multiplayer | Martini      |       -       |     8      |     -      |   **8**   | `To-Do` | Alta     |
| _Refactor GameEngine_              | Riorganizzazione architetturale tramite WorldBuilder e parametri di contesto        | Diotallevi   |       3       |     -      |     -      |   **3**   | `To-Do` | Alta     |
| _Implementazione mappa di gioco_   | Creazione di mappe di gioco in base al numero di giocatori e world-builder          | Diotallevi   |       6       |     -      |     -      |   **6**   | `To-Do` | Alta     |
| _Implementazione Match-End_        | Gestione condizione di fine partita (vittoria/limite tempo) e notifica ai client    | Torelli      |       -       |     -      |     3      |   **3**   | `To-Do` | Alta     |
| _Implementazione Death Event_      | Creazione e propagazione dell'evento di morte per le entità con cleanup             | Torelli      |       -       |     -      |     3      |   **3**   | `To-Do` | Normale  |
| _Fix errore disconnessione client_ | Indagine e risoluzione dell'eccezione lanciata dal server alla disconnessione       | Martini      |       -       |     3      |     -      |   **3**   | `To-Do` | Normale  |
| _Generazione power-up_             | Creazione del sistema di spawn per i power-up con relativi effetti temporanei       | Torelli      |       -       |     -      |     5      |   **5**   | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**           |                                                                                     |              |     **9**     |   **11**   |   **11**   |  **31**   |         |          |

## 💬 Note & Decisioni

Lo Sprint è volutamente più breve del solito, per permettere di avere un'ulteriore revisione del lavoro fatto e per permettere di fare un'ulteriore pianificazione prima della consegna finale.