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

import scala.collection.mutable.ArrayBuffer
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import simulation.Timeline
import simulation.FisherySimulation
import simulation.unit._
import model.QuotaTypeNone
import controllers.initial.InitialValuesUIToModelConfigUI
import controllers.initial.Mindoro
import simulation.agent.PriceNegotiation.startPriceNegotiation
import model.QuotaType

@RunWith(classOf[JUnitRunner])
class FisherSpec extends Specification {

  "Population of Fishers" should {

    skipAll

    "stay the same in a steady state" in {
      val populationOfFishers = 20d
      val haul = 36d
      val price = 5555.56
      val fish = TestFish()
      val fisher = TestFisher(populationOfFishers = populationOfFishers)

      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.middlemanMarket = TestMarket(price = price)
      fisher.middlemanMarket.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(36d), Tonnes(0))), null)
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fisher.tick
      fisher.updateState

      fisher.currentState.numberOfFishermenInPopulation.v must beCloseTo(populationOfFishers, 0.001)
    }

    "increase when price increases" in {
      val populationOfFishers = 20d
      val haul = 36d
      val price = 7000d
      val fisher = TestFisher(populationOfFishers = populationOfFishers)
      val fish = TestFish()
      val c = InitialValuesUIToModelConfigUI(Mindoro).baseline.municipalFisher

      TestFishFisherRelationship(fish, fisher, c.catchabilityCoeff, QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.middlemanMarket = TestMarket(price = price)
      fisher.middlemanMarket.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(36d), Tonnes(0))), null)
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fisher.tick
      fisher.updateState

      fisher.currentState.numberOfFishermenInPopulation.v must beGreaterThanOrEqualTo(populationOfFishers.v)
    }

    "decrease when price decreases" in {
      val populationOfFishers = 30d
      val haul = 36d
      val price = 4000d
      val fisher = TestFisher(populationOfFishers = 20d)
      val fish = TestFish()
      val c = InitialValuesUIToModelConfigUI(Mindoro).baseline.municipalFisher

      TestFishFisherRelationship(fish, fisher, c.catchabilityCoeff, QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.middlemanMarket = TestMarket(price = price)
      fisher.middlemanMarket.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(36d), Tonnes(0))), null)
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fisher.tick
      fisher.updateState

      fisher.currentState.numberOfFishermenInPopulation.v must beLessThanOrEqualTo(populationOfFishers.v)
    }

    "not decrease below initial effort, even if they lose money" in {
      val populationOfFishers = 20d
      val haul = 36d
      val price = 4000d
      val fisher = TestFisher(populationOfFishers = populationOfFishers)
      val fish = TestFish()
      val c = InitialValuesUIToModelConfigUI(Mindoro).baseline.municipalFisher

      TestFishFisherRelationship(fish, fisher, c.catchabilityCoeff, QuotaTypeNone, Percent(0.5))
      val effortRatePerFisherman = FishingPerTime(10) / fisher.initialNumberOfFishermenInPopulation
      fish.currentState = FishState(Tonnes(90d), Seq(FishingResult(0, fisher, IntendedGroupEffort(-1, effortRatePerFisherman, fisher.initialNumberOfFishermenInPopulation), TonnesPerTime(36d), effortRatePerFisherman)))
      fisher.middlemanMarket = TestMarket(price = price)
      fisher.middlemanMarket.currentState = MarketState(MoneyPerTonnes(price), PendingSell(fisher, FinancialTransaction(MoneyPerTonnes(price), Tonnes(36d), Tonnes(0))), null)
      fisher.currentState = TestFisherState(0, Fishermen(populationOfFishers), FishingPerTimeFishermen(1), FishingPerTimeFishermen(0))

      fisher.tick
      fisher.updateState

      //      fisher.fishingSurplusPerFishermanPerYear(fisher.currentState.effortPerFishermanPerYear, fisher.fishingResult.totalCatchRate * t.deltaT, fisher.currentState.numberOfFishermenInPopulation, fisher.totalSoldThisTick).v must beLessThan(0d)
      fisher.currentState.numberOfFishermenInPopulation.v must beCloseTo(populationOfFishers, 0.001)
    }
  }
}

protected object TestFisher {
  val c = InitialValuesUIToModelConfigUI(Mindoro).baseline.municipalFisher

  def apply(populationOfFishers: Double = 20d)(implicit t: Timeline = Timeline(10)) = {

    MunicipalFisher(Fishermen(populationOfFishers),
      c.effortGrowthCoeff,
      c.populationSizeGrowthCoeff,
      c.effortPerFishermanPerYear,
      c.alternativeEffortPerFishermanPerYear,
      c.nonFishingIncome,
      MoneyPerTonnes(0),
      c.ratioOfInitialPopulationNeverLeaving,
      c.ratioOfMaximumEffortToAcceptableEffort,
      c.ratioOfInitialEffortToAcceptableEffort,
      c.alternativeLivelihood.culturalFishingChoiceBias)
  }
}

protected object TestFisherState {
  val c = TestFisher.c

  def apply(timeCreated: Int, numberOfFishermenInPopulation: Fishermen, effortPerFishermanPerYear: FishingPerTimeFishermen, alternativeEffortPerFishermanPerYear: FishingPerTimeFishermen, costOfLivingPerFisher: Double = 10000) =
    MunicipalFisherState(timeCreated,
      numberOfFishermenInPopulation,
      effortPerFishermanPerYear,
      alternativeEffortPerFishermanPerYear,
      MoneyPerTimeFishermen(costOfLivingPerFisher),
      c.variableCostPerUnitEffort,
      c.fixedCostPerFisherman,
      c.spoiledCatchRatio,
      c.otherCatchRatio,
      c.otherValueRatio,
      c.alternativeLivelihood.alternativeJobsAvailable,
      c.alternativeLivelihood.alternativeJobWorkload,
      c.alternativeLivelihood.numberOfPeopleTrained,
      c.alternativeLivelihood.numberOfNonFishermenTrained,
      c.alternativeLivelihood.financialBenefit,
      startPriceNegotiation(stressThreshold = 0.25, stressDecayTime = Time(365 / 12 * 1) /* one month */ , timeToRetry = Time(365) /* one year  */ , target = 1.3, adherence = 0, maxAdherence = 0.1),
      false,
      numberOfFishermenInPopulation)
}

object TestFishFisherRelationship {
  def apply(fish: Fish, fisher: Fisher[_], catchabilityCoeff: PerFishing, quotaType: QuotaType, quota: Percent)(implicit t: Timeline = Timeline(10)): FishFisherRelationship = {
    val r = MunicipalFishFisherRelationship(fish, fisher, new RockefellerNoop)
    r.currentState = FishFisherRelationshipState(catchabilityCoeff, quotaType, quota)
    r
  }
}
