# Implementazione: Federico Diotallevi

Questa sezione illustra i contributi individuali sviluppati da **Federico Diotallevi** all'interno del progetto **ScalaParty**, approfondendo le scelte implementative e l'applicazione dei meccanismi avanzati e idiomatici offerti da **Scala 3**.

## Panoramica dei Contributi Personali

Durante i quattro sprint del progetto, il lavoro svolto si è concentrato prevalentemente sullo sviluppo del modulo `core`, con particolare responsabilità per:

- Il motore matematico e geometrico 2D (`com.unibo.scalaparty.core.geometry`).
- L'algoritmo di rilevamento e risoluzione delle collisioni (`CollisionSystem`).
- Il design e l'implementazione del **Map DSL** (`com.unibo.scalaparty.core.model.map`).
- L'architettura a combinatori della pipeline di simulazione (`WorldSystem`, `GameEngine`).
- La cinematica di movimento e confinamento nell'arena (`MovementSystem`, `ArenaSystem`).

Inoltre, in collaborazione con Torelli, ho contribuito alla realizzazione di:

- Il sottosistema di sparo e gestione dei proiettili (`ShootingSystem`, `BulletComponent`).
- La serializzazione dello stato verso l'esterno (`EntityAdapter`, `EntityDto`).

## Type-Level Programming nel Map DSL

Uno dei contributi più caratterizzanti dello sviluppo è stata la realizzazione del **DSL per le mappe di gioco**, progettato per consentire una definizione grafica e leggibile delle arene garantendo la correttezza dimensionale a **tempo di compilazione**.

In una mappa basata su griglia di tessere (muri, spawn, spazi vuoti), definire righe di lunghezze disuguali comporterebbe anomalie geometriche durante la partita. Un controllo tradizionale a runtime tramite eccezioni manifesterebbe l'errore solo all'avvio del match. L'obiettivo è stato quindi impedire a compile-time la creazione di mappe non rettangolari.

### Soluzione Adottata

La soluzione sfrutta l'aritmetica a livello di tipi e i tipi opachi per garantire che tutte le righe della mappa abbiano la stessa lunghezza, senza introdurre overhead a runtime.

```scala
enum MapTile:
  case Empty
  case Wall
  case Spawn

object Dsl:
  val S: MapTile = MapTile.Spawn
  val W: MapTile = MapTile.Wall
  val / : MapTile = MapTile.Empty

  // Tipo opaco parametrizzato sulla lunghezza della riga N
  opaque type MapRow[N <: Int] <: Vector[MapTile] = Vector[MapTile]

  extension (firstTile: MapTile)
    // Combina il primo tile con il secondo producendo una riga di lunghezza esatta 2
    def |(secondTile: MapTile): MapRow[2] = Vector(firstTile, secondTile)

  extension [N <: Int](row: MapRow[N])
    // Aggiunge una tessera incrementando il tipo della dimensione a N + 1
    def |(tile: MapTile): MapRow[N + 1] = tiles :+ tile

    private def tiles: Vector[MapTile] = row
```

Grazie all'aritmetica type-level, è possibile eseguire addizioni sui tipi direttamente nel compilatore. Ogni invocazione dell'operatore `|` incrementa il parametro di tipo letterale `N` della riga (`MapRow[N + 1]`).
All'interno del modulo `Dsl`, `MapRow[N]` è trattato come un `Vector[MapTile]`, beneficiando di tutte le operazioni delle collezioni standard. All'esterno del modulo, il tipo concreto è completamente nascosto e viene esposto solo il vincolo di tipo `MapRow[N]`, garantendo zero-overhead a runtime e massima type safety.

Il costruttore `GameMap.fromGrid` è definito come:

```scala
def fromGrid[N <: Int](using tileSize: TileSize = TileSize(80))(grid: MapRow[N]*): GameMap
```

Il parametro di tipo `N` è inferito dal compilatore in base alla prima riga passata come argomento. Tutte le righe successive devono avere lo stesso tipo `MapRow[N]`, altrimenti il compilatore segnala un errore di type mismatch.

```scala
// Esempio: Compila regolarmente (tutte le righe sono MapRow[5])
GameMap.fromGrid(
  W | W | W | W | W,
  W | / | S | / | W,
  W | W | W | W | W
)

// Esempio: Errore a compile-time (la seconda riga è MapRow[4])
GameMap.fromGrid(
  W | W | W | W | W,
  W | / | S | /,     // Errore: Found MapRow[4], Required MapRow[5]
  W | W | W | W | W
)
```

Inoltre il parametro contestuale `TileSize` permette di definire la dimensione di ciascun tile in maniera totalmente trasparente e soltanto al bisogno, in assenza di un valore given esplicito, il sistema utilizza la dimensione consigliata come default.

## Contextual Abstractions e Geometria 2D

Per implementare la fisica bidimensionale e l'algoritmo di collisione, è stato necessario proiettare forme geometriche eterogenee (poligoni, cerchi, rettangoli AABB) lungo assi di separazione arbitrari.
Invece di imporre ereditarietà sull'ADT `Shape`, è stato definito il trait funzionale `Projectable`:

```scala
trait Projectable:
  def projectOnto(axis: Vector2D): (Double, Double)
```

Sfruttando le **Contextual Abstractions** di Scala 3, il compilatore converte automaticamente e trasparentemente le forme geometriche in entità proiettabili:

```scala
given Conversion[Polygon, Projectable] with
  def apply(p: Polygon): Projectable = axis =>
    val projections = p.vertices.map: v =>
      v.x * axis.x + v.y * axis.y
    (projections.min, projections.max)

given Conversion[Circle, Projectable] with
  def apply(c: Circle): Projectable = axis =>
    val centerProjection = c.center.x * axis.x + c.center.y * axis.y
    (centerProjection - c.radius, centerProjection + c.radius)
```

La conversione implicita permette di applicare direttamente metodi come `projectOnto` o `minOverlappingAxis` su istanze di `Polygon` o `Circle` senza wrapper espliciti.

- **Extension Methods Polimorfe**: Tutta la logica di calcolo delle intersezioni e dei vettori di penetrazione è incapsulata in extension methods parametriche:

```scala
extension [S <: Shape](self: S)
  def intersects[B <: Shape](other: B): Boolean = ...
  def penetratingVector(other: Shape): Option[Vector2D] = ...
  def moveTo(p: Point2D): S = ...
```

Questo approccio mantiene le definizioni dei tipi nell'enum `Shape` estremamente pulite e snelle, separando la dichiarazione dei dati dalle operazioni geometriche.

## Risoluzione Immutabile nel Collision System

Il `CollisionSystem` rappresenta il componente computazionale più articolato della simulazione. Esso combina il rilevamento in due fasi (broad-phase con AABB e narrow-phase con SAT) con la risoluzione cinematica degli urti.

### Deduplicazione delle Collisioni

In una simulazione fisica con molteplici corpi in movimento, due entità che collidono vengono rilevate sia dalla prospettiva di $A$ rispetto a $B$, sia da quella di $B$ rispetto ad $A$. Per evitare di applicare due volte il rimbalzo fisico o di generare eventi di collisione duplicati, è stato introdotto il tipo `CollisionPair`:

```scala
type CollisionPair = (EntityId, EntityId)

object CollisionPair:
  def apply(a: EntityId, b: EntityId): CollisionPair =
    if a.value < b.value then (a, b) else (b, a)
```

Normalizzando la coppia in base all'ID numerico, il sistema raggruppa ed elimina le intersezioni ridondanti in modo funzionale:

```scala
val uniqueCollisions = detectedCollisions
  .groupBy((actor, target, _) => CollisionPair(actor, target))
  .values
  .flatMap(_.headOption) // Prende solo la prima collisione di ciascuna coppia
```

Ciò permette di rendere le tuple facilmente confrontabili senza dover introduurre un nuovo tipo di dato mantenendo la semplicità di una tupla `(EntityId, EntityId)`.

## Uso della for-comprehension

In tutto il modulo è stato ampiamente utilizzato il costrutto `for-comprehension` per la gestione dei valori opzionali e delle collezioni, migliorando la leggibilità e riducendo la complessità del codice.
Un esempio semplice ed elegante è la rilevazione delle collisioni tra entità all'interno del `CollisionSystem`:

```scala
private def detectCollisions(
    movingEntitiesWithShapes: Iterable[EntityWithShape],
    entitiesWithShapes: Iterable[EntityWithShape]
): Iterable[(EntityId, EntityId, Vector2D)] =
  for
    (actor, actorShape)   <- movingEntitiesWithShapes
    (target, targetShape) <- entitiesWithShapes
    if actor != target
    if actorShape.boundingBox intersects targetShape.boundingBox
    mtv <- actorShape.penetratingVector(targetShape)
  yield (actor, target, mtv)
```

La for-comprehension consente di esprimere in maniera chiara e concisa la logica di filtraggio e trasformazione dei dati.
In modo totalmente chiaro e conciso, in poche righe di codice viene eseguito un algoritmo complesso come SAT per il rilevamento delle collisioni tra entità, senza dover ricorrere a cicli annidati o a condizioni multiple.
Semplicemente leggendo il codice, è possibile comprendere la logica di rilevamento delle collisioni tra entità, senza dover analizzare dettagli implementativi complessi.

## Reflection

In Scala, le informazioni sui tipi generici vengono cancellate a runtime a causa della JVM. Per consentire a `GameWorld.findComponent[C]` di cercare componenti per tipo senza perdere la type safety.
È stato necessario sfruttare la reflection di scala attraverso il meccanismo dei `ClassTag`, attraverso il quale è possibile ottenere informazioni sul tipo generico `C` a runtime, consentendo di filtrare i componenti in base al loro tipo concreto mentendo la type safety a compile-time.

```scala
def findComponent[C <: Component: ClassTag](entityId: EntityId): Option[C] =
  findComponents(entityId).getOrElse(Nil).collectFirstOfClass[C]
```
