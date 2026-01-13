package controllers

import models.Book
import repositories.BookRepository
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.LocalDate

@Singleton
class BookController @Inject()(
                                cc: MessagesControllerComponents,
                                repo: BookRepository
                              )(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val bookForm: Form[BookData] = Form(
    mapping(
      "title" -> nonEmptyText,
      "author" -> nonEmptyText,
      "pages" -> optional(number(min = 1)),
      "publishedDate" -> localDate("yyyy-MM-dd"),
      "image" -> nonEmptyText
    )(BookData.apply)(BookData.unapply)
  )

  def index: Action[AnyContent] = Action.async { implicit request =>
    repo.all().map { books =>
      Ok(views.html.index(books))
    }
  }

  def show(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.findById(id).map {
      case Some(book) => Ok(views.html.show(book))
      case None       => NotFound("Book not found")
    }
  }

  def createForm(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.create(bookForm))
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    bookForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.create(formWithErrors))),
      data => {
        val book = Book(
          title = data.title,
          author = data.author,
          pages = data.pages,
          publishedDate = data.publishedDate,
          image = data.image
        )
        repo.create(book).map(_ => Redirect(routes.BookController.index))
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.delete(id).map(_ => Redirect(routes.BookController.index))
  }

  def editForm(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.findById(id).map {
      case Some(book) =>
        val filled = bookForm.fill(BookData(book.title, book.author, book.pages, book.publishedDate, book.image))
        Ok(views.html.edit(id, filled))
      case None => NotFound("Book not found")
    }
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(views.html.edit(id, formWithErrors))),
      data => {
        val updated = Book(
          id = id,
          title = data.title,
          author = data.author,
          pages = data.pages,
          publishedDate = data.publishedDate,
          image = data.image
        )
        repo.update(id, updated).map(_ => Redirect(routes.BookController.show(id)))
      }
    )
  }
}

case class BookData(
                     title: String,
                     author: String,
                     pages: Option[Int],
                     publishedDate: LocalDate,
                     image: String
                   )