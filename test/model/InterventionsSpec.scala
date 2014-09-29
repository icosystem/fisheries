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

package model

import controllers.{TimeInformation, InterventionUI}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import simulation.Timeline
import simulation.unit._

@RunWith(classOf[JUnitRunner])
class InterventionsSpec extends Specification {
  val endYears = 6
  val interventionStartYear = 2
  val interventionEndYear = 4
  val halfway = 3
  val timeInformation = new TimeInformation {override val stepSize: Int = 7;  override val years: Int = endYears }
  val ticksPerYear = timeInformation.ticksPerYear

  "BaselineToDefinedValueIntervention with 100%" should {

    "be baseline value before the intervention starts" in {
      val interventionUI = InterventionUI(1d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = 0
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(5d))
    }

    "be baseline value at the start of the intervention" in {
      val interventionUI = InterventionUI(1d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = interventionStartYear * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(5d))
    }

    "be halfway value halfway through the intervention" in {
      val interventionUI = InterventionUI(1d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = halfway * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(7.5d))
    }

    "be defined end at the end of the intervention" in {
      val interventionUI = InterventionUI(1d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = interventionEndYear * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(10d))
    }

    "be defined end after the intervention" in {
      val interventionUI = InterventionUI(1d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = endYears * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(10d))
    }
  }

  "BaselineToDefinedValueIntervention with 50%" should {

    "be baseline value before the intervention starts" in {
      val interventionUI = InterventionUI(.5d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = 0
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(5d))
    }

    "be baseline value at the start of the intervention" in {
      val interventionUI = InterventionUI(.5d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = interventionStartYear * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(5d))
    }

    "be one quarter way to defined end halfway through the intervention" in {
      val interventionUI = InterventionUI(.5d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = halfway * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(6.25d))
    }

    "be halfway to defined end at the end of the intervention" in {
      val interventionUI = InterventionUI(.5d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = interventionEndYear * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(7.5d))
    }

    "be halfway to defined end after the intervention" in {
      val interventionUI = InterventionUI(.5d, interventionStartYear, interventionEndYear)
      val intervention = BaselineToDefinedValueIntervention(`¤`(5d), `¤`(10d), interventionUI, timeInformation)
      val timeline = Timeline(endYears * ticksPerYear)


      timeline.now = endYears * ticksPerYear
      intervention.intervenedValue(timeline) must beEqualTo(`¤`(7.5d))
    }
  }
}
