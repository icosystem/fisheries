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

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import simulation.Timeline
import simulation.unit._
import model.QuotaTypeNone

@RunWith(classOf[JUnitRunner])
class FishSpec extends Specification {

  "Fishing calculations" should {

    "keep biomass steady for stable state" in {
      val fish = TestFish()
      val fisher = TestFisher()
      val populationOfFishers = 20d
      TestFishFisherRelationship(fish, fisher, PerFishing(0.02d), QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fish.tick
      fish.updateState

      fish.currentState.biomass.v must beEqualTo(90d)
    }

    "yield a haul of 36 for stable state" in {
      val fish = TestFish()
      val fisher = TestFisher()
      val populationOfFishers = 20d
      TestFishFisherRelationship(fish, fisher, PerFishing(0.02d), QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fish.tick
      fish.updateState

      fish.currentState.results(0).totalCatchRate.v must beEqualTo(36d)
    }

    "reduce biomass by 36 for 40 effort" in {

      val fish = TestFish()
      val fisher = TestFisher(populationOfFishers = 40d)
      TestFishFisherRelationship(fish, fisher, PerFishing(0.02d), QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.currentState = TestFisherState(0, Fishermen(40d), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fish.tick
      fish.updateState

      fish.currentState.biomass.v must beEqualTo(54d)
    }
  }
}

protected object TestFish {

  def apply(carryingCapacity: Double = 100d, biomass: Double = 90d, growthRate: Double = 4d)(implicit t: Timeline = Timeline(10)) =
    Fish(Tonnes(carryingCapacity), PerTime(growthRate))(t)

}
