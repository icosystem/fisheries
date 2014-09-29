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

package controllers.initial

import controllers.UIInputConfigs
import play.Logger

case class UIInputConfigDefaults(name: String, v: UIInputConfigs)

object UIInputConfigDefaults {
  val mindoroString = "mindoro"
  val powerfulMiddlemanString = "powerful middleman"
  val pessimisticMindoroString = "pessimistic mindoro"

  val mindoro = UIInputConfigDefaults(mindoroString, new { val i = Mindoro } with UIInputConfigs)
  val powerfulMiddleman = UIInputConfigDefaults(powerfulMiddlemanString, new { val i = PowerfulMiddleman } with UIInputConfigs)
  val pessimisticMindoro = UIInputConfigDefaults(pessimisticMindoroString, new { val i = PessimisticMindoro } with UIInputConfigs)

  def uiInputConfig(name: String) = name match {
    case `mindoroString` => mindoro.v
    case `powerfulMiddlemanString` => powerfulMiddleman.v
    case `pessimisticMindoroString` => pessimisticMindoro.v
    case _ => {
      Logger.error("Unable to find inputconfig for " + name)
      mindoro.v
    }
  }

  def initialValuesUI(name: String) = name match {
    case `mindoroString` => Mindoro
    case `powerfulMiddlemanString` => PowerfulMiddleman
    case `pessimisticMindoroString` => PessimisticMindoro
    case _ => {
      Logger.error("Unable to find initialValuesUI for " + name)
      Mindoro
    }
  }
}

