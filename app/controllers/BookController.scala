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
      "publishedDate" -> localDate("yyyy-MM-dd")
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
    request.body.asMultipartFormData match {

      case Some(formData) =>
        bookForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(views.html.create(formWithErrors))
            ),

          data =>
            formData.file("image") match {
              case Some(image) =>
                val filename =
                  java.util.UUID.randomUUID().toString + "_" + image.filename

                image.ref.copyTo(
                  new java.io.File(s"public/images/$filename"),
                  replace = true
                )

                val book = Book(
                  title = data.title,
                  author = data.author,
                  pages = data.pages,
                  publishedDate = data.publishedDate,
                  image = filename
                )

                repo.create(book).map(_ =>
                  Redirect(routes.BookController.index)
                )

              case None =>
                Future.successful(
                  BadRequest(views.html.create(
                    bookForm.fill(data)
                      .withError("image", "Image is required")
                  ))
                )
            }
        )

      case None =>
        Future.successful(BadRequest("Invalid multipart request"))
    }
  }


  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.delete(id).map(_ => Redirect(routes.BookController.index))
  }

  def editForm(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.findById(id).map {
      case Some(book) =>
        val filled = bookForm.fill(BookData(book.title, book.author, book.pages, book.publishedDate))
        Ok(views.html.edit(id, filled))
      case None => NotFound("Book not found")
    }
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    request.body.asMultipartFormData match {
      case Some(formData) =>
        bookForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(views.html.edit(id, formWithErrors))),

          data => {
            repo.findById(id).flatMap {
              case Some(existingBook) =>

                val imageFile = formData.file("image")

                val imageName = imageFile match {
                  case Some(image) =>
                    val filename =
                      java.util.UUID.randomUUID().toString + "_" + image.filename
                    image.ref.copyTo(
                      new java.io.File(s"public/images/$filename"),
                      replace = true
                    )
                    filename

                  case None =>
                    existingBook.image // keep old image
                }

                val updated = existingBook.copy(
                  title = data.title,
                  author = data.author,
                  pages = data.pages,
                  publishedDate = data.publishedDate,
                  image = imageName
                )

                repo.update(id, updated).map(_ =>
                  Redirect(routes.BookController.show(id))
                )

              case None =>
                Future.successful(NotFound("Book not found"))
            }
          }
        )

      case None =>
        Future.successful(BadRequest("Invalid form data"))
    }
  }

}

case class BookData(
                     title: String,
                     author: String,
                     pages: Option[Int],
                     publishedDate: LocalDate
                   )