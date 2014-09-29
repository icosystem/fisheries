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

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import model.ModelConfig
import simulation.unit.Time
import simulation.FisherySimulation
import model.SimulationRunChartData
import model.SimulationRunChartData._
import model.ModelConfigTest

@RunWith(classOf[JUnitRunner])
class SimulationRunnerSpec extends Specification {

  "Simulation" should {

    "run at 1-day tick" in {
      val result = FisherySimulation(ModelConfigTest.apply.copy(ticks = 365 * 10, deltaT = Time(1d / 365d))) // 3600 ticks, 0.561s
      SimulationRunChartData.create(result, 1).tableData(fishStockChart.id).rowCount must beEqualTo(3651)
    }

    "run at 1-week tick" in {
      val result = FisherySimulation(ModelConfigTest.apply.copy(ticks = 52 * 10, deltaT = Time(7d / 364d))) // 520 ticks, 0.560s
      SimulationRunChartData.create(result, 1).tableData(fishStockChart.id).rowCount must beEqualTo(521)
    }

    "run at 1-month tick" in {
      val result = FisherySimulation(ModelConfigTest.apply.copy(ticks = 12 * 10, deltaT = Time(30d / 360d))) // 120 ticks, 0.560s
      SimulationRunChartData.create(result, 1).tableData(fishStockChart.id).rowCount must beEqualTo(121)
    }

  }

}

object SimulationRunnerSpec {
  def main(args: Array[String]) {
    readLine("prompt> ")
    val result = FisherySimulation(ModelConfigTest.apply.copy(ticks = 12 * 100, deltaT = Time(30d / 360d))) // 120 ticks
    readLine("prompt> ")
  }
}