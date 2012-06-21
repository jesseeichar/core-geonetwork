package org.geonetwork.config

import scala.xml.transform.RewriteRule
import scala.xml._

object ConfigTransformer {
  private def transform(n: Node): Seq[Node] = n.label match {
    case "general" => generalDef(n)
    case "default" => defaultDef(n)
    case "resource" => resource(n)
    case _ => n.child flatMap transform
  }

  def transformResource(n: Node) = {
    val file = transformFile(<configFile name={(n \ "config" \ "url").text.drop(5).takeWhile(_ != ':')}>{n}</configFile>)
    val atts = file.attributes append new UnprefixedAttribute("enabled", n \ "@enabled" text, Null)
    file.copy(attributes = atts)
  }
  def transformFile(n: Node) = {
    val name = n.attribute("name").get.text
    <configFile name={ name }>
      <beans xmlns="http://www.springframework.org/schema/beans" 
    		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    		xmlns:context="http://www.springframework.org/schema/context"
    		xmlns:p="http://www.springframework.org/schema/p" 
    		xmlns:j="http://geonetwork-opensource.org/jeeves-spring-namespace http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://geonetwork-opensource.org/jeeves-spring-namespace http://geonetwork-opensource.org/jeeves-spring-namespace/jeeves-spring-namespace.xsd">
        { n.child flatMap ConfigTransformer.transform }
      </beans>
    </configFile>
  }
  private def generalDef(n: Node) = {
    val children = Map(n.child.map(c => c.label -> c.text): _*).withDefaultValue("")
    <bean id="jeevesGeneralConfig" class="jeeves.config.GeneralConfig" 
    		p:maxUploadSize={ children("maxUploadSize") } 
    		p:uploadDir={ children("uploadDir") } 
    		p:debug={ children("debug") }/>

  }
  private def defaultDef(n: Node) = {
    val children = Map(n.child.map(c => c.label -> c.text): _*).withDefaultValue("")
    <bean id="jeevesGeneralConfig" class="jeeves.config.GeneralConfig" 
    		p:service={ children("service") } 
    		p:startupErrorService={ children("startupErrorService") } 
    		p:localized={ children("localized") } 
    		p:contentType={ children("contentType") } 
    		p:language={ children("language") }>
      <property name="guiServices">
        <list>{
          val guiElems = n \ "gui" \ "_"
          guiElems map guiservice
        }</list>
      </property>
      <property name="errorPages">
        <list>{
          val errors = n \ "error" \ "_"
          errors map guiservice
        }</list>
      </property>
    </bean>
  }

  private def resource(n: Node) = {
    <bean id={(n \ "name").text.trim} class={(n \ "provider").text.trim}>
		<constructor-arg>
			<map>{
			  for(param <- n \ "config" \ "_") yield
				<entry key={param.label} value={param.text.trim}/>
			}</map>
		</constructor-arg>
	</bean>
  }
  private def guiservice(n: Node) = n match {
    case e if e.label == "call" =>
      <bean name={ e \\ "@name" text } serviceClass={ e \\ "@class" text } class="jeeves.server.dispatchers.guiservices.Call">
        { e \\ "params" }
      </bean>
    case e if e.label == "xml" =>
      <bean name={ e \\ "@name" text } file={ e \\ "@file" text } localized={ e \\ "@localized" text } language={ e \\ "@language" text } defaultLang={ e \\ "@defaultLang" text } base={ e \\ "@base" text } class="jeeves.server.dispatchers.guiservices.XmlFile"/>
  }
}

