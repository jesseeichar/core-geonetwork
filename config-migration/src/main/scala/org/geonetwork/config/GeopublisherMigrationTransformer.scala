package org.geonetwork.config

import xml._

case class GeopublisherMigrationTransformer(n: Node) {
  def transform = {
    <bean id="geopublisherConfiguration" class="org.fao.geonet.services.publisher.GeopublisherConfig">
      <property name="geoserverNodes">
		  <list>{ for(node <- n \\ "node") yield {
			  <bean class="org.fao.geonet.services.publisher.GeopublisherConfig$GeoserverNode" 
			    p:id={node \ "id" text} 
			    p:name={node \ "name" text}
			    p:namespacePrefix={node \ "namespacePrefix" text}
			    p:namespaceUrl={node \ "namespaceUrl" text}
			    p:adminUrl={node \ "adminUrl" text}
			    p:wmsUrl={node \ "wmsUrl" text}
			    p:wfsUrl={node \ "wfsUrl" text}
			    p:wcsUrl={node \ "wcsUrl" text}
			    p:stylerUrl={node \ "stylerUrl" text}
			    p:user={node \ "user" text}
			    p:password={node \ "password" text} />
            }
		  }</list>
	  </property>
    </bean>
  }
}