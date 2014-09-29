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

import com.icosystem.collections.Column
import com.icosystem.math._

import controllers.ModelConfigUI

import model.RawSeriesData

object Summarize {
  
  def apply(config: ModelConfigUI) = {
    val baseline = new RawSeriesData(FisherySimulation(config.baseline), 1)
    val interventions = new RawSeriesData(FisherySimulation(config.interventions), 1)
    summarizeData(baseline, interventions)
  }

  private def summarizeData(baseline: RawSeriesData, intervention: RawSeriesData) = {
    val fishStock = summarizeSingleSeries(baseline.fishBiomassCol, intervention.fishBiomassCol)
    val municipalFisherPopulation = summarizeSingleSeries(baseline.municipalFisherPopulationCol.copy(name = "Population"), intervention.municipalFisherPopulationCol.copy(name = "Population"))
    val municipalFisherProfitPerCapita = summarizeSingleSeries(baseline.municipalFisherFishingPerCapitaProfitCol, intervention.municipalFisherFishingPerCapitaProfitCol)
    val municipalFisherBottomLinePerCapitaSurplus = summarizeSingleSeries(baseline.municipalFisherBottomLinePerCapitaSurplusCol, intervention.municipalFisherBottomLinePerCapitaSurplusCol)
    val totalMunicipalHarvest = summarizeSingleSeries(baseline.municipalFisherTotalCatchCol, intervention.municipalFisherTotalCatchCol)
    IndexedSeq(fishStock, municipalFisherPopulation, municipalFisherProfitPerCapita, municipalFisherBottomLinePerCapitaSurplus, totalMunicipalHarvest)
  }

  private def summarizeSingleSeries(baselineVals: Column[Double], interventionVals: Column[Double]) = {
    val metricName = baselineVals.name
    val start = d2(baselineVals(0))
    val baselineEnd = d2(baselineVals.last)
    val interventionEnd = d2(interventionVals.last)
    (metricName, start, baselineEnd, interventionEnd)
  }
  
}