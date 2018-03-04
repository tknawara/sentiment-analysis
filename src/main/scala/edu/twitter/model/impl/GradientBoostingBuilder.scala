package edu.twitter.model.impl

import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.evaluation.{ModelEvaluator, Record}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.rdd.RDD

/**
  * Build and evaluate a gradient boosting model from training and testing data set.
  */
class GradientBoostingBuilder(sc: SparkContext) extends GenericModelBuilder {

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
    implicit def toRecords(data: RDD[(String, LabeledPoint)]): RDD[Record] = data.map { case (msg, p) => Record(msg, p.label) }
    implicit def toLabeledPoints(data: RDD[(String, LabeledPoint)]): RDD[LabeledPoint] = data.map(_._2)

    val tweetsLoader = new TweetsLoader(sc)
    val dataPath = this.getClass.getClassLoader.getResource("labeled-tweets").getPath
    val twitterData = new SentimentModelDataCreator(tweetsLoader.loadDataSet(dataPath))
    val (trainingSet, testSet) = twitterData.getTrainingAndTestingData()

    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.setNumIterations(20)
    boostingStrategy.treeStrategy.setNumClasses(2)
    boostingStrategy.treeStrategy.setMaxDepth(5)

    val model = GradientBoostedTrees.train(trainingSet, boostingStrategy)
    val genericModel = new GradientBoostingModel(model)
    val modelEvaluator = new ModelEvaluator(sc)
    modelEvaluator.evaluate(genericModel, trainingSet, testSet)
    genericModel
  }
}
