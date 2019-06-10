package recommender

import akka.actor.{Actor, ActorLogging, Props}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SQLContext}

object ModelTrainer {
  case object Train

  case class TrainingResult(model: MatrixFactorizationModel)

  def props(sc: SparkContext) = Props(new ModelTrainer(sc))
}

class ModelTrainer(sc: SparkContext) extends Actor with ActorLogging {
  import recommender.ModelTrainer._

  val sqlContext = new SQLContext(sc)

  def receive = {
    case Train => trainModel()
  }

  private def getDataFromSQL() = {
    val dataframe_mysql = sqlContext.read
      .format("jdbc")
      .option("url", "jdbc:mysql://34.90.125.25:3306/betterreads?allowPublicKeyRetrieval=true&useSSL=false")
      .option("databaseName", "betterreads")
      .option("driver", "com.mysql.cj.jdbc.Driver")
      .option("user", "betterreads")
      .option("password", "mybetterreads123")
      .option("dbtable", "lists")
      .load()

    dataframe_mysql.select("user_id", "book_id", "rating")
      .rdd
      .map(row => Rating(row.getAs[Int](0), row.getAs[Int](1), row.getAs[Int](2).doubleValue()))

  }

  private def trainModel() = {

    // Later - get data from whichever database we're using
    //var data = sc.textFile("resources/ratings.csv")
    //val header = data.first()

    // Filter out the header from the dataset
    //data = data.filter(row => row != header)

    val data = getDataFromSQL()

//    val ratings = data match {
//      case Array(user_id, book_id, rating) => Rating(user_id, book_id, rating)
//    }

    val rank = 10
    val iterations = 10
    val lambda = 0.01

    val model = ALS.train(data, rank, iterations, lambda)

    sender ! TrainingResult(model)

    context.stop(self)
  }
}


