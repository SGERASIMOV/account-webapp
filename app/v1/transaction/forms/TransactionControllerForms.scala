package v1.transaction.forms

import play.api.data.Form

trait TransactionControllerForms {

  case class TransactionFormInput(actionType: String, changeAmount: BigDecimal)

  import play.api.data.Forms._

  private def isAmountPositive(changeAmount: BigDecimal): Boolean = {
    changeAmount >= 0
  }

  val form = Form(
    mapping(
      "actionType" -> nonEmptyText,
      "changeAmount" -> bigDecimal.verifying("amount is less than 0", isAmountPositive _)
    )(TransactionFormInput.apply)(TransactionFormInput.unapply)
  )
}
