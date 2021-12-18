package com.gemini.jobcoin.handlers

import com.gemini.jobcoin.clients.{AddressClient, TransactionClient}
import com.gemini.jobcoin.db.{Deposit, Transaction}
import com.gemini.jobcoin.json
import com.gemini.jobcoin.repository.{DepositRepository, TransactionRepository}
import com.typesafe.config.Config

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class TransactionHandler(config: Config,
  addressClient: AddressClient,
  transactionClient: TransactionClient,
  depositRepository: DepositRepository,
  transactionRepository: TransactionRepository) {
  private final val homeAccountAddress = config.getString("jobcoin.homeAccountAddress")

  def depositToHome(): Future[Unit] = async {
    val generatedDepositAddresses = await(depositRepository.get())

    val addressesFromNetwork = await(
      Future.sequence(
        generatedDepositAddresses
          .map(depositAddress =>
            addressClient
              .get(depositAddress.key)
              .map(address => depositAddress -> address))))

    Future.sequence(
      addressesFromNetwork
        .filter(userAlreadyDeposited)
        .map(transferToHome))
  }

  private def userAlreadyDeposited(value: (Deposit, json.Address)): Boolean = {
    val (_, address) = value
    address.balance > 0
  }

  private def transferToHome(value: (Deposit, json.Address)): Future[Unit] = {
    val (depositAddress, address) = value
    val addressKey = depositAddress.key
    val feeDiscount = address.balance.toDouble * .3
    val leftBalance = address.balance.toDouble - feeDiscount
    val discount = leftBalance / 10
    val transaction = Transaction(addressKey, 10, discount)
    val postTransaction = json.Transaction(
      fromAddress = Some(addressKey),
      toAddress = homeAccountAddress,
      amount = address.balance
    )

    for {
      _ <- transactionRepository.write(transaction)
      result <- transactionClient.post(postTransaction)
      finalResult <- rollbackIfError(addressKey, result)
    } yield finalResult
  }

  private def rollbackIfError(depositAddress: String, result: Map[String, String]): Future[Unit] = {
    result.headOption match {
      case Some(("status", "OK")) => Future.unit
      case _ => transactionRepository.delete(depositAddress).map(_ => ())
    }
  }

  def transferToUsers(): Future[Unit] = async {
    val homeTransactions = await(transactionRepository.get())
    await(
      Future.sequence(
        homeTransactions
          .filter(_.leftTransactions > 0)
          .map(transferToUser)))
  }

  private def transferToUser(transaction: Transaction): Future[Unit] = async {
    val userAddresses = await(depositRepository.getById(transaction.key))
      .map(_.addresses)
      .getOrElse(Seq.empty)
    val amountPerAddress = transaction.discountAmount
    val addressesToDeposit = if (transaction.leftTransactions < userAddresses.size) {
      userAddresses.take(transaction.leftTransactions)
    } else {
      userAddresses
    }
    val results = await {
      Future.sequence(
        addressesToDeposit.map { userAddress =>
          val postTransaction = json.Transaction(
            fromAddress = Some(homeAccountAddress),
            toAddress = userAddress,
            amount = amountPerAddress
          )
          transactionClient.post(postTransaction)
        })
    }

    val successfulDeposits = results
      .map(_.headOption)
      .filter {
        case Some(("status", "OK")) => true
        case _ => false
      }

    val futureUpdateTransaction = transactionRepository
      .update(
        transaction.key,
        transaction.copy(
          leftTransactions = transaction.leftTransactions - successfulDeposits.size))

    futureUpdateTransaction.flatMap(_ => Future.unit)
  }
}
