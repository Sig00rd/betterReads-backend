import akka.actor.ActorSystem
import akka.util.Timeout
import restservice.RestService

import scala.concurrent.duration._

object Program extends App {
  implicit val system = ActorSystem("recommended-content-service")
  implicit val timeout: Timeout = 30 seconds

  val service = new RestService("localhost")

  service.start()

}
