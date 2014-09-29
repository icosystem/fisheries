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

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import simulation.unit._

@RunWith(classOf[JUnitRunner])
class ValueTableSpec extends Specification {

  "BaselineToDefinedValueIntervention" should {

    "aggregate a column" in {
      val col = Column("test", IndexedSeq(1, 2, 3, 4, 5, 6, 7, 8, 9))
      col.aggregate(2, s => s.reduceLeft(_ + _)) must beEqualTo(Column("test", IndexedSeq(1 + 2, 3 + 4, 5 + 6, 7 + 8)))
    }
    
  }

}
