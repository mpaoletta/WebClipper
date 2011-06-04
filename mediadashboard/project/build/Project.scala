import sbt._
import de.element34.sbteclipsify._
import Process._

class Project(info: ProjectInfo) extends DefaultWebProject(info) with AkkaProject with Eclipsify {

  // -------------------------------------------------------------------------------------------------------------------
  // All repositories *must* go here! See ModuleConfigurations below.
  // -------------------------------------------------------------------------------------------------------------------
  object Repositories {
    // e.g. val akkaRepo = MavenRepository("Akka Repository", "http://akka.io/repository")
    //  val novusRels = "repo.novus rels" at "http://repo.novus.com/releases/"
    // e.g. val akkaModuleConfig = ModuleConfiguration("se.scalablesolutions.akka", akkaRepo)

    // For scalatra
    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    // For Scalate
    val fuseSourceSnapshots = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"
    // For salat
    val novusSnaps = "repo.novus snaps" at "http://repo.novus.com/snapshots/"
  }

  // -------------------------------------------------------------------------------------------------------------------
  // ModuleConfigurations
  // Every dependency that cannot be resolved from the built-in repositories (Maven Central and Scala Tools Releases)
  // must be resolved from a ModuleConfiguration. This will result in a significant acceleration of the update action.
  // Therefore, if repositories are defined, this must happen as def, not as val.
  // -------------------------------------------------------------------------------------------------------------------
  import Repositories._

  //  lazy val scalaTestModuleConfig  = ModuleConfiguration("org.scalatest", ScalaToolsRepo) 
  //  //equiv of saying: "When you look for something with group id "org.scalatest", look n this repo, but not otherwise
  lazy val salatModuleConfig = ModuleConfiguration("com.novus", novusSnaps)
  lazy val scalatraModuleConfig = ModuleConfiguration("org.scalatra", sonatypeNexusSnapshots)
  lazy val scalateModuleConfig = ModuleConfiguration("org.fusesourcee", fuseSourceSnapshots)

  // -------------------------------------------------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------------------------------------------------
  override val akkaActor = akkaModule("actor") withSources () // it's good to always have the sources around
  val akkaHttp = akkaModule("http") withSources ()
  val akkaCamel = akkaModule("camel") withSources ()
  val camelQuartz = "org.apache.camel" % "camel-quartz" % "2.7.1" withSources ()

  val JETTY_VERSION = "8.0.0.M2"
  //val specs       = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % JETTY_VERSION % "test"
  val jettyWebApp = "org.eclipse.jetty" % "jetty-webapp" % JETTY_VERSION % "test"

  val morphia = "com.google.code" % "morphia" % "0.91" withSources ()

  val guava = "com.google.guava" % "guava" % "r09"

  val twitter4jCore = "org.twitter4j" % "twitter4j-core" % "2.2.2" withSources ()
  val twitter4jStream = "org.twitter4j" % "twitter4j-stream" % "2.2.2" withSources ()

  val sjson = "net.debasishg" % "sjson_2.8.0" % "0.8" 
  val scalaredis = "net.debasishg" % "redisclient_2.8.1" % "2.3.1" intransitive ()

  val salat = "com.novus" % "salat-core_2.9.0-1" % "0.0.8-SNAPSHOT" withSources ()

  val scalatraVersion = "2.0.0-SNAPSHOT"
  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
  
  val jedis = "redis.clients" % "jedis" % "1.3.0" withSources ()

}
