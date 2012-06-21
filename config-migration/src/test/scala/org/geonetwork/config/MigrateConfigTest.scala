package org.geonetwork.config


import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.Assert._
import org.junit.After
import org.junit.Before
import org.junit.Test

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
		
		for( f <- folder.listFiles().filter(_.isFile)) {
		  println("checking that "+f.getName()+" has data")
		  val configData = xml.XML.loadFile(f)
		  assertTrue("There should not be empty configuration files", configData.child.collect{case e:xml.Elem => e}nonEmpty)
		}
	}

}