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

import com.google.common.cache.LoadingCache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.collect.{ HashBiMap, BiMap }
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import model.ModelConfig
import simulation.SimulationResults
import simulation.FisherySimulation

/*
 * Cache a result for a given ModelConfig
 * so we don't re-run simulations if all we have
 * changed is aggregation parameters
 */
class FisherySimulationCache {
  val resultsCache: LoadingCache[ModelConfig, SimulationResults] = CacheBuilder.newBuilder()
    .maximumSize(25)
    .expireAfterWrite(10, TimeUnit.MINUTES)
    .build((c: ModelConfig) => FisherySimulation(c))

  def getOrRun(c: ModelConfig) = {
    resultsCache.getUnchecked(c)
  }
}

