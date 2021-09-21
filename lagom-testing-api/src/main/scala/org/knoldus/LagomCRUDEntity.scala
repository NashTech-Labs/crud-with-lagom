package org.knoldus

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.Method

trait LagomCRUDEntity extends Service {

  def addUser(): ServiceCall[User, String]
  def findUser(id: Option[String]): ServiceCall[NotUsed, Seq[FilteredUserResponse]]
  def updateUser(id: Option[String]): ServiceCall[UpdateUserRequest, String]
  def deleteUser(idOption: Option[String]): ServiceCall[NotUsed, Done]

  override def descriptor: Descriptor = {
    import Service._
    named("user_service").withCalls(
    restCall(Method.POST, "/users/add/", addUser _),
    restCall(Method.GET, "/users/find/:id", findUser _),
    restCall(Method.PUT, "/users/user/update/:id", updateUser _),
    restCall(Method.DELETE, "/user/delete/:id", deleteUser _)
    ).withAutoAcl(true)
  }
}
