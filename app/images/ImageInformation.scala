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

package images

import java.net.URL

/**
 * https://wiki.creativecommons.org/Best_practices_for_attribution
 *
 * Title? "Creative Commons 10th Birthday Celebration San Francisco"
 * Author? "tvol" - linked to his profile page
 * Source? "Creative Commons 10th Birthday Celebration San Francisco" - linked to original Flickr page
 * License? "CC BY 2.0" - linked to license deed
 */
case class ImageInformation(asset: String, title: String, source: URL, author: String, authorSite: Option[URL], license: License) {
  def toTitle = "Image: \"" + title + "\" by " + author + " (" + license.name + ")"
}

trait License {
  val name: String
  val link: URL
}

object `CC BY 2.0` extends License {
  val name = "CC BY 2.0"
  val link = new URL("http://creativecommons.org/licenses/by/2.0/")
}

object `CC BY ND 2.0` extends License {
  val name = "CC BY ND 2.0"
  val link = new URL("https://creativecommons.org/licenses/by-nd/2.0/")
}

object `CC BY NC 2.0` extends License {
  val name = "CC BY NC 2.0"
  val link = new URL("https://creativecommons.org/licenses/by-nc/2.0/")
}

object `CC BY NC ND 2.0` extends License {
  val name = "CC BY NC ND 2.0"
  val link = new URL("https://creativecommons.org/licenses/by-nc-nd/2.0/")
}

object `CC BY NC SA 2.0` extends License {
  val name = "CC BY NC SA 2.0"
  val link = new URL("https://creativecommons.org/licenses/by-nc-sa/2.0/")
}

object `used with permission` extends License {
  val name = "used with permission"
  val link = new URL("http://#")
}

object `royalty free` extends License {
  val name = "royalty free"
  val link = new URL("http://http://www.freeimages.com/help/7_2")
}

object ImageInformation {

  def main(args: Array[String]) {
    for(i <- allImages) {
      println(i.toTitle)
    }
  }

}