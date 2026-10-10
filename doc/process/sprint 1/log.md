---
tipo: meeting
sprint: "1"
data: 2026-08-17
---

# 📅 Sprint Planning - 1

- **Data:** 2026-08-17
- **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli

---

## 🎯 Obiettivo dello Sprint

Al termine del primo Sprint, l'obiettivo è rilasciare il prototipo fondamentale del sistema, focalizzato sulle funzionalità di base:

- consentire l'accesso alla piattaforma tramite browser
- assegnazione e spawn di una navicella al giocatore
- gestione della simulazione di base che prevede il movimento costante e il controllo della sua rotazione.

## 📋 Task Assegnati (Sprint Points)

Suddivisione Item scelti dal Product Backlog:

| ID Obiettivo | Requisito / Area          | Task Associato                   |
| :----------- | :------------------------ | :------------------------------- |
| **RFU1**     | Accesso alla Piattaforma  | Analisi rotte di accesso         |
| **RFU1**     | Accesso alla Piattaforma  | Implementazione rotte di accesso |
| **AA1**      | Analisi Architettura      | Analisi architettura             |
| **RFU3**     | Controllo della Navicella | Implementazione invio comandi    |
| **RFU3**     | Controllo della Navicella | Implementazione navicella        |
| **RFU3**     | Controllo della Navicella | Implementazione rotazione        |
| **RFS1**     | Movimento Continuo        | Implementazione movimento        |

| Task ID                              | Descrizione                                                                                                   | Assegnatario                 | SP Diotallevi | SP Martini | SP Torelli | SP Totali |  Stato  | Priorità |
| :----------------------------------- | :------------------------------------------------------------------------------------------------------------ | :--------------------------- | :-----------: | :--------: | :--------: | :-------: | :-----: | :------- |
| **Analisi architettura**             | Definizione della struttura generale del server                                                               | Diotallevi, Martini, Torelli |       2       |     2      |     2      |   **6**   | `To-Do` | Alta     |
| **Analisi rotte di accesso**         | Analisi della costruzione del server e delle rotte                                                            | Martini, Torelli             |       -       |     2      |     2      |   **4**   | `To-Do` | Alta     |
| **Implementazione rotte di accesso** | Sviluppo degli endpoint di rete e gestione dei canali di comunicazione                                        | Martini, Torelli             |       -       |     4      |     4      |   **8**   | `To-Do` | Normale  |
| **Implementazione invio comandi**    | Realizzazione del meccanismo di invio e gestione asincrona dei comandi utente da client a server              | Martini, Torelli             |       -       |     4      |     4      |   **8**   | `To-Do` | Normale  |
| **Implementazione navicella**        | Modellazione dell'entità navicella all'interno del dominio di gioco                                           | Diotallevi                   |       5       |     -      |     -      |   **5**   | `To-Do` | Normale  |
| **Implementazione movimento**        | Realizzazione della logica di movimento continuo della navicella gestita dal ciclo di simulazione             | Diotallevi                   |       4       |     -      |     -      |   **4**   | `To-Do` | Normale  |
| **Implementazione rotazione**        | Sviluppo del controllo di rotazione in tempo reale dell'entità navicella in risposta agli input del giocatore | Diotallevi                   |       3       |     -      |     -      |   **3**   | `To-Do` | Normale  |
| **TOTALE SPRINT POINTS**             |                                                                                                               |                              |    **13**     |   **11**   |   **11**   |  **38**   |         |          |

## 💬 Note & Decisioni

- I task di implementazione sono ovviamente dipendenti dai task di analisi
