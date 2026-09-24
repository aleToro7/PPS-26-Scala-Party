package com.unibo.scalaparty.prolog

import alice.tuprolog.{Int as PrologInt, Struct, Term, Var}
import com.unibo.scalaparty.core.geometry.Point2D
import com.unibo.scalaparty.core.map.{GameMap, GameMapProvider, SpawnPoint}
import com.unibo.scalaparty.core.model.ArenaSettings

/** A [[GameMapProvider]] generating maps through a Prolog theory.
 *
 *  The theory must define `game_map(+Players, -Map)`, where `Map` has the shape
 *  `game_map(arena(Width, Height), [spawn(X, Y, Heading), ...])`.
 *
 *  @param engine the engine answering goals against the game map theory, the default one unless otherwise specified
 */
final class PrologGameMapProvider(
    engine: PrologEngine = PrologEngine.fromResource(PrologGameMapProvider.DefaultTheory)
) extends GameMapProvider:

  /** @inheritdoc
   *
   *  @throws IllegalStateException if the theory answers with a map that does not have the expected shape
   */
  override def mapFor(players: Int): Option[GameMap] =
    require(players > 0, "A map must be requested for at least one player")
    // A solution is the goal itself with Map bound, so it always has the shape game_map(Players, Map).
    engine.solve(Struct("game_map", PrologInt(players), Var("Map"))).headOption.map:
      case Compound(_, _, map) => toGameMap(map)

  private def toGameMap(term: Term): GameMap = term match
    case Compound("game_map", Compound("arena", Num(width), Num(height)), PrologList(spawns)) =>
      GameMap(ArenaSettings(width.toInt, height.toInt), spawns.map(toSpawnPoint))
    case _ => malformed("game map", term)

  private def toSpawnPoint(term: Term): SpawnPoint = term match
    case Compound("spawn", Num(x), Num(y), Num(heading)) => SpawnPoint(Point2D(x, y), heading)
    case _ => malformed("spawn point", term)

  private def malformed(expected: String, term: Term): Nothing =
    throw IllegalStateException(s"The game map theory answered with a malformed $expected: $term")

object PrologGameMapProvider:

  /** The classpath location of the default game map theory. */
  val DefaultTheory: String = "theories/game_map.pl"
