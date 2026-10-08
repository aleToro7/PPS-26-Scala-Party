# Retrospettiva

In questa sezione viene analizzato a posteriori il percorso di sviluppo di ScalaParty: il processo adottato dal team, il flusso di lavoro su Git e GitHub, i possibili sviluppi futuri del progetto e alcune considerazioni finali.
Le osservazioni riportate si basano sui verbali degli Sprint (planning, review e retrospective) e sulla storia del repository [PPS-26-Scala-Party](https://github.com/aleToro7/PPS-26-Scala-Party). 

## Analisi del processo di sviluppo

L'adozione di un processo agile, nella forma di una versione semplificata di Scrum, è stata una delle principali novità del progetto, insieme alla scelta di Scala 3 e dell'ecosistema funzionale Typelevel.
L'esperienza ha mostrato come la metodologia si applichi meglio a problemi scomponibili in moduli isolati.
La separazione tra `core` e `infrastructure`, collegati solo dall'interfaccia `GameEngine`, ha permesso di sviluppare in parallelo dominio e server. Molte funzionalità erano però verticali e attraversavano motore, serializzazione, server e client: ad esempio lo sparo, inutilizzabile finché il server non ne serializzava lo stato, o la fine della partita, che richiedeva un nuovo evento nel motore e la relativa notifica ai giocatori. Queste interdipendenze si sono sciolte solo quando il contratto tra motore e server si è stabilizzato, e fino ad allora hanno causato lavoro sequenziale e task rimandati.

Il processo si adatta inoltre meglio a gruppi con un livello omogeneo di autonomia progettuale, perché in fase di planning è impossibile definire i task nel dettaglio e ogni membro gode di ampia libertà nel realizzarli. Nel nostro caso questo ha prodotto soluzioni curate ma anche stime superate, come per il sistema di collisione, che ha richiesto di rivedere la geometria dello Sprint precedente. Le difficoltà sono state superate lavorando in coppia sui task condivisi e confrontandosi durante la revisione delle pull request.

La pianificazione è stata più precisa nella prima metà del progetto. In seguito, con l'accumularsi dei task rimandati, è stato più difficile restare nei canoni di Scrum, e il team ha scelto di chiudere con uno Sprint dal carico ridotto, dedicato ai filoni ancora aperti e alla revisione del codice.

Sono stati usati tutti i principali strumenti di Scrum (product backlog, planning, review e retrospective), ad eccezione degli incontri giornalieri. Organizzarli era difficile per la distanza fisica tra i membri e per il periodo estivo, durante il quale impegni personali e lavorativi rendevano complicato trovare un orario comune. Al loro posto il team si è coordinato tramite il contatto diretto tra i membri e le discussioni nelle pull request.

### Evoluzione del processo

Le retrospective hanno avuto un impatto concreto sul modo di lavorare del team. Le principali lezioni apprese, Sprint dopo Sprint, sono state le seguenti:

- **Sprint 1: rispettare la durata dello Sprint.** A causa di imprevisti personali, parte del lavoro è stata riassegnata da un membro all'altro e lo Sprint è stato prolungato di circa una settimana oltre la data prevista. La retrospective ha evidenziato come questa scelta avesse compromesso l'agilità del team, causando dei blocchi tra i membri. A partire dallo Sprint 2 è stato quindi stabilito che ogni Sprint si sarebbe concluso *inderogabilmente* alla data fissata, lasciando slittare al ciclo successivo i task non completati (come avvenuto, ad esempio, per la collisione con l'arena e il refactor del game engine).
- **Sprint 2: comunicare tra dominio e infrastruttura.** La divisione del lavoro tra lo sviluppo del dominio di gioco e lo sviluppo dell'infrastruttura di rete ha consentito di lavorare in parallelo con pochi conflitti in fase di merge. Tuttavia, la mancata comunicazione sulla serializzazione dei messaggi ha impedito di rendere effettivamente utilizzabile lo sparo, pur essendo stato implementato. Da qui l'impegno a mantenere un confronto costante tra i membri.
- **Sprint 3: evitare la suddivisione a cascata.** La comunicazione è sensibilmente migliorata. È invece emerso che la suddivisione del lavoro sulle collisioni (prima la geometria, poi il sistema di collisione) era avvenuta in modo parzialmente sequenziale, rendendo necessaria una revisione del lavoro dello Sprint precedente.
- **Sprint 4: collaborazione e scelte condivise.** L'ultimo Sprint, con un carico ridotto per lasciare spazio alla revisione del codice prima della consegna, è stato quello con il maggior grado di confronto tra i membri. Tutti i task principali sono stati completati nei tempi previsti e la demo finale ha soddisfatto l'intero team.

Altre pratiche si sono consolidate nel corso del progetto:

- l'inserimento di **task opzionali o di riserva** nel planning, da affrontare solo in caso di stime sovrastimate (ad esempio l'analisi multi-lobby, i muri, la generazione dei power-up, il benchmarking);
- la **riassegnazione flessibile** dei task tra i membri che lavoravano sulla stessa area, per bilanciare i carichi di lavoro. Pur funzionando bene grazie a una buona comunicazione, la retrospective dello Sprint 2 ha sottolineato come una migliore suddivisione iniziale avrebbe potuto evitarla;
- una divisione delle aree di lavoro inizialmente netta (Diotallevi sul dominio di gioco, Martini e Torelli sul server), che si è progressivamente ammorbidita: negli ultimi Sprint Torelli ha lavorato su funzionalità del dominio (vita e danno, morte, fine partita, power-up) e Martini si è concentrato sul server (multi-partita, multiplayer, gestione delle disconnessioni).

## Possibili sviluppi futuri

### Funzionalità di gioco

- **Generazione dei power-up (RFS8).** Il modello dei power-up e il relativo DSL sono già presenti nel `core` (`PowerUp`, `Effect`, `Stat`). Resta da implementare il sistema che li genera casualmente nell'arena e ne applica gli effetti temporanei alle navicelle, da inserire come ulteriore `WorldSystem` nella pipeline del motore di gioco.
- **Replay delle partite (RFU7).** Poiché il server è autoritativo e la simulazione avanza a tick discreti, una partita può essere registrata salvando per ogni tick i comandi ricevuti o lo stato pubblicato, e successivamente riprodotta.
- **Bot con intelligenza artificiale.** L'ADR sull'integrazione di **tuProlog** prevedeva l'uso di Prolog, invocato solo in corrispondenza di eventi discreti, per lo spawn dei power-up e per le decisioni di bot controllati dal server. Questa integrazione non è stata realizzata e rappresenta un'estensione naturale del progetto, utile anche a rendere giocabili le partite con pochi utenti connessi.
- **Partite di dimensione variabile.** Attualmente il `QueuedLobbyManager` avvia ogni partita con un numero fisso di giocatori (`playersPerMatch`, configurato a 2). Si potrebbe consentire di giocare partite da due a quattro giocatori, ad esempio avviando la partita con i giocatori disponibili dopo un tempo massimo di attesa.
- **Riconnessione e identità del giocatore.** Attualmente un giocatore disconnesso perde la propria navicella. Si potrebbe consentire la riconnessione a una partita in corso e l'assegnazione di un nickname, sempre senza memorizzare dati personali (RNF3).