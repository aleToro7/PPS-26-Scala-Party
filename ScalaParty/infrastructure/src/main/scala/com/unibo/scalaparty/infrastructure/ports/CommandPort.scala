package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.core.model.PlayerCommand
import com.unibo.scalaparty.infrastructure.model.{MatchId, PlayerId}

trait CommandPort[F[_]]:
  def handleCommand(matchId: MatchId, playerId: PlayerId, command: PlayerCommand): F[Unit]
  
