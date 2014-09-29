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

case class ModelConfigUIFish(
  val growthRate: Double,
  val ratioOfStockToK: Double,
  val ratioOfCatchToGrowthOfCurrentStock: Double,
  val initialTotalCatch: Double) {

  def toModel = {
    val carryingCapacity = calculateCarryingCapacity
    val biomass = carryingCapacity * ratioOfStockToK

    ModelConfigFish(biomass = Tonnes(biomass),
      carryingCapacity = Tonnes(carryingCapacity),
      growthRate = PerTime(growthRate))
  }

  def calculateCarryingCapacity = {
    // X = stock (a.k.a. biomass), K = carrying capacity, r = growthRate

    // The above values are based on the fact that FAO has "fully exploited" status for Tuna in Philippines
    // Status definitions from: http://www.fao.org/docrep/009/a0653e/a0653e04.htm
    //  Not known (N): not much information is available to make a judgement;
    //	Underexploited (U): undeveloped or new fishery. Believed to have a significant potential for expansion in total production;
    //	Moderately exploited (M): exploited with a low fishing effort. Believed to have some limited potential for expansion in total production;
    //	Fully exploited (F): the fishery is operating at or close to optimal yield/effort, with no expected room for further expansion;
    //	Overexploited (O): the fishery is being exploited above the optimal yield/effort which is believed to be sustainable in the long term, with no potential room for further expansion and a higher risk of stock depletion/collapse;
    //	Depleted (D): catches are well below historical optimal yields, irrespective of the amount of fishing effort exerted;
    //	Recovering (R): catches are again increasing after having been depleted or a collapse from a previous high.

    val growth = initialTotalCatch / ratioOfCatchToGrowthOfCurrentStock
    // growth = r * X * (1 - X/K) = r * (K * ratioOfStockToK) * ( 1 - ratioOfStockToK)
    // => K = growth / (r * ratioOfStockToK * (1 - ratioOfStockToK)
    val K = growth / (growthRate.v * ratioOfStockToK * (1.0 - ratioOfStockToK))
    K
  }
}

object ModelConfigUIFish {
  implicit val modelConfigUIFishFormat = Json.format[ModelConfigUIFish]
}

