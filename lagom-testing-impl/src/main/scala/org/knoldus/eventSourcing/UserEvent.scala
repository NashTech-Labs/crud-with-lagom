package org.knoldus.eventSourcing

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import com.typesafe.config.{Config, ConfigFactory}
import org.knoldus.User
import play.api.libs.json.{Format, Json}


trait EventHandler{
  def getInternalState: UserState

  def getUser: Option[User]

  def onUserAdded(evt: UserCreateEvent): UserState = {
    getInternalState.copy(user =
      Some(
        User(
          id = evt.userID,
          name = evt.userName
        )
      )
    )
  }

  def onUserUpdate(evt: UserUpdateEvent): UserState = {
    getInternalState.copy(user =
      getUser.map(u =>
        u.copy(
          id = evt.id,
          name =evt.name
        )
      )
    )
  }

  def handleEvent: UserEvent => UserState = {
    case event: UserCreateEvent => onUserAdded(event)
    case event: UserUpdateEvent => onUserUpdate(event)
  }
}

sealed trait UserEvent extends AggregateEvent[UserEvent] {
  override def aggregateTag: AggregateEventTagger[UserEvent] = UserEvent.Tag
}

object UserEvent {
  val config: Config  = ConfigFactory.load()
  val Tag: AggregateEventTag[UserEvent] = AggregateEventTag[UserEvent]()
}

case class UserCreateEvent(
                          userID: String,
                          userName: String
                          ) extends UserEvent

case class UserFindEvent(id: String) extends UserEvent

object UserCreateEvent{
  implicit val format: Format[UserCreateEvent] = Json.format[UserCreateEvent]
}

final case class UserUpdateEvent(
  id: String,
  name: String
                                ) extends UserEvent
object UserUpdateEvent{
  implicit val format: Format[UserUpdateEvent] = Json.format[UserUpdateEvent]
}

case class DeleteUserEvent(
  id: String
                          )
