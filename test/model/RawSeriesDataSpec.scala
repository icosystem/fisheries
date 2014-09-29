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

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import simulation.unit._
import model.ChartData._
import simulation.agent.Inactive
import simulation.agent.Active

//'Inactive
// 'FairPrice
// 'Waiting
// 'Abort
// 'Active

@RunWith(classOf[JUnitRunner])
class RawSeriesDataSpec extends Specification {

  "compressStatusColumns" should {

    "do no compression when switching every 2 steps" in {
      val r = compressStatusColumns(IndexedSeq(1d, 2d, 3d, 4d, 5d), IndexedSeq(Inactive, Active, Inactive, Inactive, Active))
      r.time must beEqualTo(IndexedSeq(1d, 2d, 3d, 4d, 5d))
      r.active must beEqualTo(IndexedSeq(-1d, Active.number, -1d, -1d, Active.number))
      r.inactive must beEqualTo(IndexedSeq(Inactive.number, -1d, Inactive.number, Inactive.number, -1d))
    }


    "compress switches that take longer then 2 steps down to 2" in {
      val r = compressStatusColumns(IndexedSeq(1d, 2d, 3d, 4d, 5d), IndexedSeq(Inactive, Active, Active, Active, Inactive))
      r.time must beEqualTo(IndexedSeq(1d, 2d, 4d, 5d))
      r.active must beEqualTo(IndexedSeq(-1d, Active.number, Active.number, -1d))
      r.inactive must beEqualTo(IndexedSeq(Inactive.number, -1d, -1d, Inactive.number))
    }

  }
}

