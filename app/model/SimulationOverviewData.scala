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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import simulation.SimulationResults
import com.icosystem.math._
import play.Logger
import com.icosystem.collections.Column

case class SimulationOverviewDataField(symbol: String, percentChange: String, value: Int, max: Int, baseline: Double, intervention: Double, actualChange: Double, changeInInterventionFromStart: Double, changeInBaselineFromStart: Double)
object SimulationOverviewDataField {
  implicit val simulationOverviewDataFieldFormat = Json.format[SimulationOverviewDataField]
}

case class SimulationOverviewData(fishStock: SimulationOverviewDataField, municipalFisherPopulation: SimulationOverviewDataField, municipalFisherProfitPerCapita: SimulationOverviewDataField, municipalFisherSurplusPerCapita: SimulationOverviewDataField, totalMunicipalHarvest: SimulationOverviewDataField, consumerDemandPer1000: SimulationOverviewDataField)

object SimulationOverviewData {
  val fishStock = "fishStock"
  val municipalFisherPopulation = "municipalFisherPopulation"
  val municipalFisherProfitPerCapita = "municipalFisherProfitPerCapita"
  val municipalFisherSurplusPerCapita = "municipalFisherSurplusPerCapita"
  val totalMunicipalHarvest = "totalMunicipalHarvest"
  val consumerDemandPer1000 = "consumerDemandPer1000"
  val MAX_GLYPH = 10
  val CLIP_PERCENTAGE_CHANGE = 100

  val ids: Vector[String] = Vector(fishStock, municipalFisherPopulation, municipalFisherProfitPerCapita, municipalFisherSurplusPerCapita, totalMunicipalHarvest, consumerDemandPer1000)

  implicit val simulationOverviewDataFormat = Json.format[SimulationOverviewData]

  def create(baselineResults: SimulationResults, interventionResults: SimulationResults, aggSize: Int) = {
    val fishStock = summarize(baselineResults.fishBiomass, interventionResults.fishBiomass, aggSize)
    val municipalFisherPopulation = summarize(baselineResults.municipalFisherPopulation, interventionResults.municipalFisherPopulation, aggSize)
    val municipalFisherProfitPerCapita = summarize(baselineResults.profitPerFisherman, interventionResults.profitPerFisherman, aggSize)
    val municipalFisherSurplusPerCapita = summarize(baselineResults.surplusPerFisherman, interventionResults.surplusPerFisherman, aggSize) //+++
    val totalMunicipalHarvest = summarize(baselineResults.totalCatch, interventionResults.totalCatch, aggSize)
    val consumerDemandPer1000 = summarize(baselineResults.consumerDemandPer1000, interventionResults.consumerDemandPer1000, aggSize)
    SimulationOverviewData(fishStock = fishStock, municipalFisherPopulation = municipalFisherPopulation, municipalFisherProfitPerCapita = municipalFisherProfitPerCapita, municipalFisherSurplusPerCapita = municipalFisherSurplusPerCapita, totalMunicipalHarvest = totalMunicipalHarvest, consumerDemandPer1000 = consumerDemandPer1000)
  }

  private def last[T](s: Seq[T]) = s.last

  private def summarize(baselineVals: Seq[Double], interventionVals: Seq[Double], aggSize: Int) = {
    val start = if (baselineVals.isEmpty) 0d else baselineVals.head
    val baseline = d2(Column("", IndexedSeq.empty ++ baselineVals).aggregate(aggSize, s => s.sum / s.size).values.last)
    val intervention = d2(Column("", IndexedSeq.empty ++ interventionVals).aggregate(aggSize, s => s.sum / s.size).values.last)
    val actualChange = Math.round(intervention - baseline)
    val relativeChange = calculatePercentChange(baseline, intervention)
    Logger.info(baseline + " to " + intervention + " = " + relativeChange + "% change")
    val relativeChangeString = "%.1f".format(relativeChange)
    val symbol = asSymbol(relativeChange)
    val rangeValue = asRange(relativeChange)
    val changeInInterventionFromStart = intervention - start
    val changeInBaselineFromStart = baseline - start
    SimulationOverviewDataField(symbol, relativeChangeString, rangeValue, 10, baseline, intervention, actualChange, changeInInterventionFromStart = changeInInterventionFromStart, changeInBaselineFromStart = changeInBaselineFromStart)
  }

  private def asRange(relativeChange: Double): Int = {
    if (relativeChange < -CLIP_PERCENTAGE_CHANGE) {
      -MAX_GLYPH
    } else if (relativeChange > CLIP_PERCENTAGE_CHANGE) {
      MAX_GLYPH
    } else if (Math.abs(relativeChange) < 1) {
      0
    } else {
      (Math.signum(relativeChange) * Math.ceil(Math.abs(relativeChange) / CLIP_PERCENTAGE_CHANGE * MAX_GLYPH)).toInt
    }
  }

  private def calculatePercentChange(baselineVal: Double, interventionVal: Double) = {
    val relativeChange = if (baselineVal != 0) {
      ((interventionVal - baselineVal) / Math.abs(baselineVal)) * 100d //http://en.wikipedia.org/wiki/Relative_change_and_difference
    } else {
      ((interventionVal - baselineVal) / 0.0001d) * 100d //arbitrarily make non-zero
    }
    relativeChange
  }

  private def asSymbol(relativeChange: Double) = {
    val magnitude = Math.abs(relativeChange);
    val positive = relativeChange >= 0;
    val symbol = if (magnitude < 10) "=" else { if (relativeChange > 0) "⬆︎" else "⬇︎" }
    var symString = symbol
    for (a <- 2 to (magnitude / 25).toInt) {
      symString = symString + symbol
    }
    symString
  }
}