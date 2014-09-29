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

package com.icosystem.collections

object Tuples {

  def flatten[A, B, C](t: ((A, B), C)) = (t._1._1, t._1._2, t._2)

  def flatten[A, B, C, D](t: (((A, B), C), D)) = (t._1._1._1, t._1._1._2, t._1._2, t._2)

  def flatten[A, B, C, D, E](t: ((((A, B), C), D), E)) = (t._1._1._1._1, t._1._1._1._2, t._1._1._2, t._1._2, t._2)

  def flatten[A, B, C, D, E, F](t: (((((A, B), C), D), E), F)) = (t._1._1._1._1._1, t._1._1._1._1._2, t._1._1._1._2, t._1._1._2, t._1._2, t._2)

  def flatten[A, B, C, D, E, F, G](t: ((((((A, B), C), D), E), F), G)) = (t._1._1._1._1._1._1, t._1._1._1._1._1._2, t._1._1._1._1._2, t._1._1._1._2, t._1._1._2, t._1._2, t._2)

}

