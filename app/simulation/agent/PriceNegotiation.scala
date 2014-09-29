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

package simulation.agent

import simulation.unit.Time
import simulation.unit.MoneyPerTonnes
import simulation.unit.FishingPerTime
import simulation.unit.FishingPerTimeFishermen
import math.min
import simulation.unit.FishingPerTimeFishermen
import CumulativeExposureFunction.cumulativeExposure
import simulation.unit.TonnesPerTimeFishermen
import simulation.unit.Money
import simulation.unit.MoneyPerTimeFishermen
import simulation.unit.MoneyPerTimeFishermen
import simulation.unit.MoneyPerTimeFishermen
import simulation.unit.MoneyPerFishing
import play.Logger
import simulation.unit.FishingPerTimeFishermen

case class PriceNegotiation private (
  val stressThreshold: Double, // baseline
  val stressDecayTime: Time, // advanced
  val timeToRetry: Time, // baseline
  val target: Double, //baseline
  val adherence: Double, // intervention
  val maxAdherence: Double,

  // STATE variables
  val timeSinceLastAborted: Double = Double.PositiveInfinity,
  val stress: Double,

  // BOOKKEEPING these are not state, just stored by the update method for debugging purposes
  val tickStress: Double,
  val interventionEffort: FishingPerTimeFishermen,
  val lastKnownIndicatedEffort: FishingPerTimeFishermen,
  val status: PriceNegotiationStatus = NotStarted) {

  private val stressUpdateFunction = cumulativeExposure(stressDecayTime.v)(_, _, _)

  def update(
    interventionActive: Boolean,
    indicatedEffort: FishingPerTimeFishermen,
    deltaT: Time,
    expectedFinancialsWithCurrentEffort: PerFishermanFinancialRates,
    isPriceFair: Boolean,
    acceptableEffortPerFishermanPerYear: FishingPerTimeFishermen): PriceNegotiation = {

    var newTimeSinceLastAborted = timeSinceLastAborted + deltaT.v

    val newTickStress = {
      if (expectedFinancialsWithCurrentEffort.fishermanMakesEndsMeet) { 0 }
      else if (expectedFinancialsWithCurrentEffort.effortIsUnprofitable) { 1 }
      else { 1.0 - (expectedFinancialsWithCurrentEffort.totalProfitsForCombinedEffort.v / expectedFinancialsWithCurrentEffort.costOfLiving.v) }
    }

    val newStress = stressUpdateFunction(stress, newTickStress, deltaT.v)

    var newStatus: PriceNegotiationStatus = null

    var newLastKnownIndicatedEffort = if (status == Active) lastKnownIndicatedEffort else indicatedEffort

    val newInterventionEffort = {
      if (!interventionActive) {
        newStatus = Inactive
        indicatedEffort
      } else if (isPriceFair) {
        newStatus = FairPrice
        //indicatedEffort
        newLastKnownIndicatedEffort
      } else if (newTimeSinceLastAborted < timeToRetry.v) {
        // Can't retry intervention yet
        newStatus = Waiting
        indicatedEffort
      } else if (newStress >= stressThreshold) {
        // Abort Intervention
        newStatus = Abort
        newTimeSinceLastAborted = 0
//        indicatedEffort
        newLastKnownIndicatedEffort
      } else {
        newStatus = Active
        FishingPerTimeFishermen(min(newLastKnownIndicatedEffort.v, acceptableEffortPerFishermanPerYear.v) * (1 - adherence))
      }
    }
    Logger.info("Status: " + newStatus.name)
    Logger.debug(newStatus + " (" + newTimeSinceLastAborted + ") " + newTickStress + " " + newTickStress)

    PriceNegotiation(
      stressThreshold = stressThreshold,
      stressDecayTime = stressDecayTime,
      timeToRetry = timeToRetry,
      target = target,
      adherence = adherence,
      maxAdherence = maxAdherence,
      timeSinceLastAborted = newTimeSinceLastAborted,
      stress = newStress,
      tickStress = newTickStress,
      interventionEffort = newInterventionEffort,
      lastKnownIndicatedEffort = newLastKnownIndicatedEffort,
      status = newStatus)
  }

  override def toString = {
    "%s %.1f ts %.2f s %.2f eff lastk %f".format(status.name, timeSinceLastAborted * 52, tickStress, stress, lastKnownIndicatedEffort.v)
  }
}

object PriceNegotiation {
  def startPriceNegotiation(
    stressThreshold: Double, // baseline
    stressDecayTime: Time, // advanced
    timeToRetry: Time, // baseline
    target: Double, //baseline
    adherence: Double, // intervention
    maxAdherence: Double) = PriceNegotiation(stressThreshold = stressThreshold,
    stressDecayTime = stressDecayTime,
    timeToRetry = timeToRetry,
    target = target,
    adherence = adherence,
    maxAdherence = maxAdherence,
    // initial values for state variables
    timeSinceLastAborted =
      Double.PositiveInfinity, stress = 0.0,
    tickStress = Double.NaN,
    interventionEffort = null,
    lastKnownIndicatedEffort = null,
    status = NotStarted)
}