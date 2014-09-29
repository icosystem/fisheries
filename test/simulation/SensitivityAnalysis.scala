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

import java.io.File
import com.icosystem.collections._
import com.icosystem.io._
import com.icosystem.math._
import controllers.ModelConfigUI
import controllers.initial.InitialValuesUIToModelConfigUI
import controllers.initial.Mindoro
import play.api.libs.functional.syntax._
import play.api.libs.json._
import controllers.UIInputConfigs
import controllers.UIInputConfigs
import controllers.initial.UIInputConfigDefaults
import controllers.InputConfig
import controllers.ModelConfigUIInterventions
import controllers.initial.PowerfulMiddleman

object SensitivityAnalysis {

	val numYears = 40
  
	def main(args: Array[String]) {
		runBothBaselinesOneParamAllInterventions(Sweeps.fishStockHealth)
		runBothBaselinesOneParamAllInterventions(Sweeps.fishFishingBehavior)
		runBothBaselinesOneParamAllInterventions(Sweeps.municipalFisherPopulationGrowth)
		runBothBaselinesOneParamAllInterventions(Sweeps.municipalFisherRatioOfInitialPopulationNeverLeaving)
		runBothBaselinesOneParamAllInterventions(Sweeps.municipalFisherRatioOfMaximumEffortToAcceptableEffort)
		runBothBaselinesOneParamAllInterventions(Sweeps.municipalFisherRatioOfInitialEffortToAcceptableEffort)
		runBothBaselinesOneParamAllInterventions(Sweeps.commercialFisherGrowth)
		runBothBaselinesOneParamAllInterventions(Sweeps.consumerGrowth)
	}
  
	def runBothBaselinesOneParamAllInterventions(oneParamSweep : Seq[ParameterSweepValue[Double]]) = {
		runMindoroBaselineOneParamAllInterventions(oneParamSweep)
		runPowerfulMiddlemanBaselineOneParamAllInterventions(oneParamSweep)
	}
	
  def runMindoroBaselineOneParamAllInterventions(oneParamSweep : Seq[ParameterSweepValue[Double]]) = {
	val startingConfig = InitialValuesUIToModelConfigUI(Mindoro).copy(years = numYears)
    val interventionSweeps = Sweeps.Interventions.all(UIInputConfigDefaults.mindoro.v)
    val baselineName = "Mindoro"

    runOneParamAllInterventions(startingConfig, oneParamSweep, interventionSweeps, interventionSweeps, baselineName)
  }

  def runPowerfulMiddlemanBaselineOneParamAllInterventions(oneParamSweep: Seq[ParameterSweepValue[Double]]) = {
    val startingConfig = InitialValuesUIToModelConfigUI(PowerfulMiddleman).copy(years = numYears)
    val interventionSweeps = Sweeps.Interventions.all(UIInputConfigDefaults.powerfulMiddleman.v)
    val baselineName = "PowerfulMiddleman"

    runOneParamAllInterventions(startingConfig, oneParamSweep, interventionSweeps, interventionSweeps, baselineName)
  }
  
  def runOneParamAllInterventions(startingConfig : ModelConfigUI, oneParamSweep : Seq[Sweep], intervention1Sweeps : Seq[Sweep], intervention2Sweeps : Seq[Sweep], baselineName : String) = {
    val results = (for (param <- oneParamSweep; 
    intervention1 <- intervention1Sweeps if intervention1 != Sweeps.Interventions.noop; 
    intervention2 <- intervention2Sweeps if intervention1 != intervention2) yield {
      val config = intervention2(intervention1(param(startingConfig)))
      Summarize(config).map(param.value +: intervention1.name +: intervention1.value +: intervention2.name +: intervention2.value +: _)
    }).flatten

    val paramName = oneParamSweep.head.name
    val resultsString =
      (paramName, "Intervention 1", "Intervention 1 Value", "Intervention 2", "Intervention 2 Value", "Metric", "start of baseline", "end of baseline", "end of intervention").mkString("", ",", "\n") +
        results.map(_.mkString(",")).mkString("\n")

    new File("generated-" + numYears + "years/" + baselineName + "-" + paramName + "-allInterventions.txt") write resultsString
  }

	def allInterventions {
		val startingConfig = InitialValuesUIToModelConfigUI(Mindoro).copy(years = numYears)
        val interventionSweeps = Sweeps.Interventions.all(UIInputConfigDefaults.mindoro.v)
		
		val results = (for (fishGrowth <- Sweeps.fishGrowth; municipalFisherPopulationGrowth <- Sweeps.municipalFisherPopulationGrowth; intervention <- interventionSweeps) yield {
			val config = intervention(municipalFisherPopulationGrowth(fishGrowth(startingConfig)))
			Summarize(config).map(fishGrowth.value +: municipalFisherPopulationGrowth.value +: intervention.name +: intervention.value +: _)
		}).flatten

		val resultsString =
			("fishGrowth", "municipalFisherPopulationGrowth", "Intervention", "Intervention Value", "Metric", "start of baseline", "end of baseline", "end of intervention").mkString("", ",", "\n") +
				results.map(_.mkString(",")).mkString("\n")

		new File("generated-" + numYears + "years/BiomassAndPopulationAllInterventionsSweepOutput.txt") write resultsString
	}

}
