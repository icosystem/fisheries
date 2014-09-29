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

package simulation.unit

case class MoneyPerFishing(v: Double) extends DoubleUnit {
  def *(o: Double) = MoneyPerFishing(v * o.v)
  def *(o: Percent) = MoneyPerFishing(v * o.v)
  def *(o: FishingPerTimeFishermen) = MoneyPerTimeFishermen(v * o.v)
  def *(o: FishingPerTime) = MoneyPerTime(v * o.v)
}