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

import com.icosystem.collections._

class RawTableData(d: RawSeriesData) {
  // tables for combining multiple agents
  val allProfitsAndCostsPerTonTable = ValueTable(d.timeCol, d.fisherCostPerTonCaughtCol, d.fisherProfitPerTonCaughtCol, d.middlemanCostPerTonBoughtCol, d.middlemanProfitPerTonBoughtCol, d.exporterCostPerTonBoughtCol, d.exporterProfitPerTonBoughtCol)
  val allPercentProfitsAndCostsPerTonTable = ValueTable(d.timeCol, d.fisherPercentCostPerTonCaughtCol, d.fisherPercentProfitPerTonCaughtCol, d.middlemanPercentCostPerTonBoughtCol, d.middlemanPercentProfitPerTonBoughtCol, d.exporterPercentCostPerTonBoughtCol, d.exporterPercentProfitPerTonBoughtCol)
  val allProfitsPerTonTable = ValueTable(d.timeCol, d.fisherProfitPerTonCaughtCol, d.middlemanProfitPerTonBoughtCol, d.exporterProfitPerTonBoughtCol)
  val allProfitsTable = ValueTable(d.timeCol, d.fisherProfitCaughtCol, d.middlemanProfitBoughtCol, d.exporterProfitBoughtCol)

  // tables for fish charts
  val fishStockTable = ValueTable(d.timeCol, d.fishBiomassCol, d.fishBiomassAtMSYCol)
  val fishHarvestTable = ValueTable(d.timeCol, d.fishMSYCol, d.fishSYCol, d.municipalFisherCatchCol, d.commercialFishCatchCol, d.totalFishCatchCol)
  val fishSpoiledTable = ValueTable(d.timeCol, d.middlemanMarketFishSpoiledCol, d.exporterMarketFishSpoiledCol, d.consumerMarketFishSpoiledCol, d.municipalFisherGradeACatchCol)

  // tables for fisher charts
  val municipalFisherFishingEffortTable = ValueTable(d.timeCol, d.municipalFisherTotalFishingEffortCol, d.municipalFisherPer1000HouseholdsFishingEffortCol)
  val municipalFisherCombinedTotalCommunityEffortTable = ValueTable(d.timeCol, d.municipalFisherTotalFishingEffortCol4Stack, d.municipalFisherTotalAlternativeLivelihoodEffortCol4Stack)
  val municipalFisherCombinedPerCapitaEffortTable = ValueTable(d.timeCol, d.municipalFisherPerCapitaFishingEffortCol4Stack, d.municipalFisherPerCapitaAlternativeLivelihoodEffortCol4Stack)
  val municipalFisherCatchTable = ValueTable(d.timeCol, d.municipalFisherTotalCatchCol2, d.municipalFisherPerCapitaCatchCol2)
  val municipalFisherTotalVolumeTable = ValueTable(d.timeCol, d.municipalFisherTotalCatchCol, d.municipalFisherGradeACaughtCol, d.municipalFisherSoldCol)
  val municipalFisherPerCapitaVolumeTable = ValueTable(d.timeCol, d.municipalFisherPerCapitaCatchCol, d.municipalFisherPerCapitaSoldCol)
  val municipalFisherCatchPerUnitEffortTable = ValueTable(d.timeCol, d.municipalFisherCatchPerUnitEffortCol)
  val municipalFisherPopulationTable = ValueTable(d.timeCol, d.municipalFisherPopulationCol)
  val municipalFisherPopulationBreakdownTable = ValueTable(d.timeCol, d.municipalFisherNonTrainedFishermenCol, d.municipalFisherTrainedFishermenCol, d.municipalFisherTrainedNonFishermenCol)
  val municipalFisherPerCapitaFishingMoneyTable = ValueTable(d.timeCol, d.municipalFisherPerCapitaRevenueCol, d.municipalFisherPerCapitaCostOfFishingCol, d.municipalFisherFishingPerCapitaProfitCol, d.municipalFisherPerCapitaCostOfLivingCol)
  val municipalFisherFishingPerCapitaSurplusTable = ValueTable(d.timeCol, d.municipalFisherFishingPerCapitaProfitCol, d.municipalFisherPerCapitaCostOfLivingCol, d.municipalFisherFishingPerCapitaSurplusCol) // <--------------------
  val municipalFisherBottomLinePerCapitaSurplusTable = ValueTable(d.timeCol, d.municipalFisherFishingPerCapitaSurplusCol, d.municipalFisherNonFishingIncome, d.municipalFisherBottomLinePerCapitaSurplusCol) // <--------------------
  val municipalFisherIncomingMoneyBreakdownTable = ValueTable(d.timeCol, d.municipalFisherNonFishingIncome, d.municipalFisherPerCapitaAlternativeLivelihoodIncomeCol, d.municipalFisherFishingPerCapitaProfitCol) // <--------------------
  val municipalFisherCatchBreakdownTable = ValueTable(d.timeCol, d.municipalFisherGradeACatch, d.municipalFisherOtherCatch, d.municipalFisherSpoiledCatch)
  val municipalFisherPerCapitaAlternativeLivelihoodIncomeTable = ValueTable(d.timeCol, d.municipalFisherPerCapitaAlternativeLivelihoodIncomeCol, d.municipalFisherPerCapitaCostOfLivingCol)
  val municipalFisherAlternativeLivelihoodEffortTable = ValueTable(d.timeCol, d.municipalFisherMaxPossibleAlternativePopulationEffortCapacityCol, d.municipalFisherMaxPossibleAlternativeJobEffortCapacityCol, d.municipalFisherMaxPossibleAlternativeTraineeEffortCapacityCol, d.municipalFisherTotalAlternativeLivelihoodEffortCol, d.municipalFisherPer1000HouseholdsAlternativeLivelihoodEffortCol)
  val municipalFisherPerCapitaBottomLineTable = ValueTable(d.timeCol, d.municipalFisherPerCapitaTotalMoneyComingInCol, d.municipalFisherPerCapitaCostOfLivingCol)
  val municipalFisherProfitabilityTable = ValueTable(d.timeCol, d.municipalFisherBiasedPerceptionOfFishingProfitabilityCol, d.municipalFisherFishingProfitabilityCol, d.municipalFisherAlternativeProfitabilityCol, d.municipalFisherMinimumWageCol)
  val municipalFisherPriceNegotiationStressTable = ValueTable(d.timeCol, d.municipalFisherCumulativeStressCol, d.municipalFisherDailyStressCol)
  val municipalFisherPriceNegotiationStatusLineTable = ValueTable(d.municipalFisherPriceNegotiationStatusTimeCol, d.municipalFisherPriceNegotiationStatusInactiveCol, d.municipalFisherPriceNegotiationStatusActiveCol, d.municipalFisherPriceNegotiationStatusWaitingCol, d.municipalFisherPriceNegotiationStatusAbortCol, d.municipalFisherPriceNegotiationStatusFairPriceCol)

  // tables for middleman market charts
  val middlemanMarketVolumeTable = ValueTable(d.timeCol, d.middlemanMarketOfferCol, d.middlemanMarketDemandCol, d.middlemanMarketTradedCol)
  val middlemanMarketPriceTable = ValueTable(d.timeCol, d.middlemanMarketPriceCol, d.municipalFisherPriceNegotiationFairPriceCol)
  val middlemanMarketValueTable = ValueTable(d.timeCol, d.middlemanMarketValueCol)

  // tables for middleman charts
  val middlemanVolumeTable = ValueTable(d.timeCol, d.middleManBoughtCol, d.middleManSoldCol)
  val middlemanProfitTable = ValueTable(d.timeCol, d.middleManProfitCol)

  // tables for exporter market charts
  val exporterMarketVolumeTable = ValueTable(d.timeCol, d.exporterMarketOfferCol, d.exporterMarketDemandCol, d.exporterMarketTradedCol)
  val exporterMarketPriceTable = ValueTable(d.timeCol, d.exporterMarketPriceCol)
  val exporterMarketValueTable = ValueTable(d.timeCol, d.exporterMarketValueCol)

  // tables for exporter charts
  val exporterVolumeTable = ValueTable(d.timeCol, d.exporterBoughtCol, d.exporterSoldCol)
  val exporterProfitTable = ValueTable(d.timeCol, d.exporterProfitsCol)

  // tables for consumer market charts
  val consumerMarketVolumeTable = ValueTable(d.timeCol, d.consumerMarketOfferCol, d.consumerMarketDemandCol, d.consumerMarketTradedCol)
  val consumerMarketPriceTable = ValueTable(d.timeCol, d.consumerMarketPriceCol)
  val consumerMarketValueTable = ValueTable(d.timeCol, d.consumerMarketValueCol)

  // tables for consumer charts
  val consumerPopulationTable = ValueTable(d.timeCol, d.consumerPopulationCol)
  val consumerDemandTable = ValueTable(d.timeCol, d.consumerTotalDemandCol, d.consumerDemandPer1000Col)
  val consumerConsumptionTable = ValueTable(d.timeCol, d.consumerConsumptionCol, d.consumerConsumptionPer1000Col)
}