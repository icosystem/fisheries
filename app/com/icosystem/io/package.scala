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

package com.icosystem

import java.io._
import scala.io._
import com.google.common.io.Files

package object io {

  /**
   * http://stackoverflow.com/questions/6879427/scala-write-string-to-file-in-one-statement
   */
  implicit class RichFile(val file: File) extends AnyVal {

    def read = Source.fromFile(file)(Codec.UTF8).mkString

    def write(s: String) {
      Files.createParentDirs(file)
      val out = new PrintWriter(file, "UTF-8")
      try {
        out.print(s)
      } finally { out.close }
    }
  }

}