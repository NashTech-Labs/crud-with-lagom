package org.knoldus.service

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import org.knoldus.eventSourcing.{UserCreateEvent, UserState}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

object UserSerializerRegistry extends JsonSerializerRegistry {

  implicit val receiptState: Format[UserState]     = Json.format

  override val serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[UserCreateEvent],
    JsonSerializer[UserState],
  )
}