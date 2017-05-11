import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import scopt.OptionParser
import com.lib.AbstractParams
import com.utilities.Loaders.{getTrainData, getNeighbors, getEmbeddingsinLSHFormat}


object LSHInputAndRMSE {


  case class Params(dim: Int = 100,
                    tables: Int = 1,
                    signature: Int = 16,
                    neighbors: Int = 10,
                    train: String = null,
                    test: String = null,
                    embeddings: String = null,
                    rmse: String = null,
                    predictions: String = null) extends AbstractParams[Params] with Serializable
  val defaultParams = Params()

  val parser = new OptionParser[Params]("LSH_Spark") {
    head("Main")
    opt[Int]("dim")
      .text(s"dimension for ANN model: ${defaultParams.dim}")
      .action((x, c) => c.copy(dim = x))
    opt[Int]("tables")
      .text(s"set tables: ${defaultParams.tables}")
      .action((x, c) => c.copy(tables = x))
    opt[Int]("signature")
      .text(s"Signature size: ${defaultParams.signature}")
      .action((x, c) => c.copy(signature = x))
    opt[Int]("neighbors")
      .text(s"Number of neighbors: ${defaultParams.neighbors}")
      .action((x, c) => c.copy(neighbors = x))
    opt[String]("train")
      .required()
      .text("Input train set path: empty")
      .action((x, c) => c.copy(train = x))
    opt[String]("test")
      .required()
      .text("Input test set path: empty")
      .action((x, c) => c.copy(test = x))
    opt[String]("embedding")
      .required()
      .text("Embeddings path: empty")
      .action((x, c) => c.copy(embeddings = x))
    opt[String]("rmse")
      .required()
      .text("RMSE output path: empty")
      .action((x, c) => c.copy(rmse = x))
    opt[String]("predictions")
      .required()
      .text("Predictions output path: empty")
      .action((x, c) => c.copy(predictions = x))
    note(
      """
        | Run predictions with:
        |
        | bin/spark-submit --class LSHInputAndRMSE [parameters]
      """.stripMargin
    )
  }

  def main(args: Array[String]) {

    parser.parse(args, defaultParams).map { param =>

      implicit val sc = new SparkContext("local[*]", "LSHEmbeddings");

      val trainData = getTrainData(param.train)

      val neighbors = getNeighbors(param.embeddings)

      val predictions = predictionCalculation(trainData, neighbors, param.test)

      sc.parallelize(Seq(predictions._2)).coalesce(1).saveAsTextFile(param.rmse)

      predictions._1.coalesce(1).saveAsTextFile(param.predictions)
        }.getOrElse{
      sys.exit(1)
    }
  } // end of main



  def predictionCalculation(trainData: RDD[(Int, List[(Long, Double)])], neighbors: RDD[(Long, List[(Long, Double)])], testFile: String)(implicit sc: SparkContext) = {

    // Load data using loaders
    val trainDataAsMap = trainData.collectAsMap()

    val neighborDataAsMap = neighbors.collectAsMap()

    val neighborUserKeys = neighbors.collectAsMap().keySet

    val trainDataMovieKeys =trainDataAsMap.keySet

    val predictions = sc.textFile(testFile).filter(!_.isEmpty()).map { line =>
      val test = line.split(",")
      val testUser = test(0).toLong
      val testMovie = test(1).toInt
      val testRating = test(2).toDouble
      (testUser,testMovie,testRating)
    }

    val neighborPredictions = predictions.filter(f => neighborUserKeys.contains(f._1) && trainDataMovieKeys.contains(f._2) ).map{
      case(testUser,testMovie,testRating) =>

      val trainuser = trainDataAsMap.apply(testMovie).toMap

      val neighborUser = neighborDataAsMap.apply(testUser).toMap

      val userweight = trainuser.keySet.intersect(neighborUser.keySet).map {
        f => (f, trainuser.get(f).getOrElse(0).asInstanceOf[Double], neighborUser.get(f).getOrElse(0).asInstanceOf[Double]) }.toList

      val totalDistance = userweight.map(_._3).sum

      val predictedRate = userweight.map {
        case (user, rating, distance) => ((distance / totalDistance) * rating)

      }.sum

      val errorCalc = math.pow((testRating - predictedRate), 2)


      (testUser, testMovie, testRating, predictedRate, errorCalc)


    }
    val denom= neighborPredictions.map(_._5).count()
    val numerator = neighborPredictions.map(_._5).reduce((acc, elem) => (acc + elem))

    val rmseValue = Math.sqrt(numerator / denom)


    (neighborPredictions, rmseValue)


  }

}