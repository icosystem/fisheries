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

import controllers.{ InterventionUI, InputConfig, ModelConfigUI, UIInputConfigs }
import controllers.RemoveMiddlemanUI
import controllers.LimitEntriesValues
import controllers.UseTotalAllowableCatchValues

object Sweeps {

  val fishGrowth = ParameterSweep("fishGrowthRate", Seq(1.1d, 1.2d, 1.3d), (m: ModelConfigUI, v: Double) => m.copy(fish = m.fish.copy(growthRate = v)))
  val fishStockHealth = ParameterSweep("fishRatioOfStockToK", Seq(.3, .4d, .5d, .6d, .7), (m: ModelConfigUI, v: Double) => m.copy(fish = m.fish.copy(ratioOfStockToK = v)))
  val fishFishingBehavior = ParameterSweep("fishRatioOfCatchToGrowthOfCurrentStock", Seq(.8, .9d, 1d, 1.1d, 1.2), (m: ModelConfigUI, v: Double) => m.copy(fish = m.fish.copy(ratioOfCatchToGrowthOfCurrentStock = v)))

  val municipalFisherPopulationGrowth = ParameterSweep("municipalFisherPopulationGrowthRate", Seq(0.0, 0.0001, 0.0005, 0.001), (m: ModelConfigUI, v: Double) => m.copy(municipalFisher = m.municipalFisher.copy(populationSizeGrowthCoeff = v)))
  val municipalFisherRatioOfInitialPopulationNeverLeaving = ParameterSweep("municipalFisherRatioOfInitialPopulationNeverLeaving", Seq(0, 0.2, 0.5, 0.8, 1), (m: ModelConfigUI, v: Double) => m.copy(municipalFisher = m.municipalFisher.copy(ratioOfInitialPopulationNeverLeaving = v)))
  val municipalFisherRatioOfMaximumEffortToAcceptableEffort = ParameterSweep("municipalFisherRatioOfMaximumEffortToAcceptableEffort", Seq(1.5, 2), (m: ModelConfigUI, v: Double) => m.copy(municipalFisher = m.municipalFisher.copy(ratioOfMaximumEffortToAcceptableEffort = v)))
  val municipalFisherRatioOfInitialEffortToAcceptableEffort = ParameterSweep("municipalFisherRatioOfInitialEffortToAcceptableEffort", Seq(1, 1.2, 1.5), (m: ModelConfigUI, v: Double) => m.copy(municipalFisher = m.municipalFisher.copy(ratioOfInitialEffortToAcceptableEffort = v)))

  val commercialFisherGrowth = ParameterSweep("commercialFisherGrowthRate", Seq(0.0, 0.01, 0.03, 0.05, 0.08), (m: ModelConfigUI, v: Double) => m.copy(commercialFisher = m.commercialFisher.copy(growthRateCommercialFisher = v)))

  val consumerGrowth = ParameterSweep("consumerGrowthRate", Seq(0.0, 0.01, 0.03, 0.05, 0.08), (m: ModelConfigUI, v: Double) => m.copy(consumer = m.consumer.copy(consumerGrowthRate = v)))

  object Interventions {
    val noop = ParameterSweepValue("Noop", "None", (c: ModelConfigUI, v: String) => c)
    val noopSeq = Seq(noop)

    def costOfFishingFixedSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionCostOfFishingFixed
      ParameterSweep("costOfFishingFixed", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(costOfFishingFixed = InterventionUI(v, 0, 0))))
    }

    def costOfFishingVariableSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionCostOfFishingVariable
      ParameterSweep("costOfFishingVariable", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(costOfFishingVariable = InterventionUI(v, 0, 0))))
    }

    def costOfLivingSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionCostOfLiving
      ParameterSweep("costOfLiving", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(costOfLiving = InterventionUI(v, 0, 0))))
    }

    def productivitySweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionProductivity
      ParameterSweep("productivity", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(productivity = InterventionUI(v, 0, 0))))
    }

    def fishQualitySweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionFishQuality
      ParameterSweep("fishQuality", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(fishQuality = InterventionUI(v, 0, 0))))
    }

    def competitivenessSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionCompetitiveness
      ParameterSweep("competitiveness", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(competitiveness = InterventionUI(v, 0, 0))))
    }

    def removeMiddlemanSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionRemoveMiddleman
      ParameterSweep("removeMiddleman", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(removeMiddleman = RemoveMiddlemanUI.REMOVE_MIDDLEMAN.toString)))
    }

    def quotaType1Sweep = {
      // val c: InputConfig = ic.interventionQuota1 // LATER this might be used later
//      ParameterSweep("QuotaTypeTACOpenExitEntry", Seq(0.3d), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(quotaType = QuotaTypeTACOpenExitEntry.id).copy(quota1 = InterventionUI(v, 0, 0))))
      ParameterSweep("QuotaTypeTACOpenExitEntry", Seq(0.3d), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(limitEntries = LimitEntriesValues.none._2).copy(useTotalAllowableCatch = UseTotalAllowableCatchValues.limited._2).copy(totalAllowableCatch = InterventionUI(v, 0, 0))))
    }

    def quotaType2Sweep = {
      // val c: InputConfig = ic.interventionQuota1 // LATER this might be used later
//      ParameterSweep("QuotaTypeTACMaxEntry", Seq(0.3d), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(quotaType = QuotaTypeTACMaxEntry.id).copy(quota1 = InterventionUI(v, 0, 0))))
            ParameterSweep("QuotaTypeTACMaxEntry", Seq(0.3d), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(limitEntries = LimitEntriesValues.maxEntry._2).copy(useTotalAllowableCatch = UseTotalAllowableCatchValues.limited._2).copy(totalAllowableCatch = InterventionUI(v, 0, 0))))
    }

//    def quotaType3Sweep = {
//      // val c: InputConfig = ic.interventionQuota1 // LATER this might be used later
//      ParameterSweep("QuotaTypeTACFixedCommunity", Seq(0.3d), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(quotaType = QuotaTypeTACFixedCommunity.id).copy(totalAllowableCatch = InterventionUI(v, 0, 0))))
//    }

    def singleSellerOperationKnowledgeAndSupportSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionSingleSellerOperationKnowledgeAndSupport
      ParameterSweep("SingleSellerOperationKnowledgeAndSupport", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(singleSellerOperationKnowledgeAndSupport = InterventionUI(v, 0, 0))))
    }

    def capitalSupportSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionCapitalSupport
      ParameterSweep("CapitalSupport", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(capitalSupport = v)))
    }

    def financialBenefitOfAlternativeSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionFinancialBenefitOfAlternative
      ParameterSweep("FinancialBenefitOfAlternative", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(financialBenefitOfAlternative = InterventionUI(v, 0, 0))))
    }

    def numberOfPeopleTrainedSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionNumberOfPeopleTrained
      ParameterSweep("NumberOfPeopleTrained", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(numberOfPeopleTrained = InterventionUI(v, 0, 0))))
    }

    def alternativeJobsAvailableSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionAlternativeJobsAvailable
      ParameterSweep("AlternativeJobsAvailable", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(alternativeJobsAvailable = InterventionUI(v, 0, 0))))
    }

    def alternativeJobWorkloadSweep(ic: UIInputConfigs) = {
      val c: InputConfig = ic.interventionAlternativeJobWorkload
      ParameterSweep("AlternativeJobWorkload", Seq(c.max), (m: ModelConfigUI, v: Double) => m.copy(intervention = m.intervention.copy(alternativeJobWorkload = InterventionUI(v, 0, 0))))
    }

    def all(ic: UIInputConfigs) =
      noopSeq ++
        costOfFishingFixedSweep(ic) ++
        costOfFishingVariableSweep(ic) ++
        costOfLivingSweep(ic) ++
        productivitySweep(ic) ++
        fishQualitySweep(ic) ++
        competitivenessSweep(ic) ++
        removeMiddlemanSweep(ic) ++
        quotaType1Sweep ++
        quotaType2Sweep ++
//        quotaType3Sweep ++
        singleSellerOperationKnowledgeAndSupportSweep(ic) ++
        capitalSupportSweep(ic) ++
        financialBenefitOfAlternativeSweep(ic) ++
        numberOfPeopleTrainedSweep(ic) ++
        alternativeJobsAvailableSweep(ic) ++
        alternativeJobWorkloadSweep(ic)

    def allCrossProduct(ic: UIInputConfigs) =
      for (
        a <- (noopSeq ++ costOfFishingFixedSweep(ic));
        b <- (noopSeq ++ costOfFishingVariableSweep(ic));
        c <- (noopSeq ++ costOfLivingSweep(ic));
        d <- (noopSeq ++ productivitySweep(ic));
        e <- (noopSeq ++ fishQualitySweep(ic));
        f <- (noopSeq ++ competitivenessSweep(ic));
        g <- (noopSeq ++ removeMiddlemanSweep(ic));
        h <- (noopSeq ++ quotaType1Sweep);
        i <- (noopSeq ++ quotaType2Sweep);
//        j <- (noopSeq ++ quotaType3Sweep);
        k <- (noopSeq ++ singleSellerOperationKnowledgeAndSupportSweep(ic));
        l <- (noopSeq ++ capitalSupportSweep(ic));
        m <- (noopSeq ++ financialBenefitOfAlternativeSweep(ic));
        n <- (noopSeq ++ numberOfPeopleTrainedSweep(ic));
        o <- (noopSeq ++ alternativeJobsAvailableSweep(ic));
        o <- (noopSeq ++ alternativeJobWorkloadSweep(ic))
      ) yield MultiSweep(a, b, c, d, e, f, g, h, i, /*j, */ k, l, m, n, o)

  }
}