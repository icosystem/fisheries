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

sealed trait QuotaType {
  val name: String
  val id: Int
}

object QuotaTypes {
  val all = IndexedSeq(QuotaTypeNone, QuotaTypeTACOpenExitEntry, QuotaTypeTACMaxEntry, QuotaTypeTACFixedCommunity)
  val allButNone = IndexedSeq(QuotaTypeTACOpenExitEntry, QuotaTypeTACMaxEntry, QuotaTypeTACFixedCommunity)
}

object QuotaTypeNone extends QuotaType {
  val name = "None"
  val id = 0
}

object QuotaTypeTACOpenExitEntry extends QuotaType {
  val name = "TAC w/ open exit/entry"
  val id = 1
}

object QuotaTypeTACMaxEntry extends QuotaType {
  val name = "TAC w/ max entry"
  val id = 2
}

object QuotaTypeTACFixedCommunity extends QuotaType {
  val name = "TAC w/ fixed community"
  val id = 3
}