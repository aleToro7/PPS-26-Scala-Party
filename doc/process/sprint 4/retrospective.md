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
> L'obiettivo dello Sprint era rilasciare una versione completa del gioco e, opzionalmente, aggiungere la generazione di bonus.

> **Esito**:
> Lo Sprint è andato molto bene, i tempi sono stati molto in linea con le aspettative e tutti i task principali sono stati completati. Il videogioco ha ampiamente soddisfatto tutto il team al momento della presentazione della demo nell'ultima riunione.
## 📋 Task Assegnati (Sprint Points)

| Task ID                            | Descrizione                                                                         | Assegnatario | SP Diotallevi | SP Martini | SP Torelli |       SP Totali       |  Stato  | Priorità |
| :--------------------------------- | :---------------------------------------------------------------------------------- | :----------- | :-----------: | :--------: | :--------: | :-------------------: | :-----: | :------- |
| _Multiplayer (server)_             | Implementazione della logica server-side per la gestione delle sessioni multiplayer | Martini      |       -       |    8/8     |     -      |        **8/8**        | `To-Do` | Alta     |
| _Refactor GameEngine_              | Riorganizzazione architetturale tramite WorldBuilder e parametri di contesto        | Diotallevi   |      3/3      |     -      |     -      |        **3/3**        | `To-Do` | Alta     |
| _Implementazione mappa di gioco_   | Creazione di mappe di gioco in base al numero di giocatori e world-builder          | Diotallevi   |      7/6      |     -      |     -      |        **7/6**        | `To-Do` | Alta     |
| _Implementazione Match-End_        | Gestione condizione di fine partita (vittoria/limite tempo) e notifica ai client    | Torelli      |       -       |     -      |    5/3     |        **5/3**        | `To-Do` | Alta     |
| _Implementazione Death Event_      | Creazione e propagazione dell'evento di morte per le entità con cleanup             | Torelli      |       -       |     -      |    4/3     |        **4/3**        | `To-Do` | Normale  |
| _Fix errore disconnessione client_ | Indagine e risoluzione dell'eccezione lanciata dal server alla disconnessione       | Martini      |       -       |    3/3     |     -      |        **3/3**        | `To-Do` | Normale  |
| _Generazione power-up_             | Creazione del sistema di spawn per i power-up con relativi effetti temporanei       | Torelli      |       -       |     -      |    2/5     | **2/5** -- incompleto | `To-Do` | Bassa    |
| **TOTALE SPRINT POINTS**           |                                                                                     |              |   **10/9**    | **11/11**  | **11/11**  |       **32/31**       |         |          |



## 📊 Analisi del Workload e Metriche

- **Alessandro Martini**: ha completato tutti i task perfettamente in linea coi tempi previsti
- **Alessandro Torelli**: ha impiegato più tempo nell'implementazione dei task obbligatori per la release, pertanto l'implementazione dei power-up è stata semplicemente abbozzata, preferendo tenere del tempo in più per revisione del codice già esistente. 
- **Federico Diotallevi**: ha impiegato leggermente più tempo del previsto per l'implementazione della mappa, ma la linea generale è stata comunque in linea con le stime iniziali.

## 🔍 Sprint Retrospective

### 🟢Cosa ha funzionato

- Lo Sprint ha avuto molta più collaborazione e confronto rispetto ai precedenti, portando di fatto a risultati migliori e scelte condivise. 
