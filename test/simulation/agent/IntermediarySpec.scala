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

@RunWith(classOf[JUnitRunner])
class IntermediarySpec extends Specification {
  
  def st(priceIn: Double, supplyIn: Double, demandIn: Double, priceOut: Double, supplyOut: Double, demandOut: Double) = {
    val bought = ft(priceIn, supplyIn, demandIn)
    val sold = ft(priceOut, supplyOut, demandOut)
    BoughtSold(bought, sold)
  }

  case class BoughtSold(bought:Bought, sold:Sold)
  
  def ft(p: Double, s: Double, d: Double) = FinancialTransaction.transaction(MoneyPerTonnes(p), Tonnes(s), Tonnes(d))
  
  def boughtSold(priceIn: Double, tonsIn: Double, eIn: Double, pOut: Double, tonsOut: Double, eOut: Double) = {
    val bought = FinancialTransaction(MoneyPerTonnes(priceIn), Tonnes(tonsIn), Tonnes(eIn))
    val sold = FinancialTransaction(MoneyPerTonnes(pOut), Tonnes(tonsOut), Tonnes(eOut))
    BoughtSold(bought,sold)
  }

  def mm(overheadPerTon: Double = 100, 
  	aggressiveIn: Double = 0, 
  	aggressiveOut: Double = 0, 
  	profitRateDesired: Double = 2,
  	inboundPriceDesired: Double = 100,
    boughtSold: BoughtSold = this.boughtSold(100, 1, 0, 300, 1, 0) ) = {
    val m = Middleman(MoneyPerTonnes(overheadPerTon), MoneyPerTonnes(inboundPriceDesired))(Timeline(10))
    
    val state = IntermediaryState(bought= null, sold = null, demand = null, 
    	aggressiveInboundPriceSetting = aggressiveIn,
        aggressiveOutboundPriceSetting = aggressiveOut,
        desiredProfitRate = profitRateDesired,
        desireToIncreaseMarketShare = 0d)
    
    m.currentState = state
    
    val bought = boughtSold.bought
    val sold = boughtSold.sold
    val demand = m.newDemandValue(bought, sold)
    
    m.currentState = state.copy(bought = bought, sold = sold, demand = demand) 
    
    demand.v
  }
  
  
  "Middleman" should {

    "average demand and supply" in {
      val demand = mm(boughtSold = boughtSold(100, 10, 0, 300, 10, -10))
      demand must beCloseTo(15, 0.1)
    }

    "refuse to operate at a loss" in {
      val demand = mm(boughtSold = boughtSold(100, 1, 0, 150, 1, 0))
      demand must beLessThan(0.6)
      val demand2 = mm(boughtSold =  boughtSold(100, 1, 0, 0, 1, 0))
      demand2 must beEqualTo(0)
    }
   
   "in-agressive middleman undercuts pricy stuff" in {
      val state = st(150, 10, 10, 3000, 10, 10)
      val d = mm(aggressiveIn = 5, inboundPriceDesired = 100, boughtSold = state)
      d must beLessThan(10.0)
      val notSoAggresive = mm(aggressiveIn = 1, inboundPriceDesired = 100,boughtSold = state)
      notSoAggresive must beGreaterThan(d)
      val nearInfinitelyAggressive = mm(aggressiveIn = 100, inboundPriceDesired = 100, boughtSold = state)
      nearInfinitelyAggressive must beCloseTo(0.0,1)
    }
    
    "out-agressive middleman undercuts cheap stuff" in {      
      val state = st(150, 10, 10, 300, 10, 10)
      val d = mm(aggressiveOut = 5,profitRateDesired = 4,boughtSold = state)
      d must beLessThan(10.0)
      val notSoAggresive = mm(aggressiveOut = 1,profitRateDesired = 4,boughtSold = state)
      notSoAggresive must beGreaterThan(d)
      val nearInfinitelyAggressive =mm(aggressiveOut = 100,profitRateDesired = 4,boughtSold = state)
      nearInfinitelyAggressive must beCloseTo(0.0,1)
    }
  }

}

protected object TestMiddleman {
  def apply(overheadPerTon: Double = 1111.11, purchasedLastTick: Double = 36, wholesalePriceLastTick: Double = 7777.78)(implicit t: Timeline = Timeline(10)) =
    Middleman(MoneyPerTonnes(overheadPerTon), MoneyPerTonnes(5555.56))(t)
}

protected object TestIntermediaryState {
	def apply(bought: Bought, sold: Sold, demand: Tonnes) = IntermediaryState(
			bought = bought, 
			sold = sold, 
			demand = demand,
		    aggressiveInboundPriceSetting  = 2.0,
            aggressiveOutboundPriceSetting = 2.0,
            desiredProfitRate              = 2.0,
            desireToIncreaseMarketShare    = 0d
			)
}



