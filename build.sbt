/* Copyright (c) 2018 phData inc. */

import sbt._
val hadoop_home = System.getenv("HADOOP_HOME")

lazy val IntegrationTest = config("it") extend (Test)
lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .enablePlugins(OsDetectorPlugin)
  .settings(Defaults.itSettings: _*)
  .settings(
    name := "retirement-age",
    version := "0.1-SNAPSHOT",
    organization := "io.phdata",
    scalaVersion := scalaV,
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.0",
    resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
    libraryDependencies ++= sparkDependencies ++ otherDependencies ++
      Seq(
        "org.apache.kudu" % "kudu-binary" % kuduVersion % IntegrationTest classifier osDetectorClassifier.value),
    test in assembly := {},
    scalafmtOnCompile := true,
    scalafmtTestOnCompile := true,
    scalafmtVersion := "1.2.0",
    javaOptions += s"-Djava.library.path=$hadoop_home\\bin"
  )

val sparkVersion     = "2.2.0.cloudera1"
val scalaTestVersion = "3.0.4"
val kuduVersion      = "1.9.0"

parallelExecution in Test := false
val scalaV = "2.11.8"
val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided"
).map(
  _.excludeAll(
    ExclusionRule(organization = "org.glassfish.jersey.core", name = "jersey-client"),
    ExclusionRule(organization = "log4j"),
    ExclusionRule(organization = "org.slf4j", name = "slf4j-log4j12")
  ))
val otherDependencies = Seq(
  "org.rogach"                 %% "scallop"         % "3.1.1",
  "net.jcazevedo"              %% "moultingyaml"    % "0.4.0",
  "com.google.guava"           % "guava"            % "21.0",
  "com.typesafe.scala-logging" %% "scala-logging"   % "3.7.2",
  "org.scalatest"              %% "scalatest"       % scalaTestVersion % "test",
  "com.databricks"             %% "spark-avro"      % "4.0.0",
  "org.apache.kudu"            %% "kudu-spark2"     % kuduVersion,
  "org.apache.kudu"            % "kudu-test-utils"  % kuduVersion % "test",
  "org.slf4j"                  % "log4j-over-slf4j" % "1.7.26",
  "ch.qos.logback"             % "logback-classic"  % "1.2.3"
).map(
  _.excludeAll(ExclusionRule(organization = "com.fasterxml.jackson.core"),
               ExclusionRule(organization = "org.apache.logging.log4j"),
               ExclusionRule(organization = "log4j")))
