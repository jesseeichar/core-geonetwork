package org.geonetwork.config

import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.junit.Test
import xml._

class MigrateConfigTest {

  private[this] var folder: File = _
  @Before
  def setup {
    folder = File.createTempFile("migrationtest", "_tmp")
    folder.delete
    folder.mkdirs
  }
  @After
  def after {
    FileUtils.deleteDirectory(folder);
  }
  @Test
  def test {
    val sources = (new File(classOf[MigrateConfigTest].getResource("testdata/config.xml").getFile())).getParentFile();
    FileUtils.copyDirectory(sources, folder);

    new MigrateConfiguration().migrate(folder.getAbsolutePath, true)

    
    for {
      f <- folder.listFiles().filter(_.isFile)
      if f.getName != "config-gui.xml"
      if f.getName != "config-notifier.xml"
    } {
      println("checking that " + f.getName() + " has data")
      val configData = xml.XML.loadFile(f)
      try {
        assertNoDuplicateIds(configData, f)
        assertCallHaveClassName(configData, f)
        assertTrue("There should not be empty configuration files in " + f.getName, configData.child.collect { case e: xml.Elem => e }nonEmpty)
        configData \\ "service" filter (_.prefix == "j") foreach {s => assertTrue((s att "id").nonEmpty) }
        f.getName match {
          case "config-csw.xml" => assertCsw(configData)
          case "config-db.xml" => assertDb(configData)
          case "config-lucene.xml" => assertLucene(configData)
          case "config-summary.xml" => assertSummary(configData)
          case "geoserver-nodes.xml" => assertGeopublisher(configData)
          case "user-profiles.xml" => assertProfiles(configData)
          case "config.xml" => assertMainConfig(configData)
          case _ => ()
        }
      } catch {
        case e =>
          println(configData)
          throw e
      }
    }
  }
  def assertNoDuplicateIds (configData: Elem, f: File) {
    var ids = Set[String]()
    for (id <- configData \\ "@id"; if !(f.getName matches "config-.*?-db.xml")) {
      assertFalse(id.text + " is a duplicate id in " + f.getName, ids contains id.text)
      ids += id.text
    }
  }
  def assertCallHaveClassName(configData: Elem, f: File) {
    for(call <- configData \\ "call") {
      assertTrue(call att "serviceClass" nonEmpty)
    }
    for(bean <- configData \\ "bean"; if (bean att "class") == "jeeves.server.dispatchers.guiservices.Call") {
       assertTrue(bean att "serviceClass" nonEmpty)
       val prefix = bean.attributes.find(_.key == "serviceClass").get.prefixedKey
       assertTrue("The p prefix is missing from serviceClass on "+bean, bean.attributes.find(_.key == "serviceClass").get.prefixedKey == "p:serviceClass")
    }
  }
  def assertCsw(configData: Elem) {
    assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetCapabilities") size)
    assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetDomain") size)
    assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetRecords") size)
    assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$DescribeRecord") size)
    assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig") size)
    val getRecords = (configData \ "bean" filter { n: Node => (n att "id") == "getRecords" })
    assertFalse((getRecords \ "property" filter { n: Node => (n att "name") == "parameters" }) \ "_" \ "bean" isEmpty)
    assertEquals(2, getRecords \\ "bean" filter { n: Node => (n att "class") == "org.fao.geonet.kernel.csw.CswCatalogConfig$TypeName" } size)
    val outputFormats = getRecords \ "property" filter { n: Node => (n att "name") == "outputFormats" }
    assertEquals(1, outputFormats.size)
    assertEquals(1, outputFormats \ "set" \ "_" size)
    val constraintLanguages = getRecords \ "property" filter { n: Node => (n att "name") == "constraintLanguages" }
    assertEquals(1, constraintLanguages.size)
    assertEquals(2, constraintLanguages \ "set" \ "_" size)
  }
  def assertDb(configData: Elem) {
    assertEquals("WEB-INF/classes/setup/sql/", (configData \ "bean").head att "basedir")

    val create = configData \ "bean" \ "property" filter (_ att "name" equals "create")
    val data = configData \ "bean" \ "property" filter (_ att "name" equals "data")
    val migrate = configData \ "bean" \ "property" filter (_ att "name" equals "migrate")

    assertEquals(1, create size)
    assertEquals(1, data size)
    assertEquals(1, migrate size)
    assertEquals(1, create \ "_" \ "bean" size)
    assertEquals(14, data \ "_" \ "bean" size)
    assertEquals(9, migrate \ "_" \ "bean" size)
    assertEquals(9, migrate \ "_" \ "bean" size)
    migrate \ "_" \ "bean" foreach { n =>
      val files = n \ "property" \ "_" \ "bean"
      assertTrue("Expected 1 -> 2 files but got " + files.size + " in " + (n att "version"), files.size == 1 || files.size == 2)
    }
  }

  def assertLucene(configData: Elem) {
    val configBean = childrenByAtt(configData, 'bean, 'class, "org.fao.geonet.kernel.search.LuceneConfig")
    val indexBean = childrenByAtt(configData, 'bean, 'class, "org.fao.geonet.kernel.search.LuceneConfig$Index")
    val searchBean = childrenByAtt(configData, 'bean, 'class, "org.fao.geonet.kernel.search.LuceneConfig$Search")
    assertEquals(1, configBean size)
    assertEquals(1, indexBean size)
    assertEquals(1, searchBean size)

    val analyzerBean = childrenByAtt(configData, 'bean, 'id, "geonetworkAnalyzer")
    assertEquals(1, analyzerBean size)
    val defaultAnalyzer = childrenByAtt(configBean, 'property, 'name, "defaultAnalyzer")
    assertTrue(defaultAnalyzer.nonEmpty && defaultAnalyzer.exists(p => (p att "ref") == "geonetworkAnalyzer"))

    assertTrue(childrenByAtt(configBean, 'property, 'name, "fieldSpecificAnalyzer").nonEmpty)
    assertTrue(childrenByAtt(configBean, 'property, 'name, "fieldSpecificSearchAnalyzer").nonEmpty)
    assertTrue(childrenByAtt(configBean, 'property, 'name, "documentBoosting") \ "bean" nonEmpty)
    assertTrue(childrenByAtt(configBean, 'property, 'name, "fieldBoosting") \ "map" \ "entry" nonEmpty)
    assertTrue(childrenByAtt(configBean, 'property, 'name, "numericFields") \ "_" \ "bean" nonEmpty)
    assertTrue(childrenByAtt(configBean, 'property, 'name, "tokenizedFields") \ "_" \ "value" nonEmpty)

    def assertExistsAndHasValue(n: NodeSeq, tagName: Symbol, attName: Symbol, attValue: String) {
      val children = childrenByAtt(n, tagName, attName, attValue)
      assertTrue(children.nonEmpty)
      assertTrue(children \\ "@value" nonEmpty)
    }

    assertExistsAndHasValue(indexBean, 'property, 'name, "ramBufferSizeMB")
    assertExistsAndHasValue(indexBean, 'property, 'name, "mergeFactor")
    assertExistsAndHasValue(indexBean, 'property, 'name, "luceneVersion")

    assertExistsAndHasValue(searchBean, 'property, 'name, "trackDocScores")
    assertExistsAndHasValue(searchBean, 'property, 'name, "trackMaxScore")
    assertExistsAndHasValue(searchBean, 'property, 'name, "docsScoredInOrder")

    assertTrue(childrenByAtt(searchBean, 'property, 'name, "dumpFields").nonEmpty)
    assertTrue(childrenByAtt(searchBean, 'property, 'name, "dumpFields") \ "map" \ "entry" nonEmpty)

    val queryBoost = childrenByAtt(searchBean, 'property, 'name, "queryBoost")
    assertExistsAndHasValue(queryBoost \ "bean", 'property, 'name, "boostQueryClass")

    childrenByAtt(queryBoost \ "bean", 'property, 'name, "params") \\ "entry" foreach {
      e =>
        assertTrue("key attribute missing in: " + e, (e att "key").trim.nonEmpty)
        assertTrue("value attribute missing in: " + e, (e att "value").trim.nonEmpty)
    }
  }
  def assertSummary(n: Node) = {
    assertEquals(1, n \ "bean" size)
    val configurations = childrenByAtt(n \ "bean", 'property, 'name, "configurations")
    assertEquals(4, configurations \\ "entry" size)
    (configurations \\ "entry") foreach { n => assertTrue("expected " + (n att "key") + " to have some elements", n \\ "bean" nonEmpty) }
    (configurations \\ "entry" \\ "bean") foreach { n => assertTrue("expected " + (n) + " to have a name attribute", (n att "name").nonEmpty) }
  }
  def assertGeopublisher(n: Node) = {
    assertEquals(1, n \ "bean" size)
    assertEquals(1, n \ "bean" \ "property" \\ "bean" size)
    (n \ "bean" \ "property" \\ "bean") foreach { n => assertTrue("expected " + (n) + " to have a name attribute", (n att "name").nonEmpty) }
  }
  def assertProfiles(n: Node) = {
	  assertEquals(7, n \ "bean" size)
	  (n \ "bean" \ "property" \\ "value") foreach { n => assertTrue("expected " + (n) + " to have text", n.text.nonEmpty) }
  }
  def assertMainConfig(configData: Node) = {
    var importedFiles = Set[String]()
    for(f <- configData \ "import" \\ "@resource") {
      assertFalse(importedFiles contains f.text)
      importedFiles += f.text
    }
    assertTrue(childrenByAtt(configData, 'import, 'resource, "classpath:JZkitApplicationContext.xml").nonEmpty)
    assertTrue(childrenByAtt(configData, 'import, 'resource, "config-h2-db.xml").nonEmpty)
  }

  def childrenByAtt(e: NodeSeq, childName: Symbol, attName: Symbol, attValue: String) =
    e \ childName.name filter (n => (n att attName.name) == attValue)
}