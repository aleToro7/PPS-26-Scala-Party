# Implementazione - Martini

In questa sezione vengono descritti i dettagli implementativi sviluppati da Alessandro Martini in ScalaParty. Il contributo personale ha toccato quasi tutte le parti dell'infrastructure dell'applicazione in collaborazione con il mio collega Alessandro Torelli:
- Implementazione dell'infrastruttura base del server (in collaborazione con Torelli)
- Implementazione del livello di rete WebSocket (in collaborazione con Torelli)
- Implementazione del MatchRunner (in collaborazione con Torelli)
- Implementazione invio notifica di lobby a un singolo giocatore (WebSocketNotifier)
- Implementazione Coda di Gioco (QueuedLobbyManager)
- Implementazione MatchCoordinator

Nei capitoli successivi vengono riportati gli aspetti più fondamentali delle parti sviluppate in cooperazione e singolarmente.
### Sviluppo Collaborativo
#### Composition root: `ServerApp`

`ServerApp` è il punto di ingresso dell'applicazione (`IOApp.Simple`) e funge da **composition root**: è l'unico punto in cui le implementazioni concrete vengono istanziate e collegate tra loro.
Non è utilizzato alcun framework di dependency injection: le dipendenze sono passate esplicitamente ai costruttori all'interno di una for-comprehension `IO`.

```scala
val run: IO[Unit] =
  for
    registry       <- ConnectionRegistry()
    lobby          <- QueuedLobbyManager.of[IO](
                        playersPerMatch = playersPerMatch,
                        maxMatches      = maxConcurrentMatches,
                        maxQueued       = maxQueuedPlayers)
    commandService <- GameCommandService()
    notifier  = WebSocketNotifier(registry)
    publisher = WebSocketBroadcaster(registry)
    coordinator    <- MatchCoordinator(lobby, registry, commandService, notifier, publisher, GameSettings.default)
    wsServer  = WebSocketServer(registry, coordinator, commandService)
    _ <- EmberServerBuilder.default[IO] /* host, porta 8081, logger, routes */ .build.use(_ => IO.never)
  yield ()
```

Ogni componente con stato espone una factory che restituisce `IO[...]`. In questo modo la creazione dello stato mutabile condiviso (un `Ref`) è anch'essa un effetto, e non può avvenire in maniera implicita al di fuori del flusso controllato da cats-effect.

Il server espone tre rotte HTTP:

| Rotta                 | Descrizione                                                     |
| --------------------- | --------------------------------------------------------------- |
| `GET /`               | Health check testuale ("Scala Party Server is up and running!") |
| `GET /scalaparty`     | Restituisce il client web (`public/index.html`)                 |
| `GET /scalaparty/ws`  | Endpoint WebSocket per il gioco                                 |

#### Adapter in ingresso: `WebSocketServer`

`WebSocketServer` è l'adapter che traduce gli eventi fisici della connessione WebSocket in invocazioni delle porte in ingresso. Gestisce tre eventi:

- **Connessione (`onConnect`)**: alla richiesta su `/ws` viene generato un nuovo `PlayerId` (UUID) e creata una coda `Queue[IO, WebSocketFrame]` dedicata ai messaggi in uscita verso quel client. Il giocatore viene registrato nel `ConnectionRegistry` e viene invocato `AccessPort.joinLobby`. Se l'esito è `Admission.Rejected` (tutte le stanze occupate e coda piena), il server mette in coda un frame di chiusura e rimuove subito la sessione.
- **Messaggio (`onMessage`)**: il frame di testo viene decodificato in un `PlayerInput`. La partita a cui il comando si riferisce viene ricavata dal registro **ad ogni messaggio** e non una volta per tutte alla connessione: un giocatore può infatti connettersi mentre è in coda ed essere assegnato a una partita solo successivamente. Gli input di un giocatore che non è ancora in partita vengono scartati, così come i JSON non validi.
- **Disconnessione (`onDisconnect`)**: la sessione viene rimossa dal registro e viene invocato `AccessPort.leaveLobby`, così da liberare il posto in coda o rimuovere la navicella dalla partita.

Il flusso dei frame verso il client è ottenuto unendo la coda dei messaggi del giocatore con un `Ping` inviato ogni 20 secondi:

```scala
def keptAlive(queue: MessageQueue): Stream[IO, WebSocketFrame] =
  Stream
    .fromQueueUnterminated(queue)
    .merge(Stream.awakeEvery[IO](keepAliveInterval).as(WebSocketFrame.Ping()))
```

Il ping si rende necessario perché Ember chiude le connessioni inattive per 60 secondi, mentre un giocatore in coda potrebbe non inviare alcun messaggio per un periodo prolungato.

#### Registro delle connessioni: `ConnectionRegistry`

`ConnectionRegistry` mantiene, per ogni giocatore connesso, la sua coda di messaggi in uscita e la partita a cui è eventualmente assegnato:

```scala
private case class Session(matchId: Option[MatchId], queue: MessageQueue)
private type RegistryState = Map[PlayerId, Session]
```

Lo stato è racchiuso in un `Ref[IO, RegistryState]` e tutte le operazioni (`register`, `assignToMatch`, `clearMatch`, `removeSession`, `queueFor`, `getQueuesForMatch`, ...) sono aggiornamenti atomici dello stesso.
Una connessione sopravvive alla partita a cui partecipa: il giocatore è registrato senza partita al momento della connessione, viene associato a una partita quando questa inizia (`assignToMatch`) e ne viene sganciato quando termina (`clearMatch`).

#### Game loop: `MatchRunner`

`MatchRunner` implementa il **ciclo autoritativo** di una singola partita. È realizzato come uno stream fs2 a frequenza fissa di circa 60 tick al secondo (`tickInterval = 16.millis`):

```scala
def run: IO[MatchOutcome] =
  Stream
    .fixedRate[IO](MatchRunner.tickInterval)
    .zipWithIndex
    .evalMapAccumulate(Set.empty[PlayerId]):   // giocatori già usciti
      case (departed, (_, tick)) =>
        for
          rawCommands <- commandQueue.drainCommands(session.matchId)
          present     <- roster
          ecsCommands   = rawCommands.flatMap((playerId, intent) => session.players.get(playerId).flatMap(intent.toDto))
          leaving       = session.players.keySet -- present -- departed
          leaveCommands = leaving.toList.flatMap(session.players.get).map(GameCommand.LeaveCommand(_))
          result        = engine.update(ecsCommands ++ leaveCommands, MatchRunner.tickInterval.toMillis)
          _ <- publisher.broadcastState(session.matchId, MatchState(tick, engine.arena, result.entities))
          ended = result.events.collectFirst { case GameEvent.MatchEnded(outcome) => outcome }
        yield (departed ++ leaving, ended)
    .collectFirst { case (_, Some(outcome)) => outcome }
    .compile
    .lastOrError
```

Lo stato del mondo di gioco è conservato tra un tick e il successivo dal `GameEngine`, che lo aggiorna a ogni invocazione di `update`. L'unica informazione che il runner deve mantenere in proprio, l'insieme dei giocatori già usciti, viene invece trasportata nell'accumulatore dello stream anziché in una variabile mutabile. L'elenco dei partecipanti ancora presenti è passato al runner come effetto (`roster: IO[Set[PlayerId]]`), così che il runner non dipenda direttamente dalla lobby.

#### Adapter in uscita: `WebSocketBroadcaster` e `WebSocketNotifier`

I due adapter in uscita implementano le corrispondenti porte serializzando i messaggi in JSON e inserendoli nelle code dei destinatari, recuperate dal `ConnectionRegistry`:

- `WebSocketBroadcaster` (`MatchEventPublisher`) invia lo stato della partita a **tutti** i giocatori associati a quella partita;
- `WebSocketNotifier` (`PlayerNotifier`) invia una notifica di lobby a un **singolo** giocatore, ignorandola se questo non è più connesso.

Gli adapter non scrivono mai direttamente sulla socket: si limitano a un `offer` sulla coda del giocatore, che viene consumata dallo stream di invio della connessione. In questo modo la produzione dei messaggi (il game loop e il coordinatore) è disaccoppiata dalla velocità di trasmissione verso ciascun client.

### Sviluppo Individuale
#### Matchmaking: `QueuedLobbyManager`

`QueuedLobbyManager` decide **chi gioca e chi attende**. I giocatori sono serviti secondo una politica *first-come-first-served*: non appena una stanza è libera e almeno `playersPerMatch` giocatori sono in attesa, esattamente `playersPerMatch` di essi vengono prelevati dalla testa della coda e inizia una nuova partita. Fino a `maxMatches` partite possono essere giocate contemporaneamente, e al massimo `maxQueued` giocatori possono attendere; chi arriva oltre viene rifiutato.

L'intero stato del matchmaking è rappresentato da un'unica struttura immutabile:

```scala
private final case class WaitingRoom(queue: Vector[PlayerId], active: Map[MatchId, ActiveMatch])
```

su cui sono definite le transizioni pure `enqueue`, `remove` e `startMatch`. La coda e le stanze attive condividono **un solo `Ref`** di proposito: liberare una stanza e scegliere chi la occupa deve essere un passo atomico, altrimenti due aggiornamenti concorrenti potrebbero selezionare gli stessi giocatori.

Le operazioni pubbliche applicano le transizioni tramite `Ref.modify` e restituiscono esiti espressi come ADT:

```scala
def join(playerId: PlayerId): F[JoinOutcome]           // Playing(activeMatch) | Queued(playersAhead) | Rejected
def leave(playerId: PlayerId): F[LeaveOutcome]         // partita sciolta e/o partita appena avviata
def finishMatch(matchId: MatchId): F[Option[ActiveMatch]] // partita avviata nella stanza appena liberata
```

L'identificativo della possibile nuova partita viene generato prima della `modify` (`withCandidateMatchId`), perché la funzione passata a `modify` deve essere pura; l'identificativo viene poi utilizzato solo se una partita inizia effettivamente.

La factory `QueuedLobbyManager.of` valida i parametri di configurazione (`playersPerMatch ≥ 1`, `maxMatches ≥ 1` e una coda capace di contenere almeno `playersPerMatch - 1` giocatori, senza la quale una partita non potrebbe mai raccogliere abbastanza partecipanti) sollevando l'errore all'interno dell'effetto. Il componente è generico sull'effetto (`F[_]: Sync`).

#### Coordinamento delle partite: `MatchCoordinator`

`MatchCoordinator` implementa la porta `AccessPort` e governa l'intero ciclo di vita di una partita, dalla coda all'ultimo tick. Mette in comunicazione `QueuedLobbyManager`, che decide *chi* gioca, e `MatchRunner`, che sa soltanto far avanzare una partita già esistente.

- **`joinLobby`**: inoltra la richiesta alla lobby e, in base al `JoinOutcome`, avvia la partita, notifica al giocatore la sua posizione in coda (`ServerMessage.Queued`) oppure lo informa che la coda è piena (`ServerMessage.QueueFull`).
- **`leaveLobby`**: rimuove il giocatore dalla lobby; se la partita è rimasta senza giocatori ne ferma la fiber, aggiorna la posizione di chi è in coda e, se si è liberata una stanza, avvia la partita successiva.
- **`startMatch`**: assegna a ogni giocatore un `EntityId` per la sua navicella, lo associa alla partita nel registro e gli invia `ServerMessage.MatchStarted(players, you)`, così che il client sappia quale navicella controlla. Avvia poi la partita su una **fiber dedicata**, registrata in un `Ref[IO, Map[MatchId, FiberIO[Unit]]]`.
- **`concludeMatch`**: al termine della partita sgancia i giocatori dalla partita, invia loro `ServerMessage.MatchEnded(outcome)`, dichiara conclusa la partita nella lobby e avvia immediatamente quella successiva, se ci sono abbastanza giocatori in attesa. In questo modo la coda avanza autonomamente.

Ogni partita è quindi eseguita su una propria fiber, isolata e indipendente dalle altre, e viene interrotta solo quando termina o quando tutti i giocatori l'hanno abbandonata.

Il coordinatore gestisce esplicitamente alcune **condizioni di corsa** che emergono dalla concorrenza tra fiber:

- la partita attende, tramite un `Deferred`, che la propria fiber sia stata registrata prima di iniziare: in caso contrario, una partita molto breve potrebbe concludersi prima della registrazione, lasciando nella mappa una fiber terminata che non verrebbe mai rimossa;
- dopo aver registrato la fiber, viene verificato che la partita sia ancora attiva: se l'ultimo giocatore è uscito durante l'avvio, la sua `leave` non ha trovato alcuna fiber da fermare, e la partita viene quindi fermata subito;
- in `concludeMatch` la fiber viene rimossa dalla mappa *prima* delle operazioni di pulizia, poiché questo codice è eseguito proprio all'interno di quella fiber: un giocatore che uscisse in quel momento la cancellerebbe a metà del lavoro.

## Identificativi e modello del server

Gli identificativi di giocatori e partite sono definiti come **opaque type** su `UUID`:

```scala
opaque type PlayerId = UUID
opaque type MatchId  = UUID
```

In questo modo il compilatore impedisce di scambiare un `PlayerId` con un `MatchId` (o con un `UUID` qualsiasi), senza alcun costo a runtime. Inoltre, i `PlayerId` sono generati casualmente a ogni connessione e non sono legati ad alcuna informazione personale dell'utente.

Gli esiti delle operazioni sono modellati come **ADT** (`Admission`, `JoinOutcome`, `LeaveOutcome`, `ServerMessage`) invece che come valori booleani o eccezioni, rendendo esplicito e verificato dal compilatore ogni caso che il chiamante deve gestire.

## Logging: `ClientDisconnectionLogger`

`ClientDisconnectionLogger` è un *decorator* del `Logger[IO]` di log4cats passato a Ember. Il server, per impostazione predefinita, registra come errore (con stack trace completo) qualsiasi WebSocket che termini con un'eccezione, compresa la semplice chiusura della scheda del browser da parte del client. Il decorator riconosce questi casi ("Connection reset", "Broken pipe", anche quando aggregati in un `CompositeFailure`) e li declassa a livello `debug`, lasciando inalterati tutti gli altri messaggi.

## Gestione della concorrenza

Il server non utilizza lock, attori o variabili mutabili condivise: la concorrenza è gestita interamente tramite le primitive funzionali di cats-effect.

| Problema                                                | Soluzione adottata                                                                 |
| ------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| Stato condiviso (lobby, registro, buffer, fiber attive) | `Ref` con aggiornamenti atomici (`update`/`modify`) di strutture immutabili        |
| Esecuzione di più partite in parallelo                  | Una fiber per partita, cancellabile con `FiberIO.cancel`                           |
| Avanzamento del tempo di gioco                          | `Stream.fixedRate` di fs2                                                          |
| Disaccoppiamento tra produttori di messaggi e socket    | Una `Queue` per client, consumata da `Stream.fromQueueUnterminated`                |
| Ordinamento tra avvio di una partita e sua registrazione | `Deferred`                                                                        |
| Mantenimento delle connessioni inattive                 | `merge` dello stream di invio con `Stream.awakeEvery` (ping periodico)             |
