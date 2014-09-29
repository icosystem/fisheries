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
import play.Logger
import simulation.unit._
import simulation.unit.FishingPerTimeFishermen
import simulation.unit.Tonnes
import Double.NaN
import model.QuotaTypeTACMaxEntry
import model.QuotaTypeTACFixedCommunity
import model.QuotaTypeNone
import simulation.unit.Fishermen
import simulation.unit.Percent
import simulation.unit.TonnesPerFishing

case class MunicipalFisher(initialNumberOfFishermenInPopulation: Fishermen /* AKA population, population size */ ,
  effortGrowthCoeff: FishingPerMoney,
  fishermanPopulationSizeGrowthCoeff: FishermenPerMoney,
  initialEffortPerFishermanPerYear: FishingPerTimeFishermen,
  initialAlternativeEffortPerFishermanPerYear: FishingPerTimeFishermen,
  nonFishingIncome: MoneyPerTimeFishermen,
  transportationCostPerTon: MoneyPerTonnes,
  ratioOfInitialPopulationNeverLeaving: Percent,
  ratioOfMaximumEffortToAcceptableEffort: Percent,
  ratioOfInitialEffortToAcceptableEffort: Percent,
  culturalFishingChoiceBias: Double)(implicit val t: Timeline) extends Agent[MunicipalFisherState](t) with Seller with Fisher[MunicipalFisherState] {

  var fishRelationship: FishFisherRelationship = null
  var middlemanMarket: Market = null
  var singleSellerSupportAvailable: SingleSellerSupportAvailable = null
  var rockefeller: Rockefeller = null

  val minimumEffortPerFishermanPerYear = FishingPerTimeFishermen(0)
  val initialTotalEffortPerFishermanPerYear = initialEffortPerFishermanPerYear + initialAlternativeEffortPerFishermanPerYear
  val acceptableEffortPerFishermanPerYear = initialTotalEffortPerFishermanPerYear / ratioOfInitialEffortToAcceptableEffort
  val maximumEffortPerFishermanPerYear = acceptableEffortPerFishermanPerYear * ratioOfMaximumEffortToAcceptableEffort

  val minimumNumberOfFishermenInPopulation = initialNumberOfFishermenInPopulation * ratioOfInitialPopulationNeverLeaving

  def fishingResult: FishingResult = fishRelationship.fish.fishingResult(this)
  def totalSoldThisTick = middlemanMarket.getSold(this)

  def totalOffer = gradeACatchRate(fishingResult.totalCatchRate) * t.deltaT
  def effortAllFishermen = currentState.effortAllFishermen

  def gatherState = {
    val lastKnownCatchPerUnitEffort = fishingResult.catchPerUnitEffort
    val lastKnownPrice = totalSoldThisTick.pricePerTon
    val expectedEffortRatePerFisherman = expectedActualEffortRatePerFisherman(currentState.effortPerFishermanPerYear, currentState.alternativeEffortPerFishermanPerYear, lastKnownCatchPerUnitEffort)
    Logger.info("expectedEffortRatePerFisherman: " + expectedEffortRatePerFisherman)
    val expectedFinancials = getExpectedFinancials(expectedEffortRatePerFisherman, lastKnownCatchPerUnitEffort, lastKnownPrice)

    val newPop = newNumberOfFishermenInPopulation_Elena_take_58(expectedFinancials)

    val newTotalEffort = newEffortPerFishermanPerYear_Elena_take_58(expectedEffortRatePerFisherman, expectedFinancials)
    val (newBrokenDownEffort, newPriceNegotiation) = breakDownTotalEffort(newTotalEffort, expectedEffortRatePerFisherman, lastKnownCatchPerUnitEffort, lastKnownPrice)

    val newNumberOfNonFishermenTrained = if (newPop > currentState.numberOfFishermenInPopulation) {
      // population growth, some of the people coming in may be trained people, so we need to decrease numberOfNonFishermenTrained
      max(currentState.numberOfNonFishermenTrained - (newPop - currentState.numberOfFishermenInPopulation), Fishermen(0))
    } else {
      // population decrease, trained people leave first, potentially increasing numberOfNonFishermenTrained
      currentState.numberOfNonFishermenTrained + min(currentState.numberOfFishermenTrained, currentState.numberOfFishermenInPopulation - newPop)
    }

    rockefeller(currentState.copy(timeCreated = t.now, numberOfFishermenInPopulation = newPop, numberOfNonFishermenTrained = newNumberOfNonFishermenTrained, effortPerFishermanPerYear = newBrokenDownEffort.fishingEffortPerFishermanPerYear, alternativeEffortPerFishermanPerYear = newBrokenDownEffort.alternativeEffortPerFishermanPerYear, priceNegotiation = newPriceNegotiation))
  }

  def moveFromCurrentToTarget(currentEffortPerFisherman: FishingPerTimeFishermen, targetEffortPerFisherman: FishingPerTimeFishermen) = targetEffortPerFisherman * effortGrowthCoeff.v + currentEffortPerFisherman * (1 - effortGrowthCoeff.v)

  ////////////////////////////////////////////////////////////////////////////////
  // expected financials
  private def getExpectedFinancials(intendedEffortRatePerFisherman: BrokenDownEffort, lastKnownCatchPerUnitEffort: TonnesPerFishing, lastKnownPrice: MoneyPerTonnes): PerFishermanFinancialRates = {
    val expectedEffortRatePerFisherman = expectedActualEffortRatePerFisherman(intendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear, intendedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear, lastKnownCatchPerUnitEffort)
    val expectedTotalCatchRatePerFisherman = likelyTotalCatchRatePerFisherman(expectedEffortRatePerFisherman.fishingEffortPerFishermanPerYear, lastKnownCatchPerUnitEffort)
    getExpectedFinancials(expectedEffortRatePerFisherman, expectedTotalCatchRatePerFisherman, lastKnownPrice)
  }

  private def getExpectedFinancials(expectedEffortRatePerFisherman: BrokenDownEffort, expectedTotalCatchRatePerFisherman: TonnesPerTimeFishermen, lastKnownPrice: MoneyPerTonnes): PerFishermanFinancialRates = {
    PerFishermanFinancialRates(
      fishingRevenue = todaysExpectedRevenueRatePerFisherman(expectedTotalCatchRatePerFisherman, lastKnownPrice),
      fishingFixedCosts = currentState.fixedCostPerFishermanPerYear,
      fishingEffortBasedCosts = effortBasedFishingCostRatePerFisherman(expectedEffortRatePerFisherman.fishingEffortPerFishermanPerYear),
      fishingCatchBasedCosts = transportBasedFishingCostRatePerFisherman(expectedTotalCatchRatePerFisherman), // for now just transport
      alternativeLivelihoodRevenue = alternativeProfitRatePerFisherman(expectedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear),
      otherFixedNonFishingIncome = nonFishingIncome,
      costOfLiving = currentState.costOfLivingPerFisherman)
  }

  def priceIsFair(lastKnownCatchPerUnitEffort: TonnesPerFishing, lastKnownPrice: MoneyPerTonnes) = {
    getFairPrice(lastKnownCatchPerUnitEffort) <= lastKnownPrice
  }

  def getFairPrice(lastKnownCatchPerUnitEffort: TonnesPerFishing) = {
    // expected actual effort if attempting to do exactly acceptable amount of effort, all of it fishing
    val expectedEffortRatePerFisherman = expectedActualEffortRatePerFisherman(acceptableEffortPerFishermanPerYear, FishingPerTimeFishermen(0), lastKnownCatchPerUnitEffort)
    val expectedFishingEffortRatePerFisherman = expectedEffortRatePerFisherman.fishingEffortPerFishermanPerYear
    Logger.info("expectedFishingEffortRatePerFisherman: " + expectedFishingEffortRatePerFisherman)
    val expectedTotalCatchRatePerFisherman = likelyTotalCatchRatePerFisherman(expectedFishingEffortRatePerFisherman, lastKnownCatchPerUnitEffort)
    Logger.info("expectedTotalCatchRatePerFisherman: " + expectedTotalCatchRatePerFisherman)
    Logger.info("moneyForHappyLife: " + PerFishermanFinancialRates.profitsNeededForGoodLife(currentState.costOfLivingPerFisherman))
    Logger.info("catchForAcceptableEffort:" + (expectedTotalCatchRatePerFisherman * (currentState.gradeACatchRatio + currentState.otherCatchRatio * currentState.otherValueRatio)))
    Logger.info("costForAcceptableEffort: " + fishingCostRatePerFisherman(expectedFishingEffortRatePerFisherman, expectedTotalCatchRatePerFisherman))

    val fairPrice = FairPriceCalculator.fairPrice(
      moneyForHappyLife = PerFishermanFinancialRates.profitsNeededForGoodLife(currentState.costOfLivingPerFisherman, currentState.priceNegotiation.target),
      catchForAcceptableEffort = (expectedTotalCatchRatePerFisherman * (currentState.gradeACatchRatio + currentState.otherCatchRatio * currentState.otherValueRatio)),
      costForAcceptableEffort = fishingCostRatePerFisherman(expectedFishingEffortRatePerFisherman, expectedTotalCatchRatePerFisherman));
    fairPrice
  }

  ////////////////////////////////////////////////////////////////////////////////
  // expectation-based population loop
  private def newNumberOfFishermenInPopulation_Elena_take_58(expectedFinancials: PerFishermanFinancialRates) = {
    Logger.info("bottomLine: " + expectedFinancials.bottomLine)
    val populationGrowthRatePerYear = expectedFinancials.bottomLine * fishermanPopulationSizeGrowthCoeff
    var newNumberOfFishermenInPopulation = max(minimumNumberOfFishermenInPopulation, currentState.numberOfFishermenInPopulation * (1 + populationGrowthRatePerYear * t.deltaT))
    if (fishRelationship.quotaType == QuotaTypeTACMaxEntry) {
      newNumberOfFishermenInPopulation = min(newNumberOfFishermenInPopulation, currentState.quotaPopulation)
    } else if (fishRelationship.quotaType == QuotaTypeTACFixedCommunity) {
      newNumberOfFishermenInPopulation = currentState.numberOfFishermenInPopulation
    }
    newNumberOfFishermenInPopulation
  }

  // expectation-based effort loop
  private val fishingToAlternativeAdjustmentSpeedCoeff = .5 // ??? 0 would mean ignoring the gradient, Inf would mean jumping straight to the gradient; 1 means we make the gradient similar in magnitude to the current effort level 
  private val irrationalChoiceBias = 0.2 // only slightly irrational
  private def breakDownTotalEffort(totalEffort: FishingPerTimeFishermen, lastIntendedEffortRatePerFisherman: BrokenDownEffort, lastKnownCatchPerUnitEffort: TonnesPerFishing, lastKnownPrice: MoneyPerTonnes) = {
    Logger.info("lastIntendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear : " + lastIntendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear)
    Logger.info("lastIntendedEffortRatePerFisherman.altEffortPerFishermanPerYear     : " + lastIntendedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear)
    Logger.info("last total effort                                                   : " + lastIntendedEffortRatePerFisherman.totalEffort)
    Logger.info("new total effort                                                    : " + totalEffort)

    val scalingFactor = totalEffort.v * fishingToAlternativeAdjustmentSpeedCoeff * t.deltaT.v

    val deltaEffort = FishingPerTimeFishermen(scalingFactor)
    val fishingGradient = getFishingGradient(lastIntendedEffortRatePerFisherman, deltaEffort, lastKnownCatchPerUnitEffort, lastKnownPrice)
    Logger.info("fishingGradient     : " + fishingGradient)
    val trueFishingGradient = fishingGradient * culturalFishingChoiceBias
    Logger.info("trueFishingGradient     : " + trueFishingGradient)
    val alternativeGradient = getAlternativeGradient(lastIntendedEffortRatePerFisherman, deltaEffort, lastKnownCatchPerUnitEffort, lastKnownPrice)
    Logger.info("alternativeGradient : " + alternativeGradient)

    val targets = BiasedChoice.getWeights(irrationalChoiceBias)(trueFishingGradient.v, alternativeGradient.v)
    val targetFishingRatio = targets(0)
    val targetAlternativeRatio = targets(1)
    Logger.info("targetFishingRatio : " + targetFishingRatio)
    Logger.info("targetAlternativeRatio : " + targetAlternativeRatio)

    val fishingEffortBeforeNormalizing = lastIntendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear + FishingPerTimeFishermen(targetFishingRatio.v * scalingFactor)
    val alternativeEffortBeforeNormalizing = lastIntendedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear + FishingPerTimeFishermen(targetAlternativeRatio.v * scalingFactor)
    val totalEffortBeforeNormalizing = fishingEffortBeforeNormalizing + alternativeEffortBeforeNormalizing
    val newFishingEffort = (fishingEffortBeforeNormalizing * (totalEffort / totalEffortBeforeNormalizing))
    val newAlternativeEffort = (alternativeEffortBeforeNormalizing * (totalEffort / totalEffortBeforeNormalizing))
    Logger.info("new fishing effort     : " + newFishingEffort)
    Logger.info("new alternative effort : " + newAlternativeEffort)

    val expectedFinancialsWithCurrentEffort = getExpectedFinancials(BrokenDownEffort(newFishingEffort, newAlternativeEffort), lastKnownCatchPerUnitEffort, lastKnownPrice)
    val isPriceFair = priceIsFair(lastKnownCatchPerUnitEffort, lastKnownPrice)
    val newPriceNegotiation = currentState.priceNegotiation.update(
      interventionActive = currentState.priceNegotiationEnabled,
      indicatedEffort = newFishingEffort,
      deltaT = t.deltaT,
      expectedFinancialsWithCurrentEffort = expectedFinancialsWithCurrentEffort,
      isPriceFair = isPriceFair,
      acceptableEffortPerFishermanPerYear = acceptableEffortPerFishermanPerYear)
    Logger.info("new fishing effort wPN : " + newPriceNegotiation.interventionEffort)
    //    System.out.println(newPriceNegotiation.toString+" = %.0f".format(newPriceNegotiation.interventionEffort.v));
    (BrokenDownEffort(newPriceNegotiation.interventionEffort, newAlternativeEffort), newPriceNegotiation)
  }

  private def getFishingGradient(lastIntendedEffortRatePerFisherman: BrokenDownEffort, deltaFishingEffort: FishingPerTimeFishermen, lastKnownCatchPerUnitEffort: TonnesPerFishing, lastKnownPrice: MoneyPerTonnes) = {
    val expectedFinancialsWithCurrentEffort = getExpectedFinancials(lastIntendedEffortRatePerFisherman, lastKnownCatchPerUnitEffort, lastKnownPrice)
    val effortWithMoreFishing = BrokenDownEffort(lastIntendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear + deltaFishingEffort, lastIntendedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear)
    val expectedFinancialsWithMoreFishing = getExpectedFinancials(effortWithMoreFishing, lastKnownCatchPerUnitEffort, lastKnownPrice)
    (expectedFinancialsWithMoreFishing.bottomLine - expectedFinancialsWithCurrentEffort.bottomLine) / deltaFishingEffort
  }

  private def getAlternativeGradient(lastIntendedEffortRatePerFisherman: BrokenDownEffort, deltaAlternativeEffort: FishingPerTimeFishermen, lastKnownCatchPerUnitEffort: TonnesPerFishing, lastKnownPrice: MoneyPerTonnes) = {
    val expectedFinancialsWithCurrentEffort = getExpectedFinancials(lastIntendedEffortRatePerFisherman, lastKnownCatchPerUnitEffort, lastKnownPrice)
    val effortWithMoreAlternative = BrokenDownEffort(lastIntendedEffortRatePerFisherman.fishingEffortPerFishermanPerYear, lastIntendedEffortRatePerFisherman.alternativeEffortPerFishermanPerYear + deltaAlternativeEffort)
    val expectedFinancialsWithMoreAlternative = getExpectedFinancials(effortWithMoreAlternative, lastKnownCatchPerUnitEffort, lastKnownPrice)
    (expectedFinancialsWithMoreAlternative.bottomLine - expectedFinancialsWithCurrentEffort.bottomLine) / deltaAlternativeEffort
  }

  private def newEffortPerFishermanPerYear_Elena_take_58(expectedEffortRatePerFisherman: BrokenDownEffort, expectedFinancials: PerFishermanFinancialRates): FishingPerTimeFishermen = {
    var newIntendedEffortRatePerFisherman = expectedEffortRatePerFisherman.totalEffort
    if (expectedFinancials.effortIsUnprofitable) {
      if (fishermanWorksTooMuch(expectedEffortRatePerFisherman.totalEffort)) {
        Logger.info("1 : " + (t.now.v * t.deltaT.v))
      } else {
        Logger.info("5 : " + (t.now.v * t.deltaT.v))
      }
      // Must stop losing money! Or maybe not even able to buy gasoline to go fishing
      newIntendedEffortRatePerFisherman = moveFromCurrentToTarget(expectedEffortRatePerFisherman.totalEffort, FishingPerTimeFishermen(0))
    } else if (expectedFinancials.fishermanDoesNotMakeEndsMeet) {
      if (fishermanWorksTooMuch(expectedEffortRatePerFisherman.totalEffort)) {
        Logger.info("2 : " + (t.now.v * t.deltaT.v))
      } else {
        Logger.info("6 : " + (t.now.v * t.deltaT.v))
      }
      // Fishing is profitable, but not making ends meet, will fish more if possible, even if already fishing more than acceptable
      newIntendedEffortRatePerFisherman = moveFromCurrentToTarget(expectedEffortRatePerFisherman.totalEffort, maximumEffortPerFishermanPerYear)
    } else if (expectedFinancials.fishermanDoesNotHaveGoodLife) {
      // Fishing profitable and making ends meet, but not having good life
      if (fishermanWorksTooMuch(expectedEffortRatePerFisherman.totalEffort)) {
        Logger.info("3 : " + (t.now.v * t.deltaT.v))
        // Conflicting goals: a) increase effort to get to good life; b) decrease effort because already working more than acceptable
        // inertia: do nothing?
      } else {
        Logger.info("7 : " + (t.now.v * t.deltaT.v))
        // Not working all that much, will try to increase effort in hope of getting to good life
        newIntendedEffortRatePerFisherman = moveFromCurrentToTarget(expectedEffortRatePerFisherman.totalEffort, acceptableEffortPerFishermanPerYear)
      }
      // do nothing? conflicting goals of up and down
    } else /* has good life */ if (fishermanWorksTooMuch(expectedEffortRatePerFisherman.totalEffort)) {
      Logger.info("4 : " + (t.now.v * t.deltaT.v))
      newIntendedEffortRatePerFisherman = moveFromCurrentToTarget(expectedEffortRatePerFisherman.totalEffort, acceptableEffortPerFishermanPerYear)
    } else {
      Logger.info("8 : " + (t.now.v * t.deltaT.v))
      // do nothing, happy living the good life with lower effort
    }
    Logger.debug("todaysIntendedEffortRatePerFisherman :" + expectedEffortRatePerFisherman)
    Logger.debug("newEffortPerFishermanPerYear :" + newIntendedEffortRatePerFisherman)
    newIntendedEffortRatePerFisherman
  }

  // generic catch computations
  private def totalCatchRatePerFisherman(effortRatePerFisherman: FishingPerTimeFishermen, catchPerUnitEffort: TonnesPerFishing) = {
    effortRatePerFisherman * catchPerUnitEffort
  }

  private def likelyTotalCatchRatePerFisherman(effortRatePerFisherman: FishingPerTimeFishermen, catchPerUnitEffort: TonnesPerFishing) = {
    val tentativeCatchRate = totalCatchRatePerFisherman(effortRatePerFisherman, catchPerUnitEffort)
    if (fishRelationship.quotaType == QuotaTypeNone) {
      tentativeCatchRate
    } else {
      min(tentativeCatchRate, fishRelationship.yearlyQuotaAmount / currentState.numberOfFishermenInPopulation)
    }
  }

  // expected effort and catch
  def expectedActualEffortRatePerFisherman(todaysIntendedEffortRatePerFisherman: FishingPerTimeFishermen, todaysIntendedAlternativeEffortRatePerFisherman: FishingPerTimeFishermen, lastKnownCatchPerUnitEffort: TonnesPerFishing) = {
    if (fishRelationship.quotaType == QuotaTypeNone) {
      BrokenDownEffort(todaysIntendedEffortRatePerFisherman, todaysIntendedAlternativeEffortRatePerFisherman)
    } else {
      BrokenDownEffort(min(todaysIntendedEffortRatePerFisherman, (fishRelationship.yearlyQuotaAmount / currentState.numberOfFishermenInPopulation) / lastKnownCatchPerUnitEffort), todaysIntendedAlternativeEffortRatePerFisherman)
    }
  }

  // generic cost computations
  def effortBasedFishingCostRatePerFisherman(effortRatePerFisherman: FishingPerTimeFishermen) = {
    effortRatePerFisherman * currentState.variableCostPerUnitEffort
  }

  def transportBasedFishingCostRatePerFisherman(totalCatchRatePerFisherman: TonnesPerTimeFishermen): MoneyPerTimeFishermen = {
    totalCatchRatePerFisherman * transportationCostPerTon
  }

  def variableFishingCostRatePerFisherman(effortPerFishermanPerYear: FishingPerTimeFishermen, totalCatchRatePerFisherman: TonnesPerTimeFishermen) = {
    effortBasedFishingCostRatePerFisherman(effortPerFishermanPerYear) + transportBasedFishingCostRatePerFisherman(totalCatchRatePerFisherman)
  }

  def fishingCostRatePerFisherman(effortPerFishermanPerYear: FishingPerTimeFishermen, totalCatchRatePerFisherman: TonnesPerTimeFishermen): MoneyPerTimeFishermen = {
    currentState.fixedCostPerFishermanPerYear + variableFishingCostRatePerFisherman(effortPerFishermanPerYear, totalCatchRatePerFisherman)
  }

  // expected revenues
  def todaysExpectedRevenueRatePerFisherman(expectedTotalCatchRatePerFisherman: TonnesPerTimeFishermen, lastKnownPrice: MoneyPerTonnes) = {
    val expectedSoldAsGradeACatchRatePerFisherman = gradeACatchRatePerFisherman(expectedTotalCatchRatePerFisherman) // this reflects the expectation that there is demand for their entire grade A catch
    val expectedSoldAsGradeOtherCatchRatePerFisherman = gradeOtherCatchRatePerFisherman(expectedTotalCatchRatePerFisherman)
    revenueRatePerFisherman(expectedSoldAsGradeACatchRatePerFisherman, expectedSoldAsGradeOtherCatchRatePerFisherman, currentState.otherValueRatio, lastKnownPrice)
  }
  def revenueRatePerFisherman(soldAsGradeACatchRatePerFisherman: TonnesPerTimeFishermen, soldAsGradeOtherCatchRatePerFisherman: TonnesPerTimeFishermen, otherValueRatio: Percent, price: MoneyPerTonnes): MoneyPerTimeFishermen = {
    soldAsGradeACatchRatePerFisherman * price + soldAsGradeOtherCatchRatePerFisherman * otherValueRatio * price
  }

  def gradeACatchRatePerFisherman(totalCatchRatePerFisherman: TonnesPerTimeFishermen): TonnesPerTimeFishermen = totalCatchRatePerFisherman * currentState.gradeACatchRatio
  def gradeACatchRate(totalCatchRate: TonnesPerTime): TonnesPerTime = totalCatchRate * currentState.gradeACatchRatio

  def gradeOtherCatchRatePerFisherman(totalCatchRatePerFisherman: TonnesPerTimeFishermen): TonnesPerTimeFishermen = totalCatchRatePerFisherman * currentState.otherCatchRatio
  def gradeOtherCatchRate(totalCatchRate: TonnesPerTime): TonnesPerTime = totalCatchRate * currentState.otherCatchRatio

  def spoiledCatchRate(totalCatchRate: TonnesPerTime): TonnesPerTime = totalCatchRate * currentState.spoiledCatchRatio

  def alternativeProfitRatePerFisherman(alternativeEffortPerFishermanPerYear: FishingPerTimeFishermen): MoneyPerTimeFishermen = {
    Logger.info("alternativeJobWorkload: " + currentState.alternativeJobWorkload)
    val totalDesiredAlternativeEffort = alternativeEffortPerFishermanPerYear * currentState.numberOfFishermenInPopulation
    val totalAvailableAlternativeEffort = currentState.alternativeJobWorkload * currentState.alternativeJobsAvailable
    Logger.info("numberOfFishermenInPopulation: " + currentState.numberOfFishermenInPopulation)
    Logger.info("numberOfPeopleTrained: " + currentState.numberOfPeopleTrained)
    Logger.info("numberOfNonFishermenTrained: " + currentState.numberOfNonFishermenTrained)
    Logger.info("numberOfFishermenTrained: " + currentState.numberOfFishermenTrained)
    if (currentState.numberOfFishermenInPopulation < currentState.numberOfFishermenTrained) { throw new IllegalArgumentException("cannot have more trained fisherman (" + currentState.numberOfFishermenTrained + ") than total fishermen (" + currentState.numberOfFishermenInPopulation + ")") }
    val totalPossibleAlternativeEffort = currentState.alternativeJobWorkload * currentState.numberOfFishermenTrained
    currentState.financialBenefitOfAlternative * min(totalDesiredAlternativeEffort, min(totalPossibleAlternativeEffort, totalAvailableAlternativeEffort)) / currentState.numberOfFishermenInPopulation
  }

  // situations
  def fishermanWorksTooMuch(effortPerFishermanPerYear: FishingPerTimeFishermen) = {
    effortPerFishermanPerYear > acceptableEffortPerFishermanPerYear
  }

  /**
   * http://en.wikipedia.org/wiki/Gompertz_function
   * z(t) = myGompertz(t) = y(-t) with a > 0, so that:
   * z(-Inf)=y(+Inf)=a  ==> a = 1, because we want z(-Inf) = 1
   * z(+Inf)=y(-Inf)=0  (because y(-Inf) = 0 for all choices of a, b and c)
   * z(0)=y(0)=a * e^b  ==> b = -ln(2a), because we want z(0)=1/2
   * Slope at 0 is -a*b*c*e^b.  Line with that slope intersects z=0 at t0=1/(2*a*b*c*e^b).  z will become very close to 0 shortly after that.
   * So set c such that z becomes very close to 0 at some t related to the costOfLiving
   * Perhaps if fishing surplus is more than what you need to live, then incentive to grow profits is close to 0 (i.e. make t0 = costOfLiving)
   */
  def myGompertz(t: Double, costOfLiving: Double) = {
    val a = 1
    val b = -math.log(2 * a)
    val c = 1 / (2 * a * b * costOfLiving * math.exp(b))
    val g = a * math.exp(b * math.exp(-c * t))
    Logger.debug(t + ":" + g)
    g
  }

}

case class MunicipalFisherState(
  timeCreated: Int,
  numberOfFishermenInPopulation: Fishermen,
  effortPerFishermanPerYear: FishingPerTimeFishermen,
  alternativeEffortPerFishermanPerYear: FishingPerTimeFishermen,
  costOfLivingPerFisherman: MoneyPerTimeFishermen,
  variableCostPerUnitEffort: MoneyPerFishing /* AKA fuel, ice */ ,
  fixedCostPerFishermanPerYear: MoneyPerTimeFishermen /* AKA owning a boat, licensing costs */ ,
  spoiledCatchRatio: Percent,
  otherCatchRatio: Percent,
  otherValueRatio: Percent,
  alternativeJobsAvailable: Fishermen,
  alternativeJobWorkload: FishingPerTimeFishermen /* number of hours of alternative work available per year per job */ ,
  numberOfPeopleTrained: Fishermen /* number of people trained */ ,
  numberOfNonFishermenTrained: Fishermen /* number of people trained */ ,
  financialBenefitOfAlternative: MoneyPerFishing,
  priceNegotiation: PriceNegotiation,
  priceNegotiationEnabled: Boolean,
  quotaPopulation: Fishermen) {
  // TODO change the word Fishing in all units to something life Work or Effort

  if ((spoiledCatchRatio + otherCatchRatio).v > 1d) { throw new IllegalArgumentException("spoiled and other catch should not total to over 100%") }
  if (numberOfNonFishermenTrained > numberOfPeopleTrained && numberOfNonFishermenTrained - numberOfPeopleTrained > 1e-6) { throw new IllegalArgumentException("cannot have more trained non-fisherman than total trained") }

  def effortAllFishermen = IntendedGroupEffort(timeCreated, effortPerFishermanPerYear, numberOfFishermenInPopulation)
  def gradeACatchRatio: simulation.unit.Percent = Percent(1d) - (spoiledCatchRatio + otherCatchRatio)
  def numberOfFishermenTrained = numberOfPeopleTrained - numberOfNonFishermenTrained
  def numberOfFishermenNotTrained = numberOfFishermenInPopulation - numberOfFishermenTrained
}

case class BrokenDownEffort(
  fishingEffortPerFishermanPerYear: FishingPerTimeFishermen,
  alternativeEffortPerFishermanPerYear: FishingPerTimeFishermen) {
  def totalEffort = fishingEffortPerFishermanPerYear + alternativeEffortPerFishermanPerYear
}

case class PerFishermanFinancialRates(
  fishingRevenue: MoneyPerTimeFishermen,
  fishingFixedCosts: MoneyPerTimeFishermen,
  fishingEffortBasedCosts: MoneyPerTimeFishermen,
  fishingCatchBasedCosts: MoneyPerTimeFishermen, // for now just transport
  alternativeLivelihoodRevenue: MoneyPerTimeFishermen,
  otherFixedNonFishingIncome: MoneyPerTimeFishermen,
  costOfLiving: MoneyPerTimeFishermen) {
  // computed amounts
  def fishingVariableCosts = fishingEffortBasedCosts + fishingCatchBasedCosts
  def fishingTotalCosts = fishingFixedCosts + fishingVariableCosts
  def fishingProfits = fishingRevenue - fishingTotalCosts
  def fishingSurplus = fishingProfits - costOfLiving // TODO remove this one?
  def totalCosts = fishingTotalCosts + costOfLiving // TODO remove this one?
  def totalProfitsForCombinedEffort = fishingProfits + alternativeLivelihoodRevenue
  def totalProfits = totalProfitsForCombinedEffort + otherFixedNonFishingIncome
  def bottomLine = totalProfits - costOfLiving
  // situations
  def effortIsUnprofitable = totalProfitsForCombinedEffort < 0

  def fishermanMakesEndsMeet = bottomLine >= 0
  def fishermanDoesNotMakeEndsMeet = bottomLine < 0

  def profitsNeededForGoodLife = PerFishermanFinancialRates.profitsNeededForGoodLife(costOfLiving)
  def surplusNeededForGoodLife = profitsNeededForGoodLife - costOfLiving
  def fishermanDoesNotHaveGoodLife = totalProfits < profitsNeededForGoodLife
  def fishermanHasGoodLife = totalProfits >= profitsNeededForGoodLife

  def fishingSituationIsFair = fishingProfits >= profitsNeededForGoodLife
}

object PerFishermanFinancialRates {
  def profitsNeededForGoodLife(costOfLiving: MoneyPerTimeFishermen, multiplier: Double = 2.0) = {
    costOfLiving * multiplier
  }
}

object FairPriceCalculator {
  def fairPrice(moneyForHappyLife: MoneyPerTimeFishermen,
    catchForAcceptableEffort: TonnesPerTimeFishermen,
    costForAcceptableEffort: MoneyPerTimeFishermen): MoneyPerTonnes = {
    val fairPrice = (moneyForHappyLife + costForAcceptableEffort) / catchForAcceptableEffort;
    fairPrice
  }
}
