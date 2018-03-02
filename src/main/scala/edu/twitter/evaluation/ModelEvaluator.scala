package edu.twitter.evaluation

import edu.twitter.model.TweetsLoader
import edu.twitter.model_api.GenericModel
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.rdd.RDD
import org.elasticsearch.spark.rdd.EsSpark

/** Grouping of the evaluation necessary fields
  * these fields are used for evaluating the training error. */
case class EvaluationFields(happyCorrect: Int, happyTotal: Int, sadCorrect: Int, sadTotal: Int) {

  /**
    * Combining two evaluation instances.
    *
    * @param o instance to combine with this one.
    * @return a new instance resulting of the combination of
    *         both instances.
    */
  def +(o: EvaluationFields): EvaluationFields = o match {
    case EvaluationFields(oHappyCorrect, oHappyTotal, oSadCorrect, oSadTotal) =>
      EvaluationFields(
        happyCorrect + oHappyCorrect,
        happyTotal + oHappyTotal,
        sadCorrect + oSadCorrect,
        sadTotal + oSadTotal
      )
  }
}

/** Representation of a training tweet that has been classified
  * by the model, it holds both the actual and model labels. */
case class EvaluatedTrainingTweet(actualLabel: Double, modelPrediction: Double, tweetText: String)

/**
  * Responsible for evaluating a model based on a given
  * testing data. this class will print training analysis
  * in the console and persist the labeled data in `Elasticsearch`
  * for further visualization.
  *
  * @param sc spark context.
  */
class ModelEvaluator(sc: SparkContext) {
  /**
    * Given a model will evaluate it based on
    * training data and log evaluation analysis.
    *
    * @param model target model for evaluation.
    */
  def evaluate(model: GenericModel): Unit = {
    val evaluation = evaluateData(model)
    performEvaluationAnalysis(evaluation)
  }

  /**
    * Given a model will evaluate it based on
    * training data, log evaluation analysis and
    * persist the labeled records in `Elasticsearch`
    *
    * @param model target model for evaluation
    */
  def evaluateAndPersist(model: GenericModel): Unit = {
    val evaluation = evaluateData(model)
    performEvaluationAnalysis(evaluation)
    EsSpark.saveToEs(evaluation, "training-analysis")
  }

  /**
    * Evaluate all the testing data.
    *
    * @param model target model for evaluation
    * @return rdd of `EvaluatedTrainingTweet` instances.
    */
  private def evaluateData(model: GenericModel): RDD[EvaluatedTrainingTweet] = {
    val tweetsLoader = new TweetsLoader(sc)
    val hashingTF = new HashingTF(2000)

    val evaluation = for {
      row <- tweetsLoader.getTweetsDataSet()
      actualLabel = row.getAs[Double]("label")
      tweetText = row.getAs[String]("msg")
      tokens = tweetText.split(" ").toSeq
      features = hashingTF.transform(tokens)
      modelPrediction = model(features)
    } yield EvaluatedTrainingTweet(actualLabel, modelPrediction, tweetText)

    evaluation
  }

  /**
    * Perform basic analysis over the model's classification.
    *
    * @param evaluation classified tweets
    */
  private def performEvaluationAnalysis(evaluation: RDD[EvaluatedTrainingTweet]): Unit = {
    val e = evaluation.aggregate(EvaluationFields(0, 0, 0, 0))(
      (e, t) => (t.actualLabel, t.modelPrediction) match {
        case (1, 1) => e.copy(happyCorrect = e.happyCorrect + 1, happyTotal = e.happyTotal + 1)
        case (1, 0) => e.copy(happyTotal = e.happyTotal + 1)
        case (0, 1) => e.copy(sadTotal = e.sadTotal + 1)
        case (0, 0) => e.copy(sadCorrect = e.sadCorrect + 1, sadTotal = e.sadTotal + 1)
      },
      (e1, e2) => e1 + e2
    )

    println("sad messages= " + e.sadTotal + " happy messages: " + e.happyTotal)
    println("happy % correct: " + e.happyCorrect.toDouble / e.happyTotal)
    println("sad % correct: " + e.sadCorrect.toDouble / e.sadTotal)

    val recordCount = evaluation.count()
    val testErr = evaluation.filter(t => t.actualLabel != t.modelPrediction).count.toDouble / recordCount
    println("data size=" + recordCount)
    println("Test Error " + testErr)
  }
}
