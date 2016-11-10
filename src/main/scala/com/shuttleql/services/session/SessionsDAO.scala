package com.shuttleql.services.session

import com.shuttleql.services.session.tables.{Session, Sessions}
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object SessionsDAO extends TableQuery(new Sessions(_)) {
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

  def current(): Option[Long] = {
    val db = initDb

    try {
      val result: Session = Await.result(db.run(this.filter(_.isActive === true).result), Duration.Inf).headOption.get
      Option(result.id)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def create(): Option[Session] = {
    val db = initDb
    val newSession = Session(0, new java.sql.Timestamp(System.currentTimeMillis()))

    try {
      // Make all existing sessions inactive
      Await.result(db.run(this.map(x => (x.isActive))
            .update(false)), Duration.Inf)

      val result: Session = Await.result(db.run(this returning this += newSession), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }

  def end() {
    val db = initDb

    try {
      val session: Session = Await.result(db.run(this.filter(_.isActive === true).result), Duration.Inf).headOption.get

      Await.result(db.run(this.filter(_.id === session.id)
            .map(x => (x.isActive))
            .update(false)), Duration.Inf)

      Option(session)
    } catch {
      case e: Exception => None
    } finally {
      db.close
    }
  }
}
