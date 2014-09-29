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

package simulation

import simulation.agent._
import simulation.unit._
import model.ModelConfig
import model.InterventionCompetiveness
import simulation.agent.PriceNegotiation.startPriceNegotiation
import model.QuotaTypeNone

case class ValueChain(middlemanMarket: Market with MarketRecorder,
  exporterMarket: Market with MarketRecorder,
  consumerMarket: ConsumerMarket with MarketRecorder,
  fish: Fish with FishRecorder,
  municipalFisher: MunicipalFisher with FisherRecorder,
  municipalFisherFishRelationship: FishFisherRelationship,
  commercialFisher: CommercialFisher with AgentRecorder[CommercialFisherState],
  commercialFisherFishRelationship: FishFisherRelationship,
  middleman: IntermediaryWithRockefeller with IntermediaryRecorder,
  exporter: Intermediary with IntermediaryRecorder,
  consumer: Consumer with AgentRecorder[ConsumerState],
  singleSellerSupportAvailable: SingleSellerSupportAvailable with AgentRecorder[SingleSellerSupportAvailableState],
  rockefeller: Rockefeller) {

  val agents = Seq(middlemanMarket, exporterMarket, consumerMarket, fish, municipalFisher, municipalFisherFishRelationship, commercialFisher, commercialFisherFishRelationship, middleman, exporter, consumer, singleSellerSupportAvailable, rockefeller)
}

trait ValueChainBuilderDefault {
  implicit val t: Timeline
  val c: ModelConfig

  val rockefeller = createRockefeller
  val middlemanMarket = createMiddlemanMarket
  val exporterMarket = createExporterMarket
  val consumerMarket = createConsumerMarket
  val fish = createFish
  val municipalFisher = createMunicipalFisher
  val commercialFisher = createCommercialFisher
  val middleman = createMiddleman
  val exporter = createExporter
  val consumer = createConsumer
  // Relationship agents are declared last so they can reference other agents
  val municipalFishFisherRelationship = createMunicipalFishFisherRelationship(rockefeller)
  val commercialFishFisherRelationship = createCommercialFishFisherRelationship
  val singleSellerSupportAvailable = createSingleSellerSupportAvailable(exporter)

  fish.currentState = createFishState
  commercialFisher.currentState = createCommercialFisherState
  municipalFisher.currentState = rockefeller(createMunicipalFisherState)
  middlemanMarket.currentState = createMiddlemanMarketState
  middleman.currentState = rockefeller.middlemanState(createMiddlemanState)
  exporterMarket.currentState = createExporterMarketState
  exporter.currentState = createExporterState
  consumerMarket.currentState = createConsumerMarketState
  consumer.currentState = createConsumerState
  municipalFishFisherRelationship.currentState = rockefeller(createMunicipalFishFisherRelationshipState, municipalFisher, fish)
  commercialFishFisherRelationship.currentState = createCommercialFishFisherRelationshipState
  singleSellerSupportAvailable.currentState = rockefeller(createSingleSellerSupportAvailableState)

  wireFish
  wireMunicipalFisher
  wireCommercialFisher
  wireMiddlemanMarket
  wireMiddleman
  wireExporterMarket
  wireExporter
  wireConsumerMarket
  wireConsumer
  wireSingleSellerSupportAvailable

  val asChain = ValueChain(middlemanMarket, exporterMarket, consumerMarket, fish, municipalFisher, municipalFishFisherRelationship, commercialFisher, commercialFishFisherRelationship, middleman, exporter, consumer, singleSellerSupportAvailable, rockefeller)

  // ===================================
  // markets; passive; no references to other agents
  // ===================================
  protected def createMiddlemanMarket: Market with MarketRecorder = new MiddlemanMarket(c.middlemanMarket.middlemanMarketSpeed) with MarketRecorder
  protected def createExporterMarket: Market with MarketRecorder = new ExporterMarket(c.exporterMarket.exporterMarketSpeed) with MarketRecorder
  protected def createConsumerMarket = new ConsumerMarket(c.consumerMarket.consumerMarketSpeed) with MarketRecorder

  // ===================================
  // non-market agents; active; have references to other agents, including markets
  // ===================================
  protected def createRockefeller = c.rockefeller match {
    case Some(r) => RockefellerIntervene(r.costOfFishingFixedIntervention, r.interventionCostOfFishingVariable, r.interventionCostOfLiving, r.interventionProductivity, r.interventionOtherCatchRatio, r.interventionSpoiledCatchRatio, InterventionCompetiveness(c.middleman, r.interventionCompetitiveness), r.interventionSingleSellerOperationKnowledgeAndSupport, r.interventionCapitalSupport, r.interventionFinancialBenefitOfAlternative, r.interventionNumberOfPeopleTrained, r.interventionAlternativeJobsAvailable, r.interventionAlternativeJobWorkload, r.quotaType, r.interventionMSYPercent)
    case None => RockefellerNoop()
  }
  protected def createFish = new Fish(c.fish.carryingCapacity, c.fish.growthRate) with FishRecorder
  protected def createMunicipalFisher = new MunicipalFisher(c.municipalFisher.numberOfFishermenInPopulation,
    c.municipalFisher.effortGrowthCoeff,
    c.municipalFisher.populationSizeGrowthCoeff,
    c.municipalFisher.effortPerFishermanPerYear,
    c.municipalFisher.alternativeEffortPerFishermanPerYear,
    c.municipalFisher.nonFishingIncome,
    MoneyPerTonnes(0),
    c.municipalFisher.ratioOfInitialPopulationNeverLeaving,
    c.municipalFisher.ratioOfMaximumEffortToAcceptableEffort,
    c.municipalFisher.ratioOfInitialEffortToAcceptableEffort,
    c.municipalFisher.alternativeLivelihood.culturalFishingChoiceBias) with FisherRecorder
  protected def createCommercialFisher = new CommercialFisher(c.commercialFisher.initialEffort, c.commercialFisher.growthRate) with AgentRecorder[CommercialFisherState]
  protected def createMiddleman: IntermediaryWithRockefeller with IntermediaryRecorder = new Middleman(c.middleman.middlemanOverhead, c.middleman.fisherCostsEstimate) with IntermediaryRecorder
  protected def createExporter: Intermediary with IntermediaryRecorder = new Exporter(c.exporter.exporterOverhead, c.exporter.middlemanCostEstimate) with IntermediaryRecorder
  protected def createConsumer = new Consumer(c.consumer.consumerGrowthRate, c.consumer.initialPrice, c.consumer.ε, c.consumer.initialDemandPerPerson) with AgentRecorder[ConsumerState]

  // ===================================
  // Relationship agents
  // ===================================
  protected def createMunicipalFishFisherRelationship(rockefeller: Rockefeller) = MunicipalFishFisherRelationship(fish, municipalFisher, rockefeller)
  protected def createCommercialFishFisherRelationship = CommercialFishFisherRelationship(fish, commercialFisher)
  protected def createSingleSellerSupportAvailable(exporter: Intermediary with IntermediaryRecorder) = new SingleSellerSupportAvailable(exporterMarket, consumerMarket) with AgentRecorder[SingleSellerSupportAvailableState]

  // ===================================
  // Helpful values in setting initial states
  // ===================================
  protected def municipalFisherInitialTotalCatchPerTick = c.municipalFisher.initialCatch * c.deltaT
  protected def commercialFisherInitialTotalCatchPerTick = c.commercialFisher.initialCatch * c.deltaT
  protected def lastMunicipalFisherToMiddlemanTransaction = FinancialTransaction(c.middlemanMarket.middlemanMarketFishPrice, c.middlemanMarket.lastTraded * c.deltaT, c.middlemanMarket.lastExcess * c.deltaT)
  protected def lastMiddlemanToExporterTransaction = FinancialTransaction(c.exporterMarket.exporterMarketPrice, c.exporterMarket.lastTraded * c.deltaT, c.exporterMarket.lastExcess * c.deltaT)
  protected def lastExporterToConsumerTransaction = FinancialTransaction(c.consumerMarket.consumerMarketPrice, c.consumerMarket.lastTraded * c.deltaT, c.consumerMarket.lastExcess * c.deltaT)

  // ===================================
  // Set initial states
  // ===================================
  protected def createFishState = FishState(c.fish.biomass, Seq(FishingResult(t.now, municipalFisher, IntendedGroupEffort(t.now - 1, c.municipalFisher.effortPerFishermanPerYear, c.municipalFisher.numberOfFishermenInPopulation), c.municipalFisher.initialCatch, c.municipalFisher.effortPerFishermanPerYear), FishingResult(t.now, commercialFisher, IntendedGroupEffort(t.now - 1, c.commercialFisher.initialEffort / Fishermen(1), Fishermen(1)), c.commercialFisher.initialCatch, c.commercialFisher.initialEffort / Fishermen(1))))
  protected def createCommercialFisherState = CommercialFisherState(t.now, c.commercialFisher.initialEffort, commercialFisherInitialTotalCatchPerTick)
  protected def priceNegotiation = startPriceNegotiation(stressThreshold = c.municipalFisher.priceNegotiation.stressThreshold, stressDecayTime = c.municipalFisher.priceNegotiation.stressDecayTime, timeToRetry = c.municipalFisher.priceNegotiation.timeToRetry, target = c.municipalFisher.priceNegotiation.target, adherence = c.municipalFisher.priceNegotiation.adherence, maxAdherence = c.municipalFisher.priceNegotiation.maxAdherence)
  protected def createMunicipalFisherState = MunicipalFisherState(t.now, c.municipalFisher.numberOfFishermenInPopulation, c.municipalFisher.effortPerFishermanPerYear, c.municipalFisher.alternativeEffortPerFishermanPerYear, c.municipalFisher.costOfLivingPerFisherman, c.municipalFisher.variableCostPerUnitEffort, c.municipalFisher.fixedCostPerFisherman, c.municipalFisher.spoiledCatchRatio, c.municipalFisher.otherCatchRatio, c.municipalFisher.otherValueRatio, c.municipalFisher.alternativeLivelihood.alternativeJobsAvailable, c.municipalFisher.alternativeLivelihood.alternativeJobWorkload, c.municipalFisher.alternativeLivelihood.numberOfPeopleTrained, c.municipalFisher.alternativeLivelihood.numberOfNonFishermenTrained, c.municipalFisher.alternativeLivelihood.financialBenefit, priceNegotiation, c.municipalFisher.priceNegotiation.enabled, c.municipalFisher.numberOfFishermenInPopulation)
  protected def createMiddlemanMarketState = MarketState(c.middlemanMarket.middlemanMarketFishPrice, PendingSell(municipalFisher, lastMunicipalFisherToMiddlemanTransaction), PendingBuy(middleman, lastMunicipalFisherToMiddlemanTransaction))
  protected def createMiddlemanState = IntermediaryState(
    bought = lastMunicipalFisherToMiddlemanTransaction,
    sold = lastMiddlemanToExporterTransaction,
    demand = c.middleman.lastDemand * c.deltaT,
    aggressiveInboundPriceSetting = c.middleman.aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = c.middleman.aggressiveOutboundPriceSetting,
    desiredProfitRate = c.middleman.middlemanDesiredProfitRate,
    desireToIncreaseMarketShare = c.middleman.desireToIncreaseMarketShare)
  protected def createExporterMarketState = MarketState(c.exporterMarket.exporterMarketPrice, PendingSell(middleman, lastMiddlemanToExporterTransaction), PendingBuy(exporter, lastMiddlemanToExporterTransaction))
  protected def createExporterState = IntermediaryState(
    bought = lastMiddlemanToExporterTransaction,
    sold = lastExporterToConsumerTransaction,
    demand = c.exporter.lastDemand * c.deltaT,
    aggressiveInboundPriceSetting = c.exporter.aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = c.exporter.aggressiveOutboundPriceSetting,
    desiredProfitRate = c.exporter.exporterDesiredProfitRate,
    desireToIncreaseMarketShare = 0d)
  protected def createConsumerMarketState = MarketState(c.consumerMarket.consumerMarketPrice, PendingSell(exporter, lastExporterToConsumerTransaction), PendingBuy(consumer, lastExporterToConsumerTransaction))
  protected def createConsumerState = ConsumerState(c.consumer.consumerPopulation, c.consumer.lastDemand * c.deltaT)
  protected def createMunicipalFishFisherRelationshipState = FishFisherRelationshipState(c.municipalFisher.catchabilityCoeff, QuotaTypeNone, Percent(0.0))
  protected def createCommercialFishFisherRelationshipState = FishFisherRelationshipState(c.commercialFisher.catchabilityCoeff, QuotaTypeNone, Percent(0.0))
  protected def createSingleSellerSupportAvailableState = SingleSellerSupportAvailableState(c.singleSellerSupportAvailable.singleSellerOperationKnowledgeAndSupport, c.singleSellerSupportAvailable.capitalSupport)

  // ===================================
  // Wire all the agents together
  // ===================================
  protected def wireFish {}
  protected def wireMunicipalFisher = {
    municipalFisher.middlemanMarket = middlemanMarket
    municipalFisher.singleSellerSupportAvailable = singleSellerSupportAvailable
    municipalFisher.rockefeller = rockefeller
  }
  protected def wireCommercialFisher {}
  protected def wireMiddlemanMarket = {
    middlemanMarket.seller = municipalFisher
    middlemanMarket.buyer = middleman
  }
  protected def wireMiddleman = {
    middleman.inboundMarket = middlemanMarket
    middleman.outboundMarket = exporterMarket
    middleman.rockefeller = rockefeller
  }
  protected def wireExporterMarket = {
    exporterMarket.seller = middleman
    exporterMarket.buyer = exporter
  }
  protected def wireExporter = {
    exporter.inboundMarket = exporterMarket
    exporter.outboundMarket = consumerMarket
  }
  protected def wireConsumerMarket = {
    consumerMarket.seller = exporter
    consumerMarket.buyer = consumer
  }
  protected def wireConsumer = consumer.consumerMarket = consumerMarket
  protected def wireSingleSellerSupportAvailable = singleSellerSupportAvailable.rockefeller = rockefeller
}

case class ValueChainBuilderFull(val c: ModelConfig)(implicit val t: Timeline) extends ValueChainBuilderDefault

case class ValueChainBuilderNoMiddleMan(val c: ModelConfig)(implicit val t: Timeline) extends ValueChainBuilderDefault {

  // Middleman Market is no longer in the chain
  override def createMiddlemanMarket = new NoopMarket with MarketRecorder
  override protected def createMiddlemanMarketState = MarketState(MoneyPerTonnes(0d), PendingSell(null, FinancialTransaction.none), PendingBuy(null, FinancialTransaction.none))
  override protected def wireMiddlemanMarket = {}

  // Municipal Fisher goes strait to wholesale market (part 1, setup the fisher)
  protected def lastMunicipalFisherToWholesaleTransaction = FinancialTransaction(c.middlemanMarket.middlemanMarketFishPrice, c.middlemanMarket.lastTraded * c.deltaT, c.middlemanMarket.lastExcess * c.deltaT)

  override protected def createMunicipalFisher = new MunicipalFisher(c.municipalFisher.numberOfFishermenInPopulation,
    c.municipalFisher.effortGrowthCoeff,
    c.municipalFisher.populationSizeGrowthCoeff,
    c.municipalFisher.effortPerFishermanPerYear,
    c.municipalFisher.alternativeEffortPerFishermanPerYear,
    c.municipalFisher.nonFishingIncome,
    c.middleman.middlemanOverhead,
    c.municipalFisher.ratioOfInitialPopulationNeverLeaving,
    c.municipalFisher.ratioOfMaximumEffortToAcceptableEffort,
    c.municipalFisher.ratioOfInitialEffortToAcceptableEffort,
    c.municipalFisher.alternativeLivelihood.culturalFishingChoiceBias) with FisherRecorder // Municipal fisher takes on middleman overhead
  override protected def createMunicipalFisherState = MunicipalFisherState(t.now, c.municipalFisher.numberOfFishermenInPopulation, c.municipalFisher.effortPerFishermanPerYear, c.municipalFisher.alternativeEffortPerFishermanPerYear, c.municipalFisher.costOfLivingPerFisherman, c.municipalFisher.variableCostPerUnitEffort, c.municipalFisher.fixedCostPerFisherman, c.municipalFisher.spoiledCatchRatio, c.municipalFisher.otherCatchRatio, c.municipalFisher.otherValueRatio, c.municipalFisher.alternativeLivelihood.alternativeJobsAvailable, c.municipalFisher.alternativeLivelihood.alternativeJobWorkload, c.municipalFisher.alternativeLivelihood.numberOfPeopleTrained, c.municipalFisher.alternativeLivelihood.numberOfNonFishermenTrained, c.municipalFisher.alternativeLivelihood.financialBenefit, priceNegotiation, c.municipalFisher.priceNegotiation.enabled, c.municipalFisher.numberOfFishermenInPopulation)

  override protected def wireMunicipalFisher = {
    municipalFisher.middlemanMarket = exporterMarket
    municipalFisher.rockefeller = rockefeller
  }

  // Municipal Fisher goes strait to wholesale market (part 2, setup the wholesale market)
  override protected def createExporterMarketState = MarketState(c.exporterMarket.exporterMarketPrice, PendingSell(municipalFisher, lastMunicipalFisherToWholesaleTransaction), PendingBuy(exporter, lastMiddlemanToExporterTransaction))
  override protected def wireExporterMarket = {
    exporterMarket.seller = municipalFisher
    exporterMarket.buyer = exporter
  }

  // Middleman is no longer in the chain
  override def createMiddleman = new NoopIntermediary with IntermediaryRecorder
  override protected def createMiddlemanState = IntermediaryState(
    bought = FinancialTransaction.none,
    sold = FinancialTransaction.none,
    demand = Tonnes(0),
    aggressiveInboundPriceSetting = c.middleman.aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = c.middleman.aggressiveOutboundPriceSetting,
    desiredProfitRate = c.middleman.middlemanDesiredProfitRate,
    desireToIncreaseMarketShare = c.middleman.desireToIncreaseMarketShare)
  override protected def wireMiddleman = {}

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
// A Value Chain used only to be able to test fisherman feedback loops in isolation from inermediary dynamics
case class ValueChainBuilderNoIntermediaries(val c: ModelConfig)(implicit val t: Timeline) extends ValueChainBuilderDefault {

  // Middleman Market is no longer in the chain
  override def createMiddlemanMarket = new NoopMarket with MarketRecorder
  override def createExporterMarket = new NoopMarket with MarketRecorder
  override protected def createMiddlemanMarketState = MarketState(MoneyPerTonnes(0d), PendingSell(null, FinancialTransaction.none), PendingBuy(null, FinancialTransaction.none))
  override protected def createExporterMarketState = MarketState(MoneyPerTonnes(0d), PendingSell(null, FinancialTransaction.none), PendingBuy(null, FinancialTransaction.none))
  override protected def wireMiddlemanMarket = {}
  override protected def wireExporterMarket = {}

  // Municipal Fisher goes strait to consumer market (part 1, setup the fisher)
  protected def lastMunicipalFisherToWholesaleTransaction = FinancialTransaction(c.middlemanMarket.middlemanMarketFishPrice, c.consumerMarket.lastTraded * c.deltaT, c.consumerMarket.lastExcess * c.deltaT)

  override protected def createMunicipalFisher = new MunicipalFisher(c.municipalFisher.numberOfFishermenInPopulation,
    c.municipalFisher.effortGrowthCoeff,
    c.municipalFisher.populationSizeGrowthCoeff,
    c.municipalFisher.effortPerFishermanPerYear,
    c.municipalFisher.alternativeEffortPerFishermanPerYear,
    c.municipalFisher.nonFishingIncome,
    MoneyPerTonnes(0),
    c.municipalFisher.ratioOfInitialPopulationNeverLeaving,
    c.municipalFisher.ratioOfMaximumEffortToAcceptableEffort,
    c.municipalFisher.ratioOfInitialEffortToAcceptableEffort,
    c.municipalFisher.alternativeLivelihood.culturalFishingChoiceBias) with FisherRecorder // Municipal fisher takes on middleman overhead
  override protected def createMunicipalFisherState = MunicipalFisherState(t.now, c.municipalFisher.numberOfFishermenInPopulation, c.municipalFisher.effortPerFishermanPerYear, c.municipalFisher.alternativeEffortPerFishermanPerYear, c.municipalFisher.costOfLivingPerFisherman, c.municipalFisher.variableCostPerUnitEffort, c.municipalFisher.fixedCostPerFisherman, c.municipalFisher.spoiledCatchRatio, c.municipalFisher.otherCatchRatio, c.municipalFisher.otherValueRatio, c.municipalFisher.alternativeLivelihood.alternativeJobsAvailable, c.municipalFisher.alternativeLivelihood.alternativeJobWorkload, c.municipalFisher.alternativeLivelihood.numberOfPeopleTrained, c.municipalFisher.alternativeLivelihood.numberOfNonFishermenTrained, c.municipalFisher.alternativeLivelihood.financialBenefit, priceNegotiation, c.municipalFisher.priceNegotiation.enabled, c.municipalFisher.numberOfFishermenInPopulation)

  override protected def wireMunicipalFisher = municipalFisher.middlemanMarket = consumerMarket

  // Municipal Fisher goes strait to wholesale market (part 2, setup the wholesale market)

  // Middleman is no longer in the chain
  override def createMiddleman = new NoopIntermediary with IntermediaryRecorder
  override def createExporter = new NoopIntermediary with IntermediaryRecorder
  override protected def createMiddlemanState = IntermediaryState(
    bought = FinancialTransaction.none,
    sold = FinancialTransaction.none,
    demand = Tonnes(0),
    aggressiveInboundPriceSetting = c.middleman.aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = c.middleman.aggressiveOutboundPriceSetting,
    desiredProfitRate = c.middleman.middlemanDesiredProfitRate,
    desireToIncreaseMarketShare = c.middleman.desireToIncreaseMarketShare)

  override protected def createExporterState = IntermediaryState(
    bought = FinancialTransaction.none,
    sold = FinancialTransaction.none,
    demand = Tonnes(0),
    aggressiveInboundPriceSetting = c.exporter.aggressiveInboundPriceSetting,
    aggressiveOutboundPriceSetting = c.exporter.aggressiveOutboundPriceSetting,
    desiredProfitRate = c.exporter.exporterDesiredProfitRate,
    desireToIncreaseMarketShare = 0d)

  override protected def wireMiddleman = {}
  override protected def wireExporter = {}
  override protected def wireConsumerMarket = {
    consumerMarket.seller = municipalFisher
    consumerMarket.buyer = consumer
  }

  override protected def createConsumer = new Consumer(c.consumer.consumerGrowthRate, c.middlemanMarket.middlemanMarketFishPrice, c.consumer.ε, c.consumer.initialDemandPerPerson) with AgentRecorder[ConsumerState]
  override protected def createConsumerMarketState = MarketState(c.middlemanMarket.middlemanMarketFishPrice, PendingSell(municipalFisher, lastMunicipalFisherToWholesaleTransaction), PendingBuy(consumer, lastMunicipalFisherToWholesaleTransaction))
}
