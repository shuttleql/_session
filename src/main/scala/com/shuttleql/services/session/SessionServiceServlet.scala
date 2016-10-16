package com.shuttleql.services.session

import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.Ok
import org.slf4j.{Logger, LoggerFactory}

class SessionServiceServlet extends SessionServiceStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats
  
  val logger =  LoggerFactory.getLogger(getClass)

  before() {
    contentType = formats("json")
  }

  get("/current") {
  	// check for secret key
  	// clubSessionId = ClubSession.id
    logger.info("current")
  	Ok()
  }

  post("/create") {
  	// check for secret key
  	// newSession = Session.create
  	// ClubSession.start(newSession)
    logger.info("create")
  	Ok()
  }

  put("/end") {
  	// check for secret key
  	// ClubSession.end
    logger.info("end")
  	Ok()
  }

  post("/checkin") {
  	// user_id = param[user_id]
    // UserToSession.create(user.id, ClubSession.id, checkin: Time.now)
    logger.info("checkin")
  	Ok()
  }

  put("/checkout") {
  	// user_id = param[user_id]
  	// UserToSession.update(ClubSession.id, user_id, checkout: Time.now)
    logger.info("checkout")
  	Ok()
  }
}
