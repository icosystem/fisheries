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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import model.QuotaTypeTACOpenExitEntry
import model.QuotaTypeTACMaxEntry
import model.QuotaTypeTACFixedCommunity
import model.QuotaTypeNone
import model.QuotaType

case class ModelConfigUIInterventions(
  costOfFishingFixed: InterventionUI,
  costOfFishingVariable: InterventionUI,
  costOfLiving: InterventionUI,
  productivity: InterventionUI,
  fishQuality: InterventionUI,
  competitiveness: InterventionUI,

  removeMiddleman: String,
  limitEntries: String,
  useTotalAllowableCatch: String,
  totalAllowableCatch: InterventionUI,
  singleSellerOperationKnowledgeAndSupport: InterventionUI,
  capitalSupport: Double,
  financialBenefitOfAlternative: InterventionUI,
  numberOfPeopleTrained: InterventionUI,
  alternativeJobsAvailable: InterventionUI,
  alternativeJobWorkload: InterventionUI) {

  def quota: QuotaType = {
      if(limitEntries == LimitEntriesValues.maxEntry._2) QuotaTypeTACMaxEntry
      else if(useTotalAllowableCatch == UseTotalAllowableCatchValues.limited._2) QuotaTypeTACOpenExitEntry
      else QuotaTypeNone
  }

  def totalAllowableCatchActual  = {
    if(useTotalAllowableCatch == UseTotalAllowableCatchValues.limited._2) totalAllowableCatch
    else totalAllowableCatch.copy(endValue = 1000)
  }
}

object ModelConfigUIInterventions {
  implicit val f = InterventionUI.interventionUIFormat

  implicit val modelConfigUIInterventionsFormat = Json.format[ModelConfigUIInterventions]
}

case class InterventionUI(endValue: Double, startYear: Int, endYear: Int) {
  def durationInYears: Int = math.max(endYear - startYear, 0)
}

object RemoveMiddlemanUI extends Enumeration {
  type RemoveMiddlemanUI = Value
  val CURRENT_BEHAVIOR, REMOVE_MIDDLEMAN = Value
}

object InterventionUI {
  implicit val interventionUIFormat = Json.format[InterventionUI]
}