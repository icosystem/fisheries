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

package model

import simulation.SimulationResults
import com.icosystem.collections._
import scala.collection.mutable.ArrayBuffer
import simulation.agent.PriceNegotiationStatus
import simulation.agent.Inactive
import simulation.agent.FairPrice
import simulation.agent.Active
import simulation.agent.Waiting
import simulation.agent.Abort

class RawSeriesData(simResults: SimulationResults, aggSize: Int) {
  import ChartData._

  private def last[T](s: Seq[T]) = s.last
  private def avg(s: Seq[Double]) = s.sum / s.size

  // Scalars
  val ΔT = simResults.t.deltaT.v
  val xAtMSY = coerce(simResults.fish.carryingCapacity.v / 2d)
  val msy = coerce(simResults.fish.msy)

  // Columns (a.k.a data series)
  val timeCol = col("t", (0 to simResults.t.end).map(_ * ΔT)).aggregate(aggSize, last)

  // for fish charts
  val fishBiomassAtMSYCol = col("Stock At MSY", Vector.fill(simResults.t.end + 1)(xAtMSY)).aggregate(aggSize, last)
  val fishMSYCol = col("MSY", Vector.fill(simResults.t.end + 1)(msy)).aggregate(aggSize, last)
  val fishSYCol = col("Sustainable Yield (SY)", simResults.fish.sustainableYields.map(_.v / ΔT)).aggregate(aggSize, last)
  val fishBiomassCol = col("Stock", simResults.fishBiomass).aggregate(aggSize, last)
  val municipalFisherHaul = simResults.municipalFisher.totalCatchRateValues.map(_.v)
  val municipalFisherGradeAHaul = simResults.municipalFisher.gradeACatchValues.map(_.v)
  val commercialFisherHaul = simResults.commercialFisher.data.map(_.haul.v / ΔT)
  val municipalFisherCatchCol = col("Municipal Catch", municipalFisherHaul).aggregate(aggSize, avg)
  val commercialFishCatchCol = col("Commercial Catch", commercialFisherHaul).aggregate(aggSize, avg)

  val middlemanMarketFishSpoiledCol = col(name = "Local Spoil", simResults.middlemanMarket.spoiledData).aggregate(aggSize, avg)
  val exporterMarketFishSpoiledCol = col(name = "Wholesale Spoil", simResults.exporterMarket.spoiledData).aggregate(aggSize, avg)
  val consumerMarketFishSpoiledCol = col(name = "Consumer Spoil", simResults.consumerMarket.spoiledData).aggregate(aggSize, avg)
  val municipalFisherGradeACatchCol = col(name = "Grade A Catch", simResults.municipalFisher.gradeACatchValues.map(_.v)).aggregate(aggSize, avg)
  val totalFishCatchCol = col(name = "Total Catch", (municipalFisherCatchCol, commercialFishCatchCol).zipped.map(_ + _))

  // for municipalFisher charts
  val municipalFisherTotalCatchCol = col("Total Caught", municipalFisherHaul).aggregate(aggSize, avg)
  val municipalFisherTotalCatchCol2 = col("Total", municipalFisherHaul).aggregate(aggSize, avg)
  val municipalFisherGradeACaughtCol = col("Grade A Caught", municipalFisherGradeAHaul).aggregate(aggSize, avg)
  val municipalFisherSold = simResults.municipalFisher.totalSoldThisTickValues.map(_.tons.v / ΔT)
  val municipalFisherSoldCol = col("Grade A Sold", municipalFisherSold).aggregate(aggSize, avg)
  val municipalFisherPopulation = simResults.municipalFisher.allFishermenCount
  val municipalFisherPerCapitaCatchCol = col("Total Caught", simResults.municipalFisher.perFishermanCatchRateValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaCatchCol2 = col("Per 1000 households", simResults.municipalFisher.perFishermanCatchRateValues.map(_.v * 1000)).aggregate(aggSize, avg)
  val municipalFisherCatchPerUnitEffortCol = col("", simResults.municipalFisher.catchPerUnitEffortValues.map(_.v * 1000 * 100)).aggregate(aggSize, avg) // (tonnes/hr) * (1000 kg/tonne) * (100hrs)
  val municipalFisherPerCapitaSoldCol = col("Grade A Sold", (municipalFisherSold zip municipalFisherPopulation).map(v => v._1 / v._2)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaRevenueCol = col("Fishing Revenue", simResults.municipalFisher.perFishermanMoneyValues.map(_.revenue)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaCostOfFishingCol = col("Cost of Fishing", simResults.municipalFisher.perFishermanMoneyValues.map(_.costOfFishing)).aggregate(aggSize, avg)
  val municipalFisherFishingPerCapitaProfitCol = col("Fishing Profit", simResults.municipalFisher.perFishermanMoneyValues.map(_.profit)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaCostOfLivingCol = col("Cost of Living", simResults.municipalFisher.perFishermanMoneyValues.map(_.costOfLiving)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaAlternativeLivelihoodIncomeCol = col("Alt. Liv. Income", simResults.municipalFisherAlternativeLivelihoodIncome).aggregate(aggSize, avg)
  val municipalFisherMaxPossibleAlternativeJobEffortCapacityCol = col("Max. Jobs Capacity", simResults.municipalFisher.maxPossibleAlternativeJobEffortCapacity).aggregate(aggSize, avg)
  val municipalFisherMaxPossibleAlternativeTraineeEffortCapacityCol = col("Max. Trainee Capacity", simResults.municipalFisher.maxPossibleAlternativeTraineeEffortCapacity).aggregate(aggSize, avg)
  val municipalFisherMaxPossibleAlternativePopulationEffortCapacityCol = col("Max. Population Capacity", simResults.municipalFisher.maxPossibleAlternativePopulationEffortCapacity).aggregate(aggSize, avg)
  val municipalFisherTotalAlternativeLivelihoodEffortCol = col("Total", simResults.municipalFisher.totalAlternativeLivelihoodEffort).aggregate(aggSize, avg)
  val municipalFisherTotalAlternativeLivelihoodEffortCol4Stack = col("Alternative", simResults.municipalFisher.totalAlternativeLivelihoodEffort).aggregate(aggSize, avg)
  val municipalFisherPer1000HouseholdsAlternativeLivelihoodEffortCol = col("Per 1000 households", simResults.municipalFisher.per1000AlternativeLivelihoodEffort).aggregate(aggSize, avg)
  val municipalFisherPerCapitaAlternativeLivelihoodEffortCol4Stack = col("Alternative", simResults.municipalFisher.per1000AlternativeLivelihoodEffort.map(_ / 1000)).aggregate(aggSize, avg)
  val municipalFisherPerCapitaTotalMoneyComingInCol = col("Total $ Coming In", simResults.municipalFisher.perCapitaTotalMoneyComingIn).aggregate(aggSize, avg)
  val municipalFisherFishingPerCapitaSurplusCol = col("Fishing Surplus", simResults.municipalFisher.perFishermanMoneyValues.map(_.surplus)).aggregate(aggSize, avg)
  val municipalFisherNonFishingIncome = col("Other Fixed Income", simResults.municipalFisher.nonFishingIncomeValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherBottomLinePerCapitaSurplusCol = col("Bottom Line Surplus", simResults.municipalFisher.bottomLineSurplusPerFishermanValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherPopulationCol = col("Families", municipalFisherPopulation).aggregate(aggSize, last)
  val municipalFisherNonTrainedFishermenCol = col("Untrained fishermen", simResults.municipalFisher.notTrainedFishermenCount).aggregate(aggSize, last)
  val municipalFisherTrainedFishermenCol = col("Trained fishermen", simResults.municipalFisher.trainedFishermenCount).aggregate(aggSize, last)
  val municipalFisherTrainedNonFishermenCol = col("Trained former fishermen", simResults.municipalFisher.trainedNonFishermenCount).aggregate(aggSize, last)
  val municipalFisherGradeACatch = col("Grade A", municipalFisherGradeAHaul).aggregate(aggSize, avg)
  val municipalFisherSpoiledCatch = col("Spoiled", simResults.municipalFisher.spoiledCatchValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherOtherCatch = col("Grade Other", simResults.municipalFisher.otherCatchValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherFishingProfitabilityCol = col("Fishing", simResults.municipalFisher.fishingProfitPerUnitEffort).aggregate(aggSize, avg)
  val municipalFisherBiasedPerceptionOfFishingProfitabilityCol = col("Biased Fishing Perception", simResults.municipalFisher.biasedPerceptionOfFishingProfitPerUnitEffort).aggregate(aggSize, avg)
  val municipalFisherAlternativeProfitabilityCol = col("Alternative", simResults.municipalFisher.alternativeProfitPerUnitEffort).aggregate(aggSize, avg)
  val municipalFisherMinimumWageCol = col("Min. Wage", simResults.municipalFisher.minimumWage).aggregate(aggSize, avg)
  val municipalFisherPerCapitaFishingEffortCol4Stack = col("Fishing", simResults.municipalFisher.actualFishingEffortRatePerFishermanValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherPer1000HouseholdsFishingEffortCol = col("Per 1000 households", simResults.municipalFisher.actualFishingEffortRatePerFishermanValues.map(_.v * 1000)).aggregate(aggSize, avg)
  val municipalFisherTotalFishingEffortCol = col("Total", simResults.municipalFisher.totalActualFishingEffortRateValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherTotalFishingEffortCol4Stack = col("Fishing", simResults.municipalFisher.totalActualFishingEffortRateValues.map(_.v)).aggregate(aggSize, avg)
  val municipalFisherCumulativeStressCol = col("Cumulative Stress", simResults.municipalFisher.municipalFisherCumulativeStress.map(_.to0to100)).aggregate(aggSize, avg)
  val municipalFisherDailyStressCol = col("Daily Stress", simResults.municipalFisher.municipalFisherDailyStress.map(_.to0to100)).aggregate(aggSize, avg)

  private val compressFisherPriceNegotiation = ChartData.compressStatusColumns((0 to simResults.t.end).map(_ * ΔT), simResults.municipalFisher.municipalFisherPriceNegotiationState)
  val municipalFisherPriceNegotiationStatusTimeCol = col("t", compressFisherPriceNegotiation.time)
  val municipalFisherPriceNegotiationStatusInactiveCol = col("Inactive", compressFisherPriceNegotiation.inactive)
  val municipalFisherPriceNegotiationStatusActiveCol = col("Active", compressFisherPriceNegotiation.active)
  val municipalFisherPriceNegotiationStatusWaitingCol = col("Waiting", compressFisherPriceNegotiation.waiting)
  val municipalFisherPriceNegotiationStatusAbortCol = col("Abort", compressFisherPriceNegotiation.abort)
  val municipalFisherPriceNegotiationStatusFairPriceCol = col("Fair Price", compressFisherPriceNegotiation.fairPrice)

  val municipalFisherPriceNegotiationFairPriceCol = col("Fair Price", simResults.municipalFisher.municipalFisherPriceNegotiationFairPrice.map(_.v)).aggregate(aggSize, avg)

  // for middleman market charts
  val middlemanMarketOfferCol = col("Offer", simResults.middlemanMarket.supplyData).aggregate(aggSize, avg)
  val middlemanMarketDemandCol = col("Demand", simResults.middlemanMarket.demandData).aggregate(aggSize, avg)
  val middlemanMarketTradedCol = col("Traded", simResults.middlemanMarket.data.map(_.volume.v / ΔT)).aggregate(aggSize, avg)
  val middlemanMarketPriceCol = col("Price", simResults.middlemanMarket.data.map(_.price.v)).aggregate(aggSize, last)
  val middlemanMarketValueCol = col("Value", simResults.middlemanMarket.totalValue).aggregate(aggSize, avg)

  // for middleman charts
  val middleManBoughtCol = col("Bought", simResults.middleman.data.map(_.bought.tons.v / ΔT)).aggregate(aggSize, avg)
  val middleManSoldCol = col("Sold", simResults.middleman.data.map(_.sold.tons.v / ΔT)).aggregate(aggSize, avg)
  val middleManProfitCol = col("Profit", simResults.middlemanTotalProfits).aggregate(aggSize, avg)

  // for exporter market charts
  val exporterMarketOfferCol = col("Offer", simResults.exporterMarket.supplyData).aggregate(aggSize, avg)
  val exporterMarketDemandCol = col("Demand", simResults.exporterMarket.demandData).aggregate(aggSize, avg)
  val exporterMarketTradedCol = col("Traded", simResults.exporterMarket.data.map(_.volume.v / ΔT)).aggregate(aggSize, avg)
  val exporterMarketPriceCol = col("Price", simResults.exporterMarket.data.map(_.price.v)).aggregate(aggSize, last)
  val exporterMarketValueCol = col("Value", simResults.exporterMarket.totalValue).aggregate(aggSize, avg)

  // for exporter charts
  val exporterBoughtCol = col("Bought", simResults.exporter.data.map(_.bought.tons.v / ΔT)).aggregate(aggSize, avg)
  val exporterSoldCol = col("Sold", simResults.exporter.data.map(_.sold.tons.v / ΔT)).aggregate(aggSize, avg)
  val exporterProfitsCol = col("Profit", simResults.exporterTotalProfits).aggregate(aggSize, avg)

  // for consumer market charts
  val consumerMarketOfferCol = col("Offer", simResults.consumerMarket.supplyData).aggregate(aggSize, avg)
  val consumerMarketDemandCol = col("Demand", simResults.consumerMarket.demandData).aggregate(aggSize, avg)
  val consumerMarketTradedCol = col("Traded", simResults.consumerMarket.data.map(_.volume.v / ΔT)).aggregate(aggSize, avg)
  val consumerMarketPriceCol = col("Price", simResults.consumerMarket.data.map(_.price.v)).aggregate(aggSize, last)
  val consumerMarketValueCol = col("Value", simResults.consumerMarket.totalValue).aggregate(aggSize, avg)

  // for consumer charts
  val consumerPopulationCol = col("", simResults.consumer.data.map(_.population.v)).aggregate(aggSize, last)
  val consumerTotalDemandCol = col("Total Demand", simResults.consumer.data.map(_.demand.v / ΔT)).aggregate(aggSize, avg)
  val consumerDemandPer1000Col = col("Demand per 1,000 Consumers", simResults.consumer.data.map(d => 1000 * (d.demandPerCapita.v / ΔT))).aggregate(aggSize, avg)
  val consumerConsumptionCol = col("Consumption", simResults.consumerMarket.data.map(_.volume.v / ΔT)).aggregate(aggSize, avg)
  val consumerConsumptionPer1000Col = col("Consumption per 1,000 Consumers", simResults.consumer.data.map(_.population.v).zip(simResults.consumerMarket.data.map(_.volume.v)).map(v => (1000 * v._2) / (v._1 * ΔT) )).aggregate(aggSize, avg)

  // for charts combining multiple agents
  val fisherProfitPerTonCaughtCol = col("Fisher Profit", simResults.municipalFisher.perTonCaughtMoneyValues.map(_.profit)).aggregate(aggSize, avg)
  val middlemanProfitPerTonBoughtCol = col(name = "Middleman Profit", simResults.middleman.perTonBoughtMoney.map(_.profit)).aggregate(aggSize, avg)
  val exporterProfitPerTonBoughtCol = col(name = "Exporter Profit", simResults.exporter.perTonBoughtMoney.map(_.profit)).aggregate(aggSize, avg)
  val fisherCostPerTonCaughtCol = col("Fisher Added Value Cost", simResults.municipalFisher.perTonCaughtMoneyValues.map(_.costOfFishing)).aggregate(aggSize, avg)
  val middlemanCostPerTonBoughtCol = col("Middleman Added Value Cost", simResults.middleman.perTonBoughtMoney.map(_.costOfValueAdded)).aggregate(aggSize, avg)
  val exporterCostPerTonBoughtCol = col("Exporter Added Value Cost", simResults.exporter.perTonBoughtMoney.map(_.costOfValueAdded)).aggregate(aggSize, avg)

  val totalProfitAndCostsPerTon = fisherProfitPerTonCaughtCol.values zip fisherCostPerTonCaughtCol zip middlemanProfitPerTonBoughtCol zip exporterProfitPerTonBoughtCol zip middlemanCostPerTonBoughtCol zip exporterCostPerTonBoughtCol map (Tuples.flatten(_)) map (t => t._1 + t._2 + t._3 + t._4 + t._5 + t._6)

  val fisherPercentProfitPerTonCaughtCol = col("Fisher Profit", fisherProfitPerTonCaughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))
  val middlemanPercentProfitPerTonBoughtCol = col(name = "Middleman Profit", middlemanProfitPerTonBoughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))
  val exporterPercentProfitPerTonBoughtCol = col(name = "Exporter Profit", exporterProfitPerTonBoughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))
  val fisherPercentCostPerTonCaughtCol = col("Fisher Added Value Cost", fisherCostPerTonCaughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))
  val middlemanPercentCostPerTonBoughtCol = col("Middleman Added Value Cost", middlemanCostPerTonBoughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))
  val exporterPercentCostPerTonBoughtCol = col("Exporter Added Value Cost", exporterCostPerTonBoughtCol.values zip totalProfitAndCostsPerTon map (f => 100.0 * f._1 / f._2))

  val fisherProfitCaughtCol = col("Fisher Profit", simResults.municipalFisher.totalFishingProfitsRateValues).aggregate(aggSize, avg)
  val middlemanProfitBoughtCol = col(name = "Middleman Profit", simResults.middleman.totalMoney.map(_.profit)).aggregate(aggSize, avg)
  val exporterProfitBoughtCol = col(name = "Exporter Profit", simResults.exporter.totalMoney.map(_.profit)).aggregate(aggSize, avg)
}

object ChartData {
  import com.icosystem.math._

  def col(name: String, vals: IndexedSeq[Double]) = Column(name, vals.map(v => d3(coerce(v))))

  def colString(name: String, vals: IndexedSeq[String]) = Column(name, vals)

  def c(n: Double) = coerce(n)

  def coerce(n: Double) = if (n.isNaN() || n.isInfinite()) -1d else n

  def compressStatusColumns(time: IndexedSeq[Double], data: IndexedSeq[PriceNegotiationStatus]) = {

    val newTime = new ArrayBuffer[Double]()
    val inactive = new ArrayBuffer[Double]()
    val active = new ArrayBuffer[Double]()
    val waiting = new ArrayBuffer[Double]()
    val abort = new ArrayBuffer[Double]()
    val fairPrice = new ArrayBuffer[Double]()

    if (time.isEmpty) {
      CompressedStatuses(time = newTime.toIndexedSeq, inactive = inactive.toIndexedSeq, active = active.toIndexedSeq, waiting = waiting.toIndexedSeq, abort = abort.toIndexedSeq, fairPrice = fairPrice.toIndexedSeq)
    } else {

      newTime += time(0)
      inactive += Inactive.numberOrNeg1(data(0))
      active += Active.numberOrNeg1(data(0))
      waiting += Waiting.numberOrNeg1(data(0))
      abort += Abort.numberOrNeg1(data(0))
      fairPrice += FairPrice.numberOrNeg1(data(0))

      if (time.length > 1) {
        for (i <- 1 to time.length - 2) {
          var previousStatus = data(i - 1)
          var currentStatus = data(i)
          var nextStatus = data(i + 1)

          if (previousStatus != currentStatus || currentStatus != nextStatus) {
            newTime += time(i)
            inactive += Inactive.numberOrNeg1(data(i))
            active += Active.numberOrNeg1(data(i))
            waiting += Waiting.numberOrNeg1(data(i))
            abort += Abort.numberOrNeg1(data(i))
            fairPrice += FairPrice.numberOrNeg1(data(i))
          }
        }

        newTime += time(time.length - 1)
        inactive += Inactive.numberOrNeg1(data(time.length - 1))
        active += Active.numberOrNeg1(data(time.length - 1))
        waiting += Waiting.numberOrNeg1(data(time.length - 1))
        abort += Abort.numberOrNeg1(data(time.length - 1))
        fairPrice += FairPrice.numberOrNeg1(data(time.length - 1))
      }

      CompressedStatuses(time = newTime.toIndexedSeq, inactive = inactive.toIndexedSeq, active = active.toIndexedSeq, waiting = waiting.toIndexedSeq, abort = abort.toIndexedSeq, fairPrice = fairPrice.toIndexedSeq)
    }
  }
}

case class CompressedStatuses(time: IndexedSeq[Double], inactive: IndexedSeq[Double], active: IndexedSeq[Double], waiting: IndexedSeq[Double], abort: IndexedSeq[Double], fairPrice: IndexedSeq[Double])
