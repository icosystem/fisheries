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

import simulation.Timeline
import simulation.unit._
import model.Intervention
import model.InterventionCompetiveness
import model.PercentIntervention
import model.QuotaType
import model.QuotaTypeNone
import simulation.agent.LinearInterp.linearInterp
import controllers.ModelConfigUIMiddleman

trait Rockefeller extends Agent[RockefellerState] {
  def apply(s: MunicipalFisherState): MunicipalFisherState

  def apply(s: FishFisherRelationshipState, mf: Fisher[_], f: Fish): FishFisherRelationshipState

  def middlemanState(s: IntermediaryState): IntermediaryState

  def apply(s: SingleSellerSupportAvailableState): SingleSellerSupportAvailableState
}

case class RockefellerIntervene(costOfFishingFixedIntervention: Intervention[MoneyPerTimeFishermen],
  interventionCostOfFishingVariable: Intervention[MoneyPerFishing],
  interventionCostOfLiving: Intervention[MoneyPerTimeFishermen],
  interventionProductivity: Intervention[PerFishing],
  interventionOtherCatchRatio: Intervention[Percent],
  interventionSpoiledCatchRatio: Intervention[Percent],
  interventionCompetitiveness: InterventionCompetiveness,
  interventionSingleSellerOperationKnowledgeAndSupport: Intervention[Percent],
  interventionCapitalSupport: Intervention[Percent],
  interventionFinancialBenefitOfAlternative: Intervention[MoneyPerFishing],
  interventionNumberOfPeopleTrained: Intervention[Fishermen],
  interventionAlternativeJobsAvailable: Intervention[Fishermen],
  interventionAlternativeJobWorkload: Intervention[FishingPerTimeFishermen],
  quotaType: QuotaType,
  var interventionMSYPercent: PercentIntervention[Percent]) //actual start value needs to be calculated at intervention start tick, so this needs to be mutable somehow... making it a var here
  (implicit val t: Timeline) extends Agent[RockefellerState](t) with Rockefeller {

  override def gatherState: RockefellerState = currentState

  def apply(s: MunicipalFisherState): MunicipalFisherState = {
    val quotaPop = if (interventionMSYPercent.startTick == t.now) {
      s.numberOfFishermenInPopulation
    } else {
      s.quotaPopulation
    }

    s.copy(fixedCostPerFishermanPerYear = costOfFishingFixedIntervention.intervenedValue,
      variableCostPerUnitEffort = interventionCostOfFishingVariable.intervenedValue,
      costOfLivingPerFisherman = interventionCostOfLiving.intervenedValue,
      spoiledCatchRatio = interventionSpoiledCatchRatio.intervenedValue,
      otherCatchRatio = interventionOtherCatchRatio.intervenedValue,
      financialBenefitOfAlternative = interventionFinancialBenefitOfAlternative.intervenedValue,
      numberOfPeopleTrained = min(interventionNumberOfPeopleTrained.intervenedValue, s.numberOfPeopleTrained + s.numberOfFishermenNotTrained), // only train non-trained fishermen, not non-trained non-fishermen
      //      numberOfPeopleTrained = if (s.numberOfFishermenTrained >= s.numberOfFishermenInPopulation /*equal, really*/ ) s.numberOfPeopleTrained /* do not train any more */ else interventionNumberOfPeopleTrained.intervenedValue,
      alternativeJobsAvailable = interventionAlternativeJobsAvailable.intervenedValue,
      alternativeJobWorkload = interventionAlternativeJobWorkload.intervenedValue,

      priceNegotiation = s.priceNegotiation.copy(adherence = interventionSingleSellerOperationKnowledgeAndSupport.intervenedValue.v * s.priceNegotiation.maxAdherence),
      priceNegotiationEnabled = interventionSingleSellerOperationKnowledgeAndSupport.intervenedValue.v > 0,
      quotaPopulation = quotaPop)
  }

  def apply(s: FishFisherRelationshipState, mf: Fisher[_], f: Fish): FishFisherRelationshipState = {
    val qt = if (t.now >= interventionMSYPercent.startTick) {
      quotaType
    } else {
      QuotaTypeNone
    }
    if (interventionMSYPercent.startTick == t.now) {
      //need initial catch as % of MSY
      //initial municipal fisher catch = total initial catch / (1 + InitialValuesUI.allPhilippinesCommercialToMunicipalRatio)
      val municipalFisherCatch = f.fishingResult(mf).totalCatchRate.v
      //msy = growthRate.v * carryingCapacity.v / 4d
      val msy = f.growthRate.v * f.carryingCapacity.v / 4
      //initial percent of MSY = initial municipal fisher catch / MSY
      val initialCatchPercentMSY = municipalFisherCatch / msy

      interventionMSYPercent = PercentIntervention(Percent(initialCatchPercentMSY), interventionMSYPercent.endValue, interventionMSYPercent.startTick, interventionMSYPercent.durationInTicks)
    }
    val q = interventionMSYPercent.intervenedValue

    s.copy(catchabilityCoeff = interventionProductivity.intervenedValue, quotaType = qt, quota = q)
  }

  def middlemanState(s: IntermediaryState): IntermediaryState = {
    s.copy(desiredProfitRate = interventionCompetitiveness.desiredProfitRate,
      aggressiveInboundPriceSetting = interventionCompetitiveness.aggressiveInboundPriceSetting,
      aggressiveOutboundPriceSetting = interventionCompetitiveness.aggressiveOutboundPriceSetting,
      desireToIncreaseMarketShare = interventionCompetitiveness.desireToIncreaseMarketShare)
  }

  def apply(s: SingleSellerSupportAvailableState): SingleSellerSupportAvailableState = {
    s.copy(singleSellerOperationKnowledgeAndSupport = interventionSingleSellerOperationKnowledgeAndSupport.intervenedValue,
      capitalSupport = interventionCapitalSupport.intervenedValue)
  }
}

case class RockefellerNoop()(implicit val t: Timeline) extends Agent[RockefellerState](t) with Rockefeller {

  override def gatherState: RockefellerState = currentState

  def apply(s: MunicipalFisherState): MunicipalFisherState = s

  def apply(s: FishFisherRelationshipState, mf: Fisher[_], f: Fish): FishFisherRelationshipState = s

  def middlemanState(s: IntermediaryState): IntermediaryState = s

  def apply(s: SingleSellerSupportAvailableState): SingleSellerSupportAvailableState = s
}

case class RockefellerState()