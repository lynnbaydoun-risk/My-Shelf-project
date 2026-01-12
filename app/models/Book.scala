package models

import java.time.LocalDate

case class Book(
                 id: Long = 0L,
                 title: String,
                 author: String,
                 pages: Option[Int],
                 publishedDate: LocalDate
               )