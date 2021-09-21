package org.knoldus.eventSourcing

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.scaladsl.{EntityContext, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import org.knoldus.User
import play.api.libs.json._

object UserBehaviour {

  val DELETE_AFTER_NUMBER_OF_EVENTS = 100
  val SNAPSHOTS_TO_KEEP             = 2

  def apply(entityContext: EntityContext[UserCommand]): Behavior[UserCommand] =
    apply(entityContext, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))


  def apply(
             entityContext: EntityContext[UserCommand],
             persistenceId: PersistenceId
           ): Behavior[UserCommand] = {
    Behaviors.setup { ctx =>
      EventSourcedBehavior
        .withEnforcedReplies[UserCommand, UserEvent, UserState](
          persistenceId = persistenceId,
          emptyState = UserState.empty,
          commandHandler = (receiptState, cmd) => receiptState.applyCommand(cmd, ctx),
          eventHandler = (receiptState, evt) => receiptState.applyEvent(evt)
        )
        .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, UserEvent.Tag))
        .withRetention(
          RetentionCriteria
            .snapshotEvery(numberOfEvents = DELETE_AFTER_NUMBER_OF_EVENTS, SNAPSHOTS_TO_KEEP)
        )
    }
  }

}

object UserState{

  val empty: UserState = UserState()
  val typeKey: EntityTypeKey[UserCommand] =
    EntityTypeKey[UserCommand]("User")

  sealed trait Confirmation extends CommandSerializable

  sealed trait Accepted extends Confirmation

  final case class Rejected(reason: String) extends Confirmation

  case object Confirmation {
    implicit val format: Format[Confirmation] = new Format[Confirmation] {
       def reads(json: JsValue): JsResult[Confirmation] = {
        if ((json \ "reason").isDefined) {
          Json.fromJson[Rejected](json)
        } else {
          Json.fromJson[Accepted](json)
        }
      }

      def writes(o: Confirmation): JsValue = {
        o match {
          case acc: Accepted => Json.toJson(acc)
          case rej: Rejected => Json.toJson(rej)
        }
      }
    }
  }


  case object Accepted extends Accepted {
    implicit val format: Format[Accepted] =
      Format(Reads(_ => JsSuccess(Accepted)), Writes(_ => Json.obj()))
  }

  object Rejected {
    implicit val format: Format[Rejected] = Json.format
  }
}

final case class UserState(
  user: Option[User] = None,
  archived: Boolean =false
) extends EventHandler
  with CommandHandler {

  def applyCommand(
                  userCommand: UserCommand,
                  ctx: ActorContext[UserCommand]
                  ): ReplyEffect[UserEvent, UserState] = handleCommand(userCommand, ctx)

  def applyEvent(userEvent: UserEvent): UserState = {
    handleEvent(
      userEvent
    )
  }

  override def getInternalState: UserState = this

  override def getUser: Option[User] = this.user

}