package com.gemini.jobcoin.handlers

import java.util.UUID

import com.gemini.jobcoin.clients.AddressClient
import com.gemini.jobcoin.db.{Address, Deposit}
import com.gemini.jobcoin.json
import com.gemini.jobcoin.repository.{AddressRepository, DepositRepository}
import com.typesafe.config.Config

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class AddressAlreadyUsed(address: String) extends Exception(s"The address $address is already used.")

class DepositAddressHandler(
  config: Config,
  addressClient: AddressClient,
  addressRepository: AddressRepository,
  depositAddressRepository: DepositRepository) {

  def generateDepositAddress(addresses: Seq[String]): Future[String] = async {
    await(checkAddressesAvailability(addresses))
    await(lockAddresses(addresses))
    val key = UUID.randomUUID().toString
    val depositAddress = Deposit(key, addresses)
    await(depositAddressRepository.write(depositAddress))
    key
  }

  private def checkAddressesAvailability(addresses: Seq[String]): Future[Unit] = {
    for {
      _ <- checkAddressAvailabilityInNetwork(addresses)
      _ <- checkAddressesAvailabilityInDb(addresses)
    } yield ()
  }

  private def checkAddressAvailabilityInNetwork(addresses: Seq[String]): Future[Unit] = async {
    val returnedAddresses = await {
      Future.sequence {
        addresses.map { address =>
          addressClient
            .get(address)
            .map(addressResponse => address -> addressResponse)
        }
      }
    }
    returnedAddresses
      .filter { case (_, addressResponse) => !isNewAddress(addressResponse) }
      .foreach { case (address, _) => throw AddressAlreadyUsed(address) }
    Future.successful()
  }

  private def checkAddressesAvailabilityInDb(addresses: Seq[String]): Future[Unit] = async {
    val alreadyUsedAddressses = await {
      Future.sequence(addresses.map(addressRepository.getById))
    }
    alreadyUsedAddressses
      .collect {
        case Some(address) => address
      }
      .foreach {
        address => throw AddressAlreadyUsed(address.key)
      }
    Future.successful()
  }

  private def isNewAddress(address: json.Address): Boolean =
    address.balance == 0 && address.transactions.isEmpty

  private def lockAddresses(addresses: Seq[String]): Future[Seq[Address]] = {
    Future.sequence(
      addresses.map(address =>
        addressRepository.write(Address(address))))
  }
}
