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

import controllers.initial.InitialValuesUI
import controllers.initial.UIInputConfigDefaults
import model._
import play.api.libs.json._
import play.api.mvc._
import simulation.unit._
import controllers.initial.{ InitialValuesUI, UIInputConfigDefaults }

trait TimeInformation {
  val stepSize: Int
  val years: Int

  def ticksCalc = yearToTick(years)

  def yearToTick(year: Int) = year * ticksPerYear

  def ticksPerYear = stepSize match {
    case 1 => 365
    case 7 => 52
    case _ => 12
  }
}

case class ModelConfigUI(
  scientist: Boolean = false,
  infotable: Boolean = false,
  stepSize: Int,
  years: Int,
  resultAggregationSize: Int,
  fish: ModelConfigUIFish,
  municipalFisher: ModelConfigUIMunicipalFisher,
  alternativeLivelihood: ModelConfigUIAlternativeLivelihood,
  commercialFisher: ModelConfigUICommercialFisher,
  middlemanMarket: ModelConfigUIMiddlemanMarket,
  middleman: ModelConfigUIMiddleman,
  exporterMarket: ModelConfigUIExporterMarket,
  exporter: ModelConfigUIExporter,
  consumerMarket: ModelConfigUIConsumerMarket,
  consumer: ModelConfigUIConsumer,
  singleSellerSupportAvailable: ModelConfigUISingleSellerSupportAvailable,
  intervention: ModelConfigUIInterventions,
  uiInputConfig: String) extends TimeInformation {

  def stepSizeDay = {
    if (stepSize == 1)
      "selected=\"true\""
  }
  def stepSizeWeek = {
    if (stepSize == 7)
      "selected=\"true\""
  }
  def stepSizeMonth = {
    if (stepSize == 30)
      "selected=\"true\""
  }

  def uiInputConfigs: UIInputConfigs = UIInputConfigDefaults.uiInputConfig(uiInputConfig)

  def deltaTCalc = Time(stepSize / 365d)

  def baseline = {
    val fish = this.fish.toModel
    val municipalFisher = this.municipalFisher.baseline(initialTotalCatch = this.fish.initialTotalCatch, initialFishStock = fish.biomass.v, alternativeLivelihood = alternativeLivelihood.baseline)
    val commercialFisher = this.commercialFisher.toModel(percentOfInitialTotalCatch = (1 - this.municipalFisher.percentOfInitialTotalCatch), initialTotalCatch = this.fish.initialTotalCatch, initialFishStock = fish.biomass.v)
    val consumer = this.consumer.toModel(initialGradeACatch = municipalFisher.initialGradeACatch.v, consumerMarketFishPrice = consumerMarket.consumerMarketPrice.v)
    val rockefeller = None

    ModelConfig(ticks = ticksCalc,
      deltaT = deltaTCalc,
      fish = fish,
      municipalFisher = municipalFisher,
      commercialFisher = commercialFisher,
      middlemanMarket = middlemanMarket.toModel,
      middleman = middleman.toModel,
      exporterMarket = exporterMarket.toModel,
      exporter = exporter.toModel,
      consumerMarket = this.consumerMarket.toModel,
      consumer = consumer,
      singleSellerSupportAvailable = singleSellerSupportAvailable.toModel,
      removeMiddleman = false,
      rockefeller = rockefeller)
  }

  def interventions = {
    val fish = this.fish.toModel
    val municipalFisher = this.municipalFisher.intervention(initialTotalCatch = this.fish.initialTotalCatch, initialFishStock = fish.biomass.v, alternativeLivelihood = alternativeLivelihood.baseline)
    val commercialFisher = this.commercialFisher.toModel(percentOfInitialTotalCatch = (1 - this.municipalFisher.percentOfInitialTotalCatch), initialTotalCatch = this.fish.initialTotalCatch, initialFishStock = fish.biomass.v)
    val consumer = this.consumer.toModel(initialGradeACatch = municipalFisher.initialGradeACatch.v, consumerMarketFishPrice = consumerMarket.consumerMarketPrice.v)

    ModelConfig(ticks = ticksCalc,
      deltaT = deltaTCalc,
      fish = fish,
      municipalFisher = municipalFisher,
      commercialFisher = commercialFisher,
      middlemanMarket = middlemanMarket.toModel,
      middleman = middleman.toModel,
      exporterMarket = exporterMarket.toModel,
      exporter = exporter.toModel,
      consumerMarket = consumerMarket.toModel,
      consumer = consumer,
      singleSellerSupportAvailable = singleSellerSupportAvailable.toModel,
      removeMiddleman = if (RemoveMiddlemanUI.CURRENT_BEHAVIOR.toString() == intervention.removeMiddleman) { false } else { true },
      rockefeller = Some(rockefellerConfig(municipalFisher, fish)))
  }

  def rockefellerConfig(municipalFisher: ModelConfigMunicipalFisher, fish: ModelConfigFish): RockefellerConfig = {
    val maxSpoilReduction = municipalFisher.spoiledCatchRatio.v - this.municipalFisher.rMinSpoilCatch.v
    val maxOtherGradeReduction = municipalFisher.otherCatchRatio.v - this.municipalFisher.rMinOtherCatch.v
    val maxReduction = maxSpoilReduction + maxOtherGradeReduction
    val spoilReductionThreshold = maxSpoilReduction / maxReduction

    val interventionFishQuality = intervention.fishQuality.endValue
    val endIntervenedSpoiledCatchRatio = {
      if (interventionFishQuality < spoilReductionThreshold) {
        Percent(municipalFisher.spoiledCatchRatio.v - maxSpoilReduction * interventionFishQuality / spoilReductionThreshold)
      } else {
        Percent(this.municipalFisher.rMinSpoilCatch)
      }
    }

    val endIntervenedOtherCatchRatio = {
      if (interventionFishQuality < spoilReductionThreshold) {
        Percent(municipalFisher.otherCatchRatio.v)
      } else {
        Percent(municipalFisher.otherCatchRatio.v - maxOtherGradeReduction * (interventionFishQuality - spoilReductionThreshold) / (1 - spoilReductionThreshold))
      }
    }

    RockefellerConfig(
      costOfFishingFixedIntervention = PercentReductionIntervention(municipalFisher.fixedCostPerFisherman, intervention.costOfFishingFixed, this),
      interventionCostOfFishingVariable = PercentReductionIntervention(municipalFisher.variableCostPerUnitEffort, intervention.costOfFishingVariable, this),
      interventionCostOfLiving = PercentReductionIntervention(municipalFisher.costOfLivingPerFisherman, intervention.costOfLiving, this),
      interventionProductivity = PercentIncreaseIntervention(municipalFisher.catchabilityCoeff, intervention.productivity, this),
      interventionOtherCatchRatio = OtherCatchRatioIntervention(municipalFisher.otherCatchRatio, endIntervenedOtherCatchRatio, intervention.fishQuality, this),
      interventionSpoiledCatchRatio = SpoiledCatchRatioIntervention(municipalFisher.spoiledCatchRatio, endIntervenedSpoiledCatchRatio, intervention.fishQuality, this),
      interventionCompetitiveness = PercentIncreaseIntervention(`0%`, intervention.competitiveness, this),
      interventionSingleSellerOperationKnowledgeAndSupport = PercentIntervention(`0%`, Percent(intervention.singleSellerOperationKnowledgeAndSupport.endValue.v), intervention.singleSellerOperationKnowledgeAndSupport, this),
      interventionCapitalSupport = PercentIntervention(`0%`, `100%`, intervention.singleSellerOperationKnowledgeAndSupport, this), /* interventionCapitalSupport uses the same phasing as singleSellerOperationKnowledgeAndSupport */
      interventionFinancialBenefitOfAlternative = BaselineToDefinedValueIntervention(municipalFisher.alternativeLivelihood.financialBenefit, MoneyPerFishing(InitialValuesUI.minimumWage.v * 2), intervention.financialBenefitOfAlternative, this),
      interventionNumberOfPeopleTrained = BaselineToDefinedValueIntervention(municipalFisher.alternativeLivelihood.numberOfPeopleTrained, municipalFisher.numberOfFishermenInPopulation * 2, intervention.numberOfPeopleTrained, this),
      interventionAlternativeJobsAvailable = BaselineToDefinedValueIntervention(municipalFisher.alternativeLivelihood.alternativeJobsAvailable, municipalFisher.numberOfFishermenInPopulation * 2, intervention.alternativeJobsAvailable, this),
      interventionAlternativeJobWorkload = BaselineToDefinedValueIntervention(municipalFisher.alternativeLivelihood.alternativeJobWorkload, FishingPerTimeFishermen(40 * 52), intervention.alternativeJobWorkload, this),
      quotaType = intervention.quota,
      interventionMSYPercent = QuotaPercentMSYIntervention(Percent(0.0), intervention.totalAllowableCatchActual, this)) //startValue is irrelevant here; we need to calculate the actual start value at intervention start time, which is in the Rockefeller agent
  }

  def toQueryString = ModelConfigUI.formatModelConfigUI.writes(this).toString
}

case class InputConfig(id: String, label: String, units: String, min: Double, max: Double, step: Double, visible: Boolean, inputType: InputType.InputType, filter: String, helpText: Option[String], leftLabel: String, rightLabel: String, radioValues: Seq[(String, String)], validation: Option[ValidationUI], dynamicVisibility: Option[VisibilityUI])

object InputConfig {
  def apply(id: String, label: String, units: String, range: Bounded, visible: Boolean, inputType: InputType.InputType, filter: String = "prettynumber", helpText: String = null, leftLabel: String = "", rightLabel: String = "", radioValues: Seq[(String, String)] = Seq(), validation: Option[ValidationUI] = None, dynamicVisibility: Option[VisibilityUI] = None) =
    new InputConfig(id, label, units, range.min, range.max, range.step, visible, inputType, filter, if (helpText == null) { None } else { Some(helpText) }, leftLabel, rightLabel, radioValues, validation, dynamicVisibility)

  def intervention(id: String, label: String, units: String = "", min: Double = 0d, max: Double = 1d, step: Double = 0.01d, visible: Boolean = true, inputType: InputType.InputType = InputType.QualitativeSlider, filter: String = "", helpText: Option[String] = None, leftLabel: String = "", rightLabel: String = "", radioValues: Seq[(String, String)] = Seq(), validation: Option[ValidationUI] = None, dynamicVisibility: Option[VisibilityUI] = None) =
    new InputConfig(id, label, units, min, max, step, visible, inputType, filter, helpText, leftLabel, rightLabel, radioValues, validation, dynamicVisibility)
}

case class InputConfigTimedIntervention(id: String, label: String, units: String, min: Double, max: Double, step: Double,
  visible: Boolean, inputType: InputType.InputType, filter: String, helpText: Option[String], endLevelLeftLabel: String, endLevelRightLabel: String, validation: Option[ValidationUI])

object UIInitial {
  val stepSize = BoundedInt(7, 0, 100, 1)
  val years = BoundedInt(10, 1, 40, 1)
}

object ModelConfigUI {
  implicit val formatModelConfigUI = Json.format[ModelConfigUI]

  def parse(json: JsValue): JsResult[ModelConfigUI] = {
    Json.fromJson[ModelConfigUI](json)
  }

  implicit def configPathBindable(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[ModelConfigUI] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ModelConfigUI]] = {
      for { either <- stringBinder.bind(key, params) } yield {
        either match {
          case Right(str) => {
            parse(Json.parse(str)).asOpt match {
              case Some(mc) => Right(mc)
              case None => Left("fail")
            }
          }
          case _ => Left("fail")
        }
      }
    }

    def unbind(key: String, config: ModelConfigUI): String =
      stringBinder.unbind(key, config.toQueryString)
  }
}
