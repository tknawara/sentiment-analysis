package edu.twitter.model

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD

/**
  * Build and evaluate a gradient boosting model from training and testing data set.
  * @param trainingData
  * @param validationData
  */
class GradientBoostingModel(trainingData: RDD[LabeledPoint], validationData: RDD[LabeledPoint]) {

  type GenericModel = (org.apache.spark.mllib.linalg.Vector => Double)

  /**
    * Create a GradientBoosting Classification model using a Gradient Boosting model.
    * The reason we chose Gradient Boosting for classification over some other model
    * is because it’s easy to use (doesn’t require tons of parameter tuning), and it
    * tends to have a high classification accuracy. For this reason it is frequently
    * used in machine learning competitions.
    * The tuning parameters we’re using here are:
        -number of iterations (passes over the data)
        -Max Depth of each decision tree
    * @return sentiment classification model
    */
  def createModel(): GenericModel = {
    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.setNumIterations(20) //number of passes over our training data
    boostingStrategy.treeStrategy.setNumClasses(2) //We have two output classes: happy and sad
    boostingStrategy.treeStrategy.setMaxDepth(5)
    //Depth of each tree. Higher numbers mean more parameters, which can cause overfitting.
    //Lower numbers create a simpler model, which can be more accurate.
    //In practice you have to tweak this number to find the best value.

    val model = GradientBoostedTrees.train(trainingData, boostingStrategy)
    evaluate(model)
    (features: org.apache.spark.mllib.linalg.Vector) => model.predict(features)
  }

  /**
    * Evaluate the model by printing some analysis.
    * @param model model needed to be evaluated
    */
  private def evaluate(model: GradientBoostedTreesModel): Unit = {
    //Evaluation
    // Evaluate model on test instances and compute test error
    var labelAndPredsTrain = trainingData.map { point =>
      val prediction = model.predict(point.features)
      Tuple2(point.label, prediction)
    }

    var labelAndPredsValid = validationData.map { point =>
      val prediction = model.predict(point.features)
      Tuple2(point.label, prediction)
    }

    //Since Spark has done the heavy lifting already, lets pull the results back to the driver machine.
    //Calling collect() will bring the results to a single machine (the driver) and will convert it to a Scala array.

    //Start with the Training Set
    printEvaluation(labelAndPredsTrain, "Training", trainingData)
    //Compute error for validation Set
    printEvaluation(labelAndPredsValid, "Validation", validationData)
  }

  /**
    * Print evaluation of model giving labeled data.
    * @param labelAndPreds labeled data (0 sad , 1 happy)
    * @param setType is data training or validation data
    */
  private def printEvaluation(labelAndPreds: RDD[(Double, Double)], setType: String, data: RDD[LabeledPoint]): Unit = {
    var results = labelAndPreds.collect()

    var happyTotal = 0
    var unhappyTotal = 0
    var happyCorrect = 0
    var unhappyCorrect = 0
    results.foreach(
      r => {
        if (r._1 == 1) {
          happyTotal += 1
        } else if (r._1 == 0) {
          unhappyTotal += 1
        }
        if (r._1 == 1 && r._2 ==1) {
          happyCorrect += 1
        } else if (r._1 == 0 && r._2 == 0) {
          unhappyCorrect += 1
        }
      }
    )
    println("unhappy messages in " + setType + " Set: " + unhappyTotal + " happy messages: " + happyTotal)
    println("happy % correct: " + happyCorrect.toDouble/happyTotal)
    println("unhappy % correct: " + unhappyCorrect.toDouble/unhappyTotal)

    var testErr = labelAndPreds.filter(r => r._1 != r._2).count.toDouble / data.count()
    println("data size=" + data.count())
    println("Test Error " + setType + " Set: " + testErr)
  }
}
