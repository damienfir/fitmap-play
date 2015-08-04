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
import models.DAO

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
  import driver.api._

  val query: TableQuery[Q]
  implicit val jsonformat: json.Format[T]

  def get(id: Int) = Action.async {
    dbConfig.db.run(DAO.byID(Tables.trainers, id)) map {
      case Some(trainer: Trainer) => Ok(toJson(trainer))
      case None => NotFound
    }
  }

  def create = Action.async(parse.json) { request =>
    dbConfig.db.run(DAO.insert(query, request.body.as[T])) map { trainer =>
      Ok(toJson(trainer))
    }
  }

  def update(id: Int) = Action.async(parse.json) { request =>
    dbConfig.db.run(DAO.update(query, id, request.body.as[T])) map { trainer =>
      Ok(toJson(trainer))
    }
  }

  def delete(id: Int) = Action.async {
    dbConfig.db.run(DAO.delete(query, id)) map { nb =>
      Ok
    }
  }
}


class Trainers extends CRUDController[Trainer, Tables.Trainers] {
  implicit val jsonformat = Json.format[Trainer]
  override val query = Tables.trainers

  // def getClients(id: Int) = Action.async {
    // dbConfig.db.run(Tables.clients.filter(_.trainerID === id))
  // }
}
