package com.unibo.scalaparty.infrastructure.ports

import com.unibo.scalaparty.core.model.{GameEvent, MatchState}
import com.unibo.scalaparty.infrastructure.model.MatchId

trait MatchEventPublisher[F[_]]:
  def broadcastState(matchId: MatchId, state: MatchState): F[Unit]
  def broadcastEvent(matchId: MatchId, event: GameEvent): F[Unit]
