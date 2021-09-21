package org.knoldus.service

import akka.{Done, NotUsed}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.{RequestHeader, ResponseHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import org.knoldus.eventSourcing.UserState.Accepted
import org.knoldus.eventSourcing.{AddUserCommand, UpdateExistingUser, UserCommand, UserState}
import org.knoldus.readSide.UserRepository
import org.knoldus.{FilteredUserResponse, LagomCRUDEntity, UpdateUserRequest, User}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class LagomCRUDEntityImpl(clusterSharding: ClusterSharding, repo: UserRepository)
                         (implicit ec: ExecutionContext) extends LagomCRUDEntity {

  implicit val timeout = Timeout(5.seconds)

  protected def ref(id:String): EntityRef[UserCommand] = {
    clusterSharding.entityRefFor(UserState.typeKey, id)
  }

  override def addUser(): ServiceCall[User, String] = {
    ServiceCall { user =>
      ref(user.id).ask(reply =>
        AddUserCommand(user, reply)).map{
        case _: Accepted => s"user with user ID - ${user.id} added"
      }
    }
  }

  override def findUser(idOption: Option[String]): ServiceCall[NotUsed, Seq[FilteredUserResponse]] = {
    ServerServiceCall{ (reqHeader: RequestHeader, _: NotUsed) =>
      val id = idOption.get
      for {
       receipts <- repo.findUserById(id)
      } yield (ResponseHeader.Ok, receipts)
    }
  }

  override def updateUser(idOption: Option[String]): ServiceCall[UpdateUserRequest, String] = {
    ServiceCall { request =>
      val newName = request.name
      val id = idOption.get
      val user = User(id, newName)
      ref(id).ask(reply =>
        UpdateExistingUser(user, reply)
      ).map{
        case _: Accepted => s"user updated with id: ${id}"
      }
    }
  }


  override def deleteUser(idOption: Option[String]): ServiceCall[NotUsed, Done] =
    ServiceCall { _ =>
      idOption match {
        case Some(id) => repo.deleteUser(id)
      }
  }
}
