package com.unibo.scalaparty.infrastructure.application

import cats.effect.IO
import com.unibo.scalaparty.infrastructure.ports.CommandPort
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}
import com.unibo.scalaparty.core.model.PlayerCommand

class GameCommandService() extends CommandPort[IO]:

  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): IO[Unit] =
    // TODO: RFU3
    IO.println(s"Ricived command $command from $playerId on $matchId")