package org.knoldus.readSide

import akka.Done
import org.knoldus.FilteredUserResponse
import org.knoldus.eventSourcing.{UserCreateEvent, UserUpdateEvent}
import play.api.Configuration
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

final case class UserRow(
                        userId: String,
                        name: String
                        )

class UserRepository(database: Database, configuration: Configuration) {
  def newUserCreated(
                    evt: UserCreateEvent
                    ): DBIO[Done] = {
    DBIO.sequence(
      Seq(
        users.insertOrUpdate(
          UserRow(
            userId = evt.userID,
            name = evt.userName
          )
        )
      )
    )
      .transactionally
      .map{_ => Done}
  }

  def oldUserUpdate(evt: UserUpdateEvent): DBIO[Done] = {
    val id = evt.id
    DBIO.sequence(
      Seq(
        findUser(id).map(request => (request.id, request.name)).update((evt.id, evt.name))
      )
    ).transactionally.map(_ => Done)
  }

  def findUser(id: String): Query[UserTable, UserRow, Seq] = {
    users.filter(_.id === id)
  }

  def findUserById(
    id: String): Future[Seq[FilteredUserResponse]] = {
    val query = for {
      user <- users.filter(_.id === id)
    } yield user.name
    toFilterUser(database.run(query.result))
  }

  def deleteUser(id: String): Future[Done] = {
    val query = users.filter(_.id === id).delete
    database.run(query).map(_ => Done)
  }

  def toFilterUser(
    dbResult: Future[Seq[String]]
                  ): Future[Seq[FilteredUserResponse]] = {
    dbResult.map(result =>
      result.map( name =>
        FilteredUserResponse(
          name = name
        )
      )
    )
  }


  class UserTable(tag: Tag) extends Table[UserRow](tag, "user") {
    override def * : ProvenShape[UserRow] = (
      id,
      name
    ).mapTo[UserRow]

    def id: Rep[String] = column[String]("id", O.PrimaryKey)
    def name: Rep[String] = column[String]("userName")
  }

  val users: TableQuery[UserTable] = TableQuery[UserTable]

}
