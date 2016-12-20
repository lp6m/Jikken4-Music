import AssemblyKeys._ /*jar作成用*/

name := "sbt-simple-java-project"

version := "0.0.1-SNAPSHOT"

organization := "org.littlewings"

libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % "3.5"
)

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.5.0"

scalacOptions ++= List("-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8")

scalaVersion := "2.11.7"

/* modena.cssがない*/
unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/ext/jfxrt.jar"))

assemblySettings
