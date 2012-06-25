package org.geonetwork

package object config {
  implicit def addAtt(n: xml.Node) = new {
    def att(name: String) =
      n.attributes.find(_.key == name).map(_.value.text).getOrElse("")
  }
}