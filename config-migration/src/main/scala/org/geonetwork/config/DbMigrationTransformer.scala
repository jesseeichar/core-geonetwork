package org.geonetwork.config

import xml.Node

case class DbMigrationTransformer(n: Node) {
  val basedir = {
    n \\ "file" \\ "@path" map {_.text} reduce { (acc, next) =>
      val zipped = next.zip(acc)
      zipped.takeWhile(c => c._1 == c._2).map(_._1).mkString
    }
  }
  def transform = {
    <bean id="databaseMigrationConfig" class="org.fao.geonet.DatabaseSetupAndMigrationConfig" p:basedir={basedir}>
      <property name="create">
        <list>{
          n \ "create" \ "_" map file 
        }</list>
      </property>
      <property name="data">
        <list>{
          n \ "data" \ "_" map file 
        }</list>
      </property>
      <property name="migrate">
        <list>{
          n \ "migrate" \ "version" map { v =>
          <bean class="org.fao.geonet.DatabaseSetupAndMigrationConfig$Version" p:version={v att "id"}>
            <property name="file">
              <list>{
                v \ "_" map file 
              }</list>
            </property>
          </bean>
          }
        }
        </list>
      </property>
    </bean>
  }
  
  def file(f: Node) = {
    <bean p:path={f att "path" drop basedir.length} p:filePrefix={f att "filePrefix"} p:fileType={f.label} class="org.fao.geonet.DatabaseSetupAndMigrationConfig$DbConfigFile"/>
  } 
}