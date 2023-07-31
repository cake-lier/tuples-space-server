import Dependencies._
import java.time.{LocalDateTime, ZoneId}

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / excludeLintKeys := Set(idePackagePrefix)

ThisBuild / scalaVersion := "3.3.0"

ThisBuild / scalafixDependencies ++= Seq(
  "io.github.ghostbuster91.scalafix-unified" %% "unified" % "0.0.9",
  "net.pixiv" %% "scalafix-pixiv-rule" % "4.5.3"
)

ThisBuild / idePackagePrefix := Some("io.github.cakelier")

ThisBuild / autoAPIMappings := true

lazy val root = project
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(DockerPlugin)
  .settings(
    name := "tuples-space-server",
    scalacOptions ++= Seq(
      "-deprecation",
      "-Xfatal-warnings",
      "-no-indent"
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    libraryDependencies ++= Seq(
      scalactic,
      scalatest,
      circeCore,
      circeGeneric,
      circeParser,
      akka,
      akkaStream,
      akkaHttp,
      akkaStreamTestkit,
      akkaHttpTestkit,
      akkaTestkit,
      core
    ),
    wartremoverErrors ++= Warts.allBut(Wart.ImplicitParameter),
    version := "0.0.0",
    coverageMinimumStmtTotal := 80,
    coverageMinimumBranchTotal := 80,
    headerLicense := Some(HeaderLicense.MIT(
      LocalDateTime.now(ZoneId.of("UTC+1")).getYear.toString,
      "Matteo Castellucci"
    )),
    assembly / assemblyJarName := "main.jar",
    assembly / mainClass := Some("io.github.cakelier.tuples.space.server.main"),
    docker / dockerfile := NativeDockerfile(file("Dockerfile")),
    docker / imageNames := Seq(
      ImageName(
        namespace = Some("matteocastellucci3"),
        repository = name.value,
        tag = Some(version.value)
      ),
      ImageName(
        namespace = Some("matteocastellucci3"),
        repository = name.value
      ),
    )
  )
