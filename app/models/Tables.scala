package models

import play.api.Play
import slick.driver.H2Driver.api._

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future


object Tables {

  trait HasID { self: Table[_] =>
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  }

  abstract class LinkTable(tag: Tag, name: String, left: String, right: String) extends Table[(Int, Int)](tag, name) {
    def leftID = column[Int](left)
    def rightID = column[Int](right)
    def * = (leftID, rightID)
  }


  class Users(tag: Tag) extends Table[User](tag, "users") with HasID {
    def name = column[String]("name")
    def * = (id.?, name) <> (User.tupled, User.unapply _)
  }
  val users = TableQuery[Users]


  class Trainers(tag: Tag) extends Table[Trainer](tag, "trainers") with HasID {
    def userID = column[Int]("user_id")
    def * = (id.?, userID) <> (Trainer.tupled, Trainer.unapply _)

    def user = foreignKey("user_fk", userID, users)(_.id)
  }
  val trainers = TableQuery[Trainers]


  class Clients(tag: Tag) extends Table[Client](tag, "trainers") with HasID {
    def userID = column[Int]("user_id")
    def * = (id.?, userID) <> (Client.tupled, Client.unapply _)
  }
  val clients = TableQuery[Clients]


  class ClientsTrainer(tag: Tag) extends LinkTable(tag, "clientstrainers", "client_id", "trainer_id")
  val clientstrainers = TableQuery[ClientsTrainer]


  class ClubsTrainers(tag: Tag) extends LinkTable(tag, "clubstrainers", "club_id", "trainer_id")
  val clubstrainers = TableQuery[ClubsTrainers]
}
