package com.shuttleql.services.session

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.shuttleql.services.session.tables.{Session, Sessions}
import com.typesafe.config.ConfigFactory
import slick.lifted.TableQuery
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

object SessionsDAO extends TableQuery(new Sessions(_)) {
  val conf = ConfigFactory.load()

  val creds = new BasicAWSCredentials(conf.getString("amazon.access_key"), conf.getString("amazon.secret_key"))
  val snsClient = new AmazonSNSClient(creds)
  snsClient.setRegion(Region.getRegion(Regions.US_WEST_2))

  def broadcastSessionUpdate(): Unit = {
    val publishReq = new PublishRequest()
      .withTopicArn(conf.getString("amazon.topic_arn"))
      .withSubject("update")
      .withMessage("{ \"resource\": \"session\" }")

    snsClient.publish(publishReq)
  }

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

  def current(): Option[Session] = {
    val db = initDb

    try {
      val result: Session = Await.result(db.run(this.filter(_.isActive === true).result), Duration.Inf).headOption.get
      Option(result)
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
      val dbAction = (
        for {
          // Make all existing sessions inactive
          _ <- this.map(x => (x.isActive)).update(false)
          newRow <- this returning this += newSession
        } yield newRow
      ).transactionally

      val result: Session = Await.result(db.run(dbAction), Duration.Inf)
      Option(result)
    } catch {
      case e: Exception => None
    } finally {
      broadcastSessionUpdate
      db.close
    }
  }

  def end(): Option[Session] = {
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
      broadcastSessionUpdate
      db.close
    }
  }
}
