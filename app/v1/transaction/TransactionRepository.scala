package v1.transaction

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

case class TransactionCreationData(actionType: String, changeAmount: BigDecimal)
case class TransactionData(id: Long, createdDate: LocalDateTime, actionType: String, changeAmount: BigDecimal)

//TODO: use H2 db
trait TransactionRepository {

  def list(): Future[Iterable[TransactionData]]

  def get(id: Long): Future[Option[TransactionData]]

  def create(data: TransactionCreationData): Future[TransactionData]
}

@Singleton
class TransactionRepositoryImpl @Inject()()(implicit ec: ExecutionContext) extends TransactionRepository {

  private val logger = Logger(this.getClass)

  private val transactionList = scala.collection.mutable.ArrayBuffer(
    TransactionData(1, LocalDateTime.now(), "debit", 1.0),
    TransactionData(2, LocalDateTime.now(), "credit", 1.0)
  )

  def list(): Future[Iterable[TransactionData]] = {
    Future {
      logger.info(s"list: ")
      transactionList
    }
  }

  override def get(id: Long): Future[Option[TransactionData]] = {
    Future {
      logger.info(s"get: id = $id")
      transactionList.find(transaction => transaction.id == id)
    }
  }

  def create(data: TransactionCreationData): Future[TransactionData] = {
    Future {
      logger.info(s"create: data = $data")
      val testId = 3
      val dataToSave = TransactionData(testId, LocalDateTime.now(), data.actionType, data.changeAmount)
      transactionList += dataToSave
      dataToSave
    }
  }

}
