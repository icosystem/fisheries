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
import Math.min
import Math.max
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import com.novocode.junit.TestMarker
import simulation.unit.MoneyPerTonnes
import simulation.unit.Tonnes
import simulation.agent.FinancialTransaction.transaction
import simulation.unit.MoneyPerTonnes

@RunWith(classOf[JUnitRunner])
class FinancialTransactionSpec extends Specification {

  "FinancialTransaction" should {

    "price supply demand work" in {
      val f = ft(10, 100, 100)
      f.pricePerTon.v must beEqualTo(10)
      f.supply.v must beEqualTo(100)
      f.demand.v must beEqualTo(100)
      f.excess.v must beEqualTo(0)
      f.tons.v must beEqualTo(100)
    }
    
    "overdemand" in {
      val g = ft(1,10,100) 
      g.pricePerTon.v must beEqualTo(1)
      g.supply.v must beEqualTo(10)
      g.demand.v must beEqualTo(100)
      g.excess.v must beEqualTo(-90)
      g.tons.v must beEqualTo(10)
    }
      "oversupply" in {
      val g = ft(11,10,9) 
      g.pricePerTon.v must beEqualTo(11)
      g.supply.v must beEqualTo(10)
      g.demand.v must beEqualTo(9)
      g.excess.v must beEqualTo(1)
      g.tons.v must beEqualTo(9)
    }
    
  }

    
    def ft(p: Double, s: Double, d: Double) = transaction(MoneyPerTonnes(p), Tonnes(s), Tonnes(d))
}
