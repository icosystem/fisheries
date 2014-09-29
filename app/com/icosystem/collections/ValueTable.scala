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

import play.api.libs.json._

object ValueTable {
  def apply[T1](c1: Column[T1]) = ValueTable1(c1)

  def apply[T1, T2](c1: Column[T1], c2: Column[T2]) = ValueTable2(c1, c2)

  def apply[T1, T2, T3](c1: Column[T1], c2: Column[T2], c3: Column[T3]) = ValueTable3(c1, c2, c3)

  def apply[T1, T2, T3, T4](c1: Column[T1], c2: Column[T2], c3: Column[T3], c4: Column[T4]) = ValueTable4(c1, c2, c3, c4)

  def apply[T1, T2, T3, T4, T5](c1: Column[T1], c2: Column[T2], c3: Column[T3], c4: Column[T4], c5: Column[T5]) = ValueTable5(c1, c2, c3, c4, c5)

  def apply[T1, T2, T3, T4, T5, T6](c1: Column[T1], c2: Column[T2], c3: Column[T3], c4: Column[T4], c5: Column[T5], c6: Column[T6]) = ValueTable6(c1, c2, c3, c4, c5, c6)

  def apply[T1, T2, T3, T4, T5, T6, T7](c1: Column[T1], c2: Column[T2], c3: Column[T3], c4: Column[T4], c5: Column[T5], c6: Column[T6], c7: Column[T7]) = ValueTable7(c1, c2, c3, c4, c5, c6, c7)

  implicit val writes = new Writes[ValueTable] {
    def writes(c: ValueTable) = c.toJSON
  }
}

trait ValueTable {
  def cols: IndexedSeq[Column[_]]

  def toJSON: JsArray

  def toCSV: String = toJSON.value.map(e => toCsv(e)).mkString("")

  private def toCsv(jsValue: JsValue) : String = jsValue match {
    case v: JsArray  => v.value.map(e => toCsv(e)).mkString("",",","\n")
    case v: JsNumber => v.toString()
    case _ => jsValue.toString()
  }

  def rowCount: Int
}

case class ValueTable1[T1](val c1: Column[T1]) extends ValueTable {
  private implicit val w1 = c1.w

  def toJSON = JsArray(Seq(Json.arr(c1.name)) ++: c1.map(Json.arr(_)))

  def ++[T2](c2: Column[T2]) = ValueTable(c1, c2)

  def rows = c1.values

  def cols = IndexedSeq(c1)

  def rowCount = c1.size
}

case class ValueTable2[T1, T2](val c1: Column[T1], val c2: Column[T2]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name)) ++: rows.map(r => Json.arr(r._1, r._2)))

  def ++[T3](c3: Column[T3]) = ValueTable(c1, c2, c3)

  def rows = c1.zip(c2)

  def cols = IndexedSeq(c1, c2)

  def rowCount = c1.size
}

case class ValueTable3[T1, T2, T3](val c1: Column[T1], val c2: Column[T2], val c3: Column[T3]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w
  private implicit val w3 = c3.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name, c3.name)) ++: rows.map(r => Json.arr(r._1, r._2, r._3)))

  def ++[T4](c4: Column[T4]) = ValueTable(c1, c2, c3, c4)

  def rows = c1.zip(c2).zip(c3).map(Tuples.flatten(_))

  def cols = IndexedSeq(c1, c2, c3)

  def rowCount = c1.size
}

case class ValueTable4[T1, T2, T3, T4](val c1: Column[T1], val c2: Column[T2], val c3: Column[T3], val c4: Column[T4]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w
  private implicit val w3 = c3.w
  private implicit val w4 = c4.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name, c3.name, c4.name)) ++: rows.map(r => Json.arr(r._1, r._2, r._3, r._4)))

  def ++[T5](c5: Column[T5]) = ValueTable(c1, c2, c3, c4, c5)

  def rows = c1.zip(c2).zip(c3).zip(c4).map(Tuples.flatten(_))

  def cols = IndexedSeq(c1, c2, c3, c4)

  def rowCount = c1.size
}

case class ValueTable5[T1, T2, T3, T4, T5](val c1: Column[T1], val c2: Column[T2], val c3: Column[T3], val c4: Column[T4], val c5: Column[T5]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w
  private implicit val w3 = c3.w
  private implicit val w4 = c4.w
  private implicit val w5 = c5.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name, c3.name, c4.name, c5.name)) ++: rows.map(r => Json.arr(r._1, r._2, r._3, r._4, r._5)))

  def rows = c1.zip(c2).zip(c3).zip(c4).zip(c5).map(Tuples.flatten(_))

  def cols = IndexedSeq(c1, c2, c3, c4, c5)

  def rowCount = c1.size
}

case class ValueTable6[T1, T2, T3, T4, T5, T6](val c1: Column[T1], val c2: Column[T2], val c3: Column[T3], val c4: Column[T4], val c5: Column[T5], val c6: Column[T6]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w
  private implicit val w3 = c3.w
  private implicit val w4 = c4.w
  private implicit val w5 = c5.w
  private implicit val w6 = c6.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name, c3.name, c4.name, c5.name, c6.name)) ++: rows.map(r => Json.arr(r._1, r._2, r._3, r._4, r._5, r._6)))

  def rows = c1.zip(c2).zip(c3).zip(c4).zip(c5).zip(c6).map(Tuples.flatten(_))

  def cols = IndexedSeq(c1, c2, c3, c4, c5, c6)

  def rowCount = c1.size
}

case class ValueTable7[T1, T2, T3, T4, T5, T6, T7](val c1: Column[T1], val c2: Column[T2], val c3: Column[T3], val c4: Column[T4], val c5: Column[T5], val c6: Column[T6], val c7: Column[T7]) extends ValueTable {
  private implicit val w1 = c1.w
  private implicit val w2 = c2.w
  private implicit val w3 = c3.w
  private implicit val w4 = c4.w
  private implicit val w5 = c5.w
  private implicit val w6 = c6.w
  private implicit val w7 = c7.w

  def toJSON = JsArray(Seq(Json.arr(c1.name, c2.name, c3.name, c4.name, c5.name, c6.name, c7.name)) ++: rows.map(r => Json.arr(r._1, r._2, r._3, r._4, r._5, r._6, r._7)))

  def rows = c1.zip(c2).zip(c3).zip(c4).zip(c5).zip(c6).zip(c7).map(Tuples.flatten(_))

  def cols = IndexedSeq(c1, c2, c3, c4, c5, c6, c7)

  def rowCount = c1.size
}

case class Column[T](val name: String, values: IndexedSeq[T])(implicit val w: Writes[T]) extends IndexedSeq[T] {
  def apply(idx: Int): T = values.apply(idx)

  def length: Int = values.length

  def aggregate(groupSize: Int, f: (Seq[T]) => T) = Column(name, values.grouped(groupSize).flatMap(s =>
    if (s.size == groupSize) { // Drop the last few if we don't divide evenly
      Some(f(s))
    } else {
      None
    }).toIndexedSeq)
}
