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

import simulation.unit._

// "compact" sigmoid function: 
// At x <= x0 takes value y0
// At x >= x1 takes value y1
// Smoothly goes from y0 to y1 for x0 < x < x1
// Symmetry: At x = (x1 - x0) / 2, y = (y1 - y0) / 2
// 
object LinearInterp {
 
  def linearInterp(y0:Double,y1:Double)(x: Double): Double = y0 * (1-x) + y1 * x
  
}