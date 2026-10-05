---
tipo: meeting
sprint: "4"
data: 2026-10-05
---

# 🔄 Sprint 4 - Review & Retrospective

- **Data:** 2026-10-05
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Sprint Review (Esito dello Sprint)

> **Obiettivo iniziale**:


> **Esito**:
L'obiettivo è stato pienamente raggiunto portando a teermine l'implementazione del gioco.
## 📋 Task Assegnati (Sprint Points)

| Task ID                            | Descrizione                                                                         | Assegnatario | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :--------------------------------- | :---------------------------------------------------------------------------------- | :----------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| _Multiplayer (server)_             | Implementazione della logica server-side per la gestione delle sessioni multiplayer | Martini      |       -       |    8/8     |     -      |  **8/8**  | `To-Do` | Alta     |
| _Refactor GameEngine_              | Riorganizzazione architetturale tramite WorldBuilder e parametri di contesto        | Diotallevi   |      3/3      |     -      |     -      |  **3/3**  | `To-Do` | Alta     |
| _Implementazione mappa di gioco_   | Creazione di mappe di gioco in base al numero di giocatori e world-builder          | Diotallevi   |      7/6      |     -      |     -      |  **7/6**  | `To-Do` | Alta     |
| _Implementazione Match-End_        | Gestione condizione di fine partita (vittoria/limite tempo) e notifica ai client    | Torelli      |       -       |     -      |    4/3     |  **4/3**  | `To-Do` | Alta     |
| _Implementazione Death Event_      | Creazione e propagazione dell'evento di morte per le entità con cleanup             | Torelli      |       -       |     -      |    2/3     |  **2/3**  | `To-Do` | Normale  |
| _Fix errore disconnessione client_ | Indagine e risoluzione dell'eccezione lanciata dal server alla disconnessione       | Martini      |       -       |    3/3     |     -      |  **3/3**  | `To-Do` | Normale  |
| _Generazione power-up_             | Creazione del sistema di spawn per i power-up con relativi effetti temporanei       | Torelli      |       -       |     -      |     5      |   **5**   | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**           |                                                                                     |              |   **10/9**    | **11/11**  |  **?/11**  | **??/31** |         |          |



## 📊 Analisi del Workload e Metriche

- **Alessandro Martini**: completato tutti i task completamente in linea coi tempi previsti
- **Alessandro Torelli**: impiegato più tempo per end-game per integrare i cambiamenti richiesti da una review, ma meno tempo per la gestione degli eventi di morte delle entità.
- **Federico Diotallevi**: ha impiegato leggermennte più tempo del previsto per l'implementazione della mappa bla bla bla

## 🔍 Sprint Retrospective

### 🟢Cosa ha funzionato

- In generale è stato uno sprint con tanta collaborazione e confronto.

### 🔴 Cosa migliorare

- Nulla