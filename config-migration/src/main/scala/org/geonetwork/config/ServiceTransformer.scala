package org.geonetwork.config

import xml._

object ServiceTransformer {
  def service(basePackage:String)(n:Node) = {
    
    <j:service id={n att "name"} 
        match={n att "match"}
        sheet={n att "sheet"}
        cache={n att "cache"}
        package={basePackage}>
        {addServiceClass(n)}
        {addOutputPage(n)}
        {addErrorPage(n)}
    
  </j:service>
  }

  def addServiceClass(n:Node) = {
    (n \ "class") map { serviceClass =>
      <serviceClass name={serviceClass att "name"}>
        {serviceClass \ "param"}
    </serviceClass>
    }
  }

  def addOutputPage(n: Node) = {
    (n \ "output") map { output =>
      <output sheet={output att "sheet"}
        testCondition={output att "testCondition"}
        contentType={output att "contentType"}
        forward={output att "forward"}
        file={output att "file"}
        blob={output att "blob"}>{
        for (c <- output \ "_") yield guiservice(c)
      }</output>
    }
  }
    
    def addErrorPage(n: Node) = {
      (n \ "error") map { error => 
        <error sheet="error-transform.xsl"
            testCondition="correctError"
            contentType="text"
            statusCode="500">{
          for (c <- error \ "_") yield guiservice(c)
        }</error>
      }
  }
    
  private def guiservice(n: Node) = n match {
    case e if e.label == "call" =>
      <call name={ e att "name" } serviceClass={ e att "class"}>
      { e \\ "param" }
      </call>
    case e if e.label == "xml" =>
      <xml name={ e att "name"} file={ e att "file" } localized={ e att "localized" } language={ e att "language" } defaultLang={ e att "defaultLang" } base={ e att "base" } class="jeeves.server.dispatchers.guiservices.XmlFile"/>
  }

}