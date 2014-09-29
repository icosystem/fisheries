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

package controllers.initial

import controllers.ModelConfigUIMunicipalFisher
import controllers.ModelConfigUICommercialFisher
import controllers.ModelConfigUIMiddleman
import controllers.ModelConfigUIFish
import controllers.ModelConfigUIInterventions
import controllers.ModelConfigUI
import controllers.ModelConfigUIExporterMarket
import controllers.ModelConfigUIConsumer
import controllers.ModelConfigUIExporter
import controllers.ModelConfigUIConsumerMarket
import controllers.ModelConfigUIMiddlemanMarket
import controllers.RemoveMiddlemanUI
import controllers.ModelConfigUIAlternativeLivelihood
import controllers.ModelConfigUISingleSellerSupportAvailable
import controllers.ModelConfigUIMunicipalFisherPriceNegotiation

object InitialValuesUIToModelConfigUI {
  def apply(i: InitialValuesUI) = {
    val fish = ModelConfigUIFish(growthRate = i.fish.growthRate.v,
      ratioOfStockToK = i.fish.ratioOfStockToK,
      ratioOfCatchToGrowthOfCurrentStock = i.fish.ratioOfCatchToGrowthOfCurrentStock,
      initialTotalCatch = i.fish.initialTotalCatch)

    val priceNegotiation = ModelConfigUIMunicipalFisherPriceNegotiation(
      stressThreshold = i.municipalFisher.priceNegotiationStressThreshold.v,
      stressDecayTime = i.municipalFisher.priceNegotiationStressDecayTime.v,
      timeToRetry = i.municipalFisher.priceNegotiationTimeToRetry.v,
      target = i.municipalFisher.priceNegotiationTarget.v,
      maxAdherence = i.municipalFisher.priceNegotiationMaxAdherence.v,
      enabled = i.municipalFisher.priceNegotiationEnabled)

    val municipalFisher = ModelConfigUIMunicipalFisher(numberOfFishermenInPopulation = i.municipalFisher.numberOfFishermenInPopulation.v,
      populationSizeGrowthCoeff = i.municipalFisher.populationSizeGrowthCoeff.v,
      effortPerFishermanPerYear = i.municipalFisher.effortPerFishermanPerYear.v,
      alternativeEffortPerFishermanPerYear = i.municipalFisher.alternativeEffortPerFishermanPerYear.v,
      effortGrowthCoeff = i.municipalFisher.effortGrowthCoeff.v,
      costOfLivingPerFisherman = i.municipalFisher.costOfLivingPerFisherman.v,
      variableCostPerUnitEffort = i.municipalFisher.variableCostPerUnitEffort.v,
      fixedCostPerFisherman = i.municipalFisher.fixedCostPerFisherman.v,
      nonFishingIncome = i.municipalFisher.nonFishingIncome.v,
      spoiledCatchRatio = i.municipalFisher.spoilCatchRatio.v,
      otherCatchRatio = i.municipalFisher.otherCatchRatio.v,
      otherValueRatio = i.municipalFisher.otherValueRatio.v,
      rMinSpoilCatch = i.municipalFisher.rMinSpoilCatch.v,
      rMinOtherCatch = i.municipalFisher.rMinOtherCatch.v,
      percentOfInitialTotalCatch = i.municipalFisher.percentOfInitialTotalCatch.v,
      ratioOfInitialPopulationNeverLeaving = i.municipalFisher.ratioOfInitialPopulationNeverLeaving.v,
      ratioOfMaximumEffortToAcceptableEffort = i.municipalFisher.ratioOfMaximumEffortToAcceptableEffort.v,
      ratioOfInitialEffortToAcceptableEffort = i.municipalFisher.ratioOfInitialEffortToAcceptableEffort.v,
      priceNegotiation = priceNegotiation)

    val alternativeLivelihood = ModelConfigUIAlternativeLivelihood(alternativeJobWorkload = i.alternativeLivelihood.alternativeJobWorkload,
      alternativeJobsAvailable = i.alternativeLivelihood.alternativeJobsAvailable,
      numberOfPeopleTrained = i.alternativeLivelihood.numberOfPeopleTrained,
      numberOfNonFishermenTrained = i.alternativeLivelihood.numberOfNonFishermenTrained,
      financialBenefitOfAlternative = i.alternativeLivelihood.financialBenefitOfAlternative,
      culturalFishingChoiceBias = i.alternativeLivelihood.culturalFishingChoiceBias)

    val commercialFisher = ModelConfigUICommercialFisher(growthRateCommercialFisher = i.commercialFisher.growthRate.v)

    val middleman = ModelConfigUIMiddleman(
      middlemanOverhead = i.middleman.middlemanOverhead.v,
      middlemanDesiredProfitRate = i.middleman.desiredProfitRate.v,
      aggressiveInboundPriceSetting = i.middleman.aggressiveInboundPriceSetting,
      aggressiveOutboundPriceSetting = i.middleman.aggressiveOutboundPriceSetting,
      fisherCostsEstimate = i.middleman.fisherCostsEstimate.v,
      lastDemand = i.middleman.lastDemand.v,
      desireToIncreaseMarketShare = i.middleman.desireToIncreaseMarketShare)

    val intervention = ModelConfigUIInterventions(costOfFishingFixed = i.intervention.costOfFishingFixed,
      costOfFishingVariable = i.intervention.costOfFishingVariable,
      costOfLiving = i.intervention.costOfLiving,
      productivity = i.intervention.productivity,
      fishQuality = i.intervention.fishQuality,
      competitiveness = i.intervention.competitiveness,
      removeMiddleman = i.intervention.removeMiddleman.toString(),
      limitEntries = i.intervention.limitEntries,
      useTotalAllowableCatch = i.intervention.useTotalAllowableCatch,
      totalAllowableCatch = i.intervention.totalAllowableCatch,
      singleSellerOperationKnowledgeAndSupport = i.intervention.singleSellerOperationKnowledgeAndSupport,
      capitalSupport = i.intervention.capitalSupport,
      financialBenefitOfAlternative = i.intervention.financialBenefitOfAlternative,
      numberOfPeopleTrained = i.intervention.numberOfPeopleTrained,
      alternativeJobsAvailable = i.intervention.alternativeJobsAvailable,
      alternativeJobWorkload = i.intervention.alternativeJobWorkload)

    val exporterMarket = ModelConfigUIExporterMarket(exporterMarketSpeed = i.exporterMarket.exporterMarketSpeed.v,
      exporterMarketPrice = i.exporterMarket.exporterMarketFishPrice.v,
      lastTraded = i.exporterMarket.lastTraded.v,
      lastExcess = i.exporterMarket.lastExcess.v)

    val consumer = ModelConfigUIConsumer(
      consumerGrowthRate = i.consumer.consumerGrowthRate.v,
      elasticityOfDemand = i.consumer.elasticityOfDemand,
      initialPopulation = i.consumer.initialPop.v)

    val exporter = ModelConfigUIExporter(exporterOverhead = i.exporter.exporterOverhead.v,
      exporterDesiredProfitRate = i.exporter.desiredProfitRate.v,
      aggressiveInboundPriceSetting = i.exporter.aggressiveInboundPriceSetting,
      aggressiveOutboundPriceSetting = i.exporter.aggressiveOutboundPriceSetting,
      middlemanCostEstimate = i.exporter.middlemanCostEstimate.v,
      lastDemand = i.exporter.lastDemand.v)

    val consumerMarket = ModelConfigUIConsumerMarket(consumerMarketSpeed = i.consumerMarket.consumerMarketSpeed.v,
      consumerMarketPrice = i.consumerMarket.consumerMarketFishPrice.v,
      lastTraded = i.consumerMarket.lastTraded.v,
      lastExcess = i.consumerMarket.lastExcess.v)

    val middlemanMarket = ModelConfigUIMiddlemanMarket(middlemanMarketSpeed = i.middlemanMarket.middlemanMarketSpeed.v,
      lastTraded = i.middlemanMarket.lastTraded.v,
      lastExcess = i.middlemanMarket.lastExcess.v,
      middlemanMarketFishPrice = i.middlemanMarket.middlemanMarketFishPrice.v)

    val singleSellerSupportAvailable = ModelConfigUISingleSellerSupportAvailable(singleSellerOperationKnowledgeAndSupport = i.singleSellerSupportAvailable.singleSellerOperationKnowledgeAndSupport.v,
      capitalSupport = i.singleSellerSupportAvailable.capitalSupport.v)

    ModelConfigUI(stepSize = i.generalSim.stepSize.v,
      years = i.generalSim.years.v,
      fish = fish,
      municipalFisher = municipalFisher,
      alternativeLivelihood = alternativeLivelihood,
      commercialFisher = commercialFisher,
      middlemanMarket = middlemanMarket,
      middleman = middleman,
      exporterMarket = exporterMarket,
      exporter = exporter,
      consumerMarket = consumerMarket,
      consumer = consumer,
      singleSellerSupportAvailable = singleSellerSupportAvailable,
      intervention = intervention,
      uiInputConfig = i.uiInputConfig,
      resultAggregationSize = i.generalSim.resultAggregationSize)
  }
}