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
import simulation.unit._
import controllers.initial.InitialValuesUIToModelConfigUI
import controllers.initial.Mindoro
import controllers.initial.InitialValuesUI
import model.QuotaTypeNone

@RunWith(classOf[JUnitRunner])
class MarketSpec extends Specification {

  def slide(price: Double, offer: Double, demand: Double, delta: Double, speed: Double) = {
    import Market.slidePrice
    slidePrice(MoneyPerTonnes(price), Tonnes(offer), Tonnes(demand), Time(delta), PerTime(speed)).v
  }

  "Slide Function" should {
    "slide price function" in {
      slide(100, 10, 10, 1, 1) must beCloseTo(100.0, 0.1) // O=D
      slide(100, 1, 10, 1, 0) must beCloseTo(100, 0.1) // speed = 0
      slide(100, 0, 10, 1, 0) must beCloseTo(100, 0.1) // bad supply 
      slide(100, 1, 0, 1, 0) must beCloseTo(100, 0.1) // bad demand
      slide(100, 1, 10, 1, 1) must beGreaterThan(100.0)
      slide(100, 1, 10, 2, 1) must beGreaterThan(slide(100, 1, 10, 1, 1))
      slide(100, 1, 10, 1, 2) must beGreaterThan(slide(100, 1, 10, 1, 1))
      slide(100, 10, 9, 1, 1) must beLessThan(100.0)
      slide(100, 10, 9, 1, 2) must beLessThan(slide(100, 10, 9, 1, 1))
      slide(100, 10, 9, 2, 1) must beLessThan(slide(100, 10, 9, 1, 1))
      val up10pct = slide(100, 10, 11, .1, .1)
      val down10pct = slide(up10pct, 10, 9, .1, .1)
      down10pct must beCloseTo(100.0, 0.1)
    }
  }

  "Market" should {

    "return initial price when demand equals supply" in {
      val market = TestMarket()
      val fisher = TestFisher()
      val fish = TestFish()
      val totalCatch = 36d
      TestFishFisherRelationship(fish, fisher, null, QuotaTypeNone, Percent(0.5))
      fisher.currentState = TestFisherState(0, Fishermen(20d), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(totalCatch), effortRatePerFisherman)))
      val gradeACatch = totalCatch * (1 - Mindoro.municipalFisher.rSpoilCatch.v - Mindoro.municipalFisher.rOtherCatch.v)
      val middleman = TestMiddleman()
      middleman.currentState = TestIntermediaryState(null, null, Tonnes(gradeACatch))
      market.seller = fisher
      market.buyer = middleman

      val price = 5555.56
      market.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0))), PendingBuy(middleman, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0d))))

      market.tick
      market.updateState

      market.currentState.price.v must beCloseTo(price, 0.0001)
    }

    "return higher price when demand greater than supply" in {
      val market = TestMarket()
      val fisher = TestFisher()
      val fish = TestFish()
      val totalCatch = 36d
      TestFishFisherRelationship(fish, fisher, null, QuotaTypeNone, Percent(0.5))
      fisher.currentState = TestFisherState(0, Fishermen(20d), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(totalCatch), effortRatePerFisherman)))
      val gradeACatch = totalCatch * (1 - Mindoro.municipalFisher.rSpoilCatch.v - Mindoro.municipalFisher.rOtherCatch.v)
      val middleman = TestMiddleman()
      middleman.currentState = TestIntermediaryState(null, null, Tonnes(gradeACatch * 1.5))
      market.seller = fisher
      market.buyer = middleman

      val price = 5555.56
      market.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0))), PendingBuy(middleman, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0))))

      market.tick
      market.updateState
      market.currentState.price.v must beGreaterThan(price)
    }

    "return lower price when demand less than supply" in {
      val market = TestMarket()
      val fisher = TestFisher()
      val fish = TestFish();
      val totalCatch = 36d
      TestFishFisherRelationship(fish, fisher, null, QuotaTypeNone, Percent(0.5))
      fisher.currentState = TestFisherState(0, Fishermen(20d), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(totalCatch), effortRatePerFisherman)))
      val gradeACatch = totalCatch * (1 - Mindoro.municipalFisher.rSpoilCatch.v - Mindoro.municipalFisher.rOtherCatch.v)
      val middleman = TestMiddleman()
      middleman.currentState = TestIntermediaryState(null, null, Tonnes(gradeACatch * .5))
      market.seller = fisher
      market.buyer = middleman

      val price = 5555.56
      market.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0))), PendingBuy(middleman, FinancialTransaction(MoneyPerTonnes(price), Tonnes(gradeACatch), Tonnes(0))))

      market.tick
      market.updateState
      market.currentState.price.v must beLessThan(5655.56)
    }
  }
}

protected object TestMarket {

  def apply(price: Double = 5555.56)(implicit t: Timeline = Timeline(10)) =
    new MiddlemanMarket(PerTime(0.1))(t)
}
