package org.geonetwork.config

import scala.xml.transform.RewriteRule
import scala.xml._

object CentralConfigTransformer extends RewriteRule {
  override def transform(n: Node): Seq[Node] = n.label match {
    case "general" => 
      val children = Map(n.child.map(c => c.label -> c.text):_*).withDefaultValue("")
      <bean id="jeevesGeneralConfig" class="jeeves.config.GeneralConfig"
          p:maxUploadSize={children("maxUploadSize")}
          p:uploadDir={children("uploadDir")}
          p:debug={children("debug")}/>
    case "default" => 
    val children = Map(n.child.map(c => c.label -> c.text):_*).withDefaultValue("")
    <bean id="jeevesGeneralConfig" class="jeeves.config.GeneralConfig"
    	p:service={children("service")}
    	p:startupErrorService={children("startupErrorService")}
    	p:localized={children("localized")}
    	p:contentType={children("contentType")}
    	p:language={children("language")}>
    	<property name="guiServices">
    		<list>{
    			val guiElems = n \ "gui" \\ "_"
    			  guiElems map {
    			  case e if e.label == "call" =>
    				  <bean class="jeeves.server.dispatchers.guiservices.Call"
    			  			name={e \\ "@name" text} serviceClass={e \\ "@class" text}>
    			  		{e \\ "params"}
    			  	  </bean>
    			  case e if e.label == "xml" =>
    			  	<bean class="jeeves.server.dispatchers.guiservices.XmlFile"/>
    	    	}
    		}</list>
    	</property>
    		<property name="errorPages">
    		<list>{
    			val errors = n \ "error"
    				errors map {
    					case e if e.label == "call" =>
    					<bean class="jeeves.server.dispatchers.guiservices.Call"
    					name={e \\ "@name" text} serviceClass={e \\ "@class" text}>
    					{e \\ "params"}
    					</bean>
    					case e if e.label == "xml" =>
    					<bean class="jeeves.server.dispatchers.guiservices.XmlFile"/>
    			}
    		}</list>
    		</property>
    </bean>
  }
}
object BeansConfigTransformer extends RewriteRule {
	override def transform(n: Node): Seq[Node] = n.label match {
	case "root" if n.child.forall(_.label == "file") => 
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:p="http://www.springframework.org/schema/p"
	xmlns:j="http://geonetwork-opensource.org/jeeves-spring-namespace"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans 
		http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
		http://www.springframework.org/schema/context
        http://www.springframework.org/schema/context/spring-context-3.0.xsd
        http://geonetwork-opensource.org/jeeves-spring-namespace
        http://geonetwork-opensource.org/jeeves-spring-namespace/jeeves-spring-namespace.xsd"> 
	{n.child}
</beans>
	}
}

object ServiceConfigTransformer extends RewriteRule {
	override def transform(n: Node): Seq[Node] = List(n)
}

