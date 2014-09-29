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

import simulation.unit.{ DoubleUnitFactory, DoubleUnit, Percent }
import controllers.{ TimeInformation, InterventionUI, ModelConfigUI }
import simulation.Timeline
import simulation.agent.LinearInterp
import simulation.agent.LinearInterp.linearInterp

trait Intervention[U <: DoubleUnit] {
  def intervenedValue(implicit t: Timeline): U
}

case class PercentIntervention[U <: DoubleUnit](startValue: U, endValue: U, startTick: Int, durationInTicks: Int)(implicit f: DoubleUnitFactory[U]) extends Intervention[U] {
  val lin = LinearInterp.linearInterp(startValue.v, endValue.v)_

  def intervenedValue(implicit t: Timeline): U = {
    var v: U = startValue
    if (t.now == -1 && startTick == 0 && durationInTicks == 0) {
      v = endValue // apply immediately before sim start
    } else if (t.now < startTick) {
      v = startValue
    } else if (t.now >= startTick + durationInTicks) {
      v = endValue
    } else {
      v = f(lin((t.now - startTick).toDouble / durationInTicks))
    }

    v
  }
}

object PercentIntervention {
  def apply[U <: DoubleUnit](startValue: U, endValue: U, interventionUIValues: InterventionUI, timeInformation: TimeInformation)(implicit f: DoubleUnitFactory[U]): PercentIntervention[U] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

case class InterventionCompetiveness(c: ModelConfigMiddleman, interventionCompetitiveness: Intervention[Percent]) {
  val competitiveScenarioAggressiveInboundPriceSetting = c.aggressiveInboundPriceSetting
  val competitiveScenarioAggressiveOutboundPriceSetting = c.aggressiveOutboundPriceSetting // 2.0
  val competitiveScenarioDesiredProfitRate = c.middlemanDesiredProfitRate // 2    
  val competitiveScenarioDesireToIncreaseMarketShare = 1

  val desiredProfitRateF = linearInterp(c.middlemanDesiredProfitRate, competitiveScenarioDesiredProfitRate)_
  val aggressiveInboundPriceSettingF = linearInterp(c.aggressiveInboundPriceSetting, competitiveScenarioAggressiveInboundPriceSetting)_
  val aggressiveOutboundPriceSettingF = linearInterp(c.aggressiveOutboundPriceSetting, competitiveScenarioAggressiveOutboundPriceSetting)_
  val desireToIncreaseMarketShareF = linearInterp(c.desireToIncreaseMarketShare, competitiveScenarioDesireToIncreaseMarketShare)_

  def intVal(implicit t: Timeline) = interventionCompetitiveness.intervenedValue(t).v

  def desiredProfitRate(implicit t: Timeline) = desiredProfitRateF(intVal)
  def aggressiveInboundPriceSetting(implicit t: Timeline) = aggressiveInboundPriceSettingF(intVal)
  def aggressiveOutboundPriceSetting(implicit t: Timeline) = aggressiveOutboundPriceSettingF(intVal)
  def desireToIncreaseMarketShare(implicit t: Timeline) = desireToIncreaseMarketShareF(intVal)
}

object PercentReductionIntervention {
  def apply[U <: DoubleUnit](startValue: U, interventionUIValues: InterventionUI, timeInformation: TimeInformation)(implicit f: DoubleUnitFactory[U]): PercentIntervention[U] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    val endPercent = Percent(1 - interventionUIValues.endValue / 2)
    val endValue = f(startValue.v * endPercent.v)
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

object PercentIncreaseIntervention {
  def apply[U <: DoubleUnit](startValue: U, interventionUIValues: InterventionUI, timeInformation: TimeInformation)(implicit f: DoubleUnitFactory[U]): PercentIntervention[U] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    val endPercent = Percent(1 + interventionUIValues.endValue)
    val endValue = f(startValue.v * endPercent.v)
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

object QuotaPercentMSYIntervention {
  def apply(startValue: Percent, interventionUIValues: InterventionUI, timeInformation: TimeInformation)(implicit f: DoubleUnitFactory[Percent]): PercentIntervention[Percent] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    val endValue = Percent(interventionUIValues.endValue)
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

object SpoiledCatchRatioIntervention {
  def apply(startValue: Percent, endValue: Percent, interventionUIValues: InterventionUI, timeInformation: TimeInformation): PercentIntervention[Percent] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

object OtherCatchRatioIntervention {
  def apply(startValue: Percent, endValue: Percent, interventionUIValues: InterventionUI, timeInformation: TimeInformation): PercentIntervention[Percent] = {
    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears
    PercentIntervention(startValue, endValue, startTick, durationInTicks)
  }
}

object BaselineToDefinedValueIntervention {
  def apply[U <: DoubleUnit](baselineValue: U, definedValue: U, interventionUIValues: InterventionUI, timeInformation: TimeInformation)(implicit f: DoubleUnitFactory[U]): PercentIntervention[U] = {

    val startTick = timeInformation.yearToTick(interventionUIValues.startYear)
    val durationInTicks = timeInformation.ticksPerYear * interventionUIValues.durationInYears

    val p = (interventionUIValues.endValue / 1d)
    val `1-p` = 1d - p
    val endValue = f(baselineValue.v * `1-p` + definedValue.v * p)

    PercentIntervention(baselineValue, endValue, startTick, durationInTicks)
  }
}
