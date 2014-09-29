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
import CumulativeExposureFunction.cumulativeExposure
import PriceNegotiation.startPriceNegotiation

@RunWith(classOf[JUnitRunner])
class PriceNegotiationSpec extends Specification {
  val ACCEPTABLE = 1000

  "Tests" should {
    "stress tests without intervention" in {
      var pn = startPriceNegotiation(
        stressThreshold = 0.25,
        stressDecayTime = Time(365 / 12 * 1), // one month 
        timeToRetry = Time(365), // one year
        target = 1.3,
        adherence = 0,
        maxAdherence = 0.1);

      // Making ends meet, no tick stress, even though price might not be fair
      var expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(100),
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(50),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(0),
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(20))

      for (i <- 1 to 100) {
        pn = pn.update(interventionActive = false,
          indicatedEffort = FishingPerTimeFishermen(1000),
          deltaT = Time(1),
          expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
          isPriceFair = false,
          //          fishSellingPrice = MoneyPerTonnes(25),
          //          catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
          //          profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(100), // makes ends meet
          //          costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
          //          costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
          acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      }
      pn.stress must beCloseTo(0.0, 0.001);

      // Combined effort not profitable, max stress per tick
      expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(100),
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(150),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(0),
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(20))
      for (i <- 1 to 365 / 12) {
        pn = pn.update(
          interventionActive = false,
          indicatedEffort = FishingPerTimeFishermen(1000),
          deltaT = Time(1),
          expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
          isPriceFair = false,
          //          fishSellingPrice = MoneyPerTonnes(25),
          //          catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
          //          profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0), // makes ends meet
          //          costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
          //          costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
          acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      }
      // Build up to 50% in one month
      pn.stress must beCloseTo(0.5, 0.001);

      // And close to 1 by the end of the year
      for (i <- 1 to (365.0 * (11.0 / 12.0)).toInt) {
        pn = pn.update(
          interventionActive = false,
          indicatedEffort = FishingPerTimeFishermen(1000),
          deltaT = Time(1),
          expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
          isPriceFair = false,
          //          fishSellingPrice = MoneyPerTonnes(25),
          //          catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
          //          profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0), //no income
          //          costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
          //          costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
          acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      }
      pn.stress must beCloseTo(1.0, 0.001);
    }

    "active interventions and timeouts" in {
      var pn = startPriceNegotiation(
        stressThreshold = 0.25,
        stressDecayTime = Time(365 / 12 * 1), // one month 
        timeToRetry = Time(365), // one year
        target = 1.3,
        adherence = 0.1,
        maxAdherence = 0.1);

      // Making ends meet, no tick stress
      var expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(0),
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(50 * ACCEPTABLE),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(0),
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(100))
      var expectedFinancialsWithAcceptableEffort = expectedFinancialsWithCurrentEffort
      // no intervention, effort unchanged
      pn = pn.update(
        interventionActive = false,
        indicatedEffort = FishingPerTimeFishermen(1000),
        deltaT = Time(1),
        expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
        isPriceFair = false,
        //        fishSellingPrice = MoneyPerTonnes(25),
        //        catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
        //        profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0), //no income
        //        costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
        //        costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
        acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      pn.interventionEffort.v must beCloseTo(1000.0, 0.001);
      pn.status must beEqualTo(Inactive)

      // not making ends meet & price unfair
      expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(0),
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(50 * ACCEPTABLE),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(0),
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(100))
      expectedFinancialsWithAcceptableEffort = expectedFinancialsWithCurrentEffort
      pn = pn.update(
        interventionActive = true,
        indicatedEffort = FishingPerTimeFishermen(1000),
        deltaT = Time(1),
        expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
        isPriceFair = false,
        //        fishSellingPrice = MoneyPerTonnes(0), // price not fair
        //        catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
        //        profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0), // no income
        //        costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
        //        costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
        acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      pn.interventionEffort.v must beLessThan(1000.0);
      pn.status must beEqualTo(Active)

      var aborted = false
      expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(0),
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(50 * ACCEPTABLE),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(0),
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(100))
      expectedFinancialsWithAcceptableEffort = expectedFinancialsWithCurrentEffort
      for (i <- 1 to 30) {
        pn = pn.update(
          interventionActive = true,
          indicatedEffort = FishingPerTimeFishermen(1000),
          deltaT = Time(1),
          expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
          isPriceFair = false,
          //          fishSellingPrice = MoneyPerTonnes(0), // price not fair
          //          catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
          //          profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0), // no income
          //          costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
          //          costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
          acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
        if (pn.status.equals(Abort)) aborted = true;
        if (!aborted) pn.interventionEffort.v must beCloseTo(ACCEPTABLE * 0.9, 0.1);
      }
      aborted must beTrue;
      pn.status must beEqualTo(Waiting);
      pn.interventionEffort.v must beCloseTo(1000.0, 0.001);
      pn.timeSinceLastAborted must beGreaterThan(0.0);
      pn.timeSinceLastAborted must beLessThan(30.0);

      // Test reset period
      expectedFinancialsWithCurrentEffort = PerFishermanFinancialRates(
        fishingRevenue = MoneyPerTimeFishermen(50 * ACCEPTABLE), // just covering fishing costs
        fishingFixedCosts = MoneyPerTimeFishermen(0),
        fishingEffortBasedCosts = MoneyPerTimeFishermen(50 * ACCEPTABLE),
        fishingCatchBasedCosts = MoneyPerTimeFishermen(0), // for now just transport
        alternativeLivelihoodRevenue = MoneyPerTimeFishermen(90), // making almost as much money to cover living costs, so minimum tick stress
        otherFixedNonFishingIncome = MoneyPerTimeFishermen(0),
        costOfLiving = MoneyPerTimeFishermen(100))
      expectedFinancialsWithAcceptableEffort = expectedFinancialsWithCurrentEffort
      for (i <- 1 to 365) {
        pn = pn.update(
          interventionActive = true,
          indicatedEffort = FishingPerTimeFishermen(1000),
          deltaT = Time(1),
          expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
          isPriceFair = false,
          //          fishSellingPrice = MoneyPerTonnes(0), // price not fair
          //          catchPerUnitOfEffort = TonnesPerTimeFishermen(10),
          //          profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(90), // minimum stress
          //          costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(100),
          //          costOfFishingPerUnitOfEffort = MoneyPerFishing(50),
          acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
      }
      pn.timeSinceLastAborted should beGreaterThan(365.0)
      pn.timeSinceLastAborted should beLessThan(365.0 + 30.0)
      pn.status must beEqualTo(Active)
      pn.interventionEffort.v must beCloseTo(ACCEPTABLE * 0.9, 0.1);
    }

    //    "fair price tests" in {
    //      // Fair price: if make ends meet = $10 / yr
    //      val makeEndsMeet = 10.0
    //
    //      // Then we need to make $20/yr for comfy life.
    //      val comfy = makeEndsMeet * 2
    //
    //      // if Catch per unit of effort is 2
    //
    //      val cpue = 2.0
    //
    //      // and cost of fishing is $1.50 /effort-year
    //
    //      val costOfFishing = 1.5
    //
    //      // and effort acceptable per Year is ... something
    //
    //      val acceptableEffortPerYear = ACCEPTABLE
    //
    //      // then if we fished exclusively we would catch as many as 
    //
    //      val tonsPerYear = acceptableEffortPerYear * cpue
    //
    //      // so to reach comfy we need to sell our tons so that 
    //
    //      // price * tons per year = comfy + cost of fishing a whole year
    //
    //      val costOfFishingTheWholeYear = acceptableEffortPerYear * costOfFishing
    //
    //      val fairPriceShouldBe = (comfy + costOfFishingTheWholeYear) / (tonsPerYear)
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
    //      pn.status must beEqualTo(FairPrice);
    //      pn.fairPriceValue.v must beCloseTo(fairPriceShouldBe, 0.1)
    //
    //      pn = pn.update(
    //        interventionActive = true, fishSellingPrice = MoneyPerTonnes(fairPriceShouldBe * 0.9), catchPerUnitOfEffort = TonnesPerTimeFishermen(cpue), profitFromCombinedEffortPerCapitaPerYear = MoneyPerTimeFishermen(0) //no stress
    //        , indicatedEffort = FishingPerTimeFishermen(1000), deltaT = Time(1), costOfLivingPerFishermanPerYear = MoneyPerTimeFishermen(makeEndsMeet), costOfFishingPerUnitOfEffort = MoneyPerFishing(costOfFishing), acceptableEffortPerFishermanPerYear = FishingPerTimeFishermen(ACCEPTABLE));
    //      pn.interventionEffort.v must beCloseTo(100, 0.1)
    //      pn.status must beEqualTo(Active)
    //
    //      1 must beEqualTo(1)
    //    }
  }

}
