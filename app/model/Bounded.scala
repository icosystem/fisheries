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

package model

import simulation.unit.DoubleUnit

trait Bounded {
  def min: Double
  def max: Double
  def step: Double
}
case class BoundedUnit[D <: DoubleUnit](initialValue: D, minValue: D, maxValue: D, stepValue: D) extends Bounded {
  def d = initialValue
  def v = initialValue.v
  def min = minValue.v
  def max = maxValue.v
  def step = stepValue.v
}
case class BoundedInt(initialValue: Int, minValue: Int, maxValue: Int, stepValue: Int) extends Bounded {
  def v = initialValue
  def min = minValue
  def max = maxValue
  def step = stepValue
}
case class BoundedDouble(initialValue: Double, minValue: Double, maxValue: Double, stepValue: Double) extends Bounded {
  def v = initialValue
  def min = minValue
  def max = maxValue
  def step = stepValue
}