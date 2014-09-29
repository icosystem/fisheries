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

object Gompertz {

  /*
 *  Generate Gompertz function such that
 *  
 *  f(-Inf) = lowAsy
 *  f(Inf) = upAsy
 *  f(x0) = y0
 *  f(x1) = y1 
 *  
 *  lowAsy can be bigger or smaller then upAsy for f increasing or decreasing
 *  
 *  
 */
  def gompertzInterp(lowerAsymptote: Double, upperAsymptote: Double, x0: Double, y0: Double, x1: Double, y1: Double)(x: Double): Double = {
    val a = upperAsymptote - lowerAsymptote
    val f0 = y0 - lowerAsymptote
    val f1 = y1 - lowerAsymptote
    val t1 = x1 - x0
    // f(0) = a exp(b) = fx0
    val b = log(f0 / a)
    // f = a e^(b e^(ct))
    // log(f/a)=b e^(ct)
    // log(log(f/a)/b) = ct
    // c = log(log(f/a)/b)/t
    val c = log(log(f1 / a) / b) / t1
    val t = x - x0
    lowerAsymptote + a * exp(b * exp(c * t))
  }

  /*
 *  f(-Inf) = lowAsy
 *  f(Inf) = upAsy
 *  f(x0) = y0
 *  f(x1) close to y1 [ f(x0)+f'(x0)*(x1-x0) = y1 ]
 */
  def gompertzSlope(lowAsy: Double, upAsy: Double, x0: Double, y0: Double, x1: Double, y1: Double)(x: Double): Double = {
    val a = upAsy - lowAsy
    val f0 = y0 - lowAsy
    val f1 = y1 - lowAsy
    val t1 = x1 - x0
    // f(0) = a exp(b) = fx0
    val b = log(f0 / a)
    // slope at 0 = m = abce^b 
    // f(t) ~ mt+f(0)
    // f(t1) ~ f1
    // mt1+f0 = f1 = a b c e^b t1 +f0
    // c = (f1-f0) / (a b e^b t1)
    val c = (f1 - f0) / (a * b * exp(b) * t1)
    //    Logger.debug(a+" "+b+" "+c+" "+exp(b))
    val t = x - x0
    lowAsy + a * exp(b * exp(c * t))
  }

}