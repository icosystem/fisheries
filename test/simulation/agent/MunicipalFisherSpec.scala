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
import PriceNegotiation.startPriceNegotiation
import model.QuotaTypeNone
import FairPriceCalculator.fairPrice

@RunWith(classOf[JUnitRunner])
class MunicipalFisherSpec extends Specification {
  val ACCEPTABLE = 1000

  "Tests" should {

    "fair price tests" in {
      // Fair price: if make ends meet = $10 / yr
      val makeEndsMeet = 10.0

      // Then we need to make $20/yr for comfy life.
      val comfy = makeEndsMeet * 2

      // if Catch per unit of effort is 2

      val cpue = 2.0

      // and cost of fishing is $1.50 /effort-year

      val costOfFishing = 1.5

      // and effort acceptable per Year is ... something

      val acceptableEffortPerYear = ACCEPTABLE

      // then if we fished exclusively we would catch as many as 

      val tonsPerYear = acceptableEffortPerYear * cpue

      // so to reach comfy we need to sell our tons so that 

      // price * tons per year = comfy + cost of fishing a whole year

      val costOfFishingTheWholeYear = acceptableEffortPerYear * costOfFishing

      val fairPriceShouldBe = (comfy + costOfFishingTheWholeYear) / (tonsPerYear)

      val fairPriceViaMath = fairPrice(
        moneyForHappyLife = MoneyPerTimeFishermen(comfy),
        catchForAcceptableEffort = TonnesPerTimeFishermen(tonsPerYear),
        costForAcceptableEffort = MoneyPerTimeFishermen(costOfFishingTheWholeYear)).v

      fairPriceShouldBe must beCloseTo(fairPriceViaMath, 0.1)

      //
      //      val pn = new PriceNegotiation(
      //        stressThreshold = 0.25, stressDecayTime = Time(365 / 12 * 1) // one month 
      //        , timeToRetry = Time(365) // one year
      //        , adherence = 0.1);
      //
      //      // no intervention, price is fair
      //      pn = pn.update(
      //        interventionActive = true, fishSellingPrice = MoneyPerTonnes(fairPriceShouldBe * 1.1), catchPerUnitOfEffort = TonnesPerTimeFishermen(cpue), indicatedEffort = FishingPerTimeFishermen(1000), profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(10000) //no stress						, indicatedEffort = FishingPerTimeFishermen(1000)
      //        , deltaT = Time(1), costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(makeEndsMeet), costOfFishingPerUnitOfEffort = MoneyPerFishing(costOfFishing), acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      //      pn.interventionEffort.v must beCloseTo(1000, 0.1)
      //      pn.status must beEqualTo('FairPrice);
      //      pn.fairPriceValue.v must beCloseTo(fairPriceShouldBe, 0.1)
      //
      //      pn = pn.update(
      //        interventionActive = true, fishSellingPrice = MoneyPerTonnes(fairPriceShouldBe * 0.9), catchPerUnitOfEffort = TonnesPerTimeFishermen(cpue), profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0) //no stress
      //        , indicatedEffort = FishingPerTimeFishermen(1000), deltaT = Time(1), costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(makeEndsMeet), costOfFishingPerUnitOfEffort = MoneyPerFishing(costOfFishing), acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      //      pn.interventionEffort.v must beCloseTo(100, 0.1)
      //      pn.status must beEqualTo('Active)
      //

      val fish = Fish(carryingCapacity = Tonnes(100000), growthRate = PerTime(1))(Timeline(10))
      val mf = MunicipalFisher(initialNumberOfFishermenInPopulation = Fishermen(1.0),
        effortGrowthCoeff = FishingPerMoney(0.0),
        fishermanPopulationSizeGrowthCoeff = FishermenPerMoney(0.0),
        initialEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE),
        initialAlternativeEffortPerFishermanPerYear = FishingPerTimeFishermen(0.0),
        nonFishingIncome = MoneyPerTimeFishermen(0.0),
        transportationCostPerTon = MoneyPerTonnes(0.0),
        ratioOfInitialPopulationNeverLeaving = Percent(0.0),
        ratioOfMaximumEffortToAcceptableEffort = Percent(0.0),
        ratioOfInitialEffortToAcceptableEffort = Percent(1.0),
        culturalFishingChoiceBias = 2d)(Timeline(10))

      val ffr = MunicipalFishFisherRelationship(fish = fish, fisher = mf, rockefeller = (new RockefellerNoop()(Timeline(10))))(Timeline(10))
      ffr.currentState = FishFisherRelationshipState(catchabilityCoeff = PerFishing(0), quotaType = QuotaTypeNone, quota = Percent(0.5))

      val pn = startPriceNegotiation(
        stressThreshold = 0.25,
        stressDecayTime = Time(365 / 12 * 1), // one month 
        timeToRetry = Time(365), // one year
        target = 1.3,
        adherence = 0,
        maxAdherence = 0.1)

      val mfs = MunicipalFisherState(timeCreated = 10,
        numberOfFishermenInPopulation = Fishermen(10),
        effortPerFishermanPerYear = FishingPerTimeFishermen(10),
        alternativeEffortPerFishermanPerYear = FishingPerTimeFishermen(0),
        costOfLivingPerFisherman = MoneyPerTimeFishermen(makeEndsMeet),
        variableCostPerUnitEffort = MoneyPerFishing(costOfFishing) /* AKA fuel, ice */ ,
        fixedCostPerFishermanPerYear = MoneyPerTimeFishermen(0) /* AKA owning a boat, licensing costs */ ,
        spoiledCatchRatio = Percent(0),
        otherCatchRatio = Percent(0),
        otherValueRatio = Percent(0),
        alternativeJobsAvailable = Fishermen(0),
        alternativeJobWorkload = FishingPerTimeFishermen(0) /* number of hours of alternative work available per year per job */ ,
        numberOfPeopleTrained = Fishermen(0) /* number of people trained */ ,
        numberOfNonFishermenTrained = Fishermen(0) /* number of people trained */ ,
        financialBenefitOfAlternative = MoneyPerFishing(0),
        priceNegotiation = pn,
        priceNegotiationEnabled = true,
        quotaPopulation = Fishermen(10))

      //      val fp = mf.profitsNeededForGoodLife(lastKnownCatchPerUnitEffort = TonnesPerFishing(0.0), lastKnownPrice = MoneyPerTonnes(0.0))

      mf.currentState = mfs

      val municipalFisherFairPrice = mf.getFairPrice(TonnesPerFishing(cpue));

      municipalFisherFairPrice.v must beCloseTo(fairPriceShouldBe, 0.1)

      true
    }
  }

}
