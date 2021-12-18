package com.gemini.jobcoin.background

import akka.actor.{ActorSystem, Cancellable}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.FiniteDuration

abstract class BackgroundProcess(
  actorSystem: ActorSystem,
  initialDelay: FiniteDuration,
  interval: FiniteDuration) {

  val cancellable: Cancellable = actorSystem.scheduler.schedule(
    initialDelay = initialDelay,
    interval = interval
  )(process())

  protected def process(): Unit
}