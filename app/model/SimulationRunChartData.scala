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

import com.icosystem.collections._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import simulation._
import simulation.agent._
import simulation.unit._
import scala.collection.mutable.ArrayBuffer

case class SimulationRunChartData(val tableData: Map[String, ValueTable])

object SimulationRunChartData {
  import ChartTypes._
  import ChartConfig._

  // charts combining multiple agents
  val allProfitsAndCostsPerTonStackedChart = ChartConfig("allProfitsAndCostsPerTonStacked", "Fish Price Breakdown", "Years", "$ / ton caught or bought", StackedArea, Seq(lightBlue, blue, lightRed, red, lightYellow, yellow))
  val allPercentProfitsAndCostsPerTonStackedChart = ChartConfig("allPercentProfitsAndCostsPerTonStacked", "Fish Price Breakdown (Percent)", "Years", "%", StackedArea, Seq(lightBlue, blue, lightRed, red, lightYellow, yellow))
  val allProfitsPerTonChart = ChartConfig("allProfitsPerTon", "Fishing Profits", "Years", "$ caught or bought", Line, Seq(blue, red, yellow))
  val allProfitsPerTonStackedChart = ChartConfig("allProfitsPerTonStacked", "Stacked Profits (Rent Capture)", "Years", "$ / ton caught or bought", StackedArea, Seq(blue, red, yellow))

  // fish charts
  val fishStockChart = ChartConfig("fishStock", "Stock", "Years", "Tons", Line, Seq(blue, green))
  val fishHarvestChart = ChartConfig("fishHarvest", "Catch", "Years", "Tons / year", Line, Seq(lightYellow, green, blue, red, yellow))
  val fishSpoiledChart = ChartConfig("fishSpoiled", "Spoiled Fish in Market", "Years", "Tons / year", Line, Seq(blue, red, yellow, green))

  // municipalFisher charts
  val municipalFisherCatchChart = ChartConfig("municipalFisherCatch", "Catch", "Years", "Tons / year", Line, Seq(blue, red))
  val municipalFisherTotalVolumeChart = ChartConfig("municipalFisherVolume", "Total Volume", "Years", "Tons / year", Line, Seq(blue, green, red))
  val municipalFisherPerCapitaVolumeChart = ChartConfig("municipalFisherPerCapitaVolume", "Per Capita Volume", "Years", "Tons / household / year", Line, Seq(blue, red))
  val municipalFisherPerCapitaFishingMoneyChart = ChartConfig("municipalFisherPerCapitaFishingMoney", "Per Capita Fishing $", "Years", "$ / household / year", Line, Seq(blue, red, green, yellow))
  val municipalFisherPerCapitaAlternativeLivelihoodIncomeChart = ChartConfig("municipalFisherPerCapitaAlternativeLivelihoodIncome", "Per Capita Alternative Livelihood Income", "Years", "$ / household / year", Line, Seq(blue, yellow))
  val municipalFisherAlternativeLivelihoodEffortChart = ChartConfig("municipalFisherAlternativeLivelihoodEffort", "Alternative Livelihood Effort", "Years", "Alternative hrs / year", Line, Seq(green, lightGreen, lightGreen2, blue, red))
  val municipalFisherPerCapitaBottomLineChart = ChartConfig("municipalFisherPerCapitaBottomLine", "Per Capita Bottom Line $", "Years", "$ / household / year", Line, Seq(blue, yellow))
  val municipalFisherFishingPerCapitaSurplusChart = ChartConfig("municipalFisherFishingPerCapitaSurplus", "Per Capita Fishing Surplus", "Years", "$ / household / year", Line, Seq(yellow, green, black)) // <--------------------
  val municipalFisherBottomLinePerCapitaSurplusChart = ChartConfig("municipalFisherBottomLinePerCapitaSurplusChart", "Per Capita Surplus after Living Costs", "Years", "$ / household / year", Line, Seq(black, lightRed, lightYellow)) // <--------------------
  val municipalFisherIncomingMoneyBreakdownChart = ChartConfig("municipalFisherIncomingMoneyBreakdownChart", "Per Capita Income Breakdown", "Years", "$ / household / year", StackedArea, Seq(lightBlue, blue, green)) // <--------------------
  val municipalFisherPopulationChart = ChartConfig("municipalFisherPopulation", "Population", "Years", "# of households", Line, Seq(blue))
  val municipalFisherPopulationBreakdownChart = ChartConfig("municipalFisherPopulationBreakdown", "Population Breakdown", "Years", "# of households", StackedArea, Seq(lightBlue, blue, yellow))
  val municipalFisherFishingEffortChart = ChartConfig("municipalFisherEffort", "Fishing Effort", "Years", "Fishing hrs / year", Line, Seq(green, red))
  val municipalFisherCombinedTotalCommunityEffortChart = ChartConfig("municipalFisherCombinedTotalCommunityEffort", "Combined Community Effort", "Years", "Fishing hrs / year", StackedArea, Seq(green, blue))
  val municipalFisherCombinedPerCapitaEffortChart = ChartConfig("municipalFisherTotalCombinedPerCapitaEffor", "Combined Per Capita Effort", "Years", "Fishing hrs / year", StackedArea, Seq(green, blue))
  val municipalFisherCatchPerUnitEffortChart = ChartConfig("municipalFisherCatchPerUnitEffort", "Catch per Unit Fishing Effort", "Years", "Kgs / 100 fishing hrs", Line, Seq(blue))
  val municipalFisherCatchBreakdown = ChartConfig("municipalFisherCatchBreakdown", "Fisher Catch Breakdown", "Years", "Tons / year", StackedArea, Seq(blue, yellow, red))
  val municipalFisherProfitabilityChart = ChartConfig("municipalFisherProfitability", "Profitability of Activities", "Years", "$ / hour", Line, Seq(lightGreen2, green, blue, red))
  val municipalFisherPriceNegotiationStressChart = ChartConfig("municipalFisherPriceNegotiationStress", "Vulnerability", "Years", "Stress (%)", Line, Seq(blue, red), min = 0, max = 100)
  val municipalFisherPriceNegotiationStatusLineChart = ChartConfig("municipalFisherPriceNegotiationStatusLine", "Price Negotiation Status", "Years", "Status", LineAsTimeline, Seq(red, yellow, lightYellow, lightRed, green))

  // middleman market charts
  val middlemanMarketVolumeChart = ChartConfig("localVolume", "Volume", "Years", "Tons / year", Line, Seq(blue, red, yellow))
  val middlemanMarketPriceChart = ChartConfig("localPrice", "Price", "Years", "$ / ton traded", Line, Seq(blue, green))
  val middlemanMarketValueChart = ChartConfig("localValue", "Total Value", "Years", "$ / year", Line, Seq(blue))

  // middleman charts
  val middlemanVolumeChart = ChartConfig("middlemanVolume", "Volume", "Years", "Tons / year", Line, Seq(blue, red))
  val middlemanProfitChart = ChartConfig("middlemanProfit", "Profit", "Years", "$ / year", Line, Seq(blue))

  // exporter market charts
  val exporterMarketVolumeChart = ChartConfig("wholesaleVolume", "Volume", "Years", "Tons / year", Line, Seq(blue, red, yellow))
  val exporterMarketPriceChart = ChartConfig("wholesalePrice", "Price", "Years", "$ / ton traded", Line, Seq(blue))
  val exporterMarketValueChart = ChartConfig("wholesaleValue", "Total Value", "Years", "$ / year", Line, Seq(blue))

  // exporter charts
  val exporterVolumeChart = ChartConfig("exporterVolume", "Volume", "Years", "Tons / year", Line, Seq(blue, red))
  val exporterProfitChart = ChartConfig("exporterProfit", "Profit", "Years", "$ / year", Line, Seq(blue))

  // consumer market charts
  val consumerMarketVolumeChart = ChartConfig("consumerVolume", "Volume", "Years", "Tons / year", Line, Seq(blue, red, yellow))
  val consumerMarketPriceChart = ChartConfig("consumerPrice", "Price", "Years", "$ / ton traded", Line, Seq(blue))
  val consumerMarketValueChart = ChartConfig("consumerValue", "Total Value", "Years", "$ / year", Line, Seq(blue))

  // consumer charts
  val consumerPopulationChart = ChartConfig("consumerPopulation", "Population", "Years", "Consumers", Line, Seq(blue))
  val consumerDemandChart = ChartConfig("consumerDemand", "Demand", "Years", "Tons / year", Line, Seq(blue, red))
  val consumerConsumptionChart = ChartConfig("consumerConsumption", "Consumption", "Years", "Tons / year", Line, Seq(blue, red))

  val simRunTables: Vector[ChartConfig] = Vector(fishStockChart, fishHarvestChart, municipalFisherCatchChart, municipalFisherTotalVolumeChart, municipalFisherPerCapitaVolumeChart, municipalFisherPerCapitaFishingMoneyChart, municipalFisherFishingPerCapitaSurplusChart, municipalFisherIncomingMoneyBreakdownChart, municipalFisherBottomLinePerCapitaSurplusChart, municipalFisherPopulationChart, municipalFisherPopulationBreakdownChart, municipalFisherFishingEffortChart, middlemanMarketVolumeChart, middlemanMarketPriceChart, middlemanMarketValueChart, middlemanVolumeChart, middlemanProfitChart, exporterMarketVolumeChart, exporterMarketPriceChart, exporterVolumeChart, exporterProfitChart, exporterMarketValueChart, consumerMarketVolumeChart, consumerMarketPriceChart, consumerMarketValueChart, consumerPopulationChart, consumerDemandChart, allProfitsAndCostsPerTonStackedChart, allPercentProfitsAndCostsPerTonStackedChart, allProfitsPerTonChart, fishSpoiledChart, allProfitsPerTonStackedChart, municipalFisherCatchPerUnitEffortChart, municipalFisherCatchBreakdown, municipalFisherPerCapitaAlternativeLivelihoodIncomeChart, municipalFisherAlternativeLivelihoodEffortChart, municipalFisherPerCapitaBottomLineChart, municipalFisherProfitabilityChart, municipalFisherPriceNegotiationStressChart, municipalFisherPriceNegotiationStatusLineChart, municipalFisherCombinedTotalCommunityEffortChart, municipalFisherCombinedPerCapitaEffortChart, consumerConsumptionChart)

  def create(simResults: SimulationResults, aggSize: Int): SimulationRunChartData = {
    val d = new RawTableData(new RawSeriesData(simResults, aggSize))

    SimulationRunChartData(Map(
      // all agents
      SimulationRunChartData.allProfitsAndCostsPerTonStackedChart.id -> d.allProfitsAndCostsPerTonTable,
      SimulationRunChartData.allPercentProfitsAndCostsPerTonStackedChart.id -> d.allPercentProfitsAndCostsPerTonTable,
      SimulationRunChartData.allProfitsPerTonChart.id -> d.allProfitsTable,
      SimulationRunChartData.allProfitsPerTonStackedChart.id -> d.allProfitsPerTonTable,

      // fish
      SimulationRunChartData.fishSpoiledChart.id -> d.fishSpoiledTable,
      SimulationRunChartData.fishStockChart.id -> d.fishStockTable,
      SimulationRunChartData.fishHarvestChart.id -> d.fishHarvestTable,

      // fisher
      SimulationRunChartData.municipalFisherFishingEffortChart.id -> d.municipalFisherFishingEffortTable,
      SimulationRunChartData.municipalFisherCombinedTotalCommunityEffortChart.id -> d.municipalFisherCombinedTotalCommunityEffortTable,
      SimulationRunChartData.municipalFisherCombinedPerCapitaEffortChart.id -> d.municipalFisherCombinedPerCapitaEffortTable,
      SimulationRunChartData.municipalFisherCatchChart.id -> d.municipalFisherCatchTable,
      SimulationRunChartData.municipalFisherTotalVolumeChart.id -> d.municipalFisherTotalVolumeTable,
      SimulationRunChartData.municipalFisherPerCapitaVolumeChart.id -> d.municipalFisherPerCapitaVolumeTable,
      SimulationRunChartData.municipalFisherPopulationChart.id -> d.municipalFisherPopulationTable,
      SimulationRunChartData.municipalFisherPopulationBreakdownChart.id -> d.municipalFisherPopulationBreakdownTable,
      SimulationRunChartData.municipalFisherPerCapitaFishingMoneyChart.id -> d.municipalFisherPerCapitaFishingMoneyTable,
      SimulationRunChartData.municipalFisherFishingPerCapitaSurplusChart.id -> d.municipalFisherFishingPerCapitaSurplusTable,
      SimulationRunChartData.municipalFisherBottomLinePerCapitaSurplusChart.id -> d.municipalFisherBottomLinePerCapitaSurplusTable,
      SimulationRunChartData.municipalFisherIncomingMoneyBreakdownChart.id -> d.municipalFisherIncomingMoneyBreakdownTable,
      SimulationRunChartData.municipalFisherCatchPerUnitEffortChart.id -> d.municipalFisherCatchPerUnitEffortTable,
      SimulationRunChartData.municipalFisherCatchBreakdown.id -> d.municipalFisherCatchBreakdownTable,
      SimulationRunChartData.municipalFisherPerCapitaAlternativeLivelihoodIncomeChart.id -> d.municipalFisherPerCapitaAlternativeLivelihoodIncomeTable,
      SimulationRunChartData.municipalFisherAlternativeLivelihoodEffortChart.id -> d.municipalFisherAlternativeLivelihoodEffortTable,
      SimulationRunChartData.municipalFisherPerCapitaBottomLineChart.id -> d.municipalFisherPerCapitaBottomLineTable,
      SimulationRunChartData.municipalFisherProfitabilityChart.id -> d.municipalFisherProfitabilityTable,
      SimulationRunChartData.municipalFisherPriceNegotiationStressChart.id -> d.municipalFisherPriceNegotiationStressTable,
      SimulationRunChartData.municipalFisherPriceNegotiationStatusLineChart.id -> d.municipalFisherPriceNegotiationStatusLineTable,

      // middleman market
      SimulationRunChartData.middlemanMarketVolumeChart.id -> d.middlemanMarketVolumeTable,
      SimulationRunChartData.middlemanMarketPriceChart.id -> d.middlemanMarketPriceTable,
      SimulationRunChartData.middlemanMarketValueChart.id -> d.middlemanMarketValueTable,

      // middleman
      SimulationRunChartData.middlemanVolumeChart.id -> d.middlemanVolumeTable,
      SimulationRunChartData.middlemanProfitChart.id -> d.middlemanProfitTable,

      // exporter market
      SimulationRunChartData.exporterMarketVolumeChart.id -> d.exporterMarketVolumeTable,
      SimulationRunChartData.exporterMarketPriceChart.id -> d.exporterMarketPriceTable,
      SimulationRunChartData.exporterMarketValueChart.id -> d.exporterMarketValueTable,

      // exporter
      SimulationRunChartData.exporterVolumeChart.id -> d.exporterVolumeTable,
      SimulationRunChartData.exporterProfitChart.id -> d.exporterProfitTable,

      // consumer market
      SimulationRunChartData.consumerMarketVolumeChart.id -> d.consumerMarketVolumeTable,
      SimulationRunChartData.consumerMarketPriceChart.id -> d.consumerMarketPriceTable,
      SimulationRunChartData.consumerMarketValueChart.id -> d.consumerMarketValueTable,

      // consumer
      SimulationRunChartData.consumerPopulationChart.id -> d.consumerPopulationTable,
      SimulationRunChartData.consumerDemandChart.id -> d.consumerDemandTable,
      SimulationRunChartData.consumerConsumptionChart.id -> d.consumerConsumptionTable))
  }

  implicit val writeSimulationResults = Json.writes[SimulationRunChartData]
}
