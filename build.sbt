import com.lightbend.lagom.core.LagomVersion.{current => lagomVersion}
organization in ThisBuild := "com.knoldus"

version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.6"

ThisBuild / lagomKafkaEnabled := false
ThisBuild / lagomCassandraEnabled := false

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.9" % Test
val postgresDriver     = "org.postgresql"            % "postgresql"                     % "42.2.22"
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.1.1"
val lagomScaladslAkkaDiscovery =
  "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % lagomVersion
val lagomScalaServer = "com.lightbend.lagom" %% "lagom-scaladsl-server" % lagomVersion


lazy val `lagom-CRUD-entity` = (project in file("."))
  .aggregate(`lagom-CRUD-api`, `lagom-CRUD-impl`)

lazy val `lagom-CRUD-api` = (project in file("lagom-testing-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `lagom-CRUD-impl` = (project in file("lagom-testing-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslTestKit,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi,
      jdbc,
      postgresDriver,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`lagom-CRUD-api`)

