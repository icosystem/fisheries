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

package simulation

import controllers.ModelConfigUI
import Sweeps.Interventions.noop

object ParameterSweep {
  def apply[V](name: String, values: Seq[V], f: (ModelConfigUI, V) => ModelConfigUI) = for (v <- values) yield { ParameterSweepValue[V](name, v, f) }
}

case class ParameterSweepValue[V](name: String, value: V, f: (ModelConfigUI, V) => ModelConfigUI) extends (ModelConfigUI => ModelConfigUI) with Sweep {
  def apply(config: ModelConfigUI) = f(config, value)
}

trait Sweep extends (ModelConfigUI => ModelConfigUI) {
  val name : String
  val value : Any  
}

case class MultiSweep(sweeps: Sweep*) extends Sweep {
  def apply(initialConfig: ModelConfigUI) = sweeps.filter(_ != noop).foldLeft(initialConfig)((config, sweep) => sweep(config))
  val name = sweeps.filter(_ != noop).map(_.name).mkString("+")
  val value = sweeps.filter(_ != noop).map(_.value).mkString("+")
}