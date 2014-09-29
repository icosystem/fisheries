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
import Gompertz.gompertzSlope
import Gompertz.gompertzInterp

@RunWith(classOf[JUnitRunner])
class GompertzSpec extends Specification {
  "gompertzSlope" should {

    "left intersect" in {
      val f = gompertzSlope(0, 1, 0, .5, 1, 1)_
      f(0) must beCloseTo(0.5,0.00001)
      f(-1000) must beCloseTo(0.0,0.001)
      f(1000) must beCloseTo(1,0.001)
      (f(0.001) - f(0))/(0.001) must beCloseTo(0.5,0.1)
      f(1) must beCloseTo(1,0.2)   
     }  
  }
  
   "gompertzInterp" should {

    "left intersect" in {
      val f = gompertzInterp(0, 1, 0, .5, 1, 0.9)_
      f(0) must beCloseTo(0.5,0.00001)
      f(-1000) must beCloseTo(0.0,0.001)
      f(1000) must beCloseTo(1,0.001)
      f(1) must beCloseTo(0.9,0.01)   
     }
    
    "shift and stretch" in {
      val f = gompertzInterp(-1, 2, 1, .5, 2, 0.9)_
      f(1) must beCloseTo(0.5,0.00001)
      f(-1000) must beCloseTo(-1.0,0.001)
      f(1000) must beCloseTo(2,0.001)
      f(2) must beCloseTo(0.9,0.01)   
     }
    
     "low x1" in {
      val f = gompertzInterp(-1, 2, 1, .5, 0, 0.1)_
      f(1) must beCloseTo(0.5,0.00001)
      f(-1000) must beCloseTo(-1.0,0.001)
      f(1000) must beCloseTo(2,0.001)
      f(0) must beCloseTo(0.1,0.0001)   
     }
     
     "negative" in {
      val f = gompertzInterp(1, -2, 1, .5, 2, -1)_
      f(1) must beCloseTo(0.5,0.00001)
      f(-1000) must beCloseTo(1.0,0.001)
      f(1000) must beCloseTo(-2,0.001)
      f(2) must beCloseTo(-1,0.01)   
     }
  }
}
