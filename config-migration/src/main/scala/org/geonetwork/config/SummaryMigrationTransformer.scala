package org.geonetwork.config
import xml._

case class SummaryMigrationTransformer(n: Node) {
    def transform = {
      <bean id="summaryConfig" class="org.fao.geonet.kernel.search.SummaryConfig">
        <property name="configurations">
    	  <map>{ for (c <- n \ "def" \ "_") yield {
    	      <entry key={c.label}>
    	        <list>{ for{i <- c \ "item"} yield
    	          <bean class="org.fao.geonet.kernel.search.SummaryConfig$Item" 
                    p:name={i att "name"}
                    p:plural={i att "plural"}
                    p:indexKey={i att "indexKey"}
                    p:order={i att "order"}
                    p:type={i att "type"}
                    p:max={i att "max"}></bean>
    	        }</list>
    	      </entry>
    	  	}
    	  }</map>
    	</property>
    	<!-- In future typeConf will also be spring injection but for now this is sufficient -->
    	<property name="typeConf">
    	  <map>{
    		for( (key, nodes) <- (n \ "typeConfig" \ "_").groupBy(_.label) )  yield {
    		  <entry key={key}>
    		  	<list>{ 
    		  	  for (value <- nodes) yield <value>{value.text}</value>
    		  	}</list>
    		  </entry>
    		 }
    	  }</map>
    	</property>
      </bean>
    }
}