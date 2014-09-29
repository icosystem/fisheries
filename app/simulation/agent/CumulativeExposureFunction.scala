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

import math.log
import math.exp
import math.pow

object CumulativeExposureFunction {

  
  def cumulativeExposure(timeSpanForHalfImportance: Double)(cumulativeExposurePreviousTick: Double, exposureThisTick: Double,deltaT: Double): Double = {
    val t0 = timeSpanForHalfImportance
  	val u = pow((1-0.5),-1.0/t0)
  	val p = exposureThisTick
  	val P0 = cumulativeExposurePreviousTick
    val discount = pow(u,-deltaT)
  	val P = p *(1-discount) + discount * P0
//	System.out.println("t0 "+t0+" u "+u+" p "+p+" P0 "+P0+" P "+P)
  	P
  }

  
def cumulativeExposure(timeSpan: Double,fractionReached:Double)(cumulativeExposurePreviousTick: Double, exposureThisTick: Double,deltaT: Double): Double = {
    val t0 = timeSpan
    val phi = fractionReached
  	val u = pow((1-phi),-1.0/t0)
  	val p = exposureThisTick
  	val P0 = cumulativeExposurePreviousTick
    val discount = pow(u,-deltaT)
  	val P = p *(1-discount) + discount * P0
//  	System.out.println("t0 "+t0+" u "+u+" p "+p+" P0 "+P0+" P "+P)
  	P
  }

}