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

sealed trait PriceNegotiationStatus {
  val name: String
  val number: Double
  def numberOrNeg1(p: PriceNegotiationStatus) = if (p == this) this.number else -1d
}
object NotStarted extends PriceNegotiationStatus {
  val name = "Not Started"
  val number = 0d
}
object Inactive extends PriceNegotiationStatus {
  val name = "Inactive"
  val number = 1d
}
object Active extends PriceNegotiationStatus {
  val name = "Active"
  val number = 2d
}
object Waiting extends PriceNegotiationStatus {
  val name = "Waiting"
  val number = 3d
}
object Abort extends PriceNegotiationStatus {
  val name = "Abort"
  val number = 4d
}
object FairPrice extends PriceNegotiationStatus {
  val name = "Fair Price"
  val number = 5d
}