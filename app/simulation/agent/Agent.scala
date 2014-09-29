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
import simulation.unit.Tonnes
import simulation.unit.FishingPerTime
import simulation.unit.FishingPerTimeFishermen
import simulation.unit.Fishermen

abstract class Agent[S](val timeline: Timeline) {
  var currentState: S = null.asInstanceOf[S]
  var nextState: S = null.asInstanceOf[S]

  def gatherState: S

  def tick { nextState = gatherState }

  def updateState = {
    currentState = nextState
    nextState = null.asInstanceOf[S]
  }

  def ΔT = timeline.deltaT
}

trait Buyer {
  def getDemand: Tonnes
}

trait Seller {
  def totalOffer: Tonnes
}

trait Fisher[S] {
  def effortAllFishermen: IntendedGroupEffort

  var fishRelationship: FishFisherRelationship

  var currentState: S
  var nextState: S
}

case class IntendedGroupEffort(val timeDecided: Int, val effortRatePerFisherman: FishingPerTimeFishermen, val numberOfFishermen: Fishermen) {
  def totalEffort = effortRatePerFisherman * numberOfFishermen
}