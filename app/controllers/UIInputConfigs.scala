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

package controllers

import model.BoundedDouble
import InputType._
import controllers.initial.InitialValuesUI
import controllers.initial.Mindoro
import controllers.initial.PowerfulMiddleman
import controllers.initial.PowerfulMiddleman
import InputConfig._
import model.QuotaTypeNone
import model.QuotaTypeTACMaxEntry
import model.QuotaTypeTACFixedCommunity
import model.QuotaTypeTACOpenExitEntry

trait UIInputConfigs {
  val i: InitialValuesUI

  val stepSize = InputConfig("stepSize", "Simulation Step Size", "{} days", i.generalSim.stepSize, false, NumberField)
  val years = InputConfig("years", "Simulation Length", "{} years", i.generalSim.years, true, Slider)

  // Fish
  val growthRate = InputConfig("fish.growthRate", "Max. Growth Rate (r)", "{} /year", i.fish.growthRate, true, Slider)
  val ratioOfStockToK = InputConfig("fish.ratioOfStockToK", "Initial Stock Health (X/K)", "{}", i.fish.ratioOfStockToK, true, QualitativeSlider, leftLabel = "Collapsed", rightLabel = "Carrying Capacity")
  val ratioOfCatchToGrowthOfCurrentStock = InputConfig("fish.ratioOfCatchToGrowthOfCurrentStock", "Effort at Start of Simulation", "{}", i.fish.ratioOfCatchToGrowthOfCurrentStock, true, QualitativeSliderWithNumbers, leftLabel = "Under-fishing", rightLabel = "Overfishing")
  val initialTotalCatch = InputConfig("fish.initialTotalCatch", "Initial Total Catch", "{} tons", i.fish.initialTotalCatch, true, Slider, helpText = "The initial municipal plus commercial catch per year in tons")

  // Municipal Fisher
  val numberOfFishermenInPopulation = InputConfig("municipalFisher.numberOfFishermenInPopulation", "Initial Population of Fishers", "{} households", i.municipalFisher.numberOfFishermenInPopulation, true, Slider)
  val ratioOfInitialPopulationNeverLeaving = InputConfig("municipalFisher.ratioOfInitialPopulationNeverLeaving", "Cultural Fishers", "{}", i.municipalFisher.ratioOfInitialPopulationNeverLeaving, true, Slider, "percentage:1", helpText = "Cultural (or career) fisherman who will continue to fish irregardless of  conditions. Expressed as a percentage of the initial population of municipal fisherman.")
  val populationSizeGrowthCoeff = InputConfig("municipalFisher.populationSizeGrowthCoeff", "Population Growth Coeff.", "{}", i.municipalFisher.populationSizeGrowthCoeff, true, Slider)
  val effortPerFishermanPerYear = InputConfig("municipalFisher.effortPerFishermanPerYear", "Initial Fishing Effort", "{} hrs/household/yr", i.municipalFisher.effortPerFishermanPerYear, true, Slider)
  val effortGrowthCoeff = InputConfig("municipalFisher.effortGrowthCoeff", "Effort Growth Coeff.", "{}", i.municipalFisher.effortGrowthCoeff, true, Slider, "number:3")
  val costOfLivingPerFisherman = InputConfig("municipalFisher.costOfLivingPerFisherman", "Cost of Living", "${}/household/yr", i.municipalFisher.costOfLivingPerFisherman, true, Slider)
  val variableCostPerUnitEffort = InputConfig("municipalFisher.variableCostPerUnitEffort", "Variable Cost of Fishing", "${}/hr", i.municipalFisher.variableCostPerUnitEffort, true, Slider)
  val fixedCostPerFisherman = InputConfig("municipalFisher.fixedCostPerFisherman", "Fixed Cost of Fishing", "${}/household/yr", i.municipalFisher.fixedCostPerFisherman, true, Slider)
  val nonFishingIncome = InputConfig("municipalFisher.nonFishingIncome", "Other Fixed Income", "${}/household/yr", i.municipalFisher.nonFishingIncome, true, Slider)
  val spoiledCatchRatio = InputConfig("municipalFisher.spoiledCatchRatio", "Catch Spoilage Rate", "{}", i.municipalFisher.spoilCatchRatio, true, Slider)
  val otherCatchRatio = InputConfig("municipalFisher.otherCatchRatio", "Catch Other Rate", "{}", i.municipalFisher.otherCatchRatio, true, Slider)
  val otherValueRatio = InputConfig("municipalFisher.otherValueRatio", "Value of Other Grade Catch", "{}", i.municipalFisher.otherValueRatio, true, Slider, validation = Some(ValidationUI("spoilageAndOtherCatchRatioInvalid(config.municipalFisher.spoiledCatchRatio,config.municipalFisher.otherCatchRatio)", "'Catch Spoilage Rate' and 'Catch Other Rate' must sum to less than 1")))
  val percentOfInitialTotalCatch = InputConfig("municipalFisher.percentOfInitialTotalCatch", "Percent of Total Catch (vs Commercial)", "{}", i.municipalFisher.percentOfInitialTotalCatch, true, QualitativeSliderWithNumbers, "percentage:1", leftLabel = "0%", rightLabel = "100%")
  val alternativeEffortPerFishermanPerYear = InputConfig("municipalFisher.alternativeEffortPerFishermanPerYear", "Initial Alternative Work Effort", "{} hrs/fisher/yr", i.municipalFisher.alternativeEffortPerFishermanPerYear, true, Slider)
  val priceNegotiationStressThreshold = InputConfig("municipalFisher.priceNegotiation.stressThreshold", "Price Negotiation Stress Threshold", "{}", i.municipalFisher.priceNegotiationStressThreshold, true, Slider)
  val priceNegotiationTimeToRetry = InputConfig("municipalFisher.priceNegotiation.timeToRetry", "Price Negotiation Time to Retry", "{} days", i.municipalFisher.priceNegotiationTimeToRetry, true, Slider)
  val priceNegotiationTarget = InputConfig("municipalFisher.priceNegotiation.target", "Fair Price Negotiation Target", "{} x basic needs", i.municipalFisher.priceNegotiationTarget, true, Slider)
  val priceNegotiationMaxAdherence = InputConfig("municipalFisher.priceNegotiation.maxAdherence", "Price Negotiation Max Adherence", "{}", i.municipalFisher.priceNegotiationMaxAdherence, true, QualitativeSliderWithNumbers, "percentage:1", leftLabel = "0%", rightLabel = "100%")

  // Municipal Fisher Alternative Livelihood
  val numberOfPeopleTrained = InputConfig("alternativeLivelihood.numberOfPeopleTrained", "# of Fishers with Alternative Training", "{} fishers", i.alternativeLivelihood.numberOfPeopleTrained, true, Slider)
  val alternativeJobsAvailable = InputConfig("alternativeLivelihood.alternativeJobsAvailable", "# of Alternative Jobs Available", "{} jobs", i.alternativeLivelihood.alternativeJobsAvailable, true, Slider)
  val alternativeJobWorkload = InputConfig("alternativeLivelihood.alternativeJobWorkload", "Alternative Job Workload", "{} hrs/week/job", i.alternativeLivelihood.alternativeJobWorkload, true, Slider)
  val financialBenefitOfAlternative = InputConfig("alternativeLivelihood.financialBenefitOfAlternative", "Financial Benefit of Alternative Work", "${}/hr", i.alternativeLivelihood.financialBenefitOfAlternative, true, Slider)
  val culturalFishingChoiceBias = InputConfig("alternativeLivelihood.culturalFishingChoiceBias", "Preference for Fishing vs Alternative", "{}", i.alternativeLivelihood.culturalFishingChoiceBias, true, QualitativeSliderWithNumbers, leftLabel = "None", rightLabel = "Significant")

  // Commercial Fisher
  val growthRateCommercialFisher = InputConfig("commercialFisher.growthRateCommercialFisher", "Growth Rate of Commercial Fishers", "{}", i.commercialFisher.growthRate, true, Slider)

  // Middleman Market
  val middlemanMarketFishPrice = InputConfig("middlemanMarket.middlemanMarketFishPrice", "Initial fish price (p)", "${}/ton", i.middlemanMarket.middlemanMarketFishPrice, true, Slider)
  val middlemanMarketSpeed = InputConfig("middlemanMarket.middlemanMarketSpeed", "Middleman Market Speed", "{}", i.middlemanMarket.middlemanMarketSpeed, false, Slider)

  // Middleman
  val middlemanOverhead = InputConfig("middleman.middlemanOverhead", "Overhead", "${}/ton", i.middleman.middlemanOverhead, true, Slider)
  val middlemanDesiredProfitRate = InputConfig("middleman.middlemanDesiredProfitRate", "Desired Profit", "{} (xCost)", i.middleman.desiredProfitRate, true, Slider)

  // Exporter Market
  val exporterMarketSpeed = InputConfig("exporterMarket.exporterMarketSpeed", "Exporter Market Speed", "{}", i.exporterMarket.exporterMarketSpeed, false, Slider)
  val exporterMarketPrice = InputConfig("exporterMarket.exporterMarketPrice", "Exporter Market Price", "${}/ton", i.exporterMarket.exporterMarketFishPrice, true, Slider)

  // Exporter
  val exporterOverhead = InputConfig("exporter.exporterOverhead", "Overhead", "${}/ton", i.exporter.exporterOverhead, true, Slider)
  val exporterDesiredProfitRate = InputConfig("exporter.exporterDesiredProfitRate", "Desired Profit", "{} (xCost)", i.exporter.desiredProfitRate, true, Slider)

  // Consumer Market
  val consumerMarketSpeed = InputConfig("consumerMarket.consumerMarketSpeed", "Consumer Market Speed", "{}", i.consumerMarket.consumerMarketSpeed, false, Slider)
  val consumerMarketPrice = InputConfig("consumerMarket.consumerMarketPrice", "Consumer Market Price", "${}/ton", i.consumerMarket.consumerMarketFishPrice, true, Slider)

  // Consumer
  val consumerGrowthRate = InputConfig("consumer.consumerGrowthRate", "Growth Rate", "{}", i.consumer.consumerGrowthRate, true, Slider, "percentage:1")

  // Intervention
  val interventionCostOfFishingFixed = intervention("intervention.costOfFishingFixed", "Capital to Decrease Fixed Fishing Costs", leftLabel = "Full Cost", rightLabel = "Half Cost", inputType = QualitativeSliderWithDateRange)
  val interventionCostOfFishingVariable = intervention("intervention.costOfFishingVariable", "Capital to Decrease Variable Fishing Costs", leftLabel = "Full Cost", rightLabel = "Half Cost", inputType = QualitativeSliderWithDateRange)
  val interventionCostOfLiving = intervention("intervention.costOfLiving", "Capital to Decrease Cost of Living", leftLabel = "Full Cost", rightLabel = "Half Cost", inputType = QualitativeSliderWithDateRange)
  val interventionProductivity = intervention("intervention.productivity", "Capital to Increase Productivity", leftLabel = "Current", rightLabel = "Double", inputType = QualitativeSliderWithDateRange)
  val interventionFishQuality = intervention("intervention.fishQuality", "Capital to Increase Quality of Fish", leftLabel = "Current", rightLabel = "Best", inputType = QualitativeSliderWithDateRange)
  val interventionCompetitiveness = intervention("intervention.competitiveness", "Middlemen Competition", leftLabel = "Monopolistic", rightLabel = "Competitive", inputType = QualitativeSliderWithDateRange)
  val interventionRemoveMiddleman = intervention("intervention.removeMiddleman", "Remove Middlemen", radioValues = Seq(("Current Middleman Behavior", RemoveMiddlemanUI.CURRENT_BEHAVIOR.toString), ("Remove Middleman", RemoveMiddlemanUI.REMOVE_MIDDLEMAN.toString)), inputType = Radio)
  val interventionLimitEntries = intervention("intervention.limitEntries", "Limit # Fishing Boats", radioValues = LimitEntriesValues.seq, inputType = Radio)
  val interventionUseTotalAllowableCatch = intervention("intervention.useTotalAllowableCatch", "Limit Total Catch",  radioValues = UseTotalAllowableCatchValues.seq, inputType = Radio)
  val interventionTotalAllowableCatch = intervention("intervention.totalAllowableCatch", "Catch Limited to", units = "{} MSY", max = 1.5, step = 0.01, inputType = QualitativeSliderWithDateRange, filter = "interventionAsPercent")
  val interventionSingleSellerOperationKnowledgeAndSupport = intervention("intervention.singleSellerOperationKnowledgeAndSupport", "Assistance in Establishing a Single Seller Operation", leftLabel = "None", rightLabel = "Full Assistance", inputType = QualitativeSliderWithDateRange)
  val interventionCapitalSupport = intervention("intervention.capitalSupport", "Capital to Support Product Withholding During Low Price Conditions (matches Single Seller Operation phasing)", leftLabel = "No Capital", rightLabel = "Full Investment", inputType = QualitativeSlider, dynamicVisibility = Some(VisibilityUI("config.intervention.singleSellerOperationKnowledgeAndSupport.endValue > 0")))
  val interventionFinancialBenefitOfAlternative = intervention("intervention.financialBenefitOfAlternative", "Increase Financial Benefit of Alternative Livelihoods", leftLabel = "Current", rightLabel = "3x Min. Wage", inputType = QualitativeSliderWithDateRange)
  val interventionNumberOfPeopleTrained = intervention("intervention.numberOfPeopleTrained", "Increase # of Fishers Trained in Alternative Livelihoods", leftLabel = "Current", rightLabel = "Double Init. Pop.", inputType = QualitativeSliderWithDateRange)
  val interventionAlternativeJobsAvailable = intervention("intervention.alternativeJobsAvailable", "Increase Number of Alternative Livelihood Jobs", leftLabel = "Current", rightLabel = "Double Init. Pop.", inputType = QualitativeSliderWithDateRange)
  val interventionAlternativeJobWorkload = intervention("intervention.alternativeJobWorkload", "Increase Workload of Alternative Livelihood Jobs", leftLabel = "Current", rightLabel = "Full Time", inputType = QualitativeSliderWithDateRange)
}

object LimitEntriesValues {
  val none = ("None", "none")
  val maxEntry = ("Max entry", "max_entry")
  val seq = Seq(none, maxEntry)
}

object UseTotalAllowableCatchValues {
  val unlimited = ("Unlimited", "unlimited")
  val limited = ("Limited", "limited")
  val seq = Seq(unlimited, limited)
}
