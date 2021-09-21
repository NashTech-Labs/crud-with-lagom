package org.knoldus

import play.api.libs.json.{Format, Json}

case class User(id:String, name:String)

object User{
  implicit val format: Format[User] = Json.format[User]
}

case class FilteredUserResponse(name: String)
case class UpdateUserRequest(name: String)

object UpdateUserRequest{
  implicit val format: Format[UpdateUserRequest] = Json.format[UpdateUserRequest]
}

object FilteredUserResponse {
  implicit val format: Format[FilteredUserResponse] = Json.format[FilteredUserResponse]
}
