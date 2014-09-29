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

package com.icosystem.cache

import java.util.concurrent.TimeUnit

import com.google.common.cache.{CacheBuilder, LoadingCache}
import com.google.common.collect.{BiMap, HashBiMap}
import com.icosystem.collections.ValueTable
import controllers.{ModelConfigUI, SimulationResultsUI, SimulationRunner}
import play.api.Logger
import scala.util._

class SimulationResultsCache {
  private var key = 0
  private val monitor: AnyRef = "I'm just a monitor"
  private val configCache: BiMap[ModelConfigUI, String] = HashBiMap.create()
  private val resultsCache: LoadingCache[String, SimulationResultsUI] = CacheBuilder.newBuilder()
    .maximumSize(100)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build((id: String) => {
    val config = configCache.inverse.get(id)

    val text = if(config != null){"modelConfigUI.hashCode = [" + config.hashCode + "]"} else {"config was null"}
    Logger.error("id = [" + id + "] " + text)

    if(config == null) throw new IllegalStateException("There was no config for id = [" + id + "]")

    SimulationRunner(config)
  })

  def getOrRun(modelConfigUI: ModelConfigUI): String = monitor.synchronized {
    val id: String = configId(modelConfigUI)
    resultsCache.getUnchecked(id)
    id
  }

  private def configId(modelConfigUI: ModelConfigUI): String =
      if (configCache.containsKey(modelConfigUI)) {
        configCache.get(modelConfigUI)
      } else {
        val newId = key.toString
        key = key + 1
        configCache.put(modelConfigUI, newId)
        newId
      }

  def getResultTableFor(runId: String, tableName: String) : Try[ValueTable] = monitor.synchronized {
    Try(resultsCache.get(runId)).flatMap(r =>
      ResultsLookup.toMapOfIdToValueTable(r).get(tableName) match {
        case Some(table) => Success(table)
        case None => Failure(new IllegalArgumentException("Could not find value with runId ["+r+"] and table name [" + tableName + "]"))
      })
  }

}

/*
 *  create a unique lookup ID for each chart
 *  TODO migrate existing model platform over to this new way
 */
object ResultsLookup {

  // TODO add animated, overview
  def toMapOfIdToValueTable(r: SimulationResultsUI): Map[String, ValueTable] = {
    (r.baseline.tableData.map(e => (SimulationResultsUI.baseline + "_" + e._1, e._2)).toSeq ++
      r.interventions.tableData.map(e => (SimulationResultsUI.interventions + "_" + e._1, e._2)).toSeq ++
      r.comparison.tableData.map(e => (SimulationResultsUI.comparison + "_" + e._1, e._2)).toSeq).toMap
  }
}