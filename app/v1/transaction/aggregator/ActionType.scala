package v1.transaction.aggregator

object ActionType extends Enumeration {
  type ActionType = Value
  val Debit = Value("Debit")
  val Credit = Value("Credit")
}
