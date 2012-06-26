package org.geonetwork.config

import xml._

case class LuceneMigrationTransformer(n: Node) {
  def transform:NodeSeq = Seq(
      luceneConfigBean,
      geonetworkAnalyzer,
      indexBean,
      <!-- Search parameters are applied at search time and does not need an index rebuild in order to be take into account. -->,
      searchBean
  )
  
  def luceneConfigBean = {
    <bean id="luceneConfig" class="org.fao.geonet.kernel.search.LuceneConfig">
      <!-- Default analyzer to use for all fields not defined in the fieldSpecificAnalyzer section. -->
      <!-- If not set, GeoNetwork use a default per field analyzer (ie. fieldSpecificAnalyzer is not -->
      <!-- take into account). The default analyzer is defined in SearchManager. -->
      <!-- -->
      <!-- The bean that is used must extend org.apache.lucene.analysis.Analyzer -->
	  <property name="defaultAnalyzer" ref="geonetworkAnalyzer"/>

	  <!-- Field analyzer -->
	  <!-- Define here specific analyzer for each fields stored in the index -->
	  <!--  -->
	  <!-- For example adding a different analyzer for any (ie. full text search)  -->
	  <!-- could be better than a standard analyzer which has a particular way of --> 
	  <!-- creating tokens. -->
	  <!--  -->
	  <!-- In that situation, when field is "mission AD-T" is tokenized to "mission" "ad" & "t" -->
	  <!-- using StandardAnalyzer. A WhiteSpaceTokenizer tokenized to "mission" "AD-T" -->
	  <!-- which could be better in some situation. But when field is "mission AD-34T" is tokenized --> 
	  <!-- to "mission" "ad-34t" using StandardAnalyzer due to number. -->
	  <!--  -->
	  <!-- doeleman: UUID must be case insensitive, as its parts are hexadecimal numbers which -->
	  <!-- are not case sensitive. StandardAnalyzer is recommended for UUIDS. -->
	  <!--  -->
	  <!-- A list of analyzer is available http://lucene.apache.org/java/2_4_0/api/org/apache/lucene/analysis/Analyzer.html -->
	  <!-- Commons analyzer: -->
	  <!-- * org.apache.lucene.analysis.standard.StandardAnalyzer -->
	  <!-- * org.apache.lucene.analysis.WhitespaceAnalyzer -->
	  <!-- * org.apache.lucene.analysis.SimpleAnalyzer -->
	  <!-- * org.fao.geonet.kernel.search.GeoNetworkAnalyzer (recommended for wildcard query support) -->
	  <!--  -->
	  <!-- The analyzer must be in the classpath. -->
	  {fieldAnalyzer("fieldSpecificAnalyzer")}
	  <!-- Define here analyzers to be overriden from fieldSpecificAnalyzer. -->
      <!-- By default, indexing and searching per field analyzers are the same. -->
      <!--  -->
      <!-- In some case, an analyzer could be applied at indexing and another  -->
      <!-- at searching time. For example when using a SynonymAnalyzer. When adding  -->
      <!-- Londres, Londinium, Londona for London at indexing time, then there is no -->
      <!-- need to add those synonyms for search if users search for Londres, it will match. -->
      <!-- We only need synonym expansion during indexing or during searching, not both. -->
	  {fieldAnalyzer("fieldSpecificSearchAnalyzer")}
	  <!-- Document boosting configuration.  -->
	  <!--  -->
	  <!-- Document boosting allows to define custom boost factor for a document -->
	  <!-- or for document fields at index time which  -->
	  <!-- will be use for score computation at search time. -->
	  <!--  -->
	  <!-- Note: Do not abuse document boost  -->
	  <!-- because it may lead to promote too much some kind of documents  -->
	  <!-- or the contrary and make end-user not so confident of your search engine! -->
	  {fieldBoosting}
	  <!-- Boosting factor for document based on document values. -->
	  <!-- "The boost factor values you should use depend on what youÕre trying to achieve; --> 
	  <!-- youÕll need to do some experimentation and -->
	  <!-- tuning to achieve the desired effect." source: Lucene In Action -->
	  {(n \ "boostDocument").headOption map {documentBoost} getOrElse Nil}
      <!-- All Lucene fields that are tokenized must be kept here because it -->
      <!-- is impossible unfortunately from Lucene API to work out which fields are -->
      <!-- tokenized and which aren't unless we read documents and we may not have -->
      <!-- an index to do this on so since most fields are not tokenized we --> 
      <!-- keep a list of tokenized fields here -->
	  <property name="tokenizedFields">
	  	<set>{ 
	  	  for (f <- n \ "tokenized" \ "Field" ) yield <value>{f att "name"}</value>
	  	}</set>
	  </property>
      <!-- All Lucene numeric fields. -->
      <!-- Use of numeric field will increase index size. -->
      <!-- It could give better search results for numeric values. -->
      <!--  -->
      <!-- Type attribute : int (default), double, long, float -->
      <!-- Precision attribute : see NumericUtils.PRECISION_STEP_DEFAULT -->
	  <property name="numericFields">
	  	<set>{ 
	  	  for (f <- n \ "numeric" \ "Field" ) yield 
	  	  	  <bean class="org.fao.geonet.kernel.search.LuceneConfig.tokenizedFields$NumericField"
	  	  		p:name={f att "name"} p:type={f att "type"} p:precisionStep={f att "precisionStep"} />
	  	}</set>
	  </property>

	</bean>
  }
  def geonetworkAnalyzer = if(n \ "defaultAnalyzer" nonEmpty) {
    bean("geonetworkAnalyzer", n \ "defaultAnalyzer")(_.head att "name")
  } else {
    <bean id="geonetworkAnalyzer" class="org.fao.geonet.kernel.search.GeoNetworkAnalyzer" />
  }
  def bean(id:String, n:NodeSeq)(clsFn: NodeSeq => String) = {
    <bean id={id} class={clsFn(n)}>{
      if (n \ "Param" nonEmpty) {
        (n \ "Param" filterNot {_ att "value" nonEmpty}) map {
          p =>
            <constructor-args>{p att "value"}</constructor-args>
        }
      }
    }</bean>
    
  }
  def fieldAnalyzer(name: String) = {
  	  <property name={name}>
		<map>{ n \ name \ "Field" map {  field =>
			<entry key={field att "name"}>
			  {bean("",field)(_.head att "analyzer")}
			</entry>
		  }
		}</map>
	  </property>
  }
  def fieldBoosting = {
	<property name="fieldBoosting">
	  <map>{ n \ "fieldBoosting" \ "Field" map { field =>
		<entry key={field att "name"}>
		  {bean("",field)(_.head att "boost")}
		</entry>
        }
	  }</map>
	</property>
  }
  def documentBoost(n: Node) = {
    <property name="documentBoosting">
	  {bean("documentBoosting", n)(_.head att "name")}
	</property>
  }
  def indexBean = {
    val index = n \ "index"
    <bean id="luceneIndexConfig" class="org.fao.geonet.kernel.search.LuceneConfig$Index">
        <!-- The amount of memory to be used for buffering documents in memory. -->
        <!-- 48MB seems to be plenty for running at least two long -->
        <!-- indexing jobs (eg. importing 20,000 records) and keeping disk --> 
        <!-- activity for lucene index writing to a minimum. -->
		<property name="ramBufferSizeMB" value={index \ "RAMBufferSizeMB" text}/>
        <!-- Determines how often segment indices are merged by addDocument(). -->
		<property name="mergeFactor" value={index \ "MergeFactor" text}/>
		<!-- Default Lucene version to use (mainly for Analyzer creation). -->
		<property name="luceneVersion" value={index \ "luceneVersion" text}/>
	</bean>
  }
  
  def searchBean = {
    val search = n \ "search"
    <bean id="luceneSearchConfig" class="org.fao.geonet.kernel.search.LuceneConfig$Search">
	    <!-- Score parameters. Turning these parameters to true, affects performance. -->
        <!-- Set track doc score to true if score needs to be displayed in results using geonet:info/score element -->
	    <property name="trackDocScores" value={search \ "trackDocScores" text}/>
	    <property name="trackMaxScore" value={search \ "trackMaxScore" text}/>
	    <!-- Not used because no Scorer defined -->
	    <property name="docsScoredInOrder" value={search \ "docsScoredInOrder" text}/>
	    {boostQuery(search)}
	    {dumpFields(search)}
	    
	</bean>
  }
  def boostQuery(search: NodeSeq) = {
	(search \ "boostQuery").flatMap{
	  boost =>
	    Seq(
          <!-- By default Lucene compute score according to search criteria -->,
          <!-- and the corresponding result set and their index content. -->,
          <!--In case of search with no criteria, Lucene will return top docs -->,
          <!--in index order (because none are more relevant than others). -->,
          <!-- -->,
          <!--In order to change the score computation, a boost function could -->,
          <!--be define. Boosting query needs to be loaded in classpath. -->,
          <!--RecencyBoostingQuery will promote recently modified documents -->,
	      <property name="queryBoost">
             <bean class="org.fao.geonet.kernel.search.BackwardsCompatibleQueryBoostFactory">
          	   <property name="boostQueryClass" value={boost att "name"}/>
        	   <property name="params">
          	    <map>{
          		  for(p <- boost \ "Param") yield 
          		  	<entry key={p att "type"} value={p att "value"}/>
                }</map>
              </property>
            </bean>
          </property>
        )
	}
   }
  def dumpFields(search: NodeSeq) = {
    <property name="dumpFields">
		<map>{
			(search \ "dumpFields" \ "field").map {
				field =>
				  <entry key={field att "name"} value={field att "tagName"} />
			}
		}</map>
	</property>
  }
}