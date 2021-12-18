package com.gemini.jobcoin

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.gemini.jobcoin.background.{DepositWatcher, TransferWatcher}
import com.gemini.jobcoin.clients.{AddressClient, TransactionClient}
import com.gemini.jobcoin.handlers.{AddressAlreadyUsed, DepositAddressHandler, TransactionHandler}
import com.gemini.jobcoin.repository.{AddressRepository, DepositRepository, TransactionRepository}
import com.typesafe.config.ConfigFactory
import javax.management.openmbean.KeyAlreadyExistsException

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object JobcoinMixer {

  object CompletedException extends Exception {}

  def main(args: Array[String]): Unit = {
    // Create an actor system
    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()

    // Load Config
    val config = ConfigFactory.load()

    // Clients
    val addressClient = new AddressClient(config)
    val transactionClient = new TransactionClient(config)

    // Repositories
    val addressRepository = new AddressRepository
    val depositRepository = new DepositRepository
    val transactionRepository = new TransactionRepository

    // Handlers
    val depositHandler = new DepositAddressHandler(config, addressClient, addressRepository, depositRepository)
    val transactionHandler = new TransactionHandler(config, addressClient, transactionClient, depositRepository, transactionRepository)

    // Background processes
    val depositWatcher = new DepositWatcher(actorSystem, transactionHandler)
    val transferWatcher = new TransferWatcher(actorSystem, transactionHandler)

    try {
      while (true) {
        withExceptionHandler {
          println(prompt)
          val line = StdIn.readLine()

          if (line == "quit") throw CompletedException

          val addresses = line.split(",")
          if (line == "") {
            println(s"You must specify empty addresses to mix into!\n$helpText")
          } else {
            val depositAddress = Await.result(depositHandler.generateDepositAddress(addresses), Duration.Inf)
            println(s"You may now send Jobcoins to address $depositAddress. They will be mixed and sent to your destination addresses.")
          }
        }
      }
    } catch {
      case CompletedException => println("Quitting...")
    } finally {
      depositWatcher.cancellable.cancel()
      transferWatcher.cancellable.cancel()
      actorSystem.terminate()
    }
  }

  private def withExceptionHandler(f: => Unit): Unit = {
    try {
      f
    } catch {
      case ex: KeyAlreadyExistsException => println(s"${ex.getMessage}\n")
      case ex: AddressAlreadyUsed => println(s"${ex.getMessage}\n")
    }
  }

  val prompt: String = "Please enter a comma-separated list of new, unused Jobcoin addresses where your mixed Jobcoins will be sent."
  val helpText: String =
    """
      |Jobcoin Mixer
      |
      |Takes in at least one return address as parameters (where to send coins after mixing). Returns a deposit address to send coins to.
      |
      |Usage:
      |    run return_addresses...
    """.stripMargin
}
