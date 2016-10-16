package com.shuttleql.services.session

import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.Ok

class SessionServiceServlet extends SessionServiceStack with JacksonJsonSupport {

  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/current") {
  	// check for secret key
  	// clubSessionId = ClubSession.id
  	Ok()
  }

  post("/create") {
  	// check for secret key
  	// newSession = Session.create
  	// ClubSession.start(newSession)
  	Ok()
  }

  put("/end") {
  	// check for secret key
  	// ClubSession.end
  	Ok()
  }

  post("/checkin") {
  	// email = param[email]
  	// password = param[password]

  	// user = userService.findUser(email, password)
  	// if user.password_hash == HashWithSalt(password)
  	//		UserToSession.create(user.id, ClubSession.id, checkin: Time.now)
  	//		OK()
  	// else
  	// 		NotFound()
  	Ok()
  }

  put("/checkout") {
  	// user_id = param[user_id]
  	// UserToSession.update(ClubSession.id, user_id, checkout: Time.now)
  	Ok()
  }
}
