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

case class ModelConfigUIExporter(val exporterOverhead: Double,
  val exporterDesiredProfitRate: Double,
  val aggressiveInboundPriceSetting: Double,
  val aggressiveOutboundPriceSetting: Double,
  val middlemanCostEstimate: Double,
  val lastDemand: Double) {

  def toModel = ModelConfigExporter(exporterOverhead = MoneyPerTonnes(exporterOverhead),
    exporterDesiredProfitRate = exporterDesiredProfitRate,
    aggressiveInboundPriceSetting = aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = aggressiveOutboundPriceSetting,
    middlemanCostEstimate = MoneyPerTonnes(middlemanCostEstimate),
    lastDemand = TonnesPerTime(lastDemand))

}

object ModelConfigUIExporter {
  implicit val modelConfigUIExporterFormat = Json.format[ModelConfigUIExporter]
}

