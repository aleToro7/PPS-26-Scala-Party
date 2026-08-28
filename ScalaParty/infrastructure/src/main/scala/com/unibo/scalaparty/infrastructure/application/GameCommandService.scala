package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.ports.CommandPort
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.model.PlayerCommand

/**
 * Implementation of the [[CommandPort]] responsible for routing in-game actions.
 *
 * Note: Currently implemented as a stub for RFU1. In future iterations (e.g., RFU3),
 * this service will take the GameEngine as a dependency to process actual
 * gameplay mechanics and resolve state computations.
 */
class GameCommandService() extends CommandPort[IO]:

  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): IO[Unit] =
    // TODO: RFU3
    IO.println(s"Ricived command $command from $playerId on $matchId")