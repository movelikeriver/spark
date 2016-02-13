/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Usage:
 *
 *  sbt package
 *
 *  spark-submit --class "org.apache.spark.examples.mllib.NaiveBayesExample"
 *    --master local[4]
 *    examples/target/scala-2.11/spark-examples_2.11-2.0.0-SNAPSHOT.jar
 */

// scalastyle:off println
package org.apache.spark.examples.mllib

import org.apache.spark.{SparkConf, SparkContext}
// $example on$
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

import java.io.File;
import org.apache.commons.io.FileUtils;
// $example off$

object NaiveBayesExample {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("NaiveBayesExample")
    val sc = new SparkContext(conf)
    // $example on$
    val data = sc.textFile("data/mllib/sample_naive_bayes_data.txt")
    val parsedData = data.map { line =>
      val parts = line.split(',')
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(' ').map(_.toDouble)))
    }

    // Split data into training (60%) and test (40%).
    val splits = parsedData.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)

    val model = NaiveBayes.train(training, lambda = 1.0, modelType = "multinomial")

    val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
    val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
    println("model accuracy %f".format(accuracy))

    // Save and load model
    val outputDir = "target/tmp/myNaiveBayesModel"
    FileUtils.forceDelete(new File(outputDir))
    model.save(sc, outputDir)
    val sameModel = NaiveBayesModel.load(sc, outputDir)

    val samePredictionAndLabel = test.map(p => (sameModel.predict(p.features), p.label))
    val sameAccuracy = 1.0 * samePredictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
    println("sameModel accuracy %f".format(sameAccuracy))
    // $example off$
  }
}

// scalastyle:on println
