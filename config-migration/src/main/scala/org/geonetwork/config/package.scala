package org.geonetwork

package object config {
  implicit def addAtt(n: xml.Node) = new {
    def att(name: String) =
      (n attribute name).map(_.text).getOrElse("")
  }
}