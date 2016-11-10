package com.shuttleql.services.session

import com.shuttleql.services.session.tables.{UserToSession, UserToSessions}
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object UserToSessionsDAO extends TableQuery(new UserToSessions(_)) {
  def initDb() = {
    Database.forConfig("db")
  }

  def setupTables(): Option[Unit] = {
    val db = initDb

    try {
      Option(Await.result(db.run(this.schema.create), Duration.Inf))
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def create(userId: Long, sessionId: Long): Option[UserToSession] = {
    val db = initDb
    val now = new java.sql.Timestamp(System.currentTimeMillis())
    val newUserToSession = UserToSession(userId, sessionId, now, Some(now))

    try {
      val result: UserToSession = Await.result(db.run(this returning this += newUserToSession), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def update(userId: Long, sessionId: Long): Option[UserToSession] = {
    val db = initDb
    val now = new java.sql.Timestamp(System.currentTimeMillis())

    try {
      val userToSession = Await.result(db.run(this.filter(_.sessionId === sessionId)
            .filter(_.userId === userId).result).map(_.headOption), Duration.Inf)

      Await.result(db.run(this.filter(_.sessionId === sessionId)
            .filter(_.userId === userId)
            .map(x => (x.checkedOutAt))
            .update(now)), Duration.Inf)

      Option(userToSession.get)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def findUsersBySession(sessionId: Long): Option[Seq[UserToSession]] = {
    val db = initDb

    try {
      val result: Seq[UserToSession] = Await.result(db.run(this.filter(_.sessionId === sessionId).result), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }
}
