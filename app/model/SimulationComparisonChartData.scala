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

import com.icosystem.collections.ValueTable
import simulation.SimulationResults
import com.icosystem.collections.Column
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SimulationComparisonChartData(val tableData: Map[String, ValueTable])

object SimulationComparisonChartData {
  import ChartTypes._
  import ChartConfig._

  val biomassComparisonChart = ChartConfig("biomassComparison", "Stock", "Years", "Tons", Line, Seq(lightBlue, blue, green))
  val biomassBaselineVsInterventionComparisonChart = ChartConfig("biomassBaselineVsInterventionComparison", "Stock", "Years", "Tons", Line, Seq(lightBlue, blue))
  val fisherPopulationComparisonChart = ChartConfig("fisherPopulationComparisonChart", "Population", "Years", "# of households", Line, Seq(lightBlue, blue))
  val perFisherProfitsComparisonChart = ChartConfig("perFisherProfitsComparisonChart", "Per Capita Fishing Profits", "Years", "$ / household / year", Line, Seq(lightBlue, blue, lightYellow, yellow))
  val perFisherSurplusComparisonChart = ChartConfig("perFisherSurplusComparisonChart", "Per Capita Surplus after Living Costs", "Years", "$ / household / year", Line, Seq(lightBlue, blue, lightYellow, yellow))
  val perFisherSurplusBaselineVsInterventionComparisonChart = ChartConfig("perFisherSurplusBaselineVsInterventionComparison", "Per Capita Surplus after Living Costs", "Years", "$ / household / year", Line, Seq(lightBlue, blue))
  val fisherTotalCatchComparisonChart = ChartConfig("fisherTotalCatchComparisonChart", "Total Catch", "Years", "tons / year", Line, Seq(lightBlue, blue, green))
  val middlemanTotalProfitsComparisonChart = ChartConfig("middlemanTotalProfitsComparison", "Middleman Total Profits", "Years", "$ / year", Line, Seq(lightRed, red))
  val exporterTotalProfitsComparisonChart = ChartConfig("exporterTotalProfitsComparison", "Exporter Total Profits", "Years", "$ / year", Line, Seq(lightYellow, yellow))
  val allTotalProfitsComparisonChart = ChartConfig("allTotalProfitsComparison", "Combined Total Profits", "Years", "$ / year", Line, Seq(lightBlue, blue, lightRed, red, lightYellow, yellow))
  val consumerDemandPer1000Chart = ChartConfig("consumerDemandPer1000Chart", "Consumption per Capita", "Years", "tons / year (x1000)", Line, Seq(lightBlue, blue))
  val perFisherAlternativeLivelihoodIncomeChart = ChartConfig("perFisherAlternativeLivelihoodIncome", "Per Capita Alternative Livelihood Income", "Years", "$ / household / year", Line, Seq(lightBlue, blue, lightYellow, yellow))

  val comparisonTables: Vector[ChartConfig] = Vector(biomassComparisonChart, perFisherProfitsComparisonChart, perFisherSurplusComparisonChart, fisherTotalCatchComparisonChart, fisherPopulationComparisonChart, middlemanTotalProfitsComparisonChart, exporterTotalProfitsComparisonChart, allTotalProfitsComparisonChart, consumerDemandPer1000Chart, perFisherAlternativeLivelihoodIncomeChart, biomassBaselineVsInterventionComparisonChart, perFisherSurplusBaselineVsInterventionComparisonChart)

  def last[T](s: Seq[T]) = s.last
  def avg(s: Seq[Double]) = s.sum / s.size

  def create(baselineResults: SimulationResults, interventionResults: SimulationResults, aggSize: Int): SimulationComparisonChartData = {
    import ChartData._

    // Scalars
    val ΔT = baselineResults.t.deltaT.v
    val xAtMSY = coerce(baselineResults.fish.carryingCapacity.v / 2d)
    val msy = coerce(baselineResults.fish.msy)

    // Columns
    val time = col("t", (0 to baselineResults.t.end).map(_ * ΔT)).aggregate(aggSize, last)
    //biomass
    val baselineBiomassCol = col("Baseline Stock", baselineResults.fishBiomass).aggregate(aggSize, last)
    val interventionBiomassCol = col("Intervention Stock", interventionResults.fishBiomass).aggregate(aggSize, last)
    val fishBiomassAtMSYCol = col("Stock At MSY", Vector.fill(baselineResults.t.end + 1)(xAtMSY)).aggregate(aggSize, last)
    val fishMSYCol = col("MSY", Vector.fill(baselineResults.t.end + 1)(msy)).aggregate(aggSize, last)

    // profits
    val baselinePerFisherProfitsCol = col("Baseline", baselineResults.profitPerFisherman).aggregate(aggSize, avg)
    val interventionPerFisherProfitsCol = col("Intervention", interventionResults.profitPerFisherman).aggregate(aggSize, avg)
    val baselinePerFisherBottomLinePerCapitaSurplusCol = col("Baseline", baselineResults.municipalFisher.bottomLineSurplusPerFishermanValues.map(_.v)).aggregate(aggSize, avg)
    val interventionPerFisherBottomLinePerCapitaSurplusCol = col("Intervention", interventionResults.municipalFisher.bottomLineSurplusPerFishermanValues.map(_.v)).aggregate(aggSize, avg)
    val baselinePerFisherProfitsNeededForGoodLifeCol = col("Good life profits (b)", baselineResults.municipalFisher.perFishermanProfitsNeededForGoodLife).aggregate(aggSize, avg)
    val interventionPerFisherProfitsNeededForGoodLifeCol = col("Good life profits (i)", interventionResults.municipalFisher.perFishermanProfitsNeededForGoodLife).aggregate(aggSize, avg)
    val baselinePerFisherSurplusNeededForGoodLifeCol = col("Good life surplus (b)", baselineResults.municipalFisher.perFishermanSurplusNeededForGoodLife).aggregate(aggSize, avg)
    val interventionPerFisherSurplusNeededForGoodLifeCol = col("Good life surplus (i)", interventionResults.municipalFisher.perFishermanSurplusNeededForGoodLife).aggregate(aggSize, avg)

    val baselineFisherPopulationCol = col("Baseline", baselineResults.municipalFisherPopulation).aggregate(aggSize, last)
    val interventionFisherPopulationCol = col("Intervention", interventionResults.municipalFisherPopulation).aggregate(aggSize, last)

    val baselineFisherTotalCatchCol = col("Baseline", baselineResults.totalCatch).aggregate(aggSize, avg)
    val interventionFisherTotalCatchCol = col("Intervention", interventionResults.totalCatch).aggregate(aggSize, avg)

    val baselineFisherTotalProfitsCol = col("Baseline", baselineResults.municipalFisherTotalProfits).aggregate(aggSize, avg)
    val interventionFisherTotalProfitsCol = col("Intervention", interventionResults.municipalFisherTotalProfits).aggregate(aggSize, avg)
    val baselineMiddlemanTotalProfitsCol = col("Baseline", baselineResults.middlemanTotalProfits).aggregate(aggSize, avg)
    val interventionMiddlemanTotalProfitsCol = col("Intervention", interventionResults.middlemanTotalProfits).aggregate(aggSize, avg)
    val baselineExporterTotalProfitsCol = col("Baseline", baselineResults.exporterTotalProfits).aggregate(aggSize, avg)
    val interventionExporterTotalProfitsCol = col("Intervention", interventionResults.exporterTotalProfits).aggregate(aggSize, avg)

    val baselinePerFisherAlternativeLivelihoodIncomeCol = col("Baseline", baselineResults.municipalFisherAlternativeLivelihoodIncome).aggregate(aggSize, avg)
    val interventionPerFisherAlternativeLivelihoodIncomeCol = col("Intervention", interventionResults.municipalFisherAlternativeLivelihoodIncome).aggregate(aggSize, avg)

    // demand
    val baselineConsumerDemandPer1000Col = col("Baseline", baselineResults.consumerDemandPer1000).aggregate(aggSize, avg);
    val interventionConsumerDemandPer1000Col = col("Intervention", interventionResults.consumerDemandPer1000).aggregate(aggSize, avg);

    // Tables
    val biomassTable = ValueTable(time, baselineBiomassCol, interventionBiomassCol, fishBiomassAtMSYCol)
    val biomassBaselineVsInterventionComparisonTable = ValueTable(time, baselineBiomassCol, interventionBiomassCol)
    val perFisherProfitsTable = ValueTable(time, baselinePerFisherProfitsCol, interventionPerFisherProfitsCol, baselinePerFisherProfitsNeededForGoodLifeCol, interventionPerFisherProfitsNeededForGoodLifeCol)
    val perFisherSurplusTable = ValueTable(time, baselinePerFisherBottomLinePerCapitaSurplusCol, interventionPerFisherBottomLinePerCapitaSurplusCol, baselinePerFisherSurplusNeededForGoodLifeCol, interventionPerFisherSurplusNeededForGoodLifeCol)
    val perFisherSurplusBaselineVsInterventionComparisonTable = ValueTable(time, baselinePerFisherBottomLinePerCapitaSurplusCol, interventionPerFisherBottomLinePerCapitaSurplusCol)
    val fisherTotalCatchComparisonTable = ValueTable(time, baselineFisherTotalCatchCol, interventionFisherTotalCatchCol, fishMSYCol)
    val fisherPopulationTable = ValueTable(time, baselineFisherPopulationCol, interventionFisherPopulationCol)
    val middlemanTotalProfitsTable = ValueTable(time, baselineMiddlemanTotalProfitsCol, interventionMiddlemanTotalProfitsCol)
    val exporterTotalProfitsTable = ValueTable(time, baselineExporterTotalProfitsCol, interventionExporterTotalProfitsCol)
    val allTotalProfitsTable = ValueTable(time, col("Baseline Fishers", baselineFisherTotalProfitsCol.values), col("Intervention Fishers", interventionFisherTotalProfitsCol.values), col("Baseline Middlemen", baselineMiddlemanTotalProfitsCol.values), col("Intervention Middlement", interventionMiddlemanTotalProfitsCol.values), col("Baseline Exporter", baselineExporterTotalProfitsCol.values), col("Intervention Exporter", interventionExporterTotalProfitsCol.values))
    val consumerDemandPer1000Table = ValueTable(time, baselineConsumerDemandPer1000Col, interventionConsumerDemandPer1000Col)
    val perFisherAlternativeLivelihoodIncomeTable = ValueTable(time, baselinePerFisherAlternativeLivelihoodIncomeCol, interventionPerFisherAlternativeLivelihoodIncomeCol, baselinePerFisherProfitsNeededForGoodLifeCol, interventionPerFisherProfitsNeededForGoodLifeCol)

    SimulationComparisonChartData(Map(
      SimulationComparisonChartData.biomassComparisonChart.id -> biomassTable,
      SimulationComparisonChartData.biomassBaselineVsInterventionComparisonChart.id -> biomassBaselineVsInterventionComparisonTable,
      SimulationComparisonChartData.perFisherProfitsComparisonChart.id -> perFisherProfitsTable,
      SimulationComparisonChartData.perFisherSurplusComparisonChart.id -> perFisherSurplusTable,
      SimulationComparisonChartData.perFisherSurplusBaselineVsInterventionComparisonChart.id -> perFisherSurplusBaselineVsInterventionComparisonTable,
      SimulationComparisonChartData.fisherTotalCatchComparisonChart.id -> fisherTotalCatchComparisonTable,
      SimulationComparisonChartData.fisherPopulationComparisonChart.id -> fisherPopulationTable,
      SimulationComparisonChartData.middlemanTotalProfitsComparisonChart.id -> middlemanTotalProfitsTable,
      SimulationComparisonChartData.exporterTotalProfitsComparisonChart.id -> exporterTotalProfitsTable,
      SimulationComparisonChartData.allTotalProfitsComparisonChart.id -> allTotalProfitsTable,
      SimulationComparisonChartData.consumerDemandPer1000Chart.id -> consumerDemandPer1000Table,
      SimulationComparisonChartData.perFisherAlternativeLivelihoodIncomeChart.id -> perFisherAlternativeLivelihoodIncomeTable))
  }

  implicit val writeComparisonResults = Json.writes[SimulationComparisonChartData]
}