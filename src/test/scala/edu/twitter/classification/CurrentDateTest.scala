package edu.twitter.classification

import java.text.SimpleDateFormat
import java.util.Date

object CurrentDateTest {
  def main(args: Array[String]): Unit = {
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val date = new Date()
    println(dateFormat.format(date)) //2016/11/16 12:08:43
  }
}
