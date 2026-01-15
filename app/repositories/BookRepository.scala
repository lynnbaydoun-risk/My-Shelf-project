package repositories

import models.{Book, BookTable}
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.JdbcProfile
import play.api.db.slick.DatabaseConfigProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BookRepository @Inject()(
                                dbConfigProvider: DatabaseConfigProvider
                              )(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val db = dbConfig.db

  private val books = TableQuery[BookTable]

  def all(): Future[Seq[Book]] =
    db.run(books.sortBy(_.id.desc).result)

  def findById(id: Long): Future[Option[Book]] =
    db.run(books.filter(_.id === id).result.headOption)

  def create(book: Book): Future[Long] =
    db.run((books returning books.map(_.id)) += book.copy(id = 0L))

  def delete(id: Long): Future[Int] =
    db.run(books.filter(_.id === id).delete)

  def update(id: Long, updated: Book): Future[Int] =
    db.run(
      books.filter(_.id === id)
        .map(b => (b.title, b.author, b.pages, b.publishedDate, b.image))
        .update((updated.title, updated.author, updated.pages, updated.publishedDate, updated.image))
    )
}
