package restservice

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import recommender.RecommenderSystem
import spray.json.DefaultJsonProtocol

trait RestServiceProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit def recommendationFormat = jsonFormat2(RecommenderSystem.Recommendation)
  implicit def recommendationsFormat = jsonFormat1(RecommenderSystem.Recommendations)
  implicit def errorResponseFormat = jsonFormat1(ErrorResponse)
  implicit def genericResponseFormat = jsonFormat1(GenericResponse)
}
