package org.geonetwork.config

import java.io.File
import org.junit.rules.TemporaryFolder
import org.junit.Rule
import org.junit.Test
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.After

class ConfigurationMigrationTest {

	private[this] var folder:File = _
	@Before
	def setup {
	  folder = File.createTempFile("migrationtest", "tmp")
	  folder.delete
	  folder.mkdirs
	}
	@After
	def after {
	  FileUtils.deleteDirectory(folder);
	}
	@Test
	def test {
		val sources = new File(classOf[ConfigurationMigrationTest].getResource("config.xml").getFile()).getParentFile();
		FileUtils.copyDirectory(sources, folder);
		
		new MigrateConfiguration().migrate(folder.getAbsolutePath, true)
		println(folder);
	}

}