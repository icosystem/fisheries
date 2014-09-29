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

import com.icosystem.collections.Column
import com.icosystem.collections.ValueTable
import play.api.libs.functional.syntax._
import play.api.libs.json._

import ChartConfig.blue
import ChartConfig.lightBlue
import ChartConfig.red
import ChartConfig.yellow
import ChartTypes.Line
import ChartTypes.StackedColumn
import simulation.SimulationResults

case class SimulationAnimatedChartData(val rentCaptureComparison: Seq[ValueTable])

object SimulationAnimatedChartData {
  import ChartTypes._
  import ChartConfig._

  val rentCaptureComparison = ChartConfig("rentCaptureComparison", "Combined Profits", "Years", "$/year", StackedColumn, Seq(blue, red, yellow))

  val animatedTables = Vector(rentCaptureComparison)

   def last[T](s : Seq[T]) = s.last
  def avg(s : Seq[Double]) = s.sum / s.size
  
  def create(baselineResults: SimulationResults, interventionResults: SimulationResults, aggSize: Int): SimulationAnimatedChartData = {
    import ChartData._

    // Scalars
    val ΔT = baselineResults.t.deltaT.v
    val xAtMSY = coerce(baselineResults.fish.carryingCapacity.v / 2d)
    val msy = coerce(baselineResults.fish.growthRate.v * baselineResults.fish.carryingCapacity.v / 4d)

    // Columns
    val time = col("t", (0 to baselineResults.t.end).map(_ * ΔT)).aggregate(aggSize, last)

    val rentCaptureFisherBaseline = col("Profit", baselineResults.municipalFisherTotalProfits).aggregate(aggSize, avg)
    val rentCaptureMiddlemanBaseline = col("Profit", baselineResults.middlemanTotalProfits).aggregate(aggSize, avg)
    val rentCaptureExporterBaseline = col("Profit", baselineResults.exporterTotalProfits).aggregate(aggSize, avg)

    val rentCaptureFisherIntervention = col("Profit", interventionResults.municipalFisherTotalProfits).aggregate(aggSize, avg)
    val rentCaptureMiddlemanIntervention = col("Profit", interventionResults.middlemanTotalProfits).aggregate(aggSize, avg)
    val rentCaptureExporterIntervention = col("Profit", interventionResults.exporterTotalProfits).aggregate(aggSize, avg)

    var rentCaptureResults = Seq[ValueTable]()
    for (i <- 0 to time.size - 1) {
      val rcCols = colString("Scenario", IndexedSeq("Baseline", "Intervention"))
      val rcDataFisher = col("Fisher", IndexedSeq(rentCaptureFisherBaseline(i), rentCaptureFisherIntervention(i)))
      val rcDataMiddleman = col("Middleman", IndexedSeq(rentCaptureMiddlemanBaseline(i), rentCaptureMiddlemanIntervention(i)))
      val rcDataExporter = col("Exporter", IndexedSeq(rentCaptureExporterBaseline(i), rentCaptureExporterIntervention(i)))

      // Tables
      rentCaptureResults = rentCaptureResults :+ ValueTable(rcCols, rcDataFisher, rcDataMiddleman, rcDataExporter)
    }

    SimulationAnimatedChartData(rentCaptureResults)
  }

  implicit val writeAnimatedResults = Json.writes[SimulationAnimatedChartData]
}