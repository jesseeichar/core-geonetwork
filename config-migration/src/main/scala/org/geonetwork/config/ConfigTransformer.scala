package org.geonetwork.config

import scala.xml.transform.RewriteRule
import scala.xml._

object ConfigTransformer {
  
  private def transform(fileName: String, n: Node): Seq[Node] = n.label match {
    case "general" => generalDef(n)
    case "default" => defaultDef(n)
    case "appHandler" => appHandler(n)
    case "statistics" if fileName == "config-stats-params.xml" => n
    case "schemas" if fileName == "config-oai-prefixes.xml" => oaiConfig(n)
    case "summary" => SummaryMigrationTransformer(n).transform
    case "nodes" => GeopublisherMigrationTransformer(n).transform
    case "profiles" => UserProfilesMigrationTransformer(n).transform
    case "services" => n.child map(ServiceTransformer.service(n att "package"))
    case "operations" if n \ "operation" exists (_ att "name" equalsIgnoreCase "GetCapabilities") =>
      CswTransformer(n).transform
    case "db" =>  DbMigrationTransformer(n).transform
    case "config" if (n \ "index" nonEmpty) && (n \ "search" nonEmpty) =>  LuceneMigrationTransformer(n).transform
    case _ =>
     n.child flatMap {transform(fileName, _)}
  }

  def transformResource(n: Node) = {
    val file = transformFile(<configFile name={(n \ "config" \ "url").text.drop(5).takeWhile(_ != ':')}>{n}</configFile>, resource)
    val atts = file.attributes append new UnprefixedAttribute("enabled", n att "enabled", Null)
    file.copy(attributes = atts)
  }
  def transformFile(n: Node, transformer:(String,Node) => Seq[Node] = transform) = {
    val name = n att "name"
    <configFile name={ name }>
      <beans xmlns="http://www.springframework.org/schema/beans" 
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:context="http://www.springframework.org/schema/context"
            xmlns:p="http://www.springframework.org/schema/p" 
            xmlns:j="http://geonetwork-opensource.org/jeeves-spring-namespace http://www.springframework.org/schema/beans/spring-beans-3.0.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.0.xsd http://geonetwork-opensource.org/jeeves-spring-namespace http://geonetwork-opensource.org/jeeves-spring-namespace/jeeves-spring-namespace.xsd">
    	{constantElements(name,n)}
        { n.child flatMap {n => transformer(name, n)} }
      </beans>
    </configFile>
  }
  private def constantElements(fileName:String, n:Node) = fileName match {
    case "config.xml" => Seq(
    	  <bean id="schemaManager" class="org.fao.geonet.kernel.SchemaManager"/>,
    	  <bean id="initDbms" class="org.fao.geonet.InitializedDbms"/>,
    	  <bean id="settingsManager" class="org.fao.geonet.kernel.setting.SettingManager"/>
        )
    case _ => Nil
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

  private def resource(fileName:String, n: Node) = {
    <bean id={(n \ "name").text.trim} class={(n \ "provider").text.trim}>
        <constructor-arg>
            <map>{
              for(param <- n \ "config" \ "_") yield
                <entry key={param.label} value={param.text.trim}/>
            }</map>
        </constructor-arg>
    </bean>
  }
  var appHandlerId=0
  private def appHandler(n: Node) = {
    val params = (n \ "param" map {
      p =>
        (p att "name") -> (p att "value")
    }).toMap.withDefaultValue("")
    
    appHandlerId += 1
    val apphandlerId = "appHandler_"+appHandlerId
    <bean id={apphandlerId} class={n att "class"}/>
    <bean id={apphandlerId+"_Config"} class="org.fao.geonet.GeonetworkConfig"
        p:languageProfilesDir={params("languageProfilesDir")}
        p:licenseDir={params("licenseDir")}
        p:useSubversion={params("useSubversion")}
        p:statusActionsClassName={params("statusActionsClassName")}
        >
        <!-- Preferred schema parameter is used on import
          when a ZIP or MEF file is imported with more than one
          xml files. For example, export produce one file for
          iso19139 and on file for the ISO profil (eg. fra, che).
            
          Recommanded value is iso19139 if user is not 
          interested for having metadata in a specific
          ISO profil.
        -->
        <property name="preferredSchema" value={params("preferredSchema")}/>

        <!-- search statistics stuff -->
        <!-- true to log into DB WKT of spatial objects involved in a search operation
        CAUTION ! this can lead to HUGE database and CSV export if detailed geo objects are used:
        several Gb for instance...-->
        <property name="statLogSpatialObjects" value={params("statLogSpatialObjects")}/>
        <property name="statLogAsynch" value={params("statLogAsynch")}/>
        <!-- The list of Lucene term fields to exlucde from log, to avoid storing unecessary information -->
        <property name="statLuceneTermsExclude" value={params("statLuceneTermsExclude")}/>

        <!-- The maximum number of writes in a transaction on the spatial index 
                
         For the Apache Commons DBCP provider:
         When using PostGIS on machines with fast/large pipe to disk, set 
         this number to 1 which means that the auto_commit transaction is
         used. Also if having a long running transaction causes issues eg. 
         when database connections are reset by a firewall then set this
         number to 1 
         
         For the JNDI database provider:
         Set this number to something large eg. 1000  - setting this number 
         to 1 (=auto_commit) with a JNDI provider will cause database 
         connections involving the spatialindexwriter to be left open due 
         to what appears to be a bug in the GeoTools feature store when 
         using the auto_commit transaction. Curiously the
         JNDI provider with maxWritesInTransaction set to 1000 is actually
         faster to load and index records than ApacheDBCP provider
         with PostgisDataStoreFactory and maxWritesInTransaction set to 1 -->
        <property name="maxWritesInTransaction" value={params("maxWritesInTransaction")}/>
    </bean>
  }
  
  private def guiservice(n: Node) = n match {
    case e if e.label == "call" =>
      <bean name={ e att "name" } serviceClass={ e att "class"} class="jeeves.server.dispatchers.guiservices.Call">
        { val params = e \ "param" 
          if(params.nonEmpty) {
            <property name="param"><list>{
        	  params map {p =>
        	    <bean class="jeeves.server.dispatchers.Param" p:name={p att "name"} p:value={p att "value"}/>
        	  }
        	}</list></property>
          }
        }</bean>
    case e if e.label == "xml" =>
      <bean name={ e att "name"} file={ e att "file" } localized={ e att "localized" } language={ e att "language" } defaultLang={ e att "defaultLang" } base={ e att "base" } class="jeeves.server.dispatchers.guiservices.XmlFile"/>
  }

  def oaiConfig(n: Node) = {
    <bean id="oaiPmhDispatcher" class="org.fao.geonet.kernel.oaipmh.OaiPmhDispatcher">
	  <property name="metadataFormats">
	  	<list>{
	  		for(p <- n \\ "schema") yield 
	  		  <bean c:prefix={p att "prefix"} c:schema={p att "schemaLocation"} c:ns={p att "nsUrl"} class="org.fao.oaipmh.responses.MetadataFormat"/>
	  	}</list>
	  </property>
    </bean>

  }
  
}

