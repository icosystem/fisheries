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

object BiasedChoice {

  /**
   * bias
   *  1 = irrational choice, all options equal
   *  0.5 = proportional choice, all options same as their inputs
   *  0 = fully rational choice, max wins. Or equally distributed among maxes.
   */
  def getWeights(bias: Double)(inputs: Double*) =
    if (bias == 0) {
      val m = inputs.max
      val v = inputs.map(x => if (x == m) 1.0 else 0.0)
      val s = v.sum
      val vnormalized = if (s == 0) v.map(_ * 0.0) else v.map(_ / s)
      vnormalized
    } else {
      val q = 1.0 / bias - 1.0
      val v = inputs.map(Math.pow(_, q))
      val s = v.sum
      val vnormalized = if (s == 0) v.map(_ * 0.0) else v.map(_ / s)
      vnormalized
    }
}