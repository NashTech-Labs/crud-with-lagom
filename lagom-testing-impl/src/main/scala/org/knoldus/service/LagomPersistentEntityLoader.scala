package org.knoldus.service

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire.wire
import org.knoldus.LagomCRUDEntity
import org.knoldus.eventSourcing.{UserBehaviour, UserState}
import org.knoldus.readSide.{UserEventsProcessor, UserRepository}
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.BodyParsers.Default

import scala.concurrent.ExecutionContext

class LagomPersistentEntityLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new LagomPersistentEntityApplication(context)  with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new LagomPersistentEntityApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] = Some(readDescriptor[LagomCRUDEntity])
}

trait UserComponents
  extends LagomServerComponents
    with SlickPersistenceComponents
    with HikariCPComponents
    with AhcWSComponents {

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = UserSerializerRegistry

  lazy val userRepo: UserRepository = wire[UserRepository]
  readSide.register(wire[UserEventsProcessor])

  clusterSharding.init(
    Entity(UserState.typeKey){ entityContext =>
      UserBehaviour(entityContext)
    }
  )
}

abstract class LagomPersistentEntityApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with UserComponents {

  implicit lazy val actorSystemImpl: ActorSystem   = actorSystem
  implicit lazy val ec: ExecutionContext           = executionContext


  override lazy val lagomServer: LagomServer = serverFor[LagomCRUDEntity](wire[LagomCRUDEntityImpl])

  lazy val bodyParserDefault: Default = wire[Default]
}