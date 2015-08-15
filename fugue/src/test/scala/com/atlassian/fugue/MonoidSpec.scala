/*
   Copyright 2015 Atlassian

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.atlassian.fugue

import com.atlassian.fugue.Monoid._
import com.atlassian.fugue.Semigroup.semigroup
import com.atlassian.fugue.law.{MonoidDerivedFunctionsTests, MonoidTests}


class MonoidSpec extends TestSuite {

  val intMonoid = monoid(semigroup((i1: Int, i2: Int) => i1 + i2), 0)

  test("Monoid laws") {
    check(MonoidTests(intMonoid))
  }

  test("Monoid derived functions") {
    check(MonoidDerivedFunctionsTests(intMonoid))
  }

  val mulMonoid = monoid(semigroup((i1: Int, i2: Int) => i1 * i2), 1)

  test("Monoid composition") {
    check(MonoidTests(intMonoid.composeMonoid(mulMonoid)))
  }

}
