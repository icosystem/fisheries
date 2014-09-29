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

import simulation.unit.Fishermen
import simulation.unit.FishermenPerMoney
import simulation.unit.FishingPerMoney
import simulation.unit.FishingPerTime
import simulation.unit.FishingPerTimeFishermen
import simulation.unit.MoneyPerFishing
import simulation.unit.MoneyPerTimeFishermen
import simulation.unit.MoneyPerTonnes
import simulation.unit.People
import simulation.unit.PerFishing
import simulation.unit.PerTime
import simulation.unit.Percent
import simulation.unit.Time
import simulation.unit.Tonnes
import simulation.unit.TonnesPerTime
import simulation.unit.TonnesPerTimePeople

case class ModelConfig(
  ticks: Int,
  deltaT: Time,
  fish: ModelConfigFish,
  municipalFisher: ModelConfigMunicipalFisher,
  commercialFisher: ModelConfigCommercialFisher,
  middlemanMarket: ModelConfigMiddlemanMarket,
  removeMiddleman: Boolean,
  middleman: ModelConfigMiddleman,
  exporterMarket: ModelConfigExporterMarket,
  exporter: ModelConfigExporter,
  consumerMarket: ModelConfigConsumerMarket,
  consumer: ModelConfigConsumer,
  singleSellerSupportAvailable: ModelConfigSingleSellerSupportAvailable,
  rockefeller: Option[RockefellerConfig])

case class ModelConfigConsumerMarket(consumerMarketSpeed: PerTime,
  consumerMarketPrice: MoneyPerTonnes,
  lastTraded: TonnesPerTime,
  lastExcess: TonnesPerTime)

case class ModelConfigMiddlemanMarket(middlemanMarketSpeed: PerTime,
  lastTraded: TonnesPerTime,
  lastExcess: TonnesPerTime,
  middlemanMarketFishPrice: MoneyPerTonnes)

case class ModelConfigConsumer(
  consumerPopulation: People,
  consumerGrowthRate: PerTime,
  initialPrice: MoneyPerTonnes,
  ε: Double,
  initialDemandPerPerson: TonnesPerTimePeople,
  lastDemand: TonnesPerTime)

case class ModelConfigExporterMarket(exporterMarketSpeed: PerTime,
  exporterMarketPrice: MoneyPerTonnes,
  lastTraded: TonnesPerTime,
  lastExcess: TonnesPerTime)

case class ModelConfigExporter(exporterOverhead: MoneyPerTonnes,
  exporterDesiredProfitRate: Double,
  aggressiveInboundPriceSetting: Double,
  aggressiveOutboundPriceSetting: Double,
  middlemanCostEstimate: MoneyPerTonnes,
  lastDemand: TonnesPerTime)

case class ModelConfigFish(
  biomass: Tonnes,
  carryingCapacity: Tonnes,
  growthRate: PerTime)

case class ModelConfigMunicipalFisher(
  numberOfFishermenInPopulation: Fishermen,
  populationSizeGrowthCoeff: FishermenPerMoney,
  effortPerFishermanPerYear: FishingPerTimeFishermen,
  alternativeEffortPerFishermanPerYear: FishingPerTimeFishermen,
  effortGrowthCoeff: FishingPerMoney,
  costOfLivingPerFisherman: MoneyPerTimeFishermen,
  variableCostPerUnitEffort: MoneyPerFishing,
  fixedCostPerFisherman: MoneyPerTimeFishermen,
  nonFishingIncome: MoneyPerTimeFishermen,
  catchabilityCoeff: PerFishing,
  spoiledCatchRatio: Percent,
  otherCatchRatio: Percent,
  otherValueRatio: Percent,
  initialCatch: TonnesPerTime,
  ratioOfInitialPopulationNeverLeaving: Percent,
  ratioOfMaximumEffortToAcceptableEffort: Percent,
  ratioOfInitialEffortToAcceptableEffort: Percent,
  alternativeLivelihood: ModelConfigAlternativeLivelihood,
  priceNegotiation: ModelConfigPriceNegotiation) {

  def initialGradeACatch = initialCatch * (Percent(1.0) - spoiledCatchRatio - otherCatchRatio)
}

case class ModelConfigPriceNegotiation(
  stressThreshold: Double,
  stressDecayTime: Time,
  timeToRetry: Time,
  target: Double,
  adherence: Double,
  maxAdherence: Double,
  enabled: Boolean)

case class ModelConfigAlternativeLivelihood(
  alternativeJobsAvailable: Fishermen,
  alternativeJobWorkload: FishingPerTimeFishermen,
  numberOfPeopleTrained: Fishermen,
  numberOfNonFishermenTrained: Fishermen,
  financialBenefit: MoneyPerFishing,
  culturalFishingChoiceBias: Double)

case class ModelConfigMiddleman(
  middlemanOverhead: MoneyPerTonnes,
  middlemanDesiredProfitRate: Double,
  aggressiveInboundPriceSetting: Double,
  aggressiveOutboundPriceSetting: Double,
  fisherCostsEstimate: MoneyPerTonnes,
  lastDemand: TonnesPerTime,
  desireToIncreaseMarketShare: Double)

case class ModelConfigCommercialFisher(
  initialEffort: FishingPerTime,
  growthRate: PerTime,
  catchabilityCoeff: PerFishing,
  initialCatch: TonnesPerTime)

case class ModelConfigSingleSellerSupportAvailable(singleSellerOperationKnowledgeAndSupport: Percent, capitalSupport: Percent)

case class RockefellerConfig(costOfFishingFixedIntervention: Intervention[MoneyPerTimeFishermen],
  interventionCostOfFishingVariable: Intervention[MoneyPerFishing],
  interventionCostOfLiving: Intervention[MoneyPerTimeFishermen],
  interventionProductivity: Intervention[PerFishing],
  interventionOtherCatchRatio: Intervention[Percent],
  interventionSpoiledCatchRatio: Intervention[Percent],
  interventionCompetitiveness: Intervention[Percent],
  interventionSingleSellerOperationKnowledgeAndSupport: Intervention[Percent],
  interventionCapitalSupport: Intervention[Percent],
  interventionFinancialBenefitOfAlternative: Intervention[MoneyPerFishing],
  interventionNumberOfPeopleTrained: Intervention[Fishermen],
  interventionAlternativeJobsAvailable: Intervention[Fishermen],
  interventionAlternativeJobWorkload: Intervention[FishingPerTimeFishermen],
  quotaType: QuotaType,
  interventionMSYPercent: PercentIntervention[Percent])

