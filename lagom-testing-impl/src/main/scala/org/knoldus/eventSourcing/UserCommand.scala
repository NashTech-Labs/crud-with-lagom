package org.knoldus.eventSourcing

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import org.knoldus.User
import org.knoldus.eventSourcing.UserState.{Accepted, Confirmation}
import play.api.libs.json._


trait CommandHandler {

  def getInternalState: UserState

  def getUser: Option[User]

  def onCreateUser(command: AddUserCommand,
                   ctx: ActorContext[UserCommand]
                  ): ReplyEffect[UserEvent, UserState] = {
    Effect.persist(
      UserCreateEvent(
        userID = command.user.id,
        userName = command.user.name
      )
    )
      .thenReply(command.reply)(_ => Accepted)

  }

  def onUpdateUser(command: UpdateExistingUser, ctx: ActorContext[UserCommand]): ReplyEffect[UserEvent, UserState] = {
      Effect.persist(
        UserUpdateEvent(
          id = command.user.id,
          name = command.user.name
        )
      ).thenReply(command.reply)(_ => Accepted)
  }

  val handleCommand: (
    UserCommand,
    ActorContext[UserCommand]
  ) => ReplyEffect[UserEvent, UserState] = {
    case(command: AddUserCommand, ctx) => onCreateUser(command, ctx)
    case(command: UpdateExistingUser, ctx) => onUpdateUser(command, ctx)
  }
}
trait UserCommand extends CommandSerializable



final case class AddUserCommand(user:User,
                          reply: ActorRef[Confirmation]
                         ) extends UserCommand

object AddUserCommand{
  def format(arf: PlayJsonActorRefFormat): Format[AddUserCommand] = {
    implicit def arfmt[A]: Format[ActorRef[A]] = arf.format

    Json.format[AddUserCommand]
  }
}


final case class UpdateExistingUser(user: User,
                                    reply: ActorRef[Confirmation]
                                   ) extends UserCommand

object UpdateExistingUser{
  def format(arf: PlayJsonActorRefFormat): Format[UpdateExistingUser] = {
    implicit def arfmt[A]: Format[ActorRef[A]] = arf.format

    Json.format[UpdateExistingUser]
  }
}