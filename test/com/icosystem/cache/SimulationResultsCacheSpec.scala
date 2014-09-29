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

package com.icosystem.cache

import controllers.ModelConfigUI
import controllers.initial.{Mindoro, InitialValuesUIToModelConfigUI}
import model.ModelConfig
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class SimulationResultsCacheSpec extends Specification {

  "getOrRun" should {

    "return same id no matter how many times the same config is sent" in {
      val cache = new SimulationResultsCache()
      val config = InitialValuesUIToModelConfigUI(Mindoro).copy(years = 1)

      val runIds = (for(i <- 1 to 20) yield { () => cache.getOrRun(config) }).par.map(_()).seq

      val id = runIds(0)
      for(runId <- runIds) yield { runId must beEqualTo(id) }
    }

    "return different ids for different configs" in {
      val cache = new SimulationResultsCache()
      val config = InitialValuesUIToModelConfigUI(Mindoro)

      val runIds = (for(i <- 0 until 20) yield { () => cache.getOrRun(config.copy(years = (i % 2) + 1)) }).par.map(_()).seq

      val id1 = runIds(0)
      val id2 = runIds(1)
      for(i <- 0 until 20) yield {
        (i % 2) match {
          case 0 => runIds(i) must beEqualTo(id1)
          case 1 => runIds(i) must beEqualTo(id2)
        }
      }
    }

  }

}
