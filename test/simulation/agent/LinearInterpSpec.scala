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
import LinearInterp.linearInterp
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import simulation.unit._
import CompactSigmoidFunction.compactSigmoid

@RunWith(classOf[JUnitRunner])
class LinearInterpSpec extends Specification {
  "Simple Inputs" should {
    "interpolate" in {
      linearInterp(10, 50)(0) must beEqualTo(10)
      linearInterp(10, 50)(1) must beEqualTo(50)
      linearInterp(10, 50)(0.5) must beCloseTo(30,0.001)
   }
}
}
