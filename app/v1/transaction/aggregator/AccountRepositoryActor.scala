package v1.transaction.aggregator

import java.sql.Timestamp
import java.time.LocalDateTime

import v1.transaction.dao.TransactionDao.{TransactionDao, TransactionData}
import ActionType._
import akka.actor.Actor
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.util.Try

object AccountRepositoryActor {

  case class MakeDebit(changeAmount: BigDecimal)

  case class MakeCredit(changeAmount: BigDecimal)

  val ActionTimeout: FiniteDuration = 2 seconds
  implicit val AccountTimeout: Timeout = Timeout(ActionTimeout + (3 seconds))
}

class AccountRepositoryActor(transactionDao: TransactionDao)(implicit ex: ExecutionContext) extends Actor {

  import AccountRepositoryActor._

  override def receive: Receive = {
    case MakeDebit(changeAmount) => sender() ! debit(changeAmount)
    case MakeCredit(changeAmount) => sender() ! credit(changeAmount)
  }

  def debit(changeAmount: BigDecimal): Try[TransactionData] = {
    Try {
      val debit = transactionDao.create(createTransactionData(changeAmount, ActionType.Debit))
      Await.result(debit, ActionTimeout)
    }
  }

  def credit(changeAmount: BigDecimal): Try[TransactionData] = {
    val currentBalance = load()
    Try {
      if (currentBalance >= changeAmount) {
        val credit = transactionDao.create(createTransactionData(changeAmount, ActionType.Credit))
        Await.result(credit, ActionTimeout)
      } else {
        throw new IllegalStateException("Current balance is too low.")
      }
    }
  }

  def load(): BigDecimal = {

    val currentBalance = transactionDao.list().
      map(seq => seq.foldLeft(0: BigDecimal)((balance: BigDecimal, trData) => processTransaction(balance, trData)))

    Await.result(currentBalance, ActionTimeout)
  }

  private def processTransaction(balance: BigDecimal, transactionData: TransactionData): BigDecimal = {
    if (transactionData.actionType == ActionType.Debit) {
      balance + transactionData.changeAmount
    } else {
      balance - transactionData.changeAmount
    }
  }

  private def createTransactionData(changeAmount: BigDecimal, actionType: ActionType): TransactionData = {
    TransactionData(0, Timestamp.valueOf(LocalDateTime.now()), actionType, changeAmount)
  }

}
