package org.knoldus.eventSourcing

import akka.actor.ExtendedActorSystem
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, ActorRefResolver}
import play.api.libs.json.{Format, JsError, JsPath, JsResult, JsString, JsSuccess, JsValue, JsonValidationError, Reads, Writes}

class PlayJsonActorRefFormat(system: ExtendedActorSystem) {
  def reads[A] = new Reads[ActorRef[A]] {
    def reads(jsv: JsValue): JsResult[ActorRef[A]] =
      jsv match {
        case JsString(s) => JsSuccess(ActorRefResolver(system.toTyped).resolveActorRef(s))
        case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError(Seq("ActorRefs are strings"))))) // hopefully parenthesized that right...
      }
  }

  def writes[A] = new Writes[ActorRef[A]] {
    def writes(a: ActorRef[A]): JsValue = JsString(ActorRefResolver(system.toTyped).toSerializationFormat(a))
  }

  def format[A] = Format[ActorRef[A]](reads, writes)
}
