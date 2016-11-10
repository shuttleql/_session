package com.shuttleql.services.session.tables

import java.sql.Timestamp
import slick.profile.SqlProfile.ColumnOption.SqlType
import slick.driver.PostgresDriver.api._

case class UserToSession(
	userId: Long, 
	sessionId: Long, 
	checkedInAt: Timestamp, 
	checkedOutAt: Option[Timestamp])

class UserToSessions(tag: Tag) extends Table[UserToSession](tag, "user_to_sessions") {
  def userId = column[Long]("user_id")
  def sessionId = column[Long]("session_id")
  def checkedInAt = column[Timestamp]("checked_in_at", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
  def checkedOutAt = column[Timestamp]("checked_out_at", SqlType("timestamp"))
  def * = (userId, sessionId, checkedInAt, checkedOutAt.?) <> (UserToSession.tupled, UserToSession.unapply)
  def pk = primaryKey("pk_user_to_sessions", (userId, sessionId))
}
