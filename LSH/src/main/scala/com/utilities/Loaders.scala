package com.utilities

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.linalg.Vectors
import com.github.karlhigley.spark.neighbors.ANN

/**
  * Definition of functions to load data
  */
object Loaders {


    def getNeighbors(file: String)(implicit sc: SparkContext): RDD[(Long, List[(Long, Double)])] = {

      val inputRDD = sc.textFile(file)
      val header = inputRDD.first()
      val embeedings_data = inputRDD.filter(row => row != header)
      // inputRDD.collect().foreach(println)
      val points = getEmbeddingsinLSHFormat(embeedings_data)
      val ann =
        new ANN(100, "cosine")
          .setTables(1)
          .setSignatureLength(16)

      val model1 = ann.train(points)

      val nn = model1.neighbors(10).map { case (user, neighborsList) => (user, neighborsList.toList) }
      //, neighborsList.map(_._2).reduce((acc, elem) => (acc + elem))
      nn

    }

    def getEmbeddingsinLSHFormat(input: RDD[String]): RDD[(Long, SparseVector)] = {
      input.map {
        line =>
          val fields = line.split(" ")
          val tail = fields.tail.map(x => x.toDouble)
          val sparseVectorEmbeddings = Vectors.dense(tail).toSparse


          (fields.head.toLong, sparseVectorEmbeddings)
      }

    }

    def getTrainData(fileName: String)(implicit sc: SparkContext): RDD[(Int, List[(Long, Double)])] = {

      val trainData = sc.textFile(fileName).map { line =>
        val train = line.split(",")
        val user = train(0)
        val item = train(1)
        val rating = train(2)

        (item.toInt, List((user.toLong, rating.toDouble)))

      }

      val formatted = trainData.reduceByKey(_ ++ _)
      formatted
    }


}
