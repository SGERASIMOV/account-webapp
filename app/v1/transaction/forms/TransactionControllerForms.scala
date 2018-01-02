package v1.transaction.forms

import play.api.data.Form
import v1.transaction.aggregator.ActionType._

trait TransactionControllerForms {

  case class TransactionFormInput(actionType: ActionType, changeAmount: BigDecimal)

  import play.api.data.Forms._

  val form = Form(
    mapping(
      "actionType" -> nonEmptyText.verifying(s"No such action type.Possible action types:$Debit,$Credit.", isValidActionType _),
      "changeAmount" -> bigDecimal.verifying("amount is less than 0", isAmountPositive _)
    )((actionType, changeAmount) => TransactionFormInput(convert(actionType), changeAmount))
    (trFormInput => Some(trFormInput.actionType.toString, trFormInput.changeAmount))
  )

  private def isAmountPositive(changeAmount: BigDecimal): Boolean = {
    changeAmount > 0
  }

  private def isValidActionType(actionType: String): Boolean = {
    convert(actionType) match {
      case NonExistentTransaction => false
      case _ => true
    }
  }

  private def convert(actionType: String): ActionType = actionType match {
    case "Debit" => Debit
    case "Credit" => Credit
    case _ => NonExistentTransaction
  }
}
