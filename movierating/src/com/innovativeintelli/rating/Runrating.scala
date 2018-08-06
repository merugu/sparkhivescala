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

object Runrating {

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
    val context = new SparkContext(conf)
    var sqlContext = new HiveContext(context)
    sqlContext.setConf("hive.exec.dynamic.partition", "true")
    sqlContext.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
    val read_query = """select userid,movieid,rating from movies.movie_user_ratings"""
    val movieData = sqlContext.sql(read_query).rdd.map(f => dataMapper(f.mkString("\t")))
    val userMappedRDD = movieData.map(f => (f.movieID,1)).reduceByKey((map1, map2) => map1 + map2)   
    val rowsRDD= userMappedRDD.map(f => {
      var MovieID = f._1
      var NumberOfRatings = f._2
      Row(MovieID,NumberOfRatings)
      })
    val output = sqlContext.createDataFrame(rowsRDD, resultSchema)
    output.registerTempTable("run_ratings")
    val OutputQuery = """
      INSERT INTO movies.movie_use_case1_numberofratings SELECT MovieID, NumberOfRatings FROM run_ratings"""
	    println("Output Query : " + OutputQuery);
    sqlContext.sql(OutputQuery);
    }
}