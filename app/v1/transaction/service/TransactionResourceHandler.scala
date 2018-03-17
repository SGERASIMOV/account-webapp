package v1.transaction.service

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import play.api.libs.json._
import v1.transaction.aggregator.ActionType.{ActionType, Credit, Debit, NonExistentTransaction}
import v1.transaction.aggregator.AccountRepositoryActor.{MakeCredit, MakeDebit}
import v1.transaction.aggregator.{AccountRepositoryActor, ActionType}
import v1.transaction.dao.TransactionDao.{TransactionDao, TransactionData}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class TransactionResource(id: Long, link: String, createdDate: LocalDateTime,
                               actionType: ActionType, changeAmount: BigDecimal)

object TransactionResource {

  implicit val implicitWrites = new Writes[TransactionResource] {
    def writes(transaction: TransactionResource): JsValue = {
      Json.obj(
        "id" -> transaction.id,
        "link" -> transaction.link,
        "createdDate" -> transaction.createdDate,
        "actionType" -> transaction.actionType.toString,
        "changeAmount" -> transaction.changeAmount
      )
    }
  }

  implicit val AccountTimeout: Timeout = Timeout(2 seconds)

}

@Singleton
class TransactionResourceHandler @Inject()(system: ActorSystem, transactionDao: TransactionDao)
                                          (implicit ec: ExecutionContext) {

  import TransactionResource._

  val accountRepositoryActor: ActorRef = system.actorOf(Props(new AccountRepositoryActor(transactionDao)), "account-actor")

  def create(changeAmount: BigDecimal, actionType: ActionType): Future[TransactionResource] = actionType match {
    case Debit => debit(changeAmount)
    case Credit => credit(changeAmount)
    case NonExistentTransaction => Future.failed(throw new IllegalStateException(s"No such action type:$actionType.Possible action types:${ActionType.Debit},${ActionType.Credit}."))
  }

  private def debit(changeAmount: BigDecimal): Future[TransactionResource] = {
    (accountRepositoryActor ? MakeDebit(changeAmount))
      .mapTo[Future[TransactionData]]
      .flatten
      .map(data => createTransactionResource(data))
  }

  private def credit(changeAmount: BigDecimal): Future[TransactionResource] = {
    (accountRepositoryActor ? MakeCredit(changeAmount))
      .mapTo[Future[TransactionData]]
      .flatten
      .map(data => createTransactionResource(data))
  }

  def lookup(id: Long): Future[Option[TransactionResource]] = {
    val transactionFuture = transactionDao.get(id)
    transactionFuture.map { maybeTransactionData =>
      maybeTransactionData.map { transactionData =>
        createTransactionResource(transactionData)
      }
    }
  }

  def findAll(): Future[Iterable[TransactionResource]] = {
    transactionDao.list().map { transactionDataList =>
      transactionDataList.map(transactionData => createTransactionResource(transactionData))
    }
  }

  private def createTransactionResource(tr: TransactionData): TransactionResource = {
    TransactionResource(tr.id, link(tr.id), tr.createdDate.toLocalDateTime, tr.actionType, tr.changeAmount)
  }

  def link(id: Long): String = {
    val prefix = "/v1/transactions"
    s"$prefix/$id"
  }

}
