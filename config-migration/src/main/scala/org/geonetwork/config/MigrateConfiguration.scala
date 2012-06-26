package org.geonetwork.config

import scala.xml.transform.RuleTransformer
import java.io.File
import org.apache.commons.io.FileUtils
import scala.xml._
import org.apache.commons.io.IOUtils
import scala.xml.transform.RewriteRule

class MigrateConfiguration {
  val EXTRA_MIGRATION_FILES = Set(
    "user-profiles.xml",
    "geoserver-nodes.xml")
  def migrate(configDir: String, throwException: Boolean) = {
    if (!new File(configDir, "config-jeeves.xml").exists()) {
      if (new File(configDir, "config.xml").exists()) {
        Log.info("since the file 'config-jeeves' does not exist I will attempt to migrate the configuration files to the spring dependency injection framework");
        val configJeeves = classOf[MigrateConfiguration].getResourceAsStream("config-jeeves.xml");
        try {
          val backupDir = new File(configDir, "config_backup");
          backupDir.mkdirs();

          Log.info("backing up previous configuration files to: " + backupDir);
          for (f <- Option(new File(configDir).listFiles()).flatten) {
            if (f.isFile() && !f.getName.startsWith("config-override") && f.getName().startsWith("config") || EXTRA_MIGRATION_FILES.contains(f.getName())) {
              FileUtils.copyFile(f, new File(backupDir, f.getName()));
              f.delete
            }
          }

          Log.info("Copying config-jeeves template");
          FileUtils.copyInputStreamToFile(configJeeves, new File(configDir, "config-jeeves.xml"));

          var migrationInput = List[Elem]()
          for (f <- Option(backupDir.listFiles()).flatten) {
            Log.info("Adding " + f + " to migration input");
            val data = XML.loadFile(f);

            migrationInput ::= <configFile name={ f.getName() }>{ data }</configFile>
          }
          
          def isConfigXml(n:Node) = n.attribute("name").exists(_.text == "config.xml")
          def importElem(n:Node) = <import resource={n.attribute("name").getOrElse(throw new Error("Unable to find name of resource "+n))}/>
          val migratedData = migrationInput flatMap (n => ConfigTransformer.transformFile(n))
          val resources = migrationInput flatMap (_ \ "resources" \ "resource") flatMap ConfigTransformer.transformResource

          val configXmlWithImports = migratedData map {
            case file if !isConfigXml(file) => file
            case configXml:Elem =>
              val fileImports = migratedData filterNot isConfigXml map importElem
              val resourceImports = Comment("Uncomment import to use resource") +: (resources map {
                case r if r.attribute("enabled").exists(_.text == "true") => 
                  importElem(r)
                case r =>
                  val text = importElem(r).toString
                  val v = Comment(text)
                  v
              })
              
              val imports = fileImports ++ resourceImports
              
              new RuleTransformer(new ConfigXmlTransformer(imports))(configXml)
          }
          
          val prunedData = (configXmlWithImports ++ resources) map (n => new RuleTransformer(PruneTransformer).apply(n)) 
          val pp = new scala.xml.PrettyPrinter(140, 2)
          prunedData foreach {
            n => 
              val xmlString = """<?xml version="1.0" encoding="UTF-8"?>"""+(pp formatNodes n.child)
              val filename = (n attribute "name").get.text
              FileUtils.write(new File(configDir, filename), xmlString, "UTF-8")
          }
        } catch {
          case e =>
            Log.error("Attempted to migrate configuration to spring dependency injection configuration and failed:", e);
            if (throwException) {
              throw new RuntimeException(e);
            }
        } finally {
          IOUtils.closeQuietly(configJeeves);
        }
      }
    }
  }
}

object PruneTransformer extends RewriteRule {
  override def transform(n: Node) = n match {
    case n:Elem => 
      n.copy(attributes = n.attributes.filter(att => att.value.text.trim != "")) :: Nil
    case n => n :: Nil
  }
}

class ConfigXmlTransformer(imports:Seq[Node]) extends RewriteRule {
	override def transform(n: Node) = n match {
	case n:Elem if n.label == "beans" => n.copy(child = imports ++ n.child) :: Nil
	case t:Text if t.text.trim == "" => Nil
	case n => n :: Nil
	}
}