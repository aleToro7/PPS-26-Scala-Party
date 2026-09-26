package com.unibo.scalaparty.core.map

/** Port through which the game obtains the map a match is played on.
 *
 *  Implementations decide the whole layout of the map, from the arena bounds to the placement of its spawn points.
 *  Providing a map is expected to happen once per match, never during the game loop.
 */
trait GameMapProvider:

  /** Provides a map able to host the given number of players.
   *
   *  @param players the number of players taking part in the match, which must be positive
   *  @return the map to play on, or None if no suitable map exists for that many players
   */
  def mapFor(players: Int): Option[GameMap]

object GameMapProvider:

  /** Creates a provider that always hands out the same map, as long as it can host the requested players.
   *
   *  @param map the map to provide
   *  @return a [[GameMapProvider]] backed by the given map
   */
  def fixed(map: GameMap): GameMapProvider = players =>
    require(players > 0, "A map must be requested for at least one player")
    Option.when(players <= map.capacity)(map)
