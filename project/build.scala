/*
 * Copyright (c) 2013  Bridgewater Associates, LP
 *
 * Distributed under the terms of the Modified BSD License.  The full license is in
 * the file COPYING, distributed as part of this software.
 */
import org.apache.ivy.core.module.id.ModuleRevisionId
import sbt._
import Keys._
import org.apache.ivy.core.install.InstallOptions
import com.untyped.sbtjs.Plugin._
import scala.Some

object NotebookBuild extends Build {

  implicit def toRichProject(project: Project) = new RichProject(project)
  import Dependencies._

  override def settings = super.settings ++ Seq(
    organization := "com.bwater",
    version := "0.3.0-SNAPSHOT",
    
    scalaVersion in ThisBuild := "2.10.0",

    fork in Test in ThisBuild := true,
    parallelExecution in Test in ThisBuild := false,
    // these java options are for the forked test JVMs
    javaOptions in ThisBuild ++= Seq("-Xmx256M", "-XX:MaxPermSize=128M"),

    resolvers in ThisBuild ++= Seq(
      Resolver.typesafeRepo("releases"),
      Resolver.typesafeIvyRepo("releases"),
      Resolver.typesafeIvyRepo("snapshots")
  ),

    compileOrder := CompileOrder.Mixed,
    publishMavenStyle := false,
    javacOptions ++= Seq("-Xlint:deprecation", "-g"),
    scalacOptions += "-deprecation",
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v") //Suppress test output unless there is a failure
  )

  lazy val root = Project(id = "scala-notebook", base = file("."))
    .aggregate(subprocess, observable, common, kernel, server)
    .settings(
      publish := {}, // don't publish an empty jar for the root project
      publishLocal := {}
    )

  lazy val subprocess = Project(id = "subprocess", base = file("subprocess"))
    .projectDefaults
    .settings(
      libraryDependencies ++= Seq(
        akkaRemote,
        akkaSlf4j,
        akkaTestkit,
        slf4jLog4j,
        scalaTest,
        apacheExec,
        commonsIO
      )
    )
	
  lazy val observable = Project(id = "observable", base = file("observable"))
    .dependsOn(subprocess)
    .projectDefaults
    .withWebAssets
    .settings(
      libraryDependencies ++= Seq(
        akkaRemote,
        akkaSlf4j,
        akkaTestkit,
        slf4jLog4j,
        unfilteredFilter,
        unfilteredWebsockets,
        unfilteredJson,
        scalaTest,
        rxCore,
        rxScala
      )
    )
	
  lazy val common = Project(id = "common", base = file("common"))
    .dependsOn(observable)
    .projectDefaults
    .settings(
      name := "notebook-common",

      libraryDependencies ++= Seq(
        akka,
        unfilteredJson,
        scalaTest,
        log4jPlus,
        scalaZCore
      )
    )
  
  lazy val kernel = Project(id = "kernel", base = file("kernel"))
    .dependsOn(common, subprocess, observable)
    .projectDefaults
    .settings(
      name := "notebook-kernel",

      libraryDependencies ++= Seq(
        akkaRemote,
        akkaSlf4j,
        akkaTestkit,
        slf4jLog4j,
        commonsIO,
        scalaTest
      ),

      libraryDependencies <++= (scalaVersion in ThisBuild) { scalaVersion => Seq(
        "org.scala-lang" % "jline" % scalaVersion,
        "org.scala-lang" % "scala-compiler" % scalaVersion
      )}
    )

  lazy val server = Project(id = "server", base = file("server"))
    .dependsOn(common, kernel)
    .projectDefaults
    .withWebAssets
    .settings(
      name := "notebook-server",

      mainClass in (Compile, run) := Some("com.bwater.notebook.Server"),

      libraryDependencies ++= Seq(
        akkaRemote,
        akkaSlf4j,
        slf4jLog4j,
        unfilteredFilter,
        unfilteredWebsockets,
        akkaTestkit,
        unfilteredJson,
        commonsIO,
        scalaTest,
        scalate,
        apacheHttpClient,
        scalaMock
      )
    )

  object Dependencies {

    val scalate = "org.fusesource.scalate" %% "scalate-core" % "1.6.1"

    val unfilteredVersion = "0.6.5"
    val unfilteredFilter = "net.databinder" %% "unfiltered-filter" % unfilteredVersion
    val unfilteredWebsockets = "net.databinder" %% "unfiltered-netty-websockets" % unfilteredVersion
    val unfilteredJson = "net.databinder" %% "unfiltered-json" % unfilteredVersion

    val akkaVersion = "2.1.1"
    val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
    val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

    val rxVersion = "0.5.3"
    val rxCore = "com.netflix.rxjava" % "rxjava-core" % rxVersion
    val rxScala = "com.netflix.rxjava" % "rxjava-scala" % rxVersion

    val apacheExec = "org.apache.commons" % "commons-exec" % "1.1"
    val apacheHttpClient = "org.apache.httpcomponents" % "httpclient" % "4.2.3"

    val scalaZCore = "org.scalaz" %% "scalaz-core" % "7.0.0"
        
    val commonsIO = "commons-io" % "commons-io" % "1.4"
    val log4jPlus = "log4j" % "log4j" % "1.2.+"
    val slf4jLog4j = "org.slf4j" % "slf4j-log4j12" % "1.6.4"

    val scalaTest = "org.scalatest" %% "scalatest" % "1.9.1" % "test"
    val scalaMock = "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"
  }

  
  class RichProject(project: Project)  {
    def projectDefaults = project.settings(
      resourceDirectories in Test <++= resourceDirectories in Compile
    )

    def withWebAssets = {
      project.settings(jsSettings : _*)
        .settings(
          (sourceDirectory in (Compile, JsKeys.js)) <<= (sourceDirectory in Compile)(_ / "assets"),
          (resourceGenerators in Compile) <+= (JsKeys.js in Compile),
          // Disable minification 
          // TODO: make this conditional.  Ideally have minification off when running from SBT, on when packaging/publishing.
          // Might also be useful to publish debug binaries, maybe in an alternate config/classifier?
          (JsKeys.variableRenamingPolicy in (Compile, JsKeys.js)) := VariableRenamingPolicy.OFF,
          (JsKeys.prettyPrint in (Compile, JsKeys.js)) := true,

          /* Copy all non-compiled assets */
          unmanagedResourceDirectories in Compile <+= (sourceDirectory in Compile) (_ / "assets"),
          excludeFilter in (Compile, unmanagedResources) ~= (filter => filter || "*.js" || "*.coffee" || "*.jsm")
      )
    }
  }
}
