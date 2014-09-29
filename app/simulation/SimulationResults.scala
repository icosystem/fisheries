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

import simulation.agent._

case class SimulationResults(middlemanMarket: Market with MarketRecorder,
  exporterMarket: Market with MarketRecorder,
  consumerMarket: ConsumerMarket with MarketRecorder,
  fish: Fish with FishRecorder,
  municipalFisher: MunicipalFisher with FisherRecorder,
  commercialFisher: CommercialFisher with AgentRecorder[CommercialFisherState],
  middleman: Intermediary with IntermediaryRecorder,
  exporter: Intermediary with IntermediaryRecorder,
  consumer: Consumer with AgentRecorder[ConsumerState],
  t: Timeline) {

  val fishBiomass = fish.data.map(_.biomass.v)
  val municipalFisherPopulation = municipalFisher.data.map(_.numberOfFishermenInPopulation.v)
  val totalCatch = {
    val municipalFisherTotalCatch = municipalFisher.totalCatchRateValues.map(_.v)
    val commercialFisherTotalCatch = commercialFisher.data.map(_.haul.v / t.deltaT.v)
    (municipalFisherTotalCatch, commercialFisherTotalCatch).zipped.map(_ + _)
  }
  val municipalFisherTotalProfits = municipalFisher.totalFishingProfitsRateValues
  val profitPerFisherman = municipalFisher.perFishermanMoneyValues.map(_.profit)
  val surplusPerFisherman = municipalFisher.bottomLineSurplusPerFishermanValues.map(_.v)
  val middlemanTotalProfits = middleman.totalMoney.map(_.profit)
  val exporterTotalProfits = exporter.totalMoney.map(_.profit)
  val consumerDemandPer1000 = consumer.data.map(d => 1000 * (d.demandPerCapita.v / t.deltaT.v))
  val municipalFisherAlternativeLivelihoodIncome = municipalFisher.perFishermanAlternativeLivelihoodIncome
}

object SimulationResults {

  def apply(valueChain: ValueChain)(implicit t: Timeline): SimulationResults = {
    SimulationResults(middlemanMarket = valueChain.middlemanMarket,
      exporterMarket = valueChain.exporterMarket,
      consumerMarket = valueChain.consumerMarket,
      fish = valueChain.fish,
      municipalFisher = valueChain.municipalFisher,
      commercialFisher = valueChain.commercialFisher,
      middleman = valueChain.middleman,
      exporter = valueChain.exporter,
      consumer = valueChain.consumer,
      t = t)
  }
}

