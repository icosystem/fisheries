/*
 *  Copyright (C) 2014 Icosystem
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package simulation.agent

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import simulation.unit._

@RunWith(classOf[JUnitRunner])
class ConstantElasticityFunctionSpec extends Specification {

  "function" should {

    "return y0 at x0" in {
      val consumer = TestConsumer()
      val x0 = 10d
      val y0 = 15d
      val ε = 2d
      val cef = ConstantElasticityFunction.constantElasticityFunction(x0, y0, ε)_

      cef(x0) must beCloseTo(y0, 0.0000001)
    }
  
  }
  
}