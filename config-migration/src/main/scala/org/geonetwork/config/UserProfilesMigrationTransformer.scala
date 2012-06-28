package org.geonetwork.config

import xml._

case class UserProfilesMigrationTransformer(n: Node) {
	def transform = for (p <- n \ "profile" ) yield {
	  	<bean class="jeeves.server.Profile" id={p att "name"}  p:extends={p att "extends"}>
		<property name="access">
			<list>{ 
			  for (a <- p \ "allow") yield <value>{a att "service"}</value> 
			}</list>
		</property>
	</bean>

	}
}