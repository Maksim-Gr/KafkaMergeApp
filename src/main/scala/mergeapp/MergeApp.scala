package org.example
package mergeapp

import org.apache.spark.sql.SparkSession

object MergeApp:
  @main def run(): Unit =
    val spark = SparkSession.builder
      .appName("MergeStreamsApp")
