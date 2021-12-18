import com.gemini.jobcoin.clients.{AddressClient, TransactionClient}
import com.gemini.jobcoin.db.{Deposit, Transaction}
import com.gemini.jobcoin.handlers.TransactionHandler
import com.gemini.jobcoin.json
import com.gemini.jobcoin.repository.{DepositRepository, TransactionRepository}
import com.typesafe.config.ConfigFactory
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class TransactionHandlerSpec extends FlatSpec with Matchers with BeforeAndAfter {

  val config = ConfigFactory.load()
  val addressClient = mock(classOf[AddressClient])
  val transactionClient = mock(classOf[TransactionClient])
  val depositRepository = mock(classOf[DepositRepository])
  val transactionRepository = mock(classOf[TransactionRepository])

  def mkTransactionHandler(): TransactionHandler = {
    new TransactionHandler(
      config,
      addressClient,
      transactionClient,
      depositRepository,
      transactionRepository
    )
  }

  before {
    reset(
      addressClient,
      transactionClient,
      depositRepository,
      transactionRepository)
  }

  "depositToHome" should "not transfer money to the home account when there are no records yet" in {
    when(depositRepository.get()) thenReturn Future.successful(List.empty[Deposit])
    Await.result(mkTransactionHandler().depositToHome(), Duration.Inf)
    verify(addressClient, times(0)).get(any[String])
    verify(transactionRepository, times(0)).write(any[Transaction])
    verify(transactionClient, times(0)).post(any[json.Transaction])
    verify(transactionRepository, times(0)).delete(any[String])
  }

  "depositToHome" should "not transfer money to the home account when the balance is 0" in {
    val depositAddresses = Deposit("1", "a" :: "b" :: Nil) :: Nil

    when(depositRepository.get()) thenReturn Future.successful(depositAddresses)
    when(addressClient.get("1")) thenReturn Future.successful(json.Address(0, Seq.empty))
    Await.result(mkTransactionHandler().depositToHome(), Duration.Inf)
    verify(transactionRepository, times(0)).write(any[Transaction])
    verify(transactionClient, times(0)).post(any[json.Transaction])
    verify(transactionRepository, times(0)).delete(any[String])
  }

  "depositToHome" should "transfer money to the home account" in {
    val fee = 10 * .3
    val leftAmount = 10 - fee
    val depositAddresses = Deposit("1", "a" :: "b" :: Nil) :: Nil
    val discountAmount = leftAmount / 10
    val createdTransaction = Transaction("1", 10, discountAmount)

    when(depositRepository.get()) thenReturn Future.successful(depositAddresses)
    when(addressClient.get("1")) thenReturn Future.successful(json.Address(10, Seq.empty))
    when(transactionRepository.write(createdTransaction)) thenReturn Future.successful(createdTransaction)
    when(transactionClient.post(
      json.Transaction(
        toAddress = "home_account",
        fromAddress = Some("1"),
        amount = 10
      ))).thenReturn(Future.successful(Map("status" -> "OK")))
    Await.result(mkTransactionHandler().depositToHome(), Duration.Inf)
    verify(transactionRepository, times(0)).delete(any[String])
  }

  "depositToHome" should "rollback deposit to home when post transaction fails" in {
    val fee = 10 * .3
    val leftAmount = 10 - fee
    val depositAddresses = Deposit("1", "a" :: "b" :: Nil) :: Nil
    val discountAmount = leftAmount / 10
    val createdTransaction = Transaction("1", 10, discountAmount)

    when(depositRepository.get()) thenReturn Future.successful(depositAddresses)
    when(addressClient.get("1")) thenReturn Future.successful(json.Address(10, Seq.empty))
    when(transactionRepository.write(createdTransaction)) thenReturn Future.successful(createdTransaction)
    when(transactionClient.post(
      json.Transaction(
        toAddress = "home_account",
        fromAddress = Some("1"),
        amount = 10
      ))).thenReturn(Future.successful(Map("error" -> "Insufficient Funds")))
    Await.result(mkTransactionHandler().depositToHome(), Duration.Inf)
    verify(transactionRepository).delete(any[String])
  }

  "transferToUsers" should "not transfer money to addresses when there are not transactions" in {
    when(transactionRepository.get()) thenReturn Future.successful(List.empty[Transaction])
    Await.result(mkTransactionHandler().transferToUsers(), Duration.Inf)
    verify(depositRepository, times(0)).getById(any[String])
    verify(transactionClient, times(0)).post(any[json.Transaction])
  }

  "transferToUsers" should "not transfer money to addresses when transactions amount are in 0" in {
    val transactions = Transaction("1", 0, 0) :: Nil
    when(transactionRepository.get()) thenReturn Future.successful(transactions)
    Await.result(mkTransactionHandler().transferToUsers(), Duration.Inf)
    verify(depositRepository, times(0)).getById(any[String])
    verify(transactionClient, times(0)).post(any[json.Transaction])
  }

  "transferToUsers" should "transfer money to addresses when available" in {
    val discount = (10 / 10) / 3
    val transactions = Transaction("1", 10, discount) :: Nil
    val addresses = Seq("a", "b", "c")
    val transactionToUpdate = Transaction("1", 10 - 3, discount)

    when(transactionRepository.get()) thenReturn Future.successful(transactions)
    when(depositRepository.getById("1")) thenReturn Future.successful(Some(Deposit("1", addresses)))
    when(transactionRepository.update("1", transactionToUpdate)) thenReturn Future.successful(transactionToUpdate)
    addresses.foreach { address =>
      when(transactionClient.post(
        json.Transaction(
          toAddress = address,
          fromAddress = Some("home_account"),
          amount = discount
        )
      )).thenReturn(Future.successful(Map("status" -> "OK")))
    }
    Await.result(mkTransactionHandler().transferToUsers(), Duration.Inf)
  }
}
