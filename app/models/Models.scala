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
  implicit val trainerjson = Json.format[Trainer]
  implicit val clientjson = Json.format[Client]
}


object Queries {

  implicit class IDQueryExtensions[E, T <: Table[_] with HasID](val q: Query[T,E,Seq]) {
    def byID(id: Int) = q.filter(_.id === id)
  }

  implicit class LinkQueryExtension[E, T <: LinkTable](val q: Query[T,E,Seq]) {
    // def byLeft(id: Column[Int]) = 
  }
}


object DAO {
  import Queries._

  def byID[T <: Table[_] with HasID](query: TableQuery[T], id: Int) = query.byID(id).result map (_.headOption)

  def insert[E <: WithID[E], T <: Table[E] with HasID](query: TableQuery[T], item: E) = {
    (query returning query.map(_.id) into ((created, id) => created.withID(id))) += item
  }

  def update[E, T <: Table[E] with HasID](query: TableQuery[T], id: Int, item: E) = query.byID(id).update(item)
  def delete[T <: Table[_] with HasID](query: TableQuery[T], id: Int) = query.byID(id).delete
}
