package org.geonetwork.config

import xml._ 

case class CswTransformer(n: Node) {
    def transform = 
        Seq(<bean id="cswCatalogConfig" class="org.fao.geonet.kernel.csw.CswCatalogConfig"/>) ++
          getCapabilities ++
          getDomain ++
          getRecords ++
          describeRecord
 
    
     def getCapabilities = {
        n \ "operation" find (_ att "name" equalsIgnoreCase "GetCapabilities") map {
          op =>
            <bean id="getCapabilities" class="org.fao.geonet.kernel.csw.CswCatalogConfig$GetCapabilities">
                <!-- Defines the number of keywords displayed in capabilities, ordered by frequency -->
                <property name="numberOfKeywords" value={op \ "numberOfKeywords" text}/>
                <!-- Defines the number of records that will be processed to build the keyword frequency list  -->
                <property name="maxNumberOfRecordsForKeywords" value={op \ "maxNumberOfRecordsForKeywords" text}/>
            </bean>
      } 
    }
    def getDomain = {
        n \ "operation" find (_ att "name" equalsIgnoreCase "GetDomain") map {
          op =>
            <bean id="getDomain" class="org.fao.geonet.kernel.csw.CswCatalogConfig$GetDomain">
              <!-- Defines the number of records that will be processed for any propertyname  -->
              <property name="maxNumberOfRecordsForPropertyNames" value={op \ "maxNumberOfRecordsForPropertyNames" text}/>
            </bean>
        }
    }
    def getRecords = {
      n \ "operation" find (_ att "name" equalsIgnoreCase "GetRecords") map {
        op =>
    <bean id="getRecords" class="org.fao.geonet.kernel.csw.CswCatalogConfig$GetRecords">
        <property name="parameters">
            <list>{ for( p <- op \ "parameters" \ "parameter") yield {
                <bean class="org.fao.geonet.kernel.csw.CswCatalogConfig$GetRecordParameter"
                            p:name={p att "name"} p:field={p att "field"}  p:type={p att "type"} p:range={p att "range"}>
                    <property name="xpaths">
                      <list>{
                        for (x <- p \ "xpath") yield {
                          <bean p:schema={p att "schema"}  p:path={p att "path"} class="org.fao.geonet.kernel.csw.CswCatalogConfig$XPath"/>
                        }
                      }</list>
                    </property>
                </bean>
              }
            }</list>
        </property>
        {outputFormats(op)}
        {constraintLanguages(op)}
        {typenames(op)}
    </bean>
      }
    }
    def constraintLanguages(op: Node) = {
      <property name="constraintLanguages">
            <set>{
              for (x <- op \ "constraintLanguage" \ "value" ) yield {
                <value>{x text}</value>
              }
            }</set>
        </property>
    }
    def outputFormats(op: Node) = {
        <property name="outputFormats">
            <set>{
              for (x <- op \ "outputformat" \ "format" ) yield {
                <value>{x text}</value>
              }
            }</set>
        </property>

    }
    def typenames(op: Node) = {
         <property name="typeNames">
            <set>{
              for (t <- op \ "typenames" \ "typename" ) yield {
                <bean class="org.fao.geonet.kernel.csw.CswCatalogConfig$TypeName"
                            p:namespace={t att "namespace"} p:prefix={t att "prefix"}  p:name={t att "name"} p:schema={t att "schema"}/>
              }
            }</set>
        </property>
    }
    def describeRecord = {
      n \ "operation" find (_ att "name" equalsIgnoreCase "DescribeRecord") map {
        op =>
            <bean id="describeRecord" class="org.fao.geonet.kernel.csw.CswCatalogConfig$DescribeRecord">
                {typenames(op)}
                {outputFormats(op)}
            </bean>
      }
    }
}