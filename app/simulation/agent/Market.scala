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

class Market(val marketSpeed: PerTime, val ID: String = "NONAME")(implicit val t: Timeline) extends Agent[MarketState](t) {

  var seller: Seller = null
  var buyer: Buyer = null

 
  override def gatherState = {
    val tonsOffer = supply
    val tonsDemand = demand
    val tonsToBuy = min(tonsOffer, tonsDemand)
    val excessTons = tonsOffer - tonsDemand

    val transaction = FinancialTransaction(tons = tonsToBuy, pricePerTon = currentState.price, excess = excessTons)
    val currentPrice = currentState.price 

    val newPrice = Market.slidePrice(currentPrice,tonsOffer,tonsDemand, ΔT, marketSpeed)
    
    Logger.debug("%s O %8.2f D %8.2f $%8.2f $%8.2f".format(ID ,tonsOffer.v ,tonsDemand.v,currentState.price.v,newPrice.v))

    MarketState(newPrice, PendingSell(seller, transaction), PendingBuy(buyer, transaction))
  }

  def supply: Tonnes = seller.totalOffer

  def demand: Tonnes = buyer.getDemand

  def getSold(seller: Seller) = {
    if (currentState.pendingSell.seller == seller) {
      currentState.pendingSell.sold
    } else {
      throw new IllegalStateException("No pending sell for seller " + seller)
    }
  }

  def getBought(buyer: Buyer) = {
    if (currentState.pendingBuy.buyer == buyer) {
      currentState.pendingBuy.bought
    } else {
      throw new IllegalStateException("No pending buy for seller " + seller)
    }
  }

  def marketPrice = currentState.price
}

case class MarketState(val price: MoneyPerTonnes, val pendingSell: PendingSell[Seller], val pendingBuy: PendingBuy[Buyer]) {
  def volume = pendingSell.sold.tons
}

case class MiddlemanMarket(val speed: PerTime)(override implicit val t: Timeline) extends Market(speed, "MIDDLEMAN MARKET")(t)

case class ExporterMarket(val speed: PerTime)(override implicit val t: Timeline) extends Market(speed, "EXPORTER MARKET")(t)

case class ConsumerMarket(val speed: PerTime)(override implicit val t: Timeline) extends Market(speed, "CONSUMER MARKET")(t)

case class NoopMarket(override implicit val t: Timeline) extends Market(PerTime(0d), "NOOP MARKET")(t) {
  seller = new Seller { def totalOffer = `0t` }
  buyer = new Buyer { def getDemand = `0t` }
  override def gatherState = this.currentState
}

case class PendingBuy[+A <: Buyer](buyer: A, bought: Bought)

case class PendingSell[+A <: Seller](seller: A, sold: Sold)

object Market {
   def slidePrice(currentPrice:MoneyPerTonnes, tonsOffer: Tonnes, tonsDemand: Tonnes,delta:Time,speed:PerTime): MoneyPerTonnes = {
    import math.max
    import math.min
    
    val offer = tonsOffer.v
    val demand = tonsDemand.v
    val price = currentPrice.v
    val years = delta.v
  
  
    val increasedPrice:Double = if (min(offer,demand) <= 0) { price }
    else {
      price * (1.0 + (demand - offer) * years * speed.v/ offer)
      }
    
    val newPrice = increasedPrice 
    MoneyPerTonnes(newPrice)
}
  
}