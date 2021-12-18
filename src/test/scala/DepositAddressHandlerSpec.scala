import com.gemini.jobcoin.clients.AddressClient
import com.gemini.jobcoin.db.{Address, Deposit}
import com.gemini.jobcoin.handlers.{AddressAlreadyUsed, DepositAddressHandler}
import com.gemini.jobcoin.json
import com.gemini.jobcoin.repository.{AddressRepository, DepositRepository}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{mock, reset, _}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class DepositAddressHandlerSpec extends FlatSpec with Matchers with BeforeAndAfter {

  val config = ConfigFactory.load()
  val addressClient = mock(classOf[AddressClient])
  val addressRepository = mock(classOf[AddressRepository])
  val depositRepository = mock(classOf[DepositRepository])

  def mkTransactionHandler(): DepositAddressHandler = {
    new DepositAddressHandler(
      config,
      addressClient,
      addressRepository,
      depositRepository
    )
  }

  before {
    reset(
      addressClient,
      addressRepository,
      depositRepository)
  }

  "generateDepositAddress" should "throw AddressAlreadyUsed when the address is already used in the network" in {
    val address = "1"
    when(addressClient.get(address)) thenReturn Future.successful(json.Address(1, Seq.empty))
    intercept[AddressAlreadyUsed] {
      Await.result(mkTransactionHandler().generateDepositAddress(Seq(address)), Duration.Inf)
    }
  }

  "generateDepositAddress" should "throw AddressAlreadyUsed when the address in the db" in {
    val address = "1"
    when(addressClient.get(address)) thenReturn Future.successful(json.Address(0, Seq.empty))
    when(addressRepository.getById(address)) thenReturn Future.successful(Some(Address(address)))
    intercept[AddressAlreadyUsed] {
      Await.result(mkTransactionHandler().generateDepositAddress(Seq(address)), Duration.Inf)
    }
  }

  "generateDepositAddress" should "create the new Deposit address" in {
    val address = "1"
    when(addressClient.get(address)) thenReturn Future.successful(json.Address(0, Seq.empty))
    when(addressRepository.getById(address)) thenReturn Future.successful(None)
    when(addressRepository.write(Address(address))) thenReturn Future.successful(Address(address))
    when(depositRepository.write(any[Deposit])) thenReturn Future.successful(Deposit("key", Seq(address)))
    Await.result(mkTransactionHandler().generateDepositAddress(Seq(address)), Duration.Inf)
  }
}
