package com.innovativeintelli.rating.dbconnection

import java.sql.DriverManager
import java.sql.Connection

object HiveConnection {

  def getHiveConnection():Connection = {
    val driver = "org.apache.hive.jdbc.HiveDriver"
    val url = "jdbc:hive2://127.0.0.1:10000/movies" //usually you will be using knox url along with ssl certificate
    val username = "maria_dev"
    val password = "maria_dev"

    // there's probably a better way to do this
    var connection:Connection = null

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      // create the statement, and run the select query
//      val statement = connection.createStatement()
//      val resultSet = statement.executeQuery("select userid,movieid from movie_user_ratings limit 1")
//      while ( resultSet.next() ) {
//        val userid = resultSet.getString("userid")
//        val movieid = resultSet.getString("movieid")
//        println("host, user = " + userid + ", " + movieid)
    } catch {
      case e  => e.printStackTrace
    }
   return connection
  }
  
  def closeConnection(connection: Connection) {
    connection.close()
  }

}