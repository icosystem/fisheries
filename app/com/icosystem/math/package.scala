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

package com.icosystem

package object math {

  def roundAt(p: Int)(n: Double): Double =
    if(n != null) {
    		val s = scala.math pow (10, p)
    		(scala.math round n * s) / s 
    } else n
    
  def d2(v: Double) = roundAt(2)(v)

  def d3(v: Double) = roundAt(3)(v)

  def bound(v: Double, min: Double, max: Double) = { scala.math.max(scala.math.min(max, v), min) }
}