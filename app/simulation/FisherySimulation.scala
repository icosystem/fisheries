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

import scala.collection.immutable.Seq
import model.ModelConfig
import simulation.agent._
import model.SimulationRunChartData
import simulation.unit._

object FisherySimulation extends (ModelConfig => SimulationResults) {

  def apply(c: ModelConfig) = {
    implicit val timeline = Timeline(c.ticks, c.deltaT)

    val valueChain = if (c.removeMiddleman) { ValueChainBuilderNoMiddleMan(c).asChain }
    else { ValueChainBuilderFull(c).asChain }
//    else { ValueChainBuilderNoIntermediaries(c).asChain } // option for testing fisherman feedback loops in isolation from intermediaries

    while (timeline.tick) {
      valueChain.agents.foreach(_.tick)
      valueChain.agents.foreach(_.updateState)
    }

    SimulationResults(valueChain)
  }

}

case class Timeline(end: Int, deltaT: Time = Time(1.0)) {
  var now = -1;

  def tick() = {
    now += 1
    now <= end
  }
}

