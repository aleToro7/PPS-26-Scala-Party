---
tipo: meeting
sprint: "2"
data: 2026-09-16
---

# 🔄 Sprint 1 - Review & Retrospective

- **Data:** 2026-09-16
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Sprint Review (Esito dello Sprint)

> **Obiettivo iniziale**:
> L'obiettivo dello Sprint 2 era quello di implementare la gestione del ciclo di vita della partita e lo sviluppo della fisica di gioco, in particolare la gestione della coda di attesa dei giocatori, l'inizio e la fine della partita, la possibilità di sparare e la gestione delle collisioni con i muri dell'arena.

> **Esito**:
> L'obiettivo dello Sprint 2 è stato parzialmente raggiunto:
> Il team ha completato con successo la gestione della coda di attesa dei giocatori, la partita inizia e termina dopo 1 minuto di gioco.
> Quando una partita termina, il prossimo giocatore in coda inizia la partita, e così via fino a quando non ci sono più giocatori in coda.
> La gestione dello sparo è stata implementata, ma non è ancora possibile sparare in quanto la logica di serializzazione del messaggio lato server non è ancora stata implementata.
> La geometria delle collissioni è stata implementata, ma non è stata gestita la collisione coi muri dell'arena.

## 📋 Task Assegnati (Sprint Points)

| Task ID                                      | Descrizione                                                                                                    | Assegnatario     | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :------------------------------------------- | :------------------------------------------------------------------------------------------------------------- | :--------------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| _Gestione Coda e Lobby_                      | Logica server per mantenere la coda e istanziare le partite in sequenza                                        | Martini, Torelli |       -       |    5/5     |    5/5     |  _10/10_  | `Done`  | Alta     |
| _Gestione Disconnessioni_                    | Rilevamento disconnessione dei client e pulizia degli slot occupati in coda o in lobby                         | Martini, Torelli |       -       |    4/2     |    0/2     |   _4/4_   | `Done`  | Alta     |
| _Implementazione Sparo_                      | Creazione proiettili ereditando la direzione della navicella e gestione del loro ciclo di vita                 | Diotallevi       |      6/5      |     -      |     -      |   _6/5_   | `Done`  | Alta     |
| _Scontri con l'Arena_                        | Applicazione della geometria per impedire alle entità di uscire dai bordi o attraversare i muri                | Diotallevi       |   rimandato   |     -      |     -      | rimandato | `To-Do` | Normale  |
| _Geometria Collisioni_                       | Implementazione delle funzioni matematiche per il rilevamento intersezioni tra forme                           | Diotallevi       |      8/5      |     -      |     -      |   _8/5_   | `Done`  | Alta     |
| _Configurazione dei parametri della partita_ | Gestione unificata dei parametri (single/multi, mappa) condivisi tra gioco ed engine                           | Martini, Torelli |       -       |    0/3     |    6/3     |   _6/6_   | `Done`  | Normale  |
| _Broadcast Stato Lobby_                      | Trasmissione ai client via socket degli aggiornamenti di stato della lobby ("ci sono 3 giocatori prima di te") | Martini, Torelli |       -       |    6/3     |    0/3     |   _6/6_   | `Done`  | Normale  |
| _TOTALE SPRINT POINTS_                       |                                                                                                                |                  |    _14/13_    |  _15/13_   |  _11/13_   |  _42/36_  |         |          |

## 📊 Analisi del Workload e Metriche

- **Alessandro Martini**: ha completato tutti i task assegnati perfettamente in linea con le stime iniziali L'effort totale è stato di 15SP, superando di 2SP le stime iniziali. Il surplus di SP è da considerarsi come recupero del deficit di SP dello Sprint precedente.
- **Alessandro Torelli**: ha delegato uno dei suoi task a Martini, recuperando il surplus di SP dello Sprint precedente. Ha completato tutti i task assegnati, perfettamente in linea con le stime iniziali.
- **Federico Diotallevi**: ha completato solo in parte i task assegnati, poiché l'implementazione della geometria delle collisioni ha richiesto più tempo del previsto a causa della complessità del problema.

## 🔍 Sprint Retrospective

### 🟢Cosa ha funzionato

- La suddivisione dei task tra sviluppo del dominio e sviluppo infrastrutturale/di rete (Torelli/Martini) ha permesso di lavorare in parallelo creando conflitti minori e del tutto trascurabili durante il merge.
- I tempi di sviluppo sono stati rispettati e le stime iniziali si sono rivelate abbastanza accurate.

### 🔴 Cosa migliorare

- Serve un po' più comunicazione tra i membri del team. La divsione parallela tra i task di infrastruttura e dominio permette di lavorare in modo indipendente, ma richiede comunicazione costante. Se si fosse comunicato meglio, si sarebbe potuto evitare il problema della serializzazione dei messaggi lato server, che ha impedito di completare l'implementazione dello sparo.
- La suddivisione dei task può migliorare: Martini e Torelli si sono scambiati alcuni task per lavorare in modo più efficiente. Ottimo perché sono riusciti a comunicare perfettamente, ma una migliore suddivisione iniziale ne avrebbe evitato la necessità.
