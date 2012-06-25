package org.geonetwork.config


import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.junit.Test
import xml._

class MigrateConfigTest {

	private[this] var folder:File = _
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
		
		for{ f <- folder.listFiles().filter(_.isFile)
		  	 if f.getName != "config-gui.xml" } {
		  println("checking that "+f.getName()+" has data")
		  val configData = xml.XML.loadFile(f)
		  assertTrue("There should not be empty configuration files in "+f.getName, configData.child.collect{case e:xml.Elem => e}nonEmpty)
		  f.getName match {
		    case "config-csw.xml" => assertCsw(configData)
		    case "config-db.xml" => assertDb(configData)
		    case _ => ()
		  }
		}
	}
	
	def assertCsw(configData: Elem) {
	  assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetCapabilities") size)
	  assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetDomain") size)
	  assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$GetRecords") size)
	  assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig$DescribeRecord") size)
	  assertEquals(1, configData \ "bean" filter (_ att "class" equals "org.fao.geonet.kernel.csw.CswCatalogConfig") size)
	  val getRecords = (configData \ "bean" filter {n: Node => (n att "id") == "getRecords"})
	  assertFalse((getRecords \ "property" filter {n:Node => (n att "name") == "parameters"}) \ "_" \ "bean" isEmpty)
	  assertEquals(2, getRecords \\ "bean" filter {n: Node => (n att "class") == "org.fao.geonet.kernel.csw.CswCatalogConfig$TypeName"} size)
	  val outputFormats = getRecords \ "property" filter {n: Node => (n att "name") == "outputFormats"}
	  assertEquals(1, outputFormats.size)
	  assertEquals(1, outputFormats \ "set" \ "_" size)
	  val constraintLanguages = getRecords \ "property" filter {n: Node => (n att "name") == "constraintLanguages"}
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
	  migrate \ "_" \ "bean" foreach {n =>
	    val files = n \ "property" \ "_" \ "bean"
	    assertTrue("Expected 1 -> 2 files but got "+files.size+" in "+(n att "version"), files.size == 1 || files.size  == 2)
	  }
	}
}