name := "betterReads"

version := "0.1"

scalaVersion := "2.12.8"

// Spark dependencies
libraryDependencies ++= {
  val sparkVersion = "2.4.2"

  Seq(
    // https://mvnrepository.com/artifact/org.apache.spark/spark-core
    "org.apache.spark" %% "spark-core" % sparkVersion,
    // https://mvnrepository.com/artifact/org.apache.spark/spark-mllib
    "org.apache.spark" %% "spark-mllib" % sparkVersion
  )
}

// Akka http dependencies
libraryDependencies ++= {
  val akkaHttpVersion = "10.1.8"

  Seq(
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-http-experimental
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-http-spray-json-experimental
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  )
}

// Akka dependencies
libraryDependencies ++= {
  val akkaVersion = "2.6.0-M1"

  Seq(
    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,

    // https://mvnrepository.com/artifact/com.typesafe.akka/akka-stream
    "com.typesafe.akka" %% "akka-stream" % akkaVersion
  )
}

