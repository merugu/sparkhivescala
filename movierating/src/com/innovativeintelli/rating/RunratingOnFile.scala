package com.innovativeintelli.rating
import java.sql.Date
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.sql._
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scala.collection.immutable.TreeMap
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.IntegerType
import org.apache.spark.sql.hive.HiveContext

object RunratingOnFile {

  case class MovieRating(userID: Int, movieID: Int, rating: Int)
  
  def dataMapper(line: String): MovieRating = {
     val data = line.split("\t")
     val userID = if (isEmpty(data(0))) 0 else data(0).toInt
     val movieID = if (isEmpty(data(1))) 0 else data(1).toInt
     val rating = if (isEmpty(data(2))) 0 else data(2).toInt
     val movierating:MovieRating = MovieRating(userID, movieID, rating)
     return movierating
  }
  
  def resultSchema: StructType = { StructType(
      StructField("MovieID",IntegerType) ::
      StructField("NumberOfRatings",IntegerType) :: Nil
      )
  }
  
  def isEmpty(x: String) = x == null || x.isEmpty
  
  def main(args: Array[String]) {
    
    val conf = new SparkConf()
    conf.setAppName("Movierating")
    conf.set("spark.driver.allowMultipleContexts" ,"true")
    conf.setMaster("local") //set this variable to run spark in local mode
    val context = new SparkContext(conf)
    var sqlContext = new org.apache.spark.sql.SQLContext(context)
    val  data= context.textFile("/Users/amitheshmerugu/Downloads/ml-100k/u.data") //read from file
    val movieData= data.map(f => dataMapper(f))
    val userMappedRDD = movieData.map(f => (f.movieID,1)).reduceByKey((map1, map2) => map1 + map2)   
    //userMappedRDD.foreach(f => println("Key "+f._1+" Value "+f._2)) //print values in local
    val rowsRDD= userMappedRDD.map(f => {
      var MovieID = f._1
      var NumberOfRatings = f._2
      Row(MovieID,NumberOfRatings)
      })
    val outputFile = sqlContext.createDataFrame(rowsRDD, resultSchema)
    outputFile.write.mode("overwrite").format("com.databricks.spark.csv").option("header", "true").option("delimiter", "|").save("/Users/amitheshmerugu/spark_output")
    }
}