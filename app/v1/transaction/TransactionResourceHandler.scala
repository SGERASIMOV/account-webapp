package v1.transaction

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import java.time.LocalDateTime

case class TransactionResource(id: Long, link: String, createdDate: LocalDateTime,
                               actionType: String, changeAmount: BigDecimal)

object TransactionResource {

  implicit val implicitWrites = new Writes[TransactionResource] {
    def writes(transaction: TransactionResource): JsValue = {
      Json.obj(
        "id" -> transaction.id,
        "link" -> transaction.link,
        "createdDate" -> transaction.createdDate,
        "actionType" -> transaction.actionType,
        "changeAmount" -> transaction.changeAmount
      )
    }
  }
}

@Singleton
class TransactionResourceHandler @Inject()(transactionRepository: TransactionRepositoryImpl)
                                          (implicit ec: ExecutionContext) {

  def create(transactionInput: TransactionFormInput): Future[TransactionResource] = {
    transactionRepository.create(TransactionCreationData(transactionInput.actionType, transactionInput.changeAmount))
      .map { savedData =>createTransactionResource(savedData)
    }
  }

  def lookup(id: Long): Future[Option[TransactionResource]] = {
    val transactionFuture = transactionRepository.get(id)
    transactionFuture.map { maybeTransactionData =>
      maybeTransactionData.map { transactionData =>
        createTransactionResource(transactionData)
      }
    }
  }

  def findAll(): Future[Iterable[TransactionResource]] = {
    transactionRepository.list().map { transactionDataList =>
      transactionDataList.map(transactionData => createTransactionResource(transactionData))
    }
  }

  private def createTransactionResource(p: TransactionData): TransactionResource = {
    TransactionResource(p.id, link(p.id), p.createdDate, p.actionType, p.changeAmount)
  }

  def link(id: Long): String = {
    val prefix = "/v1/transactions"
    s"$prefix/$id"
  }

}
