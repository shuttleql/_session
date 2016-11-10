package com.shuttleql.services.session

import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.Ok
import org.slf4j.{Logger, LoggerFactory}
import com.shuttleql.services.session.tables.{Session, Sessions, UserToSession, UserToSessions}
import org.scalatra._

// Strong params
case class UserParams(id: Int)

class SessionServiceServlet extends SessionServiceStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
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

  // TODO check for secret key in all routes below

  get("/current") {
    SessionsDAO.current() match {
      case Some(id) =>
        Ok(id)
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
        case Some(id) =>
          UserToSessionsDAO.create(userId, id) match {
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
        case Some(id) =>
          UserToSessionsDAO.update(userId, id) match {
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
      case Some(id) =>
        UserToSessionsDAO.findUsersBySession(id) match {
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
