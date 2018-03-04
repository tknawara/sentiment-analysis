package edu.twitter.model.impl.gradientboosting

import java.io.File

import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import edu.twitter.model.impl.TweetsLoader
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD

/**
  * Build and evaluate a gradient boosting model from training and testing data set.
  */
class GradientBoostingBuilder(sc: SparkContext) extends GenericModelBuilder {

  val modelPath =  this.getClass().getClassLoader().getResource("saved-models").getPath() + File.separator + "GradientBoosting"

  /**
    * Create a GradientBoosting Classification model using a Gradient Boosting model.
    * The reason we chose Gradient Boosting for classification over some other model
    * is because it’s easy to use (doesn’t require tons of parameter tuning), and it
    * tends to have a high classification accuracy. For this reason it is frequently
    * used in machine learning competitions.
    * The tuning parameters we’re using here are:
    * -number of iterations (passes over the data)
    * -Max Depth of each decision tree
    *
    * @return GenericModel
    */
  def build(): GenericModel = {

    if(checkModelExist()){
      return new GradientBoostingModel(GradientBoostedTreesModel.load(sc, modelPath))
    }

    val tweetsLoader = new TweetsLoader(sc)
    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val twitterData = new SentimentModelDataCreator(tweetsLoader.loadDataSet(dataPath))
    val (trainingSet, testSet) = twitterData.getTrainingAndTestingData()

    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.setNumIterations(20)
    boostingStrategy.treeStrategy.setNumClasses(2)
    boostingStrategy.treeStrategy.setMaxDepth(5)

    val model = GradientBoostedTrees.train(trainingSet, boostingStrategy)
    evaluate(model, trainingSet, "Training")
    evaluate(model, testSet, "Testing")
    model.save(sc, modelPath)
    new GradientBoostingModel(model)
  }

  /**
    * Evaluate the `GradientBoostingModel`.
    *
    * @param model   target model for evaluation
    * @param data    data used in evaluation
    * @param setType type of the data used for evaluation
    */
  private def evaluate(model: GradientBoostedTreesModel, data: RDD[LabeledPoint], setType: String): Unit = {
    val predictionAndLabels = data.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    println(s"================ $setType ==================")
    val metrics = new MulticlassMetrics(predictionAndLabels)

    val accuracy = metrics.accuracy
    println("Summary Statistics")
    println(s"Accuracy = $accuracy")

    metrics.labels.foreach { l =>
      println(s"Precision($l) = ${metrics.precision(l)}")
      println(s"Recall($l) = ${metrics.recall(l)}")
      println(s"FPR($l) = ${metrics.falsePositiveRate(l)}")
      println(s"F1-Score($l) = ${metrics.fMeasure(l)}")
    }
 }

  private def checkModelExist(): Boolean = {
    val file = new File(modelPath)
    return file.exists()
  }
}
