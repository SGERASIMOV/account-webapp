package v1.transaction.dao

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta.MTable
import TransactionDao.{TransactionDao, TransactionData}
import v1.transaction.aggregator.ActionType
import v1.transaction.aggregator.ActionType.ActionType

import scala.concurrent.{ExecutionContext, Future}

object SlickTransactionDao {

  class Transactions(tag: Tag) extends Table[TransactionData](tag, "TRANSACTIONS") {

    implicit val actionTypeTypeMapper = MappedColumnType.base[ActionType, String](
      e => e.toString,
      s => ActionType.withName(s)
    )

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def created_date = column[Timestamp]("created_date")

    def action_type = column[ActionType]("action_type")

    def change_amount = column[BigDecimal]("change_amount")

    override def * = (id, created_date, action_type, change_amount) <> (TransactionData.tupled, TransactionData.unapply)
  }

  val transactions = TableQuery[Transactions]
}


import v1.transaction.dao.SlickTransactionDao._

@Singleton
class SlickTransactionDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends TransactionDao with HasDatabaseConfigProvider[H2Profile] {

  private val dbInit = db.run {
    MTable.getTables(transactions.baseTableRow.tableName).
      flatMap { tables =>
        if (tables.isEmpty) transactions.schema.create else DBIO.successful(())
      }
  }

  // Allows to run any queries to DB only when it is completely initialized.
  private def initialized[T](block: => Future[T]): Future[T] = dbInit.flatMap(_ => block)

  override def list(): Future[Iterable[TransactionData]] = initialized {
    db.
      run {
        {
          for {
            tr <- transactions
          } yield tr
        }.result.flatMap {
          transactions => DBIO.successful(transactions)
        }.transactionally
      }
  }

  override def get(id: Long): Future[Option[TransactionData]] = initialized {
    db.
      run {
        {
          for {
            tr <- transactions
            if tr.id === id
          } yield tr
        }.result.headOption.flatMap {
          transaction => DBIO.successful(transaction)
        }.transactionally
      }
  }

  override def create(data: TransactionData): Future[TransactionData] = initialized {
    db.run(transactions returning transactions.map(_.id) into ((tr, id) => tr.copy(id = id)) += data)
  }

}
