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
import CompactSigmoidFunction.compactSigmoid

@RunWith(classOf[JUnitRunner])
class CompactSigmoidFunctionSpec extends Specification {
  "Simple Inputs" should {

    "left intersect" in {
      val f = compactSigmoid(0.0, 0.0, 1.0, 1.0)_
//      assertEquals(0.0,f(0),
      f(0) must beCloseTo(0.0,0.00001)
      f(1) must beCloseTo(1.0,0.00001)
      f(-0.01) must beEqualTo(0.0)
      f(0.01) must beCloseTo(0.0,0.01)
      f(0.99) must beCloseTo(1.0,0.01)
      f(1.01) must beEqualTo(1.0)
      f(0.5) must beCloseTo(0.5,0.0001)
     }
    
    "steepness" in {
      val f = compactSigmoid(0.0,0.0,1.0,1.0)_
      val steeper = compactSigmoid(0,0,1,1,2)_
      f(0.99) must beGreaterThan(steeper(0.99))
    }
  }
}
