package org.example
package mergeapp

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.functions.split
import org.apache.spark.sql.{DataFrame, Dataset, functions}
import org.apache.spark.sql.SparkSession

object MergeApp:
  @main def run(): Unit =
    val spark = SparkSession.builder
      .appName("MergeStreamsApp")
      .config("spark.master", "local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    def readFromKafka(topic:String):DataFrame = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe",topic)
      .option("StartingOffsets", "earliest")
      .load()
      .selectExpr("CAST(Value as String)").as[String]
      .splitKafkaValue()

    implicit class dsUtils[T](ds: Dataset[T]) {
      def consolePrint(mode:String): Unit = ds
        .writeStream
        .format("console")
        .outputMode(mode)
        .start()
        .awaitTermination()


      def splitKafkaValue(): DataFrame = ds
        .withColumn("split_", split(col("value"), " "))
        .select(
          col("split_").getItem(0).as("id"),
          col("split_").getItem(1).as("name"),
          col("split_").getItem(2).as("time")
        )
    }

    val kafkaStream1 = readFromKafka("stream1")
    val kafkaStream2 = readFromKafka("stream2")

    val predicate = kafkaStream1.col("id") === kafkaStream2.col("id")
    kafkaStream1.join(kafkaStream2,predicate).consolePrint("append")
    kafkaStream1.consolePrint("append")


