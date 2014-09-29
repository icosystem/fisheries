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

import simulation.unit._

// excess = supply - demand

case class FinancialTransaction(val pricePerTon: MoneyPerTonnes, val tons: Tonnes, val excess:Tonnes) extends Sold with Bought {
  def value = pricePerTon * tons
  def supply = if (excess.v > 0) tons+excess else tons
  def demand = if (excess.v < 0) tons-excess else tons
}

trait Sold {
  def value: Money

  def tons: Tonnes

  def pricePerTon: MoneyPerTonnes
  
  def excess : Tonnes
  def supply: Tonnes
  def demand: Tonnes
  
}

trait Bought {
  def value: Money

  def tons: Tonnes

  def pricePerTon: MoneyPerTonnes
      
  def excess : Tonnes
  def supply: Tonnes
  def demand: Tonnes
}


object FinancialTransaction {
  val none = FinancialTransaction(MoneyPerTonnes(0), Tonnes(0), Tonnes(0))
  /**
   * Create a FinancialTransaction object by supplying price, supply & demand instead of price, volume traded and excess volume. 
   * It is less prone to confusion because we never know if excess is positive or negative when demand was larger than supply and vice versa
   */
  def transaction(price: MoneyPerTonnes, supply: Tonnes, demand: Tonnes): FinancialTransaction = {
    val tons: Tonnes = Tonnes(Math.min(supply.v,demand.v))
    FinancialTransaction(price,tons,supply - demand)
  }
 
}