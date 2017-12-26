package v1.transaction.forms

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, Matchers}

class TransactionControllerFormsTest extends FunSpec with Matchers with MockitoSugar {

  class Fixture extends TransactionControllerForms {}

  describe("the 'Debit' form") {

    it("should not accept negative value") {
      val fixture = new Fixture()
      import fixture._

      val resultForm = form.bind(Map(
        "accountType" -> "debit",
        "changeAmount" -> "-1"
      ))

      resultForm.hasErrors should equal(true)
    }

  }

}
