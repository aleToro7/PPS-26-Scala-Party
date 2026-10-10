# Testing

## Tecnologie utilizzate

- **ScalaTest: framework di riferimento per tutti i test del progetto. Sono stati adottati gli stili `AnyFlatSpec` per il modulo _core_, la cui natura puramente funzionale si presta a specifiche brevi e lineari, e `AnyWordSpec`/`AsyncWordSpec` per il modulo _infrastructure_, dove la struttura annidata permette di raggruppare i comportamenti per operazione (es. _joining_, _leaving_). Le asserzioni sono scritte con i `Matchers` di ScalaTest, che rendono i test leggibili come specifiche (`collisions should have size 1`).
- **cats-effect-testing (`AsyncIOSpec`)**: integra ScalaTest con Cats Effect, permettendo di scrivere test che restituiscono direttamente un `IO[Assertion]`. In questo modo i componenti effectful del server (lobby, coordinatore delle partite, registro delle connessioni) vengono testati componendo gli effetti in una for-comprehension, senza mai bloccare thread con `unsafeRunSync`.
- **sbt**: esecuzione dei test, separata per modulo (`core`, `infrastructure`) e aggregata dal progetto radice.
- **sbt-scoverage**: utilizzato per misurare la copertura del codice.
- **GitHub Actions**: la pipeline di Continuous Integration esegue, ad ogni push e ad ogni pull request verso `main` e `develop`, tre job in sequenza: compilazione, esecuzione dell'intera suite di test e verifica della formattazione con `scalafmtCheckAll`. Una pull request non viene integrata se uno dei job fallisce.

## Strategia di testing

Il Test Driven Development è stato adottato dal gruppo come linea guida generale, ogni componente del team lo ha applicato al proprio lavoro nel modo che riteneva più adatto, alternando test scritti prima dell'implementazione a test sviluppati contestualmente alla funzionalità. L'obiettivo comune restava quello di specificare il comportamento atteso di ogni componente e di proteggere i successivi refactoring. Ogni feature branch includeva, oltre al codice di produzione, le specifiche che ne descrivono il comportamento atteso. Le pull request venivano revisionate anche sul versante dei test, e la CI ne garantiva l'esecuzione prima di ogni merge su `develop`.

## Grado di copertura

La copertura è stata misurata con sbt-scoverage sull'intera suite.

| Modulo | Statement coverage | Branch coverage |
|---|---|---|
| core | 97,81% | 86,62% |
| infrastructure | 82,07% | 86,81% |
| **Totale** | **93,34%** | **86,70%** |

Il modulo core, che contiene l'intera logica di gioco, è coperto quasi integralmente. La logica applicativa del server (package `application`: lobby, coordinatore, runner delle partite) raggiunge il 99,5%.

Alcuni buchi di copertura sono frutto di una scelta consapevole, poiché riguardano codice che non appartiene alla logica del progetto oppure codice che si limita a delegare ad altre parti già testate:

- **Codice generato dalle librerie**: i codec JSON di `ProtocolCodecs` sono derivati automaticamente tramite le macro di circe (`deriveEncoder`/`deriveDecoder`), che generano metodi conteggiati da scoverage ma non scritti dal team. Il formato prodotto da questi codec è comunque verificato da `ProtocolCodecsSpec`.
- **Codice di sola delega**: l'entry point `ServerApp` si limita a istanziare e collegare i componenti; il metodo `routes` di `WebSocketServer` configura l'endpoint http4s e delega la gestione di connessione, messaggi e disconnessione ai metodi `onConnect`, `onMessage` e `onDisconnect`, testati singolarmente; i metodi di `ClientDisconnectionLogger` diversi da `error` inoltrano il messaggio al logger sottostante senza alcuna logica aggiuntiva.

## Esempi rilevanti

### Sistemi ECS come funzioni pure

Poiché i sistemi non mutano il mondo ma ne restituiscono una nuova istanza, è possibile verificare anche le proprietà di immutabilità. Nel test seguente si controlla che il `CollisionSystem` generi un nuovo mondo quando risolve una collisione, e che restituisca la stessa istanza quando non c'è nulla da risolvere:

```scala
it should "resolve collisions between entities correctly and maintain world immutability invariants" in:
  val world = GameWorld(List(entity1, entity2))
  val (updatedWorld, events) = CollisionSystem.update(world, Set.empty, 1000)
  events.collect { case c: CollisionDetected => c } should have size 1
  world.id should not equal updatedWorld.id
  val (sameWorld, newEvents) = CollisionSystem.update(updatedWorld, Set.empty, 1000)
  newEvents shouldBe empty
  sameWorld.id shouldBe updatedWorld.id
```

Allo stesso modo, il `GameEngine` viene testato iniettando pipeline costruite ad hoc (es. un sistema che svuota il mondo o che emette un evento di morte), isolando il motore dai sistemi reali.

### Verifica dei DSL a tempo di compilazione

Il DSL per la definizione delle mappe garantisce a tempo di compilazione che tutte le righe abbiano la stessa lunghezza. Il test lo verifica con `assertDoesNotCompile`:

```scala
it should "validate that all rows have the same length at compile time" in:
  assertDoesNotCompile("GameMap.fromGrid(" +
    "S | / | / | W | / | / | S," +
    "/ | W | / | W | / | W," + // 6 tiles instead of 7
    "S | / | / | W | / | / | S" +
    ")")
```

Analogamente, `PowerUpDslSpec` verifica che una dichiarazione lasciata a metà non produca un `PowerUp`:

```scala
"""val p: PowerUp = powerUp("damage") lasting 8.seconds""" shouldNot typeCheck
```

### Test double per le porte esagonali

In `MatchCoordinatorSpec` le porte di uscita sono sostituite da implementazioni che registrano le chiamate in un `Ref`, così da poter verificare cosa è stato notificato a ciascun giocatore:

```scala
private class RecordingNotifier(sent: Ref[IO, List[(PlayerId, ServerMessage)]]) extends PlayerNotifier[IO]:
  override def send(playerId: PlayerId, message: ServerMessage): IO[Unit] =
    sent.update(_ :+ (playerId -> message))
```

Una `Fixture` assembla tutto il cablaggio sotto test con partite di durata ridotta (50 ms), permettendo di osservare in pochi istanti l'intero ciclo di vita di una partita: avvio, notifica ai giocatori, terminazione e promozione dei giocatori in coda.

### Test di codice concorrente senza attese fisse

Le partite vengono eseguite in fiber separati, quindi alcuni effetti sono osservabili solo dopo un certo tempo. Per evitare test fragili basati su `sleep` di durata arbitraria, è stato definito un combinatore `eventually` che ripete un controllo finché la condizione non è soddisfatta, con un timeout complessivo:

```scala
private def eventually[A](action: IO[A])(predicate: A => Boolean): IO[A] =
  action
    .flatMap(value =>
      if predicate(value) then IO.pure(value) else IO.sleep(10.millis) *> eventually(action)(predicate)
    )
    .timeout(10.seconds)
```

### Test di integrazione del server WebSocket

`ServerIntegrationSpec` collega i componenti reali e simula le connessioni dei client tramite code di frame. Verifica, ad esempio, che un giocatore in eccesso riceva il messaggio `QueueFull` e veda chiusa la propria connessione con il codice previsto, e che il server continui a inviare frame di ping ai client inattivi.

### Formato del protocollo

`ProtocolCodecsSpec` fissa il formato JSON dei messaggi scambiati con il client (stato della partita, entità etichettate per tipo, esiti di fine partita). Questi test fanno da contratto tra server e client: un cambiamento involontario della serializzazione viene rilevato prima di rompere il client.

## Altri elementi

- **Stile uniforme dei test**: nel corso del progetto sono stati fatti refactoring mirati della suite (helper per la creazione delle entità, generazione automatica degli `EntityId`, uniformazione dello stile tra le specifiche), trattando il codice di test con la stessa cura del codice di produzione.
- **Test come documentazione**: i nomi dei test sono scritti come frasi che descrivono il comportamento (es. _"hold the first players back until a whole group is waiting"_), così che l'output di sbt costituisca una specifica leggibile del sistema.
