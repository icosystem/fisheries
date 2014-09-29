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

import java.net.URL

package object images {
  val walkingFishers = ImageInformation("images/tiles/3874534318_eea18e89b2_z-270x157.jpeg", "Fishermen in Suqui", new URL("https://www.flickr.com/photos/barsvd/3874534318"), "Bar Fabella", Some(new URL("https://www.flickr.com/photos/barsvd/")), `CC BY NC SA 2.0`)

  val boatAndSunset = ImageInformation("images/tiles/4482119522_7fd8f20c33_z-270x157.jpeg", "Bulalacao, Or. Mindoro, Philippines", new URL("https://www.flickr.com/photos/barsvd/4482119522"), "Bar Fabella", Some(new URL("https://www.flickr.com/photos/barsvd/")), `CC BY NC SA 2.0`)

  val floatingFishMarket = ImageInformation("images/tiles/8560562494_f3c8156564_z-270x157.jpeg", "floating fish market", new URL("https://www.flickr.com/photos/detroitstylz/8560562494"), "detroitstylz", Some(new URL("https://www.flickr.com/photos/detroitstylz/")), `CC BY NC 2.0`)

  val tunaCannery = ImageInformation("images/tiles/8600603284_20e3360811_z-250-157.jpeg", "IMG_0941", new URL("https://www.flickr.com/photos/penmanila/8600603284"), "Butch Dalisay", Some(new URL("https://www.flickr.com/photos/penmanila/")), `CC BY NC ND 2.0`)

  val wetMarket = ImageInformation("images/tiles/5711797111_c53480e05f_z-270-157.jpeg", "Market then eat", new URL("https://www.flickr.com/photos/tzf093/5711797111"), "Mr. Leeds", Some(new URL("https://www.flickr.com/photos/tzf093/")), `CC BY ND 2.0`)

  val sushi = ImageInformation("images/tiles/sushi-270x157.jpeg", "Sushi", new URL("http://www.freeimages.com/photo/1379833"), "miholz", Some(new URL("http://www.freeimages.com/profile/miholz")), `royalty free`)

  val fish = ImageInformation("images/tiles/fish-middleman-270x157.jpeg", "Fish", new URL("http://www.freeimages.com/photo/1129343"), "Feikje Meeuwsen", Some(new URL("http://www.freeimages.com/profile/feikje")), `royalty free`)

  val fisherBoat = ImageInformation("images/tiles/fisher-boat-270x157.jpeg", "Fisher Boat", new URL("http://www.freeimages.com/photo/1111107"), "Jan Willem Geertsma", Some(new URL("http://www.freeimages.com/profile/jan-willem")), `royalty free`)

  val standOut = ImageInformation("images/tiles/stand-out-270x157.jpeg", "Stand Out", new URL("http://www.freeimages.com/photo/889735"), "sachyn", None, `royalty free`)

  val boatFishersAndTuna = ImageInformation("images/tiles/MelvinSamson-BoatFishersAndTuna-270x157.jpeg", "Boat, Fishers and Tuna", new URL("http://#"), "Melvin Santos", None, `used with permission`)

  val paddyField = ImageInformation("images/tiles/paddy-field-1-270x157.jpeg", "Paddy Field 1", new URL("http://www.freeimages.com/photo/764057"), "Rita Juliana", Some(new URL("http://www.freeimages.com/profile/rjuliana")), `royalty free`)

  val fish3 = ImageInformation("images/tiles/fish-3-270x157.jpeg", "fish 3", new URL("http://www.freeimages.com/photo/593375"), "elvis santana", Some(new URL("http://www.freeimages.com/profile/tome213")), `royalty free`)

  val putridFishing = ImageInformation("images/tiles/putrid-fishing-270x157.jpeg", "Putrid Fishing", new URL("http://www.freeimages.com/photo/1302939"), "dimitri_c", Some(new URL("http://www.freeimages.com/profile/dimitri_c")), `royalty free`)

  val deadFish = ImageInformation("images/tiles/dead-fish-270x157.jpeg", "Dead Fish", new URL("http://www.freeimages.com/photo/1054046"), "Raquel Teixeira", Some(new URL("http://www.freeimages.com/profile/raqueltex")), `royalty free`)

  val sardines = ImageInformation("images/tiles/sardines-270x157.jpeg", "sardines", new URL("http://www.freeimages.com/photo/443955"), "Aron Kremer", Some(new URL("http://www.freeimages.com/profile/akphotos")), `royalty free`)

  val allImages = List(walkingFishers, boatAndSunset, floatingFishMarket, tunaCannery, wetMarket, sushi, fish, fisherBoat, standOut, boatFishersAndTuna, paddyField, fish3, putridFishing, deadFish, sardines)
}
