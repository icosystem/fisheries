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

package controllers

import model._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Controller
import simulation.unit._

case class ModelConfigUIConsumerMarket(val consumerMarketSpeed: Double,
  val consumerMarketPrice: Double,
  val lastTraded: Double,
  val lastExcess: Double) {

  def toModel = ModelConfigConsumerMarket(
    consumerMarketSpeed = PerTime(consumerMarketSpeed),
    consumerMarketPrice = MoneyPerTonnes(consumerMarketPrice),
    lastTraded = TonnesPerTime(lastTraded),
    lastExcess = TonnesPerTime(lastExcess))

}

object ModelConfigUIConsumerMarket {
  implicit val modelConfigUIConsumerMarketFormat = Json.format[ModelConfigUIConsumerMarket]
}

