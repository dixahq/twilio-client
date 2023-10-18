package com.dixa.twilio.model

import org.scalatest.wordspec.AnyWordSpec

final class SidAbstractTestExtra extends AnyWordSpec {

  classOf[SidAbstract].getSimpleName should {

    "make sure that default case class implementations don't get a copy method that can be used to create invalid instances" in {
      assertTypeError(
        """val instance = SidAbstractTestExtra.TestSidType.unsafe("TEXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
          |instance.copy(toString = "not valid")
          |""".stripMargin
      )
    }
  }

}

private object SidAbstractTestExtra {

  final case class TestSidType private[SidAbstractTestExtra] (override val toString: String)
      extends SidAbstract

  object TestSidType
      extends SidAbstract.SidCompanionObject[TestSidType](
        List(SidAbstract.Prefix("TE")),
        new TestSidType(_)
      )
}
