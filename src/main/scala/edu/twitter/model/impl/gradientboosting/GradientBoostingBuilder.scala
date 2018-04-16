package edu.twitter.model.impl.gradientboosting

import java.io.File

import com.typesafe.scalalogging.Logger
import edu.twitter.config.AppConfig
import edu.twitter.model.api.{GenericModel, GenericModelBuilder}
import edu.twitter.model.evaluation.ModelEvaluator
import edu.twitter.model.impl.TweetsLoader
import org.apache.spark.SparkContext
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.GradientBoostedTrees
import org.apache.spark.mllib.tree.configuration.BoostingStrategy
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD

/**
  * Build and evaluate a gradient boosting model from training and testing data set.
  */
class GradientBoostingBuilder(sc: SparkContext)(implicit appConfig: AppConfig) extends GenericModelBuilder {
  private val logger = Logger(classOf[GradientBoostingModel])
  private val modelPath = appConfig.paths.savedGradientBoostingModelPath

  /**
    * Build a GradientBoosting Classification model using a Gradient Boosting model
    * if it's not already saved in the specified directory, otherwise load it directly.
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
    if (checkModelExist()) {
      logger.info("The model is already trained, load it directly")
      return new GradientBoostingModel(GradientBoostedTreesModel.load(sc, modelPath))
    }

    val tweetsLoader = new TweetsLoader(sc)
    val twitterData = new SentimentModelDataCreator(tweetsLoader.loadDataSet(appConfig.paths.trainingDataPath))
    val (trainingSet, testSet) = twitterData.getTrainingAndTestingData()

    val boostingStrategy = BoostingStrategy.defaultParams("Classification")
    boostingStrategy.setNumIterations(appConfig.gradientIterations)
    boostingStrategy.treeStrategy.setNumClasses(2)
    boostingStrategy.treeStrategy.setMaxDepth(appConfig.gradientDepth)

    val model = GradientBoostedTrees.train(trainingSet, boostingStrategy)
    evaluate(model, trainingSet, "Training")
    evaluate(model, testSet, "Testing")
    model.save(sc, modelPath)
    if (appConfig.evaluateModels) {
      new ModelEvaluator(sc).evaluate(GradientBoostingModel.name)
    }

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

    logger.info(s"================ $setType ==================")
    val metrics = new MulticlassMetrics(predictionAndLabels)

    val accuracy = metrics.accuracy
    logger.info("Summary Statistics")
    logger.info(s"Accuracy = $accuracy")

    metrics.labels.foreach { l =>
      logger.info(s"Precision($l) = ${metrics.precision(l)}")
      logger.info(s"Recall($l) = ${metrics.recall(l)}")
      logger.info(s"FPR($l) = ${metrics.falsePositiveRate(l)}")
      logger.info(s"F1-Score($l) = ${metrics.fMeasure(l)}")
    }
  }

  private def checkModelExist(): Boolean = {
    val file = new File(modelPath)
    file.exists()
  }
}
