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

import scala.collection.mutable.ArrayBuffer
import simulation.Timeline
import play.Logger
import simulation.unit._
import simulation.unit.Tonnes._
import model.QuotaTypeNone
import model.QuotaTypeTACMaxEntry
import model.QuotaTypeTACOpenExitEntry
import model.QuotaTypeTACFixedCommunity
import play.Logger
import play.Logger

case class Fish(val carryingCapacity: Tonnes, val growthRate: PerTime)(implicit val t: Timeline) extends Agent[FishState](t) {

  val fisherRelationships = ArrayBuffer[FishFisherRelationship]()

  override def gatherState = {
    val results = fishingResults
    val haulAllFishers = totalHaul(results)

    //should we grow based on the starting or ending biomass.  Right now this is doing starting biomass    
    var newBiomass = currentState.biomass + growth - haulAllFishers // (growth and totalHaul get * 1 Tick)

    newBiomass = if (newBiomass > carryingCapacity) {
      carryingCapacity
    } else {
      newBiomass
    }

    FishState(newBiomass, results)
  }

  private def fishingResults: Seq[FishingResult] = {
    var results = fisherRelationships.map(r => preliminaryFishingResult(r))
    val totalCaught = totalHaul(results)

    if (totalCaught > currentState.biomass) {
      val scalingFactor = currentState.biomass / totalCaught
      Logger.debug("scalingFactor: " + scalingFactor)
      results = results.map(r => { FishingResult(r.timeWhenFished, r.fisher, r.intendedGroupEffort, r.totalCatchRate * scalingFactor, r.actualEffortRatePerFisherman) })
    }

    results
  }

  private def preliminaryFishingResult(r: FishFisherRelationship): FishingResult = {
    val intendedGroupEffort = r.fisher.effortAllFishermen
    Logger.debug("FISH.fishingResult: t.now = " + t.now)
    Logger.debug("FISH.fishingResult: effort decided @ " + intendedGroupEffort.timeDecided)
    val numberOfFishermen = intendedGroupEffort.numberOfFishermen
    val intendedTotalEffort = intendedGroupEffort.totalEffort
    var totalPreliminaryCatchRate = r.catchabilityCoeff * intendedTotalEffort * currentState.biomass
    if (r.quotaType == QuotaTypeTACOpenExitEntry || r.quotaType == QuotaTypeTACMaxEntry || r.quotaType == QuotaTypeTACFixedCommunity) {
      var quotaCatchRate = r.yearlyQuotaAmount

      var actualTotalEffort = intendedTotalEffort
      if (totalPreliminaryCatchRate > quotaCatchRate) {
        actualTotalEffort = intendedTotalEffort * (quotaCatchRate / totalPreliminaryCatchRate)
        totalPreliminaryCatchRate = quotaCatchRate
      }

      FishingResult(t.now, r.fisher, intendedGroupEffort, totalPreliminaryCatchRate, actualTotalEffort / numberOfFishermen)
    } else {
      Logger.debug("intendedGroupEffort: " + intendedTotalEffort)
      FishingResult(t.now, r.fisher, intendedGroupEffort, totalPreliminaryCatchRate, intendedGroupEffort.effortRatePerFisherman)
    }
  }

  private def totalHaul(fishings: Seq[FishingResult]): Tonnes = {
    fishings.foldLeft(Tonnes(0d))((current: Tonnes, r: FishingResult) => current + (r.totalCatchRate * t.deltaT))
  }

  def growth = growthRate * currentState.biomass * (1 - (currentState.biomass / carryingCapacity)) * t.deltaT

  def fishingResult(fisher: Fisher[_]) = {
    val result = currentState.results.filter(r => r.fisher == fisher)
    if (result.size != 1) {
      throw new IllegalArgumentException("No fisher " + fisher + " found on fish; current registered fishers are " + fisherRelationships)
    }
    result.head
  }

  def msy = growthRate.v * carryingCapacity.v / 4d
}

case class FishState(val biomass: Tonnes, val results: Seq[FishingResult])

case class FishingResult(val timeWhenFished: Int, val fisher: Fisher[_], intendedGroupEffort: IntendedGroupEffort, val totalCatchRate: TonnesPerTime, val actualEffortRatePerFisherman: FishingPerTimeFishermen) {
  def actualTotalEffortRate = actualEffortRatePerFisherman * intendedGroupEffort.numberOfFishermen
  def catchPerUnitEffort = totalCatchRate / actualTotalEffortRate
  def actualTotalCatchRatePerFisherman = totalCatchRate / intendedGroupEffort.numberOfFishermen
}

