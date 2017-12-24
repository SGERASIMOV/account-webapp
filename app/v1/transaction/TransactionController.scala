package v1.transaction

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class TransactionFormInput(actionType: String, changeAmount: BigDecimal)

/**
  * Takes HTTP requests and produces JSON.
  */
@Singleton
class TransactionController @Inject()(cc: ControllerComponents, transactionResourceHandler: TransactionResourceHandler)
                                     (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  private val logger = Logger(getClass)

  def index: Action[AnyContent] = Action.async { implicit request =>
    logger.info("index: ")
    transactionResourceHandler.findAll().map { transactions =>
      Ok(Json.toJson(transactions))
    }
  }

  def process: Action[AnyContent] = Action.async { implicit request =>
    logger.info("process: ")
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
      input => transactionResourceHandler.create(input).map { transaction =>
        Created(Json.toJson(transaction)).withHeaders(LOCATION -> transaction.link)
      }
    )

  }

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"show: id = $id")
    toLong(id) match {
      case None => Future.successful(BadRequest("transaction id must be of type Long"))
      case Some(id) => {
        transactionResourceHandler.lookup(id).map { transaction =>
          Ok(Json.toJson(transaction))
        }
      }
    }
  }

  import play.api.data.Forms._

  private val form = Form(
    mapping(
      "actionType" -> nonEmptyText,
      "changeAmount" -> bigDecimal
    )(TransactionFormInput.apply)(TransactionFormInput.unapply)
  )

  def toLong(s: String): Option[Long] = {
    try {
      Some(s.toLong)
    } catch {
      case e: NumberFormatException => None
    }
  }

}
