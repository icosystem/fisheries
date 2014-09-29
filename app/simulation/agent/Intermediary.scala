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

import play.Logger
import simulation.Timeline
import simulation.agent.CompactSigmoidFunction.compactSigmoid
import simulation.agent.ConstantElasticityFunction.constantElasticityFunction
import simulation.unit.Double2UnitDouble
import simulation.unit.MoneyPerTonnes
import simulation.unit.Tonnes
import simulation.unit.MoneyPerTonnes
import play.Logger

class Intermediary(val overheadPerTon: MoneyPerTonnes,
  val inboundPriceDesired: MoneyPerTonnes,
  val name: String = "NONAME")(implicit val t: Timeline) extends Agent[IntermediaryState](t) with Buyer with Seller {

  var inboundMarket: Market = null
  var outboundMarket: Market = null

  def aggressiveInboundPriceSetting: Double = currentState.aggressiveInboundPriceSetting
  def aggressiveOutboundPriceSetting: Double = currentState.aggressiveOutboundPriceSetting
  def desiredProfitRate: Double = currentState.desiredProfitRate
  def desireToIncreaseMarketShare: Double = currentState.desireToIncreaseMarketShare

  override def gatherState = {
    val bought = inboundMarket.getBought(this)
    val sold = outboundMarket.getSold(this)

    val newDemandValue = this.newDemandValue(bought, sold)
    Logger.debug("Intermediary[" + name + "] " + bought + " " + sold + " " + newDemandValue)
    currentState.copy(bought = bought, sold = sold, demand = newDemandValue)
  }

  def newDemandValue(bought: Bought, sold: Sold) = {

    val inboundPrice = bought.pricePerTon

    val outboundPriceDesired = inboundPrice * desiredProfitRate

    val outboundPriceDampeningFunction = constantElasticityFunction(outboundPriceDesired.v, 1.0, aggressiveOutboundPriceSetting)_

    val outboundPrice = sold.pricePerTon

    val outboundMultiplier = outboundPriceDampeningFunction(outboundPrice.v)

    val inboundPriceDampeningFunction = constantElasticityFunction(inboundPriceDesired.v, 1.0, -1.0 * aggressiveInboundPriceSetting)_

    val inboundMultiplier = inboundPriceDampeningFunction(inboundPrice.v).v

    val tonsSold = sold.tons

    val tonsCouldHaveSold = sold.demand

    val tonsCouldHaveBought = bought.supply

    val tonsIWantToBuy = (tonsCouldHaveBought.v + tonsCouldHaveSold.v) / 2.0
    //    val tonsIWantToBuy = math.max(tonsCouldHaveBought.v,tonsCouldHaveSold.v) 

    // Do not trade below costs

    val totalCostsPerTon = overheadPerTon

    // this will be one for any selling price above costs (meaning no dampening) but intervene when prices drop below costs, cutting purchases. Eventually at price = 0, buy 0. 

    val tradingProfitPerTon = sold.pricePerTon.v - bought.pricePerTon.v

    val doNotTradeBelowCostsMultiplier = compactSigmoid(0, 0, totalCostsPerTon.v, 1, 1)(tradingProfitPerTon)

    val desireToIncreaseMarketSharePotencialIncrease = LinearInterp.linearInterp(1, 1.1)(desireToIncreaseMarketShare)

    val minimumSaleToCostRatio = 1.18

    val awesomeSaleToCostRatio = 1.6

    val currentSaleToCostRatio = outboundPrice.v / (inboundPrice.v + totalCostsPerTon.v)

    val desireToIncreaseMarketShareMultiplier = compactSigmoid(minimumSaleToCostRatio.v, 1, awesomeSaleToCostRatio, desireToIncreaseMarketSharePotencialIncrease)(currentSaleToCostRatio)

    val combinedMultiplier = Math.sqrt(inboundMultiplier.v * outboundMultiplier.v) * desireToIncreaseMarketShareMultiplier * doNotTradeBelowCostsMultiplier

    val newDemandValue = Tonnes(combinedMultiplier * tonsIWantToBuy)
    //    if (name.equals("MIDDLEMAN"))
    Logger.info("%10s $%6.2f %6.2f %6.2f $%6.2f %6.2f %6.2f = PR %4.2f (%4.2f %4.2f) %6.2f".format(name, inboundPrice.v, bought.supply.v, bought.demand.v, outboundPrice.v, sold.supply.v, sold.demand.v, currentSaleToCostRatio, inboundMultiplier, outboundMultiplier, newDemandValue.v))
    //    Logger.info(t.now + " " + name + ": " + inboundMultiplier.v + " * " + outboundMultiplier.v + " * " + doNotTradeBelowCostsMultiplier.v + " => " + combinedMultiplier.v + " * " + tonsCouldHaveSold.v + " = " + (combinedMultiplier * tonsCouldHaveSold.v))

    newDemandValue
  }

  @deprecated("not used to compute new state", "") def expectedProfit = (currentState.sold.tons * (currentState.sold.pricePerTon - currentState.bought.pricePerTon - overheadPerTon)) / ΔT;

  override def getDemand = currentState.demand

  override def totalOffer = currentState.bought.tons

}

case class IntermediaryState(
  @deprecated("pull from inbound market instead", "") bought: Bought,
  @deprecated("pull from outbound market instead", "") sold: Sold,
  demand: Tonnes,
  aggressiveInboundPriceSetting: Double,
  aggressiveOutboundPriceSetting: Double,
  desiredProfitRate: Double,
  desireToIncreaseMarketShare: Double)

trait IntermediaryWithRockefeller extends Intermediary {
  var rockefeller: Rockefeller
}

case class Middleman(
  override val overheadPerTon: MoneyPerTonnes,
  override val inboundPriceDesired: MoneyPerTonnes)(override implicit val t: Timeline)
  extends Intermediary(overheadPerTon, inboundPriceDesired, "MIDDLEMAN")(t) with IntermediaryWithRockefeller {
  var rockefeller: Rockefeller = null

  override def gatherState = rockefeller.middlemanState(super.gatherState)
}

case class Exporter(
  override val overheadPerTon: MoneyPerTonnes,
  override val inboundPriceDesired: MoneyPerTonnes)(override implicit val t: Timeline)
  extends Intermediary(overheadPerTon, inboundPriceDesired, "EXPORTER")(t)

case class NoopIntermediary(override implicit val t: Timeline) extends Intermediary(MoneyPerTonnes(0), MoneyPerTonnes(0), "NOOP")(t) with IntermediaryWithRockefeller {
  override def gatherState = this.currentState
  override var rockefeller: Rockefeller = null
}
