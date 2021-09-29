package com.capstone.admin.routes

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import slick.jdbc.MySQLProfile.api._
import com.capstone.admin.actor.AdminActor
import com.capstone.components.AdminDAO
import com.capstone.flyway.FlywayService
import com.capstone.models.AppConfiguration
import com.typesafe.scalalogging.LazyLogging
import org.fusesource.jansi.Ansi.Color._
import org.fusesource.jansi.Ansi._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

import scala.concurrent.duration._
import scala.util.{Failure, Success}
// NOTE* - DO NOT remove this import, It may be visible as unused import.
//import pureconfig.generic.auto._

object AdminHTTPServer extends App with AdminService with LazyLogging {

  implicit val system: ActorSystem = ActorSystem("admin")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val config: AppConfiguration = ConfigSource
    .resources("application.conf")
    .withFallback(ConfigSource.systemProperties)
    .load[AppConfiguration] match {
    case Left(e: ConfigReaderFailures) =>
      throw new RuntimeException(
        s"Unable to load config, original error: ${e.prettyPrint()}")
    case Right(x) => x
  }

  implicit val schema: String = config.dbConfig.schema
  implicit val db: Database = Database.forURL(
    config.dbConfig.url,
    user = config.dbConfig.user,
    password = config.dbConfig.password,
    driver = config.dbConfig.driver,
    executor = AsyncExecutor("mysql",
                             numThreads = config.dbConfig.threadsPoolCount,
                             queueSize = config.dbConfig.queueSize)
  )
  implicit val searchLimit: Int = config.dbConfig.searchLimit

  val futureAwaitTime: FiniteDuration =
    config.akka.futureAwaitDurationMins.minutes
  implicit val futureAwaitDuration: FiniteDuration =
    FiniteDuration(futureAwaitTime.length, futureAwaitTime.unit)

  val flyWayService = new FlywayService(config.dbConfig)
  flyWayService.migrateDatabaseSchema()

  val adminDAO = new AdminDAO()
  val adminActor: ActorRef = system.actorOf(
    AdminActor
      .props(adminDAO)
      .withRouter(RoundRobinPool(nrOfInstances = config.akka.akkaWorkersCount)),
    "admin")

  lazy val routes: Route = getUserRoutes(adminActor)

  //bind route to server
  val binding = Http().bindAndHandle(routes, config.app.host, config.app.port)

  //scalastyle:off
  binding.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      println(
        ansi()
          .fg(GREEN)
          .a("""
               |
               |
               |░█████╗░██████╗░███╗░░░███╗██╗███╗░░██╗  ░██████╗███████╗██████╗░██╗░░░██╗██╗░█████╗░███████╗
               |██╔══██╗██╔══██╗████╗░████║██║████╗░██║  ██╔════╝██╔════╝██╔══██╗██║░░░██║██║██╔══██╗██╔════╝
               |███████║██║░░██║██╔████╔██║██║██╔██╗██║  ╚█████╗░█████╗░░██████╔╝╚██╗░██╔╝██║██║░░╚═╝█████╗░░
               |██╔══██║██║░░██║██║╚██╔╝██║██║██║╚████║  ░╚═══██╗██╔══╝░░██╔══██╗░╚████╔╝░██║██║░░██╗██╔══╝░░
               |██║░░██║██████╔╝██║░╚═╝░██║██║██║░╚███║  ██████╔╝███████╗██║░░██║░░╚██╔╝░░██║╚█████╔╝███████╗
               |╚═╝░░╚═╝╚═════╝░╚═╝░░░░░╚═╝╚═╝╚═╝░░╚══╝  ╚═════╝░╚══════╝╚═╝░░╚═╝░░░╚═╝░░░╚═╝░╚════╝░╚══════╝

               |
               |
               |                                                 MySql configuration
               |---------------------------------------------------------------------------------------------------------------------------------
               |
               |+----------+-----------+
               || url      | localhost |
               |+----------+-----------+
               || port     | 3306      |
               |+----------+-----------+
               || user     | root      |
               |+----------+-----------+
               || password | root      |
               |+----------+-----------+
               |
               |""".stripMargin)
          .reset())
      //scalastyle:on

      logger.info(
        s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
    case Failure(e) ⇒
      logger.error(s"Binding failed with ${e.getMessage}")
      system.terminate()
  }
}
