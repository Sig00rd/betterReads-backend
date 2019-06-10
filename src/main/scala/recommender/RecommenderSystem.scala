package recommender

import java.io.Serializable

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.rdd.RDD

object RecommenderSystem {
  case object Train {}
  case class GenerateRecommendationsForUser(userId: Int)
  case class FindSimilarBooks(bookId: Int)
  case class Recommendation(contentItemId: Int, rating: Double)
  case class Recommendations(items: Seq[Recommendation])

  def props(sc: SparkContext) = Props(new RecommenderSystem(sc))
}


class RecommenderSystem(sc: SparkContext) extends Actor with ActorLogging {
  import RecommenderSystem._

  var model: Option[MatrixFactorizationModel] = None

  def receive = {
    case Train => trainModel()
    case GenerateRecommendationsForUser(userId) => generateRecommendationsForUser(userId, 10)
    case FindSimilarBooks(bookId) => findSimilarBooks(bookId, 10)
    case ModelTrainer.TrainingResult(model) => storeModelAndProductFeatures(model)
  }


  private def trainModel(): Unit = {
    // Start a separate actor to train the recommendation system.
    // This enables the service to continue service requests while it learns new recommendations.
    val trainer = context.actorOf(ModelTrainer.props(sc), "model-trainer")
    trainer ! ModelTrainer.Train
  }

  // todo: get ids of the 10 books with the highest cosine similarity to the one with bookId, then return it as list
  // todo: of recommendations
  private def findSimilarBooks(bookId: Int, count: Int): Unit = {
    log.info(s"Finding ${count} books similar to book with ID ${bookId}")

    val results = model match {
      case Some(m) => Nil
      //case Some(m) => mostSimilarBooks(bookId, count)
      case None => Nil
    }

    sender ! Recommendations(results)
  }

//  private def mostSimilarBooks(bookId: Int, count: Int): Recommendations = {
//
//  }


  private def storeModelAndProductFeatures(model: MatrixFactorizationModel): Unit = {
    this.model = Some(model)
  }

  private def generateRecommendationsForUser(userId: Int, count: Int): Unit = {
    log.info(s"Generating ${count} recommendations for user with ID ${userId}")

    // Generate recommendations based on the machine learning model.
    // When there's no trained model return an empty list instead.
    val results = model match {
      case Some(m) => m.recommendProducts(userId,count)
        .map(rating => Recommendation(rating.product,rating.rating))
        .toList

      case None => Nil
    }

    sender ! Recommendations(results)
  }
}
