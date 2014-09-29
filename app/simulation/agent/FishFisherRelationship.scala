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

import simulation.unit._
import simulation.Timeline
import model.QuotaType

trait FishFisherRelationship extends Agent[FishFisherRelationshipState] {
  val fish: Fish
  val fisher: Fisher[_]

  // This relationships must be kept in synch so wire them here
  fish.fisherRelationships += this
  fisher.fishRelationship = this

  override def gatherState: FishFisherRelationshipState = currentState

  def quota = currentState.quota

  def catchabilityCoeff = currentState.catchabilityCoeff

  def yearlyQuotaAmount = TonnesPerTime(currentState.quota * fish.msy)

  def quotaType = currentState.quotaType
}

case class MunicipalFishFisherRelationship(fish: Fish, fisher: Fisher[_], rockefeller: Rockefeller)(implicit val t: Timeline) extends Agent[FishFisherRelationshipState](t) with FishFisherRelationship {
  override def gatherState: FishFisherRelationshipState = rockefeller(currentState, fisher, fish)
}

case class CommercialFishFisherRelationship(fish: Fish, fisher: Fisher[_])(implicit val t: Timeline) extends Agent[FishFisherRelationshipState](t) with FishFisherRelationship

case class FishFisherRelationshipState(catchabilityCoeff: PerFishing, quotaType: QuotaType, quota: Percent)
