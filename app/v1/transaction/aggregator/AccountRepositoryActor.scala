package v1.transaction.aggregator

import java.sql.Timestamp
import java.time.LocalDateTime

import v1.transaction.dao.TransactionDao.{TransactionDao, TransactionData}
import ActionType._
import akka.actor.Actor

import scala.concurrent.{ExecutionContext, Future}

object AccountRepositoryActor {

  case class MakeDebit(changeAmount: BigDecimal)

  case class MakeCredit(changeAmount: BigDecimal)

}

class AccountRepositoryActor(transactionDao: TransactionDao)(implicit ex: ExecutionContext) extends Actor {

  import AccountRepositoryActor._

  override def receive: Receive = {
    case MakeDebit(changeAmount) => sender() ! debit(changeAmount)
    case MakeCredit(changeAmount) => sender() ! credit(changeAmount)
  }

  def debit(changeAmount: BigDecimal): Future[TransactionData] = {
    val debit = transactionDao.create(createTransactionData(changeAmount, ActionType.Debit))
    debit
  }

  def credit(changeAmount: BigDecimal): Future[TransactionData] = {
    val currentBalance = load()
    currentBalance.flatMap {
      case balance if balance >= changeAmount => transactionDao.create(createTransactionData(changeAmount, ActionType.Credit))
      case _ => throw new IllegalStateException("Current balance is too low.")
    }
  }

  def load(): Future[BigDecimal] = {
    val currentBalance = transactionDao.list().
      map(seq => seq.foldLeft(0: BigDecimal)((balance: BigDecimal, trData) => processTransaction(balance, trData)))
    currentBalance
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
