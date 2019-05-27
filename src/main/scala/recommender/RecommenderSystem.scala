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
  var productFeatures: Option[RDD[(Int, Array[Double])]] = None

  def receive = {
    case Train => trainModel()
    case GenerateRecommendationsForUser(userId) => generateRecommendationsForUser(userId, 10)
    case FindSimilarBooks(bookId) => findSimilarBooks(bookId, 10)
    case ModelTrainer.TrainingResult(model, productFeatures) => storeModelAndProductFeatures(model, productFeatures)
  }

  private def trainModel(): Unit = {
    // Start a separate actor to train the recommendation system.
    // This enables the service to continue service requests while it learns new recommendations.
    val trainer = context.actorOf(ModelTrainer.props(sc), "model-trainer")
    trainer ! ModelTrainer.Train
  }

  private def findSimilarBooks(bookId: Int, count: Int): Unit = {
    log.info(s"Finding ${count} books similar to book with ID ${bookId}")

    val results = model match {
      case None => Nil
    }

    sender ! Recommendations(results)
  }

  private def storeModelAndProductFeatures(model: MatrixFactorizationModel,
                                           productFeatures: RDD[(Int, Array[Double])]): Unit = {
    this.model = Some(model)
    this.productFeatures = Some(productFeatures)
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
