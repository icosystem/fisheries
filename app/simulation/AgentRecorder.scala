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

package simulation

import controllers.initial.InitialValuesUI

import scala.collection.mutable.ArrayBuffer
import simulation.agent._
import model.SimulationRunChartData._
import model.ChartData._
import simulation.unit._

trait AgentRecorder[S] extends Agent[S] {
  val data = ArrayBuffer[S]()

  def now = timeline.now * ΔT.v // LATER might want to change this to Time

  abstract override def gatherState: S = {
    data += currentState
    super.gatherState
  }

}

case class FisherMoney(val time: Double, val revenue: Double, val costOfFishing: Double, val profit: Double, val costOfLiving: Double, val surplus: Double) {

  def totalCost = costOfFishing + costOfLiving
}

trait FishRecorder extends Fish with AgentRecorder[FishState] {
  val sustainableYields = ArrayBuffer[Tonnes]()
  override def gatherState() = {
    sustainableYields += growth

    super.gatherState
  }
}

trait FisherRecorder extends MunicipalFisher with AgentRecorder[MunicipalFisherState] {
  val actualFishingEffortRatePerFishermanValues = ArrayBuffer[FishingPerTimeFishermen]()
  val totalActualFishingEffortRateValues = ArrayBuffer[FishingPerTime]()

  val perFishermanCatchRateValues = ArrayBuffer[TonnesPerTimeFishermen]()
  val totalCatchRateValues = ArrayBuffer[TonnesPerTime]()
  val gradeACatchValues = ArrayBuffer[TonnesPerTime]()
  val spoiledCatchValues = ArrayBuffer[TonnesPerTime]()
  val otherCatchValues = ArrayBuffer[TonnesPerTime]()

  val catchPerUnitEffortValues = ArrayBuffer[TonnesPerFishing]()

  val allFishermenCount = ArrayBuffer[Double]()
  val trainedFishermenCount = ArrayBuffer[Double]()
  val notTrainedFishermenCount = ArrayBuffer[Double]()
  val trainedNonFishermenCount = ArrayBuffer[Double]()

  val perFishermanMoneyValues = ArrayBuffer[FisherMoney]()
  val perTonCaughtMoneyValues = ArrayBuffer[FisherMoney]()
  val nonFishingIncomeValues = ArrayBuffer[MoneyPerTimeFishermen]()
  val bottomLineSurplusPerFishermanValues = ArrayBuffer[MoneyPerTimeFishermen]()
  val totalFishingProfitsRateValues = ArrayBuffer[Double]()
  val perFishermanAlternativeLivelihoodIncome = ArrayBuffer[Double]()
  val maxPossibleAlternativeJobEffortCapacity = ArrayBuffer[Double]()
  val maxPossibleAlternativeTraineeEffortCapacity = ArrayBuffer[Double]()
  val maxPossibleAlternativePopulationEffortCapacity = ArrayBuffer[Double]()
  val totalAlternativeLivelihoodEffort = ArrayBuffer[Double]()
  val per1000AlternativeLivelihoodEffort = ArrayBuffer[Double]()
  val perCapitaTotalMoneyComingIn = ArrayBuffer[Double]()
  val perFishermanSurplusNeededForGoodLife = ArrayBuffer[Double]()
  val perFishermanProfitsNeededForGoodLife = ArrayBuffer[Double]()

  val fishingProfitPerUnitEffort = ArrayBuffer[Double]()
  val biasedPerceptionOfFishingProfitPerUnitEffort = ArrayBuffer[Double]()
  val alternativeProfitPerUnitEffort = ArrayBuffer[Double]()
  val minimumWage = ArrayBuffer[Double]()

  val totalSoldThisTickValues = ArrayBuffer[Sold]()

  val municipalFisherCumulativeStress = ArrayBuffer[Percent]()
  val municipalFisherDailyStress = ArrayBuffer[Percent]()
  val municipalFisherPriceNegotiationState = ArrayBuffer[PriceNegotiationStatus]()
  val municipalFisherPriceNegotiationFairPrice = ArrayBuffer[MoneyPerTonnes]()

  override def gatherState() = {
    allFishermenCount += currentState.numberOfFishermenInPopulation.v
    trainedFishermenCount += currentState.numberOfFishermenTrained.v
    notTrainedFishermenCount += currentState.numberOfFishermenNotTrained.v
    trainedNonFishermenCount += currentState.numberOfNonFishermenTrained.v

    val actualEffortRatePerFisherman = fishingResult.actualEffortRatePerFisherman
    actualFishingEffortRatePerFishermanValues += actualEffortRatePerFisherman
    totalActualFishingEffortRateValues += fishingResult.actualTotalEffortRate
    catchPerUnitEffortValues += fishingResult.catchPerUnitEffort

    val actualTotalCatchRatePerFisherman = fishingResult.actualTotalCatchRatePerFisherman

    val totalCatchRate = fishingResult.totalCatchRate
    totalCatchRateValues += totalCatchRate
    perFishermanCatchRateValues += actualTotalCatchRatePerFisherman

    totalSoldThisTickValues += totalSoldThisTick

    gradeACatchValues += gradeACatchRate(totalCatchRate)
    spoiledCatchValues += spoiledCatchRate(totalCatchRate)
    otherCatchValues += gradeOtherCatchRate(totalCatchRate)

    // computing actual revenues is super tough, we use an approximation, and not even the best one we could make 
    val actualPerFishermanFinancialRates = PerFishermanFinancialRates(approximateActualFishingRevenueRatePerFisherman(fishingResult, totalSoldThisTick, currentState.otherCatchRatio, currentState.otherValueRatio, currentState.numberOfFishermenInPopulation),
      currentState.fixedCostPerFishermanPerYear,
      effortBasedFishingCostRatePerFisherman(actualEffortRatePerFisherman),
      transportBasedFishingCostRatePerFisherman(actualTotalCatchRatePerFisherman),
      alternativeProfitRatePerFisherman(currentState.alternativeEffortPerFishermanPerYear),
      nonFishingIncome,
      currentState.costOfLivingPerFisherman)

    totalFishingProfitsRateValues += (actualPerFishermanFinancialRates.fishingProfits * currentState.numberOfFishermenInPopulation).v
    perFishermanAlternativeLivelihoodIncome += actualPerFishermanFinancialRates.alternativeLivelihoodRevenue.v
    maxPossibleAlternativeJobEffortCapacity += (currentState.alternativeJobWorkload * currentState.alternativeJobsAvailable).v
    maxPossibleAlternativeTraineeEffortCapacity += (currentState.alternativeJobWorkload * currentState.numberOfFishermenTrained).v
    maxPossibleAlternativePopulationEffortCapacity += (currentState.alternativeJobWorkload * currentState.numberOfFishermenInPopulation).v
    totalAlternativeLivelihoodEffort += currentState.alternativeEffortPerFishermanPerYear.v * currentState.numberOfFishermenInPopulation.v
    per1000AlternativeLivelihoodEffort += currentState.alternativeEffortPerFishermanPerYear.v * 1000d
    bottomLineSurplusPerFishermanValues += actualPerFishermanFinancialRates.bottomLine
    nonFishingIncomeValues += nonFishingIncome

    fishingProfitPerUnitEffort += (actualPerFishermanFinancialRates.fishingProfits / actualEffortRatePerFisherman).v
    biasedPerceptionOfFishingProfitPerUnitEffort += (actualPerFishermanFinancialRates.fishingProfits / actualEffortRatePerFisherman).v * culturalFishingChoiceBias
    //    alternativeProfitPerUnitEffort += (actualPerFishermanFinancialRates.fishingProfits / currentState.alternativeEffortPerFishermanPerYear).v // needs division by 0 guard
    alternativeProfitPerUnitEffort += currentState.financialBenefitOfAlternative.v
    minimumWage += InitialValuesUI.minimumWage.v

    perFishermanSurplusNeededForGoodLife += actualPerFishermanFinancialRates.surplusNeededForGoodLife.v
    perFishermanProfitsNeededForGoodLife += actualPerFishermanFinancialRates.profitsNeededForGoodLife.v

    val perFishermanMoney = FisherMoney(now, c(actualPerFishermanFinancialRates.fishingRevenue.v), c(actualPerFishermanFinancialRates.fishingTotalCosts.v), c(actualPerFishermanFinancialRates.fishingProfits.v), c(actualPerFishermanFinancialRates.costOfLiving.v), c(actualPerFishermanFinancialRates.fishingSurplus.v))
    perFishermanMoneyValues += perFishermanMoney
    perTonCaughtMoneyValues += FisherMoney(now, c(perFishermanMoney.revenue / actualTotalCatchRatePerFisherman.v), c(perFishermanMoney.costOfFishing / actualTotalCatchRatePerFisherman.v), c(perFishermanMoney.profit / actualTotalCatchRatePerFisherman.v), c(perFishermanMoney.costOfLiving / actualTotalCatchRatePerFisherman.v), c(perFishermanMoney.surplus / actualTotalCatchRatePerFisherman.v))

    perCapitaTotalMoneyComingIn += actualPerFishermanFinancialRates.fishingProfits.v + actualPerFishermanFinancialRates.alternativeLivelihoodRevenue.v + actualPerFishermanFinancialRates.otherFixedNonFishingIncome.v

    municipalFisherCumulativeStress += Percent(currentState.priceNegotiation.stress)
    municipalFisherDailyStress += Percent(currentState.priceNegotiation.tickStress)
    if (currentState.priceNegotiation.status == null) {
      println("hmmm")
    }
    municipalFisherPriceNegotiationState += currentState.priceNegotiation.status
    municipalFisherPriceNegotiationFairPrice += getFairPrice(fishingResult.catchPerUnitEffort)

    super.gatherState
  }

  // computing actual revenues is super tough, we use an approximation, and not even the best one we could make 
  def approximateActualFishingRevenueRatePerFisherman(fishingResult: FishingResult, totalSoldThisTick: Sold, otherCatchRatio: Percent, otherValueRatio: Percent, numberOfFishermen: Fishermen) = {
    val totalHaulThisTick = fishingResult.totalCatchRate * t.deltaT
    // grade A
    val gradeARevenueAllFishermen = totalSoldThisTick.value / t.deltaT
    // grade A sold as gradeOther because of fishing surplus supply
    val gradeASoldAsOtherRevenueAllFishermen = if (totalSoldThisTick.excess > 0) totalSoldThisTick.excess else Tonnes(0)
    // above + true gradeOther
    val gradeOtherRevenueAllFishermen = (totalHaulThisTick * otherCatchRatio + gradeASoldAsOtherRevenueAllFishermen) * otherValueRatio * totalSoldThisTick.pricePerTon / t.deltaT
    (gradeARevenueAllFishermen + gradeOtherRevenueAllFishermen) / numberOfFishermen
  }

  //    // computing actual revenues is super tough; TODO: replace above w/ smth like below (unfinished), reusing some logic about how revenues are computed for actuals here and expectation in MunicipalFisher 
  //    val soldAsGradeACatchRate = totalSoldThisTick.tons / t.deltaT
  //    val approximateSoldAsGradeACatchRatePerFisherman = soldAsGradeACatchRate / numberOfFishermen // approximation comes from not knowing exact number of fisherman at time sale was made 
  //    val totalGradeACatchRate = totalSoldThisTick.supply / t.deltaT
  //    val approximateTotalAsGradeACatchRatePerFisherman = totalGradeACatchRate / numberOfFishermen // approximation comes from not knowing exact number of fisherman at time sale was made
  //    val approximateRespectiveTotalCatchRatePerFisherman = approximateTotalAsGradeACatchRatePerFisherman / gradeACatchRatio // further approximation since gradeACatchRatio reads from current state, which may have changed since the sale
  //    val approximateActualFishingRevenueRatePerFisherman = revenueRatePerFisherman(soldAsGradeACatchRatePerFisherman, soldAsGradeOtherCatchRatePerFisherman, otherValueRatio, price)

}

case class IntermediaryMoney(val time: Double, val revenue: Double, val costOfValueAdded: Double, val purchaseCost: Double, val totalCost: Double, val profit: Double)

trait IntermediaryRecorder extends Intermediary with AgentRecorder[IntermediaryState] {
  val totalMoney = ArrayBuffer[IntermediaryMoney]()
  val perTonBoughtMoney = ArrayBuffer[IntermediaryMoney]()

  override def gatherState = {

    // TODO Move bookkeeping values (eg currentState.bought) off of currentState and into recording vars or functions to resolve deprecation issues

    val tonsBought = currentState.bought.tons

    val revenue = currentState.sold.value / t.deltaT;
    val costOfValueAdded = (tonsBought * overheadPerTon) / t.deltaT
    val purchaseCost = currentState.bought.value / t.deltaT
    val totalCost = costOfValueAdded + purchaseCost;
    val profit = revenue - totalCost;

    totalMoney += IntermediaryMoney(now, c(revenue.v), c(costOfValueAdded.v), c(purchaseCost.v), c(totalCost.v), c(profit.v))

    val revenuePerTonBought = currentState.sold.value.v / tonsBought.v
    val totalCostPerTonBought = overheadPerTon.v + currentState.bought.pricePerTon.v
    perTonBoughtMoney += IntermediaryMoney(now, c(revenuePerTonBought), c(overheadPerTon.v), c(currentState.bought.pricePerTon.v), c(totalCostPerTonBought), c(revenuePerTonBought - totalCostPerTonBought))

    super.gatherState
  }
}

trait MarketRecorder extends Market with AgentRecorder[MarketState] {
  val supplyData = ArrayBuffer[Double]()
  val demandData = ArrayBuffer[Double]()
  val spoiledData = ArrayBuffer[Double]()
  val totalValue = ArrayBuffer[Double]()

  override def gatherState = {
    supplyData += supply.v / t.deltaT.v
    demandData += demand.v / t.deltaT.v
    spoiledData += math.max(0d, (supply.v - demand.v)) / t.deltaT.v
    totalValue += (currentState.price * min(supply, demand)).v / t.deltaT.v
    super.gatherState
  }

}

