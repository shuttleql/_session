package com.shuttleql.services.session

import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.slf4j.{Logger, LoggerFactory}
import com.shuttleql.services.session.tables.{Session, Sessions, UserToSession, UserToSessions}
import org.scalatra._
import com.typesafe.config._
import com.gandalf.HMACAuth

// Strong params
case class UserParams(id: Int)

class SessionServiceServlet extends SessionServiceStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  val conf = ConfigFactory.load();

  private def getRequest = enrichRequest(request)
  private def getResponse = enrichResponse(response)

  before() {
    auth
    contentType = formats("json")
  }

  def auth() {
    val token = getRequest.header("Authorization")
    val key = getRequest.header("Authorization-Key")
    val secret = conf.getString("secrets.hmac_secret")

    (token, key) match {
      case (Some(t), Some(k)) =>
        val split = t.split("HMAC ")
        split.length match {
          case 2 =>
            HMACAuth.validateHost(split(1), k, secret) match {
              case true => return
              case false =>
                halt(status=401, reason="Forbidden");
            }
          case _ =>
            halt(status=401, reason="Forbidden");
        }
      case _ =>
        halt(status=401, reason="Forbidden");
    }
  }

  get("/setup") {
    SessionsDAO.setupTables() match {
      case Some(results) =>
        Ok(reason = "Success.")
      case None =>
        InternalServerError(reason = "Error creating club session tables.")
    }

    UserToSessionsDAO.setupTables() match {
      case Some(results) =>
        Ok(reason = "Success.")
      case None =>
        InternalServerError(reason = "Error creating user to session tables.")
    }
  }

  get("/current") {
    SessionsDAO.current() match {
      case Some(session) =>
        Ok(session)
      case None =>
        NotFound(reason = "No session has started")
    }
  }

  post("/create") {
  	SessionsDAO.create() match {
      case Some(result) =>
        Ok(result)
      case None =>
        InternalServerError(reason = "Error with creating club session.")
    }
  }

  put("/end") {
  	SessionsDAO.end() match {
      case Some(result) =>
        NoContent()
      case None =>
        InternalServerError(reason = "Error with ending club session.")
    }
  }

  post("/checkin") {
    try {
    	val user = parsedBody.extract[UserParams]
      val userId = user.id.asInstanceOf[Number].longValue

      SessionsDAO.current() match {
        case Some(user) =>
          UserToSessionsDAO.create(userId, user.id) match {
            case Some(results) =>
              Ok(results)
            case None =>
              InternalServerError(reason = "Could not create user to session")
          }
        case None =>
          NotFound(reason = "No session has started")
      }
    } catch {
      case e: Exception =>
        InternalServerError(reason = "Problem with payload.")
    }
  }

  put("/checkout") {
    try {
    	val user = parsedBody.extract[UserParams]
      val userId = user.id.asInstanceOf[Number].longValue

      SessionsDAO.current() match {
        case Some(user) =>
          UserToSessionsDAO.update(userId, user.id) match {
            case Some(results) =>
              Ok(results)
            case None =>
              InternalServerError(reason = "Could not update user to session")
          }
        case None =>
          NotFound(reason = "No session has started")
      }
    } catch {
      case e: Exception =>
        InternalServerError(reason = "Problem with payload.")
    }
  }

  get("/getCurrentUsers") {
    SessionsDAO.current() match {
      case Some(user) =>
        UserToSessionsDAO.findUsersBySession(user.id) match {
          case Some(results) =>
            Ok(results)
          case None =>
            InternalServerError(reason = "Could not find users")
        }
      case None =>
        NotFound(reason = "Invalid user id")      
    }
  }
}
