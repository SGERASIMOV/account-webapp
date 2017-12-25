package v1.transaction.forms

import play.api.data.Form

trait TransactionControllerForms {

  case class TransactionFormInput(actionType: String, changeAmount: BigDecimal)

  import play.api.data.Forms._

  val form = Form(
    mapping(
      "actionType" -> nonEmptyText,
      "changeAmount" -> bigDecimal
    )(TransactionFormInput.apply)(TransactionFormInput.unapply)
  )
}
