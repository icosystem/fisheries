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
import simulation.Timeline
import simulation.unit._
import simulation.unit.TonnesPerMoney
import simulation.agent.ConstantElasticityFunction._
import play.Logger

case class Consumer(val growthRate: PerTime, val initialPrice: MoneyPerTonnes, val ε: Double, val initialDemandPerPerson: TonnesPerTimePeople)(implicit val t: Timeline) extends Agent[ConsumerState](t) with Buyer {
  val cef = constantElasticityFunction(initialPrice.v, initialDemandPerPerson.v, ε)_

  var consumerMarket: Market = null

  override def gatherState = {
    var bought = consumerMarket.getBought(this)
    val price = bought.pricePerTon.v

    val demandPerCapitaThisTick: TonnesPerPeople = TonnesPerTimePeople(cef(price)) * ΔT
    val demandThisTick = currentState.population * demandPerCapitaThisTick

    val growthThisTick = Math.pow(1.0 + growthRate.v, ΔT.v)

    val newPopulation = currentState.population * growthThisTick

    Logger.info("CONSUMER demand @ ε=%4.2f %10.2f = %10.2f".format(ε,price,demandThisTick.v))

    ConsumerState(newPopulation, demandThisTick)
  }

  override def getDemand = currentState.demand

}

case class ConsumerState(val population: People, val demand: Tonnes) {
  def demandPerCapita = demand / population
}
