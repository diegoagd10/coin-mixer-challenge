package com.gemini.jobcoin.background

import akka.actor.ActorSystem
import com.gemini.jobcoin.handlers.TransactionHandler

import scala.concurrent.Await
import scala.concurrent.duration._

class TransferWatcher(actorSystem: ActorSystem, transactionHandler: TransactionHandler) extends
  BackgroundProcess(
    actorSystem,
    initialDelay = 1.minute,
    interval = 10.seconds) {

  def process(): Unit = {
    try {
      Await.result(transactionHandler.transferToUsers(), Duration.Inf)
    } catch {
      case ex: Exception => println(s"Exception at TransferWatcher: $ex.getMessage\n")
    }
  }
}
