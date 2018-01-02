package v1.transaction.aggregator

object ActionType {

  sealed trait ActionType
  case object Debit extends ActionType
  case object Credit extends ActionType
  case object NonExistentTransaction extends ActionType

}
