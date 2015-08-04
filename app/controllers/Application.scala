package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.libs.json
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson

import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile
import slick.driver.H2Driver.api._

import models._
import models.Queries._
import models.Tables

import JsonFormats._


class Application extends Controller {
  def index = Action {
    Ok(views.html.index(""))
  }
}


trait DBController extends HasDatabaseConfig[JdbcProfile] {
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
}


abstract class CRUDController[T <: WithID[T], Q <: Table[T] with Tables.HasID] extends Controller with DBController {
  val query: TableQuery[Q]
  implicit val jsonformat: json.Format[T]

  def get(id: Int) = Action.async {
    dbConfig.db.run(query.byID(id).result map (_.headOption)) map {
      case Some(item: T) => Ok(toJson(item))
      case None => NotFound
    }
  }

  def create = Action.async(parse.json) { request =>
    val q = (query returning query.map(_.id) into ((created, id) => created.withID(id))) += request.body.as[T]
    dbConfig.db.run(q) map { item =>
      Ok(toJson(item))
    }
  }

  def update(id: Int) = Action.async(parse.json) { request =>
    dbConfig.db.run(query.byID(id).update(request.body.as[T])) map { item =>
      Ok(toJson(item))
    }
  }

  def delete(id: Int) = Action.async {
    dbConfig.db.run(query.filter(_.id === id).delete) map { nb =>
      Ok
    }
  }
}


class Trainers extends CRUDController[Trainer, Tables.Trainers] {
  implicit val jsonformat = Json.format[Trainer]
  override val query = Tables.trainers

  def getClients(id: Int) = Action.async {
    dbConfig.db.run(Tables.clientstrainers.byRight[Client, Tables.Clients](id, Tables.clients).result) map { clients =>
      Ok(toJson(clients))
    }
  }

  def addClient(id: Int) = Action.async(parse.json) {
    dbConfig.db.run()
  }
}


class Clients extends CRUDController[Client, Tables.Clients] {
  implicit val jsonformat = Json.format[Client]
  override val query = Tables.clients
}
