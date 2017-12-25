package v1.transaction.dao

import java.sql.Timestamp

import com.google.inject.ImplementedBy
import v1.transaction.aggregator.ActionType._

import scala.concurrent.Future

object TransactionDao {

  case class TransactionData(id: Long, createdDate: Timestamp, actionType: ActionType, changeAmount: BigDecimal)

  @ImplementedBy(classOf[SlickTransactionDao])
  trait TransactionDao {

    def list(): Future[Iterable[TransactionData]]

    def get(id: Long): Future[Option[TransactionData]]

    def create(data: TransactionData): Future[TransactionData]
  }

}
