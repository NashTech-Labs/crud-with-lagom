package org.knoldus.eventSourcing

//class UserEntity extends PersistentEntity{
//
//  override type Command = UserCommand[_]
//  override type Event = UserEvent
//  override type State = UserState
//
//  override def initialState: UserState = UserState(None, LocalDateTime.now().toString)
//
//  override def behavior: UserState => Actions = {
//    case UserState(_, _) => Actions().onEvent{
//      case (UserAdded(user), _) =>
//        UserState(Some(user), LocalDateTime.now().toString)
//    }
//    .onCommand[AddUserCommand, Done]{
//      case (AddUserCommand(user), ctx, _) =>
//        ctx.thenPersist(UserAdded(user))(_ => ctx.reply(Done))
//    }
//  }
//
//}
