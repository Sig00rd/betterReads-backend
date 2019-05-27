package restservice

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import org.apache.spark.{SparkConf, SparkContext}
import recommender.RecommenderSystem
import akka.pattern.ask

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class RestService(interface: String, port: Int = 8080)(implicit val system: ActorSystem) extends RestServiceProtocol {
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 30 seconds

  val config = new SparkConf()

  config.setMaster(system.settings.config.getString("spark.master"))
  config.setAppName("recommended-content-service")

  val sparkContext = new SparkContext(config)

  val recommenderSystem = system.actorOf(RecommenderSystem.props(sparkContext))

  val errorHandler = ExceptionHandler {
    case e: Exception => complete {
      StatusCodes.InternalServerError -> ErrorResponse("Internal server error")
    }
  }

  val route = {
    handleExceptions(errorHandler) {
      pathPrefix("recommendations") {
        path(Segment) { id =>
          get {
            complete {
              (recommenderSystem ? RecommenderSystem.GenerateRecommendationsForUser(id.toInt))
                .mapTo[RecommenderSystem.Recommendations]
                .flatMap(result => Future {
                  StatusCodes.OK -> result
                })
            }
          }
        }
      } ~ path("train") {
        post {
          recommenderSystem ! RecommenderSystem.Train

          complete {
            StatusCodes.OK -> GenericResponse("Training started")
          }
        }
      } ~ pathPrefix("similar_books") {
        path(Segment) { id =>
          get {
            complete {
              (recommenderSystem ? RecommenderSystem.FindSimilarBooks(id.toInt))
                .mapTo[RecommenderSystem.Recommendations]
                .flatMap(result => Future {
                  StatusCodes.OK -> result
                })
            }
          }
        }
      }
    }
  }

  def start(): Unit = {
    Http().bindAndHandle(route, interface, port)
  }


}
