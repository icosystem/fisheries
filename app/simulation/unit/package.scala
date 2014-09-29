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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import model.SimulationRunChartData
import simulation.unit.DoubleUnit

package object unit {

  val `0t` = Tonnes(0)
  val `¤0` = Money(0)
  val `0%` = Percent(0)
  val `20%` = Percent(.2)
  val `40%` = Percent(.4)
  val `100%` = Percent(1)
  val `140%` = Percent(1.4)
  val `150%` = Percent(1.5)

  def max[T <: DoubleUnit](a: T, b: T) = if (a.v > b.v) a else b

  def min[T <: DoubleUnit](a: T, b: T) = if (a.v < b.v) a else b

  def pow(b: Money, e: Double) = Money(math.pow(b.v, e))

  def pow(b: MoneyPerTime, e: Double) = MoneyPerTime(math.pow(b.v, e))

  def pow(b: MoneyPerTonnes, e: Double) = MoneyPerTonnes(math.pow(b.v, e))

  def `¤`(v: Double) = Money(v)

  implicit class Double2UnitDouble(val v: Double) {
    def /(o: Tonnes) = PerTonnes(v / o.v)
    def *(o: MoneyPerTime) = MoneyPerTime(v * o.v)
    def +(o: Fishermen) = Fishermen(v + o.v)
    def +(o: FishingPerTimeFishermen) = FishingPerTimeFishermen(v + o.v)
    def +(o: FishingTimePerMoney) = FishingTimePerMoney(v + o.v)
  }

  implicit val moneyPerTimeFishermenFactory = new DoubleUnitFactory[MoneyPerTimeFishermen] {
    override def apply(v: Double): MoneyPerTimeFishermen = MoneyPerTimeFishermen(v)
  }

  implicit val moneyFactory = new DoubleUnitFactory[Money] {
    override def apply(v: Double): Money = Money(v)
  }

  implicit val moneyPerFishingFactory = new DoubleUnitFactory[MoneyPerFishing] {
    override def apply(v: Double): MoneyPerFishing = MoneyPerFishing(v)
  }

  implicit val perFishingFactory = new DoubleUnitFactory[PerFishing] {
    override def apply(v: Double): PerFishing = PerFishing(v)
  }

  implicit val percentFactory = new DoubleUnitFactory[Percent] {
    override def apply(v: Double): Percent = Percent(v)
  }

  implicit val moneyPerTimeFactory = new DoubleUnitFactory[MoneyPerTime] {
    override def apply(v: Double): MoneyPerTime = MoneyPerTime(v)
  }

  implicit val fishermenFactory = new DoubleUnitFactory[Fishermen] {
    override def apply(v: Double): Fishermen = Fishermen(v)
  }

  implicit val unitlessFactory = new DoubleUnitFactory[Unitless] {
    override def apply(v: Double): Unitless = Unitless(v)
  }

  implicit val fishingPerTimeFishermanFactory = new DoubleUnitFactory[FishingPerTimeFishermen] {
    override def apply(v: Double): FishingPerTimeFishermen = FishingPerTimeFishermen(v)
  }
}