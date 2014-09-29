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

package controllers.initial

import com.icosystem.math._
import InitialValuesUI._
import controllers.InterventionUI
import controllers.RemoveMiddlemanUI
import model.BoundedDouble
import model.BoundedDouble
import model.BoundedInt
import model.BoundedUnit
import model.QuotaTypeNone
import simulation.unit._
import simulation.unit.Fishermen
import simulation.unit.FishermenPerMoney
import simulation.unit.FishingPerMoney
import simulation.unit.MoneyPerFishing
import simulation.unit.MoneyPerTimeFishermen
import simulation.unit.People
import simulation.unit.PerTime
import simulation.unit.Percent
import simulation.unit.Time
import simulation.unit.TonnesPerTime
import play.Logger
import controllers.LimitEntriesValues
import controllers.UseTotalAllowableCatchValues

object InitialValuesUI {
  def doubleMax(initial: Double) = initial * 2

  def step(max: Double) = {
    def p = math.log10(max)
    math.pow(10, math.round(p - 2))
  }

  val minimumWage = MoneyPerFishing(5.81d / 8d) // $5.81 per day, 8 hours in a day
}
trait InitialValuesUI {
  def uiInputConfig: String

  trait GeneralSim {
    def ticks: Int = 120
    def deltaT = Time(120d / 365d)
    def stepSize = BoundedInt(7, 0, 100, 1)
    def resultAggregationSize: Int = 52
    def years = BoundedInt(10, 1, 40, 1)
  }
  def generalSim = new GeneralSim {}

  // NOTE: These are here because otherwise we get an trait loop!  
  // We might consider pulling all constants in a StudyAndAssumptions trait and in the agent-specific traits do computations 
  // of things needed to initialize the agents, and ranges, etc.
  def allPhilippinesMunicipalCatchPerYearInTons = 56000.0 // EFACT page 101; tons/year; average for 2008-2012
  def allPhilippinesCommercialCatchPerYearInTons = 90000.0 // EFACT page 101; tons/year; average for 2008-2012; rounded from 89999
  // TODO maybe try to use actual values from 2012? trends in chart on page 101 are different for the two sectors over 2008-2012
  def allPhilippinesCommercialToMunicipalRatio = allPhilippinesCommercialCatchPerYearInTons / allPhilippinesMunicipalCatchPerYearInTons
  // assuming same ratio for Mindoro
  def totalMindoroMunicipalCatchPerYearInTons = 5000.0 // EFACT; tons/yr
  def totalMindoroCommercialCatchPerYearInTons = totalMindoroMunicipalCatchPerYearInTons * allPhilippinesCommercialToMunicipalRatio

  def defaultInitialMarketsSpeed = 5

  trait Fish {
    // X = stock (a.k.a. biomass), K = carrying capacity, r = growthRate
    def growthRate = BoundedUnit(PerTime(1.29), PerTime(0), PerTime(2.5), PerTime(0.01)) // Cornell Study, Adu-Asamoah 1982

    // Fraction of carrying capacity in current (stable) stock = X/K
    def ratioOfStockToK = BoundedDouble(0.5d, 0, 1, step(1)) // value at MSY (X_MSY = K / 2)
    // 1 = sustainable catch for current stock; > 1 is overfishing, stock will decline; < 1 is underfishing, allowing stock to grow
    def ratioOfCatchToGrowthOfCurrentStock = BoundedDouble(1, 0.25, 1.75, 0.25)

    // The above values are based on the fact that FAO has "fully exploited" status for Tuna in Philippines
    // Status definitions from: http://www.fao.org/docrep/009/a0653e/a0653e04.htm
    //  Not known (N): not much information is available to make a judgement;
    //	Underexploited (U): undeveloped or new fishery. Believed to have a significant potential for expansion in total production;
    //	Moderately exploited (M): exploited with a low fishing effort. Believed to have some limited potential for expansion in total production;
    //	Fully exploited (F): the fishery is operating at or close to optimal yield/effort, with no expected room for further expansion;
    //	Overexploited (O): the fishery is being exploited above the optimal yield/effort which is believed to be sustainable in the long term, with no potential room for further expansion and a higher risk of stock depletion/collapse;
    //	Depleted (D): catches are well below historical optimal yields, irrespective of the amount of fishing effort exerted;
    //	Recovering (R): catches are again increasing after having been depleted or a collapse from a previous high.

    def totalCatch = totalMindoroMunicipalCatchPerYearInTons + totalMindoroCommercialCatchPerYearInTons
    def initialTotalCatch = BoundedDouble(totalCatch, 0, doubleMax(totalCatch), step(doubleMax(totalCatch)))
  }
  def fish = new Fish {}

  trait MunicipalFisher {
    def tripsPerMonth = 3.0 // EFACT
    def tripLengthInDays = 4.0 // EFACT 3-5
    def fishingHoursPerTripDay = 12.0 // assumption
    def monthsInYear = 12

    def initialEpf = monthsInYear * fishingHoursPerTripDay * tripLengthInDays * tripsPerMonth // (fishingHours / year) / fishermanFamily
    def maxEpf = doubleMax(initialEpf)
    def effortPerFishermanPerYear = BoundedDouble(initialEpf, 0, maxEpf, step(maxEpf))
    def alternativeEffortPerFishermanPerYear = BoundedDouble(0, 0, maxEpf, step(maxEpf))

    def fisherPricePerTon = 1.86 * 1000 // EFACT; $ per ton (1000 kg)
    //    def fisherCostPerTonCatch = 1.23 * 1000 // EFACT; $ per ton
    //    def fisherCostPerTonCatch = fisherPricePerTon * 0.05 // costs are 5%

    def rSpoilCatch = Percent(.05)
    def rMinSpoilCatch = Percent(.01)
    def spoilCatchRatio = BoundedUnit(rSpoilCatch, rMinSpoilCatch, Percent(1.0), Percent(step(1.0)))
    def rOtherCatch = Percent(.35)
    def rMinOtherCatch = Percent(0)
    def otherCatchRatio = BoundedUnit(rOtherCatch, rMinOtherCatch, Percent(1.0), Percent(step(1.0)))

    def priceRatio = .60
    def otherValueRatio = BoundedUnit(Percent(priceRatio), Percent(0.0), Percent(1.0), Percent(step(1.0)))

    def percentOfInitialTotalCatchValue = Percent(totalMindoroMunicipalCatchPerYearInTons / (totalMindoroMunicipalCatchPerYearInTons + totalMindoroCommercialCatchPerYearInTons))
    def percentOfInitialTotalCatch = BoundedUnit(percentOfInitialTotalCatchValue, Percent(0), Percent(1), Percent(0.1))

    def initialCatch = TonnesPerTime(fish.initialTotalCatch.v * percentOfInitialTotalCatch.v)
    def initialGradeACatch = initialCatch * (Percent(1.0) - rSpoilCatch - rOtherCatch)
    def initialGradeOtherCatch = initialCatch * rOtherCatch

    def initialFishingRevenueForCommunity = (initialGradeACatch.v * fisherPricePerTon + initialGradeOtherCatch.v * fisherPricePerTon * priceRatio)

    def initialCoLPerFisherman = 12 * 177d // from Muallil, may need to be adjusted, as it included some fuel costs
    def initialIncomePerFisherman = initialCoLPerFisherman // we want bottom line to be 0
    def ratioOfFishingIncomeToTotalIncome = 0.9
    def initialFishingIncomePerFisherman = initialIncomePerFisherman * ratioOfFishingIncomeToTotalIncome // 10% from other sources
    // fishing costs are supposed to be really low, most of the value added by fishermen is in labor
    def ratioOfCostsToRevenues = 0.05
    // so fishingCosts = 5% * fishingRevenues; fishingIncome = fishingRevenue - fishingCosts = 95% * fishingRevenue => fishingRevenue = fishingIncome / 95%
    def initialFishingRevenuePerFisherman = initialFishingIncomePerFisherman / (1 - ratioOfCostsToRevenues)
    def initialFishingCostsPerFisherman = initialFishingRevenuePerFisherman * ratioOfCostsToRevenues

    // number of fishermen
    def initialN = initialFishingRevenueForCommunity / initialFishingRevenuePerFisherman
    def maxN = doubleMax(initialN)
    def numberOfFishermenInPopulation = BoundedUnit(Fishermen(initialN), Fishermen(0), Fishermen(maxN), Fishermen(step(maxN)))

    def initialPopulation = numberOfFishermenInPopulation

    def fishingCostPerFamilyPerYear = initialFishingCostsPerFisherman
    def initialVcPue = fishingCostPerFamilyPerYear / effortPerFishermanPerYear
    def maxVcPue = doubleMax(initialVcPue)
    def variableCostPerUnitEffort = BoundedUnit(MoneyPerFishing(initialVcPue), MoneyPerFishing(0), MoneyPerFishing(maxVcPue), MoneyPerFishing(step(maxVcPue)))

    def effortGrowthCoeff = BoundedUnit(FishingPerMoney(0.005), FishingPerMoney(0), FishingPerMoney(0.01), FishingPerMoney(0.001)) // TODO how do we set this for real?

    def populationSizeGrowthCoeff = BoundedUnit(FishermenPerMoney(0.0001), FishermenPerMoney(0), FishermenPerMoney(0.001), FishermenPerMoney(0.0001)) // TODO how do we set this for real?

    def maxCoL = doubleMax(initialCoLPerFisherman)
    def costOfLivingPerFisherman = BoundedUnit(MoneyPerTimeFishermen(initialCoLPerFisherman), MoneyPerTimeFishermen(0), MoneyPerTimeFishermen(maxCoL), MoneyPerTimeFishermen(step(maxCoL)))
    def fixedCostPerFisherman = BoundedUnit(MoneyPerTimeFishermen(0d), MoneyPerTimeFishermen(0), MoneyPerTimeFishermen(maxCoL), MoneyPerTimeFishermen(step(maxCoL))) // TODO Elena correct initial value
    def initialNonFishingIncome = initialIncomePerFisherman * (1 - ratioOfFishingIncomeToTotalIncome)
    def maxNonFishingIncome = doubleMax(initialNonFishingIncome)
    def nonFishingIncome = BoundedUnit(MoneyPerTimeFishermen(initialNonFishingIncome), MoneyPerTimeFishermen(0), MoneyPerTimeFishermen(maxNonFishingIncome), MoneyPerTimeFishermen(step(maxNonFishingIncome))) // TOTO Elena correct values here    

    def ratioOfInitialPopulationNeverLeaving = BoundedUnit(Percent(0.5), Percent(0), Percent(1), Percent(0.1))
    def ratioOfMaximumEffortToAcceptableEffort = Percent(1.5) // maybe more like 2 ?
    def ratioOfInitialEffortToAcceptableEffort = Percent(1) // maybe more like 1.2 ?

    def priceNegotiationStressDecayTimeDefaultValue = Time(1.0 / 12.0 * 1.0) // one month
    def priceNegotiationTimeToRetryDefaultValue = Time(365) // one year 
    def priceNegotiationStressThreshold = BoundedDouble(0.25, 0, 1, 0.05)
    def priceNegotiationStressDecayTime = BoundedUnit(priceNegotiationStressDecayTimeDefaultValue, Time(0), Time(priceNegotiationStressDecayTimeDefaultValue.v), Time(1.0))
    def priceNegotiationTimeToRetry = BoundedUnit(priceNegotiationTimeToRetryDefaultValue, Time(0), Time(priceNegotiationTimeToRetryDefaultValue.v * 10), Time(0.1))
    def priceNegotiationTarget = BoundedDouble(1.3, 1, 2, 0.05)
    def priceNegotiationMaxAdherence = BoundedDouble(0.5, 0, 0.9, 0.05)
    def priceNegotiationEnabled = false
  }
  def municipalFisher = new MunicipalFisher {}

  trait AlternativeLivelihood {
    def alternativeJobWorkload = BoundedDouble(0d, 0d, 40d, 1d)
    def alternativeJobsAvailable = BoundedDouble(0, 0d, municipalFisher.initialPopulation.v, 1d)
    def numberOfPeopleTrained = BoundedDouble(0, 0d, municipalFisher.initialPopulation.v, 1d)
    def numberOfNonFishermenTrained = BoundedDouble(0, 0d, municipalFisher.initialPopulation.v, 1d)
    def financialBenefitOfAlternative = BoundedDouble(0.0d, 0d, minimumWage.v * 3, 0.1d)
    def culturalFishingChoiceBias = BoundedDouble(2d, 1d, 3d, 0.1d)
  }
  def alternativeLivelihood = new AlternativeLivelihood {}

  trait CommercialFisher {
    def growthRate = BoundedUnit(PerTime(0.0d), PerTime(0), PerTime(1d), PerTime(step(1)))
  }
  def commercialFisher = new CommercialFisher {}

  trait MiddlemanMarket {
    def max_fisherPricePerTon = doubleMax(municipalFisher.fisherPricePerTon)
    def middlemanMarketFishPrice = BoundedUnit(MoneyPerTonnes(municipalFisher.fisherPricePerTon), MoneyPerTonnes(0), MoneyPerTonnes(max_fisherPricePerTon), MoneyPerTonnes(step(max_fisherPricePerTon)))
    def middlemanMarketSpeed = BoundedUnit(PerTime(defaultInitialMarketsSpeed), PerTime(0), PerTime(3 * defaultInitialMarketsSpeed), PerTime(0.1))
    def lastSupply = municipalFisher.initialGradeACatch
    def lastDemand = municipalFisher.initialGradeACatch
    def lastTraded = municipalFisher.initialGradeACatch
    def lastExcess = TonnesPerTime(0)
  }
  def middlemanMarket = new MiddlemanMarket {}

  trait Middleman {
    def casaCostPerTon = 0.51 * 1000 // EFACT; $/ton, a.k.a., overhead per ton
    def casaSellingPricePerTon = 2.79 * 1000 // EFACT; $/ton
    def initialOverhead = casaCostPerTon
    def maxOverhead = doubleMax(initialOverhead)
    def middlemanOverhead = BoundedUnit(MoneyPerTonnes(initialOverhead), MoneyPerTonnes(0), MoneyPerTonnes(maxOverhead), MoneyPerTonnes(step(maxOverhead)))
    def middlemanInitialProfitRate = casaSellingPricePerTon / municipalFisher.fisherPricePerTon
    def initialSellingPrice = middlemanMarket.middlemanMarketFishPrice * middlemanInitialProfitRate
    def aggressiveInboundPriceSetting = 0.0 // if 0 is a price taker, if 1 or even more, defend assumed price for buying fish
    def aggressiveOutboundPriceSetting = 1.0 // if 0 is a price taker, if 1 or even more, defend profit rate
    def fisherCostsEstimate = MoneyPerTonnes(municipalFisher.fisherPricePerTon)
    def desiredProfitRate = BoundedDouble(middlemanInitialProfitRate, 1, 10, 0.1) // profit rate = 1 when selling at cost 
    def lastDemand = municipalFisher.initialGradeACatch
    def desireToIncreaseMarketShare = 1 // "competitive setting is default for Mindoro, but See PowerfulMiddleman
  }
  def middleman = new Middleman {}

  trait ExporterMarket {
    def maxWMP = doubleMax(middleman.initialSellingPrice.v)
    def exporterMarketFishPrice = BoundedUnit(middleman.initialSellingPrice, MoneyPerTonnes(0), MoneyPerTonnes(maxWMP), MoneyPerTonnes(step(maxWMP)))
    def exporterMarketSpeed = BoundedUnit(PerTime(defaultInitialMarketsSpeed), PerTime(0), PerTime(3 * defaultInitialMarketsSpeed), PerTime(0.1))
    def lastSupply = municipalFisher.initialGradeACatch
    def lastDemand = municipalFisher.initialGradeACatch
    def lastTraded = municipalFisher.initialGradeACatch
    def lastExcess = TonnesPerTime(0)
  }
  def exporterMarket = new ExporterMarket {}

  trait Exporter {
    def exporterCostPerTon = 5.46 * 1000 // EFACT; $/ton, a.k.a., overhead per ton
    def exporterSellingPricePerTon = 16.98 * 1000 // EFACT

    def initialExporterOverhead = exporterMarket.exporterMarketFishPrice * (exporterCostPerTon / middleman.casaSellingPricePerTon)
    def maxExporterOverhead = doubleMax(initialExporterOverhead.v)
    def exporterOverhead = BoundedUnit(initialExporterOverhead, MoneyPerTonnes(0), MoneyPerTonnes(maxExporterOverhead), MoneyPerTonnes(step(maxExporterOverhead)))
    def exporterInitialProfitRate = exporterSellingPricePerTon / exporterMarket.exporterMarketFishPrice.v
    def initialSellingPrice = exporterMarket.exporterMarketFishPrice * exporterInitialProfitRate
    def aggressiveInboundPriceSetting = 0.0 // if 0 is a price taker, if 1 or even more, a price setter
    def aggressiveOutboundPriceSetting = 1.0 // if 0 is a price taker, if 1 or even more, a price setter
    def desiredProfitRate = BoundedDouble(exporterInitialProfitRate, 1, 10, 0.1) // profit rate = 1 when selling at cost
    def middlemanCostEstimate = middleman.initialSellingPrice //TODO: This is a placeholder; come up with a real value

    def lastDemand = municipalFisher.initialGradeACatch
  }
  def exporter = new Exporter {}

  trait ConsumerMarket {
    def maxCMP = doubleMax(exporter.initialSellingPrice.v)
    def consumerMarketFishPrice = BoundedUnit(exporter.initialSellingPrice, MoneyPerTonnes(0), MoneyPerTonnes(maxCMP), MoneyPerTonnes(step(maxCMP)))
    // we should also pass from here the exporterExpectedProfit, stableStateCatch and epsilon currently hardcoded in the Exporter's cef function 
    def exporterExpectedProfit: MoneyPerTime = municipalFisher.initialCatch * (consumerMarketFishPrice - exporterMarket.exporterMarketFishPrice - exporter.exporterOverhead)
    def consumerMarketSpeed = BoundedUnit(PerTime(defaultInitialMarketsSpeed), PerTime(0), PerTime(3 * defaultInitialMarketsSpeed), PerTime(0.1))
    def lastSupply = municipalFisher.initialGradeACatch
    def lastDemand = municipalFisher.initialGradeACatch
    def lastTraded = municipalFisher.initialGradeACatch
    def lastExcess = TonnesPerTime(0)
  }
  def consumerMarket = new ConsumerMarket {}

  trait Consumer {
    // from readings: world-wide pop growth rate 1% per year going down; world-wide food demand growth rate 2.2% per year; using 1.5% per year as ballpark value
    val defaultConsumerGrowthRate = 0.03

    def initialPop = People(1000)
    def maxPop = doubleMax(initialPop.v)
    def consumerPopulation = BoundedUnit(initialPop, People(1), People(maxPop), People(step(maxPop)))
    def initialPrice = consumerMarket.consumerMarketFishPrice
    def initialTotalDemand = municipalFisher.initialGradeACatch
    def elasticityOfDemand = -4
    def initialDemandPerPerson = initialTotalDemand / consumerPopulation

    val consumerGrowthRate = BoundedUnit(PerTime(defaultConsumerGrowthRate), PerTime(0), PerTime(4 * defaultConsumerGrowthRate), PerTime(0.005))
  }
  def consumer = new Consumer {}

  trait SingleSellerSupportAvailable {
    def singleSellerOperationKnowledgeAndSupport = Percent(0)
    def capitalSupport = Percent(0)
  }
  def singleSellerSupportAvailable = new SingleSellerSupportAvailable {}

  trait Intervention {
    def removeMiddleman: RemoveMiddlemanUI.RemoveMiddlemanUI = RemoveMiddlemanUI.CURRENT_BEHAVIOR;
    def costOfFishingFixed = InterventionUI(0d, 1, 4)
    def costOfFishingVariable = InterventionUI(0d, 1, 4)
    def costOfLiving = InterventionUI(0d, 1, 4)
    def productivity = InterventionUI(0d, 1, 4)
    def competitiveness = InterventionUI(0d, 1, 4)
    def fishQuality = InterventionUI(0d, 1, 4)

    def limitEntries = LimitEntriesValues.none._2
    def useTotalAllowableCatch = UseTotalAllowableCatchValues.unlimited._2
    def totalAllowableCatch = InterventionUI(0.3, 1, 4)

    def singleSellerOperationKnowledgeAndSupport = InterventionUI(0d, 1, 4)
    def capitalSupport = BoundedDouble(0, 0, 1, 0.1)

    def financialBenefitOfAlternative = InterventionUI(0d, 1, 4)
    def numberOfPeopleTrained = InterventionUI(0d, 1, 4)
    def alternativeJobsAvailable = InterventionUI(0d, 1, 4)
    def alternativeJobWorkload = InterventionUI(0d, 1, 4)
  }
  def intervention = new Intervention {}
}
