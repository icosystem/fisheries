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

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import simulation.Timeline
import scala.collection.mutable.ArrayBuffer
import simulation.unit._

@RunWith(classOf[JUnitRunner])
class ConsumerSpec extends Specification {

  "Consumer population" should {

    "grow to 1000.83 in defaults" in {
      val consumer = TestConsumer()
      consumer.currentState = ConsumerState(People(1000d), Tonnes(36d))
      consumer.consumerMarket = TestMarket()
      consumer.consumerMarket.currentState = MarketState(MoneyPerTonnes(11666.67), null, PendingBuy(consumer, FinancialTransaction(MoneyPerTonnes(11666.67), Tonnes(36d), Tonnes(0))))

      consumer.tick
      consumer.updateState
      consumer.currentState.population.v must beCloseTo(1000 * Math.pow(1.01,1.0/12.0), 0.0001)
    }
  }
}

protected object TestConsumer {
  def apply(population: Double = 1000, growthRate: Double = 0.01)(implicit t: Timeline = Timeline(10, Time(1.0 / 12.0))) =
    Consumer(PerTime(growthRate), MoneyPerTonnes(11666.67), -1.0, TonnesPerTimePeople(36.0 / 1000.0))(t)
}
