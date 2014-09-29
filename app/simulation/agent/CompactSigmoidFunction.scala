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
object CompactSigmoidFunction {
 
  def compactSigmoid(x0: Double, y0: Double, x1: Double,y1:Double,steepness: Double = 1.0)(x: Double): Double = {
	  if (x<=x0) {
	    y0
	  }
	  else if (x >= x1) {
	    y1
	  }
	  else {
	    val offset = (x - x0) / (x1 - x0) * Math.PI
	    val cosine = -Math.cos(offset) 
	    val sigmoid = (cosine + 1.0) / 2.0 // a value between 0 and 1
	    val steepSigmoid = Math.pow(sigmoid,steepness) // still a value between 0 and 1, closer to 0 if steepness > 1.0  
	    val translation = y0 + steepSigmoid * (y1 - y0)
	    translation
	  }
  }
  
  def compactSigmoid3points(x0: Double, y0: Double, x1: Double,y1:Double,xa:Double,ya:Double)(x: Double) {
    val offset = (xa - x0) / (x1 - x0) * Math.PI
    val cosine = -Math.cos(offset) 
    val v = (cosine + 1.0) / 2.0
    val u = (ya-y0) / (y1 - y0)
    val p = math.log(u) / math.log(v)
    compactSigmoid(x0,y0,y1,y1,p)(x)
  }
  
}