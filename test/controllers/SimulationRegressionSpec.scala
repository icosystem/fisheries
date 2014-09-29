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

package controllers

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json._
import controllers.initial.{ Mindoro, InitialValuesUIToModelConfigUI }

import java.io.File
import com.icosystem.io._
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class SimulationRegressionSpec extends Specification {

  skipAll

  "Simulation" should {
    "pass regression test" in {
      val result = SimulationRunner(InitialValuesUIToModelConfigUI(Mindoro))
      val jsonString = Json.toJson(result).toString
      val source = Source.fromURL(SimulationRegressionSpec.resource(SimulationRegressionSpec.jsonPath))
      jsonString must beEqualTo(source.getLines().mkString)
    }
  }

}

object SimulationRegressionSpec {
  val jsonPath = "/SimulationRegressionResults.json"

  def main(args: Array[String]) {
    val result = SimulationRunner(InitialValuesUIToModelConfigUI(Mindoro))
    val jsonString = Json.toJson(result).toString
    new File("test/resources" + jsonPath) write jsonString
  }

  /**
   * Eclipse / JUnit does not know about where Play likes to place resources so we have this hack
   *
   * http://journal.michaelahlers.org/2013/01/play-framework-and-testing-resources.html
   */
  def resource(path: String) = {
    val resource = getClass.getResource(SimulationRegressionSpec.jsonPath)
    if (resource != null) {
      resource
    } else {
      getClass.getResource("/resources" + SimulationRegressionSpec.jsonPath)
    }
  }

}