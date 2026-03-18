// Copyright 2026 Dixa A/S
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
