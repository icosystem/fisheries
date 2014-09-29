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

import com.icosystem.cache.SimulationResultsCache
import controllers.initial.{ InitialValuesUIToModelConfigUI, Mindoro, PessimisticMindoro, UIInputConfigDefaults }
import model.{ SimulationAnimatedChartData, SimulationComparisonChartData, SimulationOverviewData, SimulationRunChartData }
import org.apache.commons.lang3.exception.ExceptionUtils
import play.api.libs.json.{ JsError, Json }
import play.api.mvc.{ Action, Controller }
import com.icosystem.cache.FisherySimulationCache

import scala.util._

object Application extends Controller {
  private val resultsIdCache = new SimulationResultsCache()
  private def mindoroModelConfigUI = InitialValuesUIToModelConfigUI(Mindoro)
  private def pessimisticMindoroModelConfigUI = InitialValuesUIToModelConfigUI(PessimisticMindoro)

  /**
   * Application does not use trailing slashes so indicate to browsers
   */
  def untrail(path: String) = Action { MovedPermanently("/" + path) }

  def index = Action { Redirect(routes.Application.insights()) }

  // Insights
  def insights = Action { Ok(views.html.index()) }
  def optimism = Action { Ok(views.html.insights.mindoroFisheryOptimism(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def pessimism = Action { Ok(views.html.insights.mindoroFisheryPessimism(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def inputControls = Action { Ok(views.html.insights.inputControlsCanHelp(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def interventionsCanBackfire = Action { Ok(views.html.insights.interventionsCanBackfire(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def costVsProductivity = Action { Ok(views.html.insights.costVsProductivity(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def inputCantRestore = Action { Ok(views.html.insights.inputCantRestore(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def fairPricing = Action { Ok(views.html.insights.fairPricing(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def alternativeLivelihoods = Action { Ok(views.html.insights.alternativeLivelihood(pessimisticMindoroModelConfigUI, pessimisticMindoroModelConfigUI.uiInputConfigs)) }

  // Abouts
  def aboutTheModel = Action { Ok(views.html.aboutTheModel.landing()) }
  def aboutConsumers = Action { Ok(views.html.aboutTheModel.aboutConsumers(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutMarkets = Action { Ok(views.html.aboutTheModel.aboutMarkets(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutTunaGrowth = Action { Ok(views.html.aboutTheModel.aboutTunaGrowth(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutFishing = Action { Ok(views.html.aboutTheModel.aboutFishing(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutAgentBasedModeling = Action { Ok(views.html.aboutTheModel.aboutAgentBasedModeling(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutMindoroTunaFishery = Action { Ok(views.html.aboutTheModel.aboutMindoroTunaFishery(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutMunicipalFishersOptimistic = Action { Ok(views.html.aboutTheModel.aboutMunicipalFishers(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutMunicipalFishersPessimistic = Action { Ok(views.html.aboutTheModel.aboutMunicipalFishers(pessimisticMindoroModelConfigUI, pessimisticMindoroModelConfigUI.uiInputConfigs)) }
  def aboutCommercialFishers = Action { Ok(views.html.aboutTheModel.aboutCommercialFishers(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutMiddlemen = Action { Ok(views.html.aboutTheModel.aboutMiddlemen(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }
  def aboutExporters = Action { Ok(views.html.aboutTheModel.aboutExporters(mindoroModelConfigUI, mindoroModelConfigUI.uiInputConfigs)) }

  def defaultModel(scenario: Option[String]) = Action {
    val _uiInputConfig = scenario.getOrElse(UIInputConfigDefaults.mindoroString)
    val modelConfigUI = InitialValuesUIToModelConfigUI(UIInputConfigDefaults.initialValuesUI(_uiInputConfig))
    Ok(views.html.simulation.modelLanding(modelConfigUI, modelConfigUI.uiInputConfigs))
  }

  def customModel(config: Option[ModelConfigUI]) = Action {
    val modelOrDefault = config.getOrElse(InitialValuesUIToModelConfigUI(Mindoro))
    Ok(views.html.simulation.modelLanding(modelOrDefault, modelOrDefault.uiInputConfigs))
  }

  def runModel = Action { request =>
    request.body.asJson.map { configJson =>
      configJson.validate[ModelConfigUI]
        .map { modelConfigUI => Ok(Json.toJson(SimulationRunner(modelConfigUI))) }
        .recoverTotal { e => BadRequest("Detected error:" + JsError.toFlatJson(e)) }
    }.getOrElse(BadRequest("Expecting Json data"))
  }

  def runModel2 = Action { request =>
    request.body.asJson.map { configJson =>
      configJson.validate[ModelConfigUI]
        .map { modelConfigUI => Ok(Json.toJson(resultsIdCache.getOrRun(modelConfigUI))) }
        .recoverTotal { e => BadRequest("Detected error:" + JsError.toFlatJson(e)) }
    }.getOrElse(BadRequest("Expecting Json data"))
  }

  def data(runid: String, dataid: String) = Action { request =>
    resultsIdCache.getResultTableFor(runid, dataid).map(Json.toJson(_)) match {
      case Success(jsValue) => Ok(jsValue)
      case Failure(error) => BadRequest("No results for runid [" + runid + "] dataid [" + dataid + "] error was:\n" + ExceptionUtils.getStackTrace(error))
    }
  }

}

object SimulationRunner extends (ModelConfigUI => SimulationResultsUI) {
  val fisherySimulationCache = new FisherySimulationCache()

  def apply(conf: ModelConfigUI) = {
    val baselineResults = fisherySimulationCache.getOrRun(conf.baseline)
    val interventionResults = fisherySimulationCache.getOrRun(conf.interventions)
    val comparisonChartData = SimulationComparisonChartData.create(baselineResults, interventionResults, conf.resultAggregationSize)
    val animatedChartData = SimulationAnimatedChartData.create(baselineResults, interventionResults, conf.resultAggregationSize)
    val simulationOverviewData = SimulationOverviewData.create(baselineResults, interventionResults, conf.resultAggregationSize)
    val results = SimulationResultsUI(SimulationRunChartData.create(baselineResults, conf.resultAggregationSize), SimulationRunChartData.create(interventionResults, conf.resultAggregationSize), comparisonChartData, animatedChartData, simulationOverviewData)

    //    val chartNames = Seq(
    ////      model.SimulationComparisonChartData.perFisherSurplusComparisonChart.id,
    ////      model.SimulationComparisonChartData.fisherPopulationComparisonChart.id,
    //      model.SimulationComparisonChartData.biomassComparisonChart.id)
    //
    //    println("=============================");
    //    for(chartName <- chartNames) {
    //      val chartData = results.comparison.tableData(chartName)
    //      println(chartName)
    //      println(chartData.toCSV.replaceAll(",","\t"))
    //      println("-----------------------");
    //    }
    results
  }
}
