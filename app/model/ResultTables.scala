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

import model.SimulationRunChartData.simRunTables
import model.SimulationComparisonChartData.comparisonTables
import model.SimulationAnimatedChartData.animatedTables
import play.api.libs.functional.syntax._
import play.api.libs.json._
import controllers.SimulationResultsUI

case class ChartConfig(id: String, title: String, hLabel: String, vLabel: String, chartType: String, colors: Seq[String], min: Option[Double] = None, max: Option[Double] = None) {
  def interventionId = SimulationResultsUI.interventions + "_" + id
  def baselineId = SimulationResultsUI.baseline + "_" + id
  def comparisonId = SimulationResultsUI.comparison + "_" + id
}
object ChartConfig {
  val blue = "#254cc9"
  val red = "#d12600"
  val yellow = "#fd8800"
  val green = "#1b8800"
  val black = "#000000"

  val lightBlue = "#afc0ee"
  val lightRed = "#fbafa1"
  val lightGreen = "#3bf8a1"
  val lightGreen2 = "#3bc851"
  val lightYellow = "#fdd39d"

  implicit val formatChartConfig = Json.format[ChartConfig]
}

case class ResultTables(baseline: Vector[ChartConfig], intervention: Vector[ChartConfig], comparison: Vector[ChartConfig], animated: Vector[ChartConfig])
object ResultTables {
  val resultTables = ResultTables(simRunTables, simRunTables, comparisonTables, animatedTables)

  implicit val formatResultTables = Json.format[ResultTables]
}

object ChartTypes {
  val Line = "Line"
  val StackedArea = "StackedArea"
  val StackedColumn = "StackedColumn"
  val Column = "Column"
  val LineAsTimeline = "LineAsTimeline"
}