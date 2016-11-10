package com.shuttleql.services.session.tables

import java.sql.Timestamp
import slick.profile.SqlProfile.ColumnOption.SqlType
import slick.driver.PostgresDriver.api._

case class Session(id: Long, createdAt: Timestamp, isActive: Boolean = true)

class Sessions(tag: Tag) extends Table[Session](tag, "sessions") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[Timestamp]("created_at", SqlType("timestamp not null default CURRENT_TIMESTAMP"))
  def isActive = column[Boolean]("isActive")
  def * = (id, createdAt, isActive) <> (Session.tupled, Session.unapply)
}
