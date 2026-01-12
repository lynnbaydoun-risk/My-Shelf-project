package models

import slick.jdbc.PostgresProfile.api._
import java.time.LocalDate

class BookTable(tag: Tag) extends Table[Book](tag, "books") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def author = column[String]("author")
  def pages = column[Option[Int]]("pages")
  def publishedDate = column[LocalDate]("published_date")

  def * = (id, title, author, pages, publishedDate) <> (Book.tupled, Book.unapply)
}