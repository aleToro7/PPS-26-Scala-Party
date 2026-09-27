---
tipo: meeting
sprint: "3"
data: 2026-09-25
---

# 🔄 Sprint 3 - Review & Retrospective

- **Data:** 2026-09-25
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Sprint Review (Esito dello Sprint)

> **Obiettivo iniziale**:
> L'obiettivo dello Sprint 3 era quello di ottenere una prima versione "giocabile", con la possibilità di incominciare più di una partita single-player in parallelo.

> **Esito**:
> L'obiettivo è stato in gran parte raaggiunto, al termine dello sprint è possibile iniziare più di una partita in parallelo. Iniziata la partita è possibile navigare liberamente la mappa collidendo con l'arena e sparando proiettiili.
> Non sono stati completati i task di implementazione della mappa e di refactor del game ingine.

## 📋 Task Assegnati (Sprint Points)

| Task ID                                       | Descrizione                                                                                                                     | Assegnatario | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :-------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------ | :----------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| _Implementazione multi-partita single player_ | Supporto all'esecuzione concorrente di più partite single-player isolate gestite contemporaneamente dal server                  | Martini      |       -       |    8/8     |     -      |  **8/8**  | `To-Do` | Alta     |
| _Serializzazione proiettile_                  | Modellazione del DTO e serializzazione JSON dei proiettili per trasmetterne lo stato ai client via WebSocket                    | Torelli      |       -       |     -      |    6/3     |  **6/3**  | `To-Do` | Alta     |
| _Implementazione Collision System_            | Implementazione sistema di collisione per la risoluzione degli urti e la propagazione degli eventi di collisione                | Diotallevi   |     11/7      |     -      |     -      | **11/7**  | `To-Do` | Alta     |
| _Scontri con l'Arena_                         | Applicazione della geometria per impedire alle entità di uscire dai bordi o attraversare i muri                                 | Diotallevi   |      3/3      |     -      |     -      |  **3/3**  | `To-Do` | Normale  |
| _Limitare giocatori in coda_                  | Impostazione di un limite massimo di capienza per la lobby e gestione del rifiuto/notifica per i giocatori in eccesso           | Martini      |       -       |    3/3     |            |  **3/3**  | `To-Do` | Normale  |
| _Implementazione della logica di vita_        | Implementazione del concetto di vita per le entità spaceship, con relativi danni subiti in caso di collisione con un proiettile | Torelli      |       -       |     -      |    5/5     |  **5/5**  | `To-Do` | Normale  |
| _Implementazione mappa di gioco_              | Creazione di un sistema per definire mappe di gioco                                                                             | Torelli      |       -       |     -      | rimandato  | rimandato | `To-Do` | Bassa    |
| _Refactor game engine_                        | Riorganizzazione architetturale del motore per separare pipeline ed esecuzione, migliorando la modularità                       | Diotallevi   |   rimandato   |     -      |     -      | rimandato | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**                      |                                                                                                                                 |              |   **14/10**   | **11/11**  |  **11/8**  | **36/29** |         |          |

## 📊 Analisi del Workload e Metriche

- **Alessandro Martini**: ha completato tutti i task assegnati perfettamente in linea con le stime iniziali.
- **Alessandro Torelli**: ha impiegato più tempo del previsto sullo sviluppo della serializzazione del proiettile poiché ha integrato in esso anche un refactor del sistema di sparo. Tale variazione lo ha portato a non riuuscire a sviluppare la mappa di gioco.
- **Federico Diotallevi**: ha impiegato più tempo del previsto nel sistema di collisione a causa di un refactor del lavoro fatto nel precedente Sprint riguardo la geometria delle collisioni.

## 🔍 Sprint Retrospective

### 🟢Cosa ha funzionato

- La comunicazione del team è decisamente migliorata e ci si è tenuti sempre in costante contatto per quanto riguardava lo sviluppo dei singoli task.

### 🔴 Cosa migliorare

- Ci si è portati dietro la mala-suddivsione dei task del vecchio Sprint: per quanto riguarda le collisioni la suddivisione è stata poco agile,il lavoro è stato suddiviso parzialmente a cascata ed infatti ha necessitato subito una revsiione.