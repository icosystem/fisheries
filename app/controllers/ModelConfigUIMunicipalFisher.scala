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

import model._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Controller
import simulation.unit._
import play.Logger
import com.icosystem.math._

case class ModelConfigUIMunicipalFisher(numberOfFishermenInPopulation: Double,
  populationSizeGrowthCoeff: Double,
  effortPerFishermanPerYear: Double,
  alternativeEffortPerFishermanPerYear: Double,
  effortGrowthCoeff: Double,
  costOfLivingPerFisherman: Double,
  variableCostPerUnitEffort: Double,
  fixedCostPerFisherman: Double,
  nonFishingIncome: Double,
  spoiledCatchRatio: Double,
  otherCatchRatio: Double,
  otherValueRatio: Double,
  rMinSpoilCatch: Double,
  rMinOtherCatch: Double,
  percentOfInitialTotalCatch: Double,
  ratioOfInitialPopulationNeverLeaving: Double,
  ratioOfMaximumEffortToAcceptableEffort: Double,
  ratioOfInitialEffortToAcceptableEffort: Double,
  priceNegotiation: ModelConfigUIMunicipalFisherPriceNegotiation) {

  def baseline(initialTotalCatch: Double, initialFishStock: Double, alternativeLivelihood: ModelConfigAlternativeLivelihood) = {
    val initialCatch = percentOfInitialTotalCatch * initialTotalCatch
    // catch = q * E * X
    val catchabilityCoeff = (initialCatch / initialFishStock) / (numberOfFishermenInPopulation * effortPerFishermanPerYear)

    var spoiledCatchRatioValidated = spoiledCatchRatio
    var otherCatchRatioValidated = otherCatchRatio
    if ((spoiledCatchRatio + otherCatchRatio).v > 1d) {
      Logger.error("baseline: spoiled catch ratio & other catch ratio are should sum to < 1, but spoiledCatchRatio=" + spoiledCatchRatioValidated + " otherCatchRatio=" + otherCatchRatioValidated)
      spoiledCatchRatioValidated = bound(spoiledCatchRatioValidated, 0, 1)
      otherCatchRatioValidated = 0;
    }

    ModelConfigMunicipalFisher(numberOfFishermenInPopulation = Fishermen(numberOfFishermenInPopulation),
      populationSizeGrowthCoeff = FishermenPerMoney(populationSizeGrowthCoeff),
      effortPerFishermanPerYear = FishingPerTimeFishermen(effortPerFishermanPerYear),
      alternativeEffortPerFishermanPerYear = FishingPerTimeFishermen(alternativeEffortPerFishermanPerYear),
      effortGrowthCoeff = FishingPerMoney(effortGrowthCoeff),
      costOfLivingPerFisherman = MoneyPerTimeFishermen(costOfLivingPerFisherman),
      variableCostPerUnitEffort = MoneyPerFishing(variableCostPerUnitEffort),
      fixedCostPerFisherman = MoneyPerTimeFishermen(fixedCostPerFisherman),
      nonFishingIncome = MoneyPerTimeFishermen(nonFishingIncome),
      catchabilityCoeff = PerFishing(catchabilityCoeff),
      spoiledCatchRatio = Percent(spoiledCatchRatioValidated),
      otherCatchRatio = Percent(otherCatchRatioValidated),
      otherValueRatio = Percent(otherValueRatio),
      initialCatch = TonnesPerTime(initialCatch),
      ratioOfInitialPopulationNeverLeaving = Percent(ratioOfInitialPopulationNeverLeaving),
      ratioOfMaximumEffortToAcceptableEffort = Percent(ratioOfMaximumEffortToAcceptableEffort),
      ratioOfInitialEffortToAcceptableEffort = Percent(ratioOfInitialEffortToAcceptableEffort),
      alternativeLivelihood = alternativeLivelihood,
      priceNegotiation = priceNegotiation.baseline)
  }

  def intervention(initialTotalCatch: Double, initialFishStock: Double, alternativeLivelihood: ModelConfigAlternativeLivelihood) = {
    baseline(initialTotalCatch, initialFishStock, alternativeLivelihood).copy(priceNegotiation = priceNegotiation.intervention)
  }
}

object ModelConfigUIMunicipalFisher {
  implicit val modelConfigUIFisherFormat = Json.format[ModelConfigUIMunicipalFisher]
}

case class ModelConfigUIAlternativeLivelihood(
  alternativeJobWorkload: Double /* number of hours of alternative work available per week per job  IE 15hrs wk job */ ,
  alternativeJobsAvailable: Double /* total number of jobs */ ,
  numberOfPeopleTrained: Double,
  numberOfNonFishermenTrained: Double,
  financialBenefitOfAlternative: Double,
  culturalFishingChoiceBias: Double) {

  def baseline = ModelConfigAlternativeLivelihood(
    alternativeJobsAvailable = Fishermen(alternativeJobsAvailable),
    alternativeJobWorkload = FishingPerTimeFishermen(alternativeJobWorkload * 52), // converting from per week to per year
    numberOfPeopleTrained = Fishermen(numberOfPeopleTrained),
    numberOfNonFishermenTrained = Fishermen(numberOfNonFishermenTrained),
    financialBenefit = MoneyPerFishing(financialBenefitOfAlternative),
    culturalFishingChoiceBias = culturalFishingChoiceBias)
}

object ModelConfigUIAlternativeLivelihood {
  implicit val modelConfigUIAlternativeLivelihoodFormat = Json.format[ModelConfigUIAlternativeLivelihood]
}

object AlternativeLivelihoodUI extends Enumeration {
  type AlternativeLivelihoodUI = Value
  val EXISTS, DOES_NOT_EXIST = Value
}
