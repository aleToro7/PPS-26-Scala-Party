# Design Architetturale

In questo capitolo viene presentata l'architettura complessiva di ScalaParty, illustrando i principi guida che ne hanno determinato la struttura, i pattern architetturali adottati e la suddivisione del sistema in moduli indipendenti.

## Decomposizione in Moduli

Il progetto separa nettamente le responsabilità tra due macro-moduli principali, ciascuno con un ruolo ben definito:

1. **`core`**: Rappresenta il nucleo computazionale e di dominio del videogioco. È progettato come una libreria pura e priva di dipendenze esterne. Racchiude lo stato del gioco, le entità, la fisica e la logica di simulazione.
2. **`infrastructure`**: Costituisce il runtime del server, gestendo la comunicazione di rete, la concorrenza e l'orchestrazione dei match.

> **Nota**: Nel modulo `infrastructure` è presente anche il client web. Tale parte è a solo scopo di visualizzazione, ma non è in alcun modo parte del progetto universitario. Il client web è stato completamente generato, con largo uso di LLM, a partire dai DTO prodotti dal modulo `core` e non contiene alcuna logica di gioco.

## Relazione con l'Architettura Elm

In fase di progettazione concettuale, l'idea ispiratrice dell'architettura è stata l'**architettura Elm** (nota anche come pattern **Model-View-Update** o **MVU**), ampiamente diffusa nelle applicazioni funzionali.
Nel corso dello sviluppo, tuttavia, la classica architettura Elm è stata reinterpretata e adattata alle necessità di un videogioco multiplayer online in tempo reale.

L'essenza fondamentale del paradigma MVU è pienamente preservata all'interno del sistema:

- **Model**: È incarnato dallo stato del mondo nel modulo `core` (`GameWorld`) ed è rigorosamente immutabile: nessuna entità o componente viene mutata in-place durante la partita.
- **Update**: L'avanzamento del gioco è governato da una funzione di transizione pura incapsulata nel motore (`GameEngine.update`), priva di effetti collaterali.
- **Commands / Effetti**: Così come nel runtime di Elm gli effetti collaterali (I/O, timer, rete) sono segregati all'esterno del modello puro e gestiti tramite comandi asincroni, in ScalaParty ogni forma di side-effect è confinata nel modulo `infrastructure`.

### Variazioni Rispetto alla Classica Elm Architecture

Nonostante l'ispirazione concettuale, la nostra implementazione si discosta da una rigida architettura Elm per un paio di motivazioni tecniche principali, evidenziate dal confronto tra i due flussi orizzontali:

> **Flusso 1: Elm Architecture Classica**
>
> ```mermaid
> flowchart LR
>     Msg["1. Evento Singolo<br/>(Msg discreto da UI)"] --"Model(t), msg"--> Update["2. Update Puro"]
>     Update --"Model(t+1)"--> View["3. Interfaccia utente<br/>(Model -> HTML)"]
> ```

> **Flusso 2: Architettura ScalaParty**
>
> ```mermaid
> flowchart LR
>     Clock["1. Trigger Temporale"] --"batch di comandi"--> Pipeline["Pipeline di update"]
>     subgraph "2. Update del Modello"
>         direction LR
>         Pipeline --"Model(t), eventi"--> Adapter["Serializzatore DTO"]
>     end
>     Adapter --"DTO serializzati"--> Net["3.Broadcast sui client"]
> ```

- **Avanzamento a Tick Continuo**:
  - _In Elm classico_: l'applicazione evolve quasi esclusivamente in modo reattivo all'arrivo di singoli messaggi discreti (`Msg`) generati dall'utente.
  - _In ScalaParty_: trattandosi di un videogioco arcade con fisica e inerzia, il tempo ($\Delta t$) intercorso tra gli update rappresenta il fattore di avanzamento primario. Anche in assenza di input da parte dei giocatori, la simulazione deve progredire a frequenza fissa. L'`update` non riceve quindi un singolo evento isolato, ma un batch di comandi unito all'intervallo temporale trascorso.
- **Disaccoppiamento tramite DTO**:
  - _In Elm classico_: la funzione `view` risiede all'interno della medesima applicazione ed è una funzione pura che mappa direttamente lo stato in elementi grafici.
  - _In ScalaParty_: il sistema è distribuito, perciò il `core` non proietta direttamente l'interfaccia utente grafica. Invece, produce una rappresentazione intermedia serializzabile. Questa scelta disaccoppia completamente la simulazione interna da qualsiasi interfaccia utente, che sia un browser o un'interfaccia locale.

In sintesi, ScalaParty adotta una variante tick-based della Elm Architecture, che tenta di conservare ed adattare i vantaggi del paradigma MVU. Tali variazioni sono frutto di una scelta progettuale consapevole, fatta per garantire una totale separazione tra il modello e la sua rappresentazione.
Ad oggi, se si volesse realizzare un client nativo sarebbe sufficiente integrare un nuovo modulo di visualizzazione che si interfacci con il `core` esattamente come fa il modulo `infrastructure`, senza alcuna modifica al modello di dominio o alla logica di simulazione.

![[Report/3-design-architetturale/core/index|core]]
![[3-design-architetturale/server/index]]
