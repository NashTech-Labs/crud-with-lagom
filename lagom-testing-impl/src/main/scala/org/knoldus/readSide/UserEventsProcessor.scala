package org.knoldus.readSide

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import org.knoldus.eventSourcing.{UserCreateEvent, UserEvent, UserUpdateEvent}
import slick.dbio.DBIO

class UserEventsProcessor(readSide: SlickReadSide, repo: UserRepository) extends ReadSideProcessor[UserEvent]{
  override def buildHandler(): ReadSideProcessor.ReadSideHandler[UserEvent] =
    readSide
      .builder[UserEvent]("UserReadSide")
      .setEventHandler[UserCreateEvent](e => handleCreateUser(e.event))
      .setEventHandler[UserUpdateEvent](e => handleUpdateUser(e.event))
      .build()

  private def handleCreateUser(event: UserCreateEvent): DBIO[Done] = {
    repo.newUserCreated(event)
  }

  private def handleUpdateUser(event: UserUpdateEvent): DBIO[Done] = {
    repo.oldUserUpdate(event)
  }

  override def aggregateTags: Set[AggregateEventTag[UserEvent]] =
    Set(UserEvent.Tag)
}
