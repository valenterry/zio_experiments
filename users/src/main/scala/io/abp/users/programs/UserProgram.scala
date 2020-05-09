package io.abp.users.programs

import java.io.IOException
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneOffset

import io.abp.users.domain.User
import io.abp.users.services.users.{User => UserService}
import zio._

object UserProgram {

  //The existence check wouldn't work in a concurrent system. We need semantic locking.
  //TODO: explore ZIO.STM and ZIO.Ref
  def createUser(
      userService: UserService.Service
  )(name: String): ZIO[userService.Env, ProgramError, User.Id] =
    for {
      //result <- getUsersByName(name).mapError(ProgramError.UserError)
      user <- userService.create(name).mapError(ProgramError.UserError)
      //user <-
      //  if (result.isEmpty) users.createUser(name).mapError(ProgramError.UserError)
      //  else ZIO.fail(ProgramError.UserAlreadyExists)
    } yield user.id

  def getUser(
      userService: UserService.Service
  )(id: User.Id): ZIO[userService.Env, ProgramError, Option[User]] =
    userService.get(id).mapError(ProgramError.UserError)

  def getAllUsers(userService: UserService.Service)(): ZIO[userService.Env, ProgramError, List[User]] =
    userService.all.mapError(ProgramError.UserError)

  def getUsersCreatedBefore(
      userService: UserService.Service
  )(instant: Instant): ZIO[userService.Env, ProgramError, List[User]] =
    userService.all
      .mapError(ProgramError.UserError)
      .map(_.filter(_.createdAt.atZoneSameInstant(ZoneOffset.UTC).toInstant.isBefore(instant)))

  trait ProgramError extends Throwable
  object ProgramError {
    case class ConsoleError(underlying: IOException) extends ProgramError
    case class ClockError(underlying: DateTimeException) extends ProgramError
    case class UserError(underlying: UserService.Error) extends ProgramError
    case object UserAlreadyExists extends ProgramError
  }

}
