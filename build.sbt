organization := "egon"

name := "etikettenMaker"

version := "0.2"

scalaVersion := "2.10.3"

//javaHome := Some(file("C:/Program Files/Java/jdk1.6.0_39"))

//lazy val scalaSwingForJava7 = RootProject(uri("git://github.com/scala/scala-swing.git#java7"))

//lazy val root = project.in( file(".") ).dependsOn(scalaSwingForJava7)

libraryDependencies ++= Seq(
    //scalaSwingForJava7,
	//"org.scala-lang" % "scala-swing" % "2.10.3" withSources,
	"org.apache.poi" % "poi" % "3.7" withSources,
	"com.lowagie" % "itext" % "2.1.7" withSources
)