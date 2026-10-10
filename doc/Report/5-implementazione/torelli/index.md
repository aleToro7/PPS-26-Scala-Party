# Alessandro Torelli

Il mio contributo è stato presente sia sul modulo **core** e sia sul modulo **infrastructure**.
Per quanto riguarda la parte **core** mi sono occupato di:
- Implementazione del GameSettings
- Implementazione della salute e danno
- Implementazione del sistema di sparo (in collaborazione con Diotallevi)
- Implementazione dell'eliminazione delle navicelle
- Implementazione del MatchEndSystem
- Implementazione del DSL per la dichiarazione dei power-up (Non implementati all'interno del gioco)

Mentre lato **infrastructure** mi sono occupato di:
- Implementazione dell'infrastruttura di base del server (in collaborazione con Martini)
- Implementazione del livello di rete WebSocket (in collaborazione con Martini)
- Implementazione del MatchRunner (in collaborazione con Martini)
- Implementazione dell'invio dello stato della partita ai client (WebSocketBroadcaster)
- Implementazione dei codec JSON del protocollo di comunicazione

Nei capitoli successivi vengono riportati gli aspetti che ritengo più rilevanti delle parti da me sviluppate.

## Configurazione della partita

Tutti i parametri di gioco sono raccolti in `GameSettings`, che il server passa al `GameEngine` all'avvio di ogni partita.

```scala
final case class MatchSettings(
    timeLimit: Long
):
  require(timeLimit > 0L, "Time limit must be positive")

object MatchSettings:
  val default: MatchSettings = MatchSettings(timeLimit = 180_000L)

final case class GameSettings(
    spaceship: SpaceshipSettings,
    map: GameMap,
    matchSettings: MatchSettings
)

object GameSettings:
  val default: GameSettings = GameSettings(
    spaceship = SpaceshipSettings.default,
    map = GameMap.default,
    matchSettings = MatchSettings.default
  )
```

Tutti i parametri di gioco sono raccolti in GameSettings, che il server passa al motore all'avvio di ogni partita. La configurazione è divisa per area (navicella e relativa arma, mappa, regole della partita), così ogni parte del sistema riceve solo ciò che le serve: il `MatchEndSystem`, ad esempio, conosce solo `MatchSettings`. L'arma fa parte di `SpaceshipSettings` ed è lo stesso tipo contenuto nello ShootingComponent, quindi non richiede conversioni. Le classi non hanno parametri di default: i valori predefiniti sono istanze default nei companion object.

## Implementazione salute, danno ed eliminazione

Per la salute e per il danno ho creato due componenti differenti:
- `HealthComponent(current, max)`
- `CollisionDamageComponent(damage)`.

In questo modo il `DamageSystem` non deve conoscere il tipo delle entità coinvolte: un'entità può fare danno senza avere una salute (es. proiettile), o anche il contrario.

Il `DamageSystem` consuma gli eventi `CollisionDetected` e risolve ogni collisione in modo simmetrico, facendo colpire ciascuna entità dall'altra:

```scala
override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
  val updatedWorld = events.foldLeft(world):
    case (currentWorld, CollisionDetected(first, second)) => currentWorld.strike(first, second).strike(second, first)
    case (currentWorld, _) => currentWorld
  (updatedWorld, events)
```

Un colpo che va a segno richiede che esistano l'attaccante e il bersaglio, che l'attaccante possa fare danno a quel bersaglio e che il bersaglio abbia salute. Ho espresso queste condizioni come una for-comprehension sulla monade `Option`, che lascia il mondo invariato non appena una di esse manca. Inoltre un proiettile viene consumato appena danneggia un'entità:

```scala
private def strike(attackerId: EntityId, targetId: EntityId): GameWorld =
  val struckWorld =
    for
      attacker <- world.findComponents(attackerId)
      target   <- world.findComponents(targetId)
      damage   <- impactDamage(attacker, targetId)
      health   <- target.collectFirst { case hc: HealthComponent => hc }
      damagedWorld = world.updateComponent(targetId, health.damaged(damage))
    yield if isProjectile(attacker) then damagedWorld - attackerId else damagedWorld
  struckWorld.getOrElse(world)
```

La regola per cui un proiettile non colpisce mai chi l'ha sparato è espressa direttamente in `impactDamage`.

Sia `strike` sia `damaged` sono extension method privati del `DamageSystem`, il primo su `GameWorld` e il secondo su `HealthComponent`. In questo modo i componenti restano semplici dati e `GameWorld` resta un contenitore generico che non conosce le regole del danno: ogni operazione vive nel sistema che la usa e non è visibile al resto del codice. La chiamata mantiene comunque la forma di un metodo dell'oggetto, così la risoluzione di una collisione si legge come una catena: `currentWorld.strike(first, second).strike(second, first)`.

La transizione `damaged` satura la salute a zero, per questo il `DeathSystem` può riconoscere un'entità distrutta con un confronto esatto, `health.current == 0.0`. Il sistema rimuove queste entità ed emette per ciascuna un `GameEvent.Death`.

## Sparo (estensione `ShootingSystem`)

Ho esteso lo `ShootingSystem` perché lo sparo dipenda dall'arma della navicella: il proiettile nasce sulla punta della navicella, viaggia nella direzione corrente alla velocità dell'arma e ne eredita la potenza. Un'intenzione di sparo viene consumata a ogni aggiornamento anche se l'arma è in cooldown, evitando così che tenere premuto il tasto accumuli colpi da sparare appena l'arma è pronta.

Anche le transizioni del `ShootingComponent` sono extension method privati del sistema, per lo stesso motivo visto nel `DamageSystem`: il componente resta un dato e le regole del cooldown stanno nello `ShootingSystem`, l'unico che le usa.

```scala
extension (component: ShootingComponent)

  private def isReady(dt: Long): Boolean = component.cooldownTimer - dt <= 0

  private def reloaded: ShootingComponent =
    component.copy(isShooting = false, cooldownTimer = component.weapon.shootCooldown)

  private def cooledDown(dt: Long): ShootingComponent =
    component.copy(isShooting = false, cooldownTimer = Math.max(0, component.cooldownTimer - dt))
```

```scala
private def bulletFor(shooterId: EntityId, components: List[Component], weapon: Weapon): Option[EntityWithComponents] =
  for
    position <- components.collectFirstOfClass[PositionComponent].map(_.position)
    velocity <- components.collectFirstOfClass[MovementComponent].map(_.velocity)
    direction = velocity.normalized
  yield EntityFactory.createBullet(
    shooterId = shooterId,
    position = position + direction * weapon.muzzleOffset,
    velocity = direction * weapon.bulletSpeed,
    power = weapon.bulletPower
  )
```

## Implementazione `MatchEndSystem`

Il `MatchEndSystem` è l'ultimo sistema della pipeline, così da giudicare lo stato finale del tick, dopo che il `DeathSystem` ha rimosso le navicelle distrutte. L'intera regola è un pattern matching sulla lista dei sopravvissuti:

```scala
final case class MatchEndSystem(settings: MatchSettings) extends WorldSystem:

  override def update(world: GameWorld, events: Set[GameEvent], dt: Long): SystemOutput =
    (world, events ++ outcome(world).map(GameEvent.MatchEnded(_)))

  private def outcome(world: GameWorld): Option[MatchOutcome] =
    world.spaceships match
      case Nil => Some(MatchOutcome.NoSurvivors)
      case List(winner) => Some(MatchOutcome.LastStanding(winner))
      case _ => Option.when(world.clock.exists(_.elapsed >= settings.timeLimit))(MatchOutcome.TimeUp)
```

L'ordine dei casi stabilisce la priorità: se nello stesso tick scade il tempo e resta una sola navicella, la partita è vinta e non finisce per tempo. I sopravvissuti sono letti dal `GameWorld`, quindi il sistema tiene conto sia delle navicelle distrutte sia di quelle dei giocatori che hanno abbandonato la partita.

Anche `spaceships` e `clock` sono extension method privati su `GameWorld`: sono interrogazioni che servono solo a questo sistema, quindi non le ho aggiunte all'interfaccia del mondo, ma la regola resta leggibile come `world.spaceships`.

Nei test ho usato degli extension method locali per descrivere ogni scenario come una trasformazione dello stesso mondo di partenza. Così ogni test si legge come una frase, ad esempio `world.at(timeLimit).without(second, third)`, e la costruzione del mondo non si ripete in ogni caso:

```scala
extension (world: GameWorld)
  private def at(elapsed: Long): GameWorld = world.updateComponent(clockId, MatchClockComponent(elapsed))
  private def without(entityIds: EntityId*): GameWorld = entityIds.foldLeft(world)(_ - _)

it should "declare the winner even when the time limit is reached too" in:
  endOf(world.at(timeLimit).without(second, third)) shouldBe Some(MatchOutcome.LastStanding(first))
```

## Codec JSON del protocollo

Client e server comunicano tramite messaggi JSON, serializzati con la libreria circe. Le istanze `Encoder` e `Decoder` sono raccolte nell'oggetto `ProtocolCodecs` del modulo infrastructure, e chi ne ha bisogno le porta nello scope con `import ProtocolCodecs.given`.

L'unico tipo che ha richiesto istanze scritte a mano è `EntityId`, che nel core è un opaque type su `Long`: al di fuori del suo companion object il compilatore non lo considera un `Long`, quindi circe non sa serializzarlo. Invece di riscrivere la serializzazione, le due istanze riusano quelle già esistenti per `Long`, adattandole con `map` in lettura e `contramap` in scrittura:

```scala
given Decoder[EntityId] = Decoder.decodeLong.map(EntityId.fromLong)
given Encoder[EntityId] = Encoder.encodeLong.contramap(_.value)
```

Una volta disponibile l'istanza per `EntityId`, i messaggi che lo contengono, come `MatchOutcome.LastStanding(winner)`, vengono derivati automaticamente da circe a partire dalla struttura dei tipi:

```scala
// Inbound (Client -> Server)
given Decoder[PlayerInput] = deriveDecoder

// Outbound (Server -> Client)
given Encoder[MatchState] = deriveEncoder
given Encoder[GameEvent] = deriveEncoder
given Encoder[MatchOutcome] = deriveEncoder
```

## Buffer dei comandi

Il `GameCommandService` accumula gli input che arrivano dai WebSocket finché il `MatchRunner` non li preleva al tick successivo. Il prelievo e lo svuotamento del buffer di una partita avvengono in un'unica `Ref.modify` (stato condiviso di cats-effect), aggiornato atomicamente, così nessun comando ricevuto nel frattempo va perso o viene processato due volte:

```scala
def drainCommands(matchId: MatchId): IO[List[(PlayerId, PlayerInput)]] =
  bufferRef.modify: buffer =>
    val pending = buffer.getOrElse(matchId, List.empty)
    (buffer.removed(matchId), pending)
```

## DSL per la descrizione dei power-up

Il requisito opzionale RFS8 prevede dei bonus temporanei raccolti dalle navicelle. Per dichiararli ho pensato e realizzato un DSL interno che permette di descriverli in questo modo:

```scala
powerUp("rapid-fire") lasting 8.seconds scaling ShootCooldown by 0.5
powerUp("shield") lasting 1500.millis scaling DamageTaken by 0.25
powerUp("repair") healing 30
```

Per motivi di tempo i power-up non sono stati integrati nel gioco: non vengono generati nell'arena né applicati alle navicelle. Ho comunque scelto di implementare il DSL come base per un'implementazione futura, vista l'opzionalità del requisito.

Il modello distingue due tipi di effetto, un potenziamento temporaneo di una statistica e un ripristino immediato della salute:

```scala
final case class PowerUp(name: String, effect: Effect):
  require(!name.isBlank, "Power-up name cannot be blank")

sealed trait Effect

object Effect:
  final case class Boost(modifier: StatModifier, duration: Long) extends Effect:
    require(duration > 0L, "Boost duration must be positive")

  final case class Repair(amount: Double) extends Effect:
    require(amount > 0.0, "Repair amount must be positive")

final case class StatModifier(stat: Stat, factor: Double):
  require(factor > 0.0, "Modifier factor must be positive")
```

dove `Stat` è un `enum` con le statistiche modificabili (`ShootCooldown`, `BulletPower`, `DamageTaken`).

```scala
object PowerUpDsl:
  export Stat.*

  def powerUp(name: String): NamedPowerUp = NamedPowerUp(name)

  final case class NamedPowerUp(name: String):
    infix def lasting(duration: FiniteDuration): TimedPowerUp = TimedPowerUp(name, duration)
    infix def healing(amount: Double): PowerUp = PowerUp(name, Effect.Repair(amount))

  final case class TimedPowerUp(name: String, duration: FiniteDuration):
    infix def scaling(stat: Stat): ScalingPowerUp = ScalingPowerUp(name, duration, stat)

  final case class ScalingPowerUp(name: String, duration: FiniteDuration, stat: Stat):
    infix def by(factor: Double): PowerUp = PowerUp(name, Effect.Boost(StatModifier(stat, factor), duration.toMillis))
```

Il modificatore `infix` dichiara esplicitamente quali metodi sono pensati come parole del linguaggio. La durata è una `FiniteDuration`, così l'unità di misura è visibile nella frase, e la conversione nei millisecondi usati dal motore avviene in un solo punto.