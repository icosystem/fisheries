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

import simulation.Timeline
import play.Logger
import simulation.unit._

case class CommercialFisher(val initialEffort: FishingPerTime, val growthRate: PerTime)(implicit val t: Timeline) extends Agent[CommercialFisherState](t) with Fisher[CommercialFisherState] {

  var fishRelationship: FishFisherRelationship = null

  override def gatherState = {
    var tonsCaught = fishRelationship.fish.fishingResult(this).totalCatchRate * t.deltaT

    val growthThisTick = Math.pow(1.0 + growthRate.v, ΔT.v)
    val newEffort = currentState.effort * growthThisTick

    Logger.debug("COMMERCIAL FISHER NEXT " + newEffort + " " + tonsCaught)

    CommercialFisherState(t.now, newEffort, tonsCaught)
  }

  def effortAllFishermen = currentState.effortAllFishermen
}

case class CommercialFisherState(val timeCreated: Int, val effort: FishingPerTime, val haul: Tonnes) {

  def effortAllFishermen = IntendedGroupEffort(timeCreated, effort / Fishermen(1), Fishermen(1))
}
