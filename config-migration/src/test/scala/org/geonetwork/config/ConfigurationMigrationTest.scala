package org.geonetwork.config

import java.io.File

import org.apache.commons.io.FileUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

class ConfigurationMigrationTest {

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
		val sources = new File(classOf[ConfigurationMigrationTest].getResource("testdata/config.xml").getFile()).getParentFile();
		FileUtils.copyDirectory(sources, folder);
		
		new MigrateConfiguration().migrate(folder.getAbsolutePath, true)
	}

}