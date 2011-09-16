import sbt._

class EtikketenMakerProject(info: ProjectInfo) extends DefaultProject(info) {
  //val bindingDep = "com.jgoodies" % "binding" % "2.0.6"
  val scalaSwingDep = "org.scala-lang" % "scala-swing" % "2.8.1" withSources
  val poiDep = "org.apache.poi" % "poi" % "3.7" withSources
  val iTextDep = "com.lowagie" % "itext" % "2.1.7" withSources
}