package edu.twitter.model

import edu.twitter.model_api.{GenericModel, GenericModelBuilder}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy

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
    val tweetsLoader = new TweetsLoader(sc)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.getTweetsDataSet())
    val (trainingSet, testSet) = twitterData.getTrainingAndTestingData()

    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.setNumIterations(20) //number of passes over our training data
    boostingStrategy.treeStrategy.setNumClasses(2) //We have two output classes: happy and sad
    boostingStrategy.treeStrategy.setMaxDepth(5)
    //Depth of each tree. Higher numbers mean more parameters, which can cause overfitting.
    //Lower numbers create a simpler model, which can be more accurate.
    //In practice you have to tweak this number to find the best value.

    var model = GradientBoostedTrees.train(trainingSet, boostingStrategy)
    new GradientBoostingModel(model)
  }
}
