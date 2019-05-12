package recommender

import akka.actor.{Actor, Props}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel

object RecommenderSystem {
  case object Train {}
  case class GenerateRecommendations(userId: Int)
  case class Recommendation(contentItemId: Int, rating: Double)
  case class Recommendations(items: Seq[Recommendation])

  def props(sc: SparkContext) = Props(new RecommenderSystem(sc))
}


class RecommenderSystem(sc: SparkContext) extends Actor {
  import RecommenderSystem._

  var model: Option[MatrixFactorizationModel] = None

  def receive = {
    case Train => trainModel()
    case GenerateRecommendations(userId) => generateRecommendations(userId, 10)
    case ModelTrainer.TrainingResult(model) => storeModel(model)
  }

  private def trainModel() = {
    val trainer = context.actorOf(ModelTrainer.props(sc), "model-trainer")
    trainer ! ModelTrainer.Train
  }

  private def storeModel(model: MatrixFactorizationModel) = {
    this.model = Some(model)
  }

  private def generateRecommendations(userId: Int, count: Int) = {

    val results = model match {
      case Some(m) => m.recommendProducts(userId, count)
        .map(rating => Recommendation(rating.product, rating.rating))
        .toList

      case _ => Nil
    }
    sender ! Recommendations(results)
  }

}
