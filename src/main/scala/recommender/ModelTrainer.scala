package recommender

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD

object ModelTrainer {
  case object Train

  case class TrainingResult(model: MatrixFactorizationModel, productFeatures: RDD[(Int, Array[Double])])

  def props(sc: SparkContext) = Props(new ModelTrainer(sc))
}

class ModelTrainer(sc: SparkContext) extends Actor with ActorLogging {
  import recommender.ModelTrainer._

  def receive = {
    case Train => trainModel()
  }

  private def trainModel() = {
    // Later - get data from whichever database we're using
    var data = sc.textFile("resources/ratings.csv")
    val header = data.first()

    // Filter out the header from the dataset
    data = data.filter(row => row != header)

    val ratings = data
      .map(_.split(",")
      match {
        case Array(user_id, book_id, rating) => Rating(user_id.toInt, book_id.toInt, rating.toDouble)
      })

    val rank = 10
    val iterations = 10
    val lambda = 0.01

    val model = ALS.train(ratings, rank, iterations, lambda)

    sender ! TrainingResult(model, model.productFeatures)

    context.stop(self)
  }
}


