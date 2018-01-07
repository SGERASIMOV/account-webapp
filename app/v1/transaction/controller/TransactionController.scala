package v1.transaction.controller

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import v1.transaction.forms.TransactionControllerForms
import v1.transaction.service.TransactionResourceHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Takes HTTP requests and produces JSON.
  */
@Singleton
class TransactionController @Inject()(cc: ControllerComponents, transactionResourceHandler: TransactionResourceHandler)
                                     (implicit ec: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport with TransactionControllerForms {

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
      input => transactionResourceHandler.create(input.changeAmount, input.actionType)
        .map {
          case Right(transaction) => Created(Json.toJson(transaction)).withHeaders(LOCATION -> transaction.link)
          case Left(message) => BadRequest(message)
        }
    )
  }

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info(s"show: id = $id")
    toLong(id) fold (
      _ => Future.successful(BadRequest("transaction id must be of type Long")),
      trId =>
        transactionResourceHandler.lookup(trId).map { transaction =>
          Ok(Json.toJson(transaction))
        }
    )
  }

  def toLong(s: String): Try[Long] = {
    Try {
      s.toLong
    }
  }

}
