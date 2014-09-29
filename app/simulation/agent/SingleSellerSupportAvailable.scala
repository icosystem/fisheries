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

import simulation.unit.Percent
import simulation.Timeline
import simulation.IntermediaryRecorder
import simulation.MarketRecorder

case class SingleSellerSupportAvailable(exporterMarket: Market with MarketRecorder, consumerMarket: ConsumerMarket)(implicit val t: Timeline) extends Agent[SingleSellerSupportAvailableState](t) {
  var rockefeller: Rockefeller = null

  def exporterMarketSold = exporterMarket.currentState.pendingSell.sold
  def consumerMarketSold = consumerMarket.currentState.pendingSell.sold

  def knowledgeAndSupport = currentState.singleSellerOperationKnowledgeAndSupport
  def capitalSupport = currentState.capitalSupport

  override def gatherState: SingleSellerSupportAvailableState = rockefeller(currentState)
}

case class SingleSellerSupportAvailableState(singleSellerOperationKnowledgeAndSupport: Percent, capitalSupport: Percent)
