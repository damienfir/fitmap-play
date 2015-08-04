package models

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import slick.driver.H2Driver.api._

import models.Tables._

trait WithID[T <: WithID[T]] {
  def id: Option[Int]
  def withID(id: Int): T
}

case class User(id: Option[Int], name: String) extends WithID[User] {
  def withID(id: Int) = this.copy(id = Some(id))
}
case class Trainer(id: Option[Int], user_id: Int) extends WithID[Trainer] {
  def withID(id: Int) = this.copy(id = Some(id))
}
case class Client(id: Option[Int], user_id: Int) extends WithID[Client] {
  def withID(id: Int) = this.copy(id = Some(id))
}


object JsonFormats {
  implicit val userjson = Json.format[User]
  implicit val clientjson = Json.format[Client]
}


object Queries {

  implicit class IDQueryExtensions[E, T <: Table[_] with HasID](val q: Query[T,E,Seq]) {
    def byID(id: Rep[Int]) = q.filter(_.id === id)
  }

  implicit class LinkQueryExtension[E, T <: LinkTable](val q: Query[T,E,Seq]) {
    def byRight[E <: WithID[E], T <: Table[E] with HasID](id: Rep[Int], toQuery: TableQuery[T]) = for {
      row <- q
      x <- toQuery if row.rightID === id && row.leftID === x.id
    } yield x
  }
}
