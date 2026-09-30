package com.unibo.scalaparty.core.ecs.systems

import com.unibo.scalaparty.core.model.GameSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SystemPipelineSpec extends AnyFlatSpec with Matchers:

  private val defaultSystems = SystemPipeline.default(GameSettings.default).toList

  "The default SystemPipeline" should "remove the destroyed entities after damage has been applied" in:
    defaultSystems should contain inOrder (DamageSystem, DeathSystem)
