package edu.twitter.model.evaluation

import edu.twitter.model.api.GenericModel
import edu.twitter.model.impl.TweetsLoader
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.elasticsearch.spark.rdd.EsSpark

/** Representation of the records used for training
  * and testing the model. */
case class Record(tweetText: String, actualLabel: Double)

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
    val tweetsLoader = new TweetsLoader(sc)
    val evaluation = evaluateData(model, tweetsLoader.getTweetsDataSet())
    performEvaluationAnalysis(evaluation)
  }

  /**
    * Given a model and its training, testing data. this method
    * will measure how will the model will perform.
    *
    * @param model             target model for evaluation
    * @param trainingRecords   model's training data
    * @param validationRecords model's validation data
    */
  def evaluate(model: GenericModel, trainingRecords: RDD[Record], validationRecords: RDD[Record]): Unit = {
    val trainingEvaluation = evaluateRecords(model, trainingRecords)
    val validationEvaluation = evaluateRecords(model, validationRecords)
    performEvaluationAnalysis(trainingEvaluation, "Training")
    performEvaluationAnalysis(validationEvaluation)
  }

  /**
    * Given a model will evaluate it based on
    * training data, log evaluation analysis and
    * persist the labeled records in `Elasticsearch`
    *
    * @param model target model for evaluation
    */
  def evaluateAndPersist(model: GenericModel): Unit = {
    val tweetsLoader = new TweetsLoader(sc)
    val evaluation = evaluateData(model, tweetsLoader.getTweetsDataSet())
    performEvaluationAnalysis(evaluation)
    EsSpark.saveToEs(evaluation, "training-analysis")
  }

  /**
    * Show how the model will perform against the given data.
    *
    * @param model target model for evaluation
    * @param data  evaluation data
    * @return rdd of `EvaluatedTrainingTweet`
    */
  private def evaluateData(model: GenericModel, data: RDD[Row]): RDD[EvaluatedTrainingTweet] = {
    val transformedData = for {
      row <- data
      actualLabel = row.getAs[Double]("label")
      tweetText = row.getAs[String]("msg")
    } yield Record(tweetText, actualLabel)

    evaluateRecords(model, transformedData)
  }

  /**
    * Evaluate all the testing data.
    *
    * @param model target model for evaluation
    * @return rdd of `EvaluatedTrainingTweet` instances.
    */
  private def evaluateRecords(model: GenericModel, data: RDD[Record]): RDD[EvaluatedTrainingTweet] = {
    val evaluation = for {
      r <- data
      modelPrediction = model.getLabel(r.tweetText)
    } yield EvaluatedTrainingTweet(r.actualLabel, modelPrediction, r.tweetText)

    evaluation
  }

  /**
    * Perform basic analysis over the model's classification.
    *
    * @param evaluation classified tweets
    */
  private def performEvaluationAnalysis(evaluation: RDD[EvaluatedTrainingTweet], dataSetType: String = "Testing"): Unit = {
    val e = evaluation.aggregate(EvaluationFields(0, 0, 0, 0))(
      (e, t) => (t.actualLabel, t.modelPrediction) match {
        case (1, 1) => e.copy(happyCorrect = e.happyCorrect + 1, happyTotal = e.happyTotal + 1)
        case (1, 0) => e.copy(happyTotal = e.happyTotal + 1)
        case (0, 1) => e.copy(sadTotal = e.sadTotal + 1)
        case (0, 0) => e.copy(sadCorrect = e.sadCorrect + 1, sadTotal = e.sadTotal + 1)
      },
      (e1, e2) => e1 + e2
    )

    println(s"=============== $dataSetType Evaluation ==================")
    println(s"sad messages=${e.sadTotal}, happy messages=${e.happyTotal}")
    println(s"happy % correct=${e.happyCorrect.toDouble / e.happyTotal}")
    println(s"sad % correct=${e.sadCorrect.toDouble / e.sadTotal}")

    val recordCount = evaluation.count()
    val testErr = evaluation.filter(t => t.actualLabel != t.modelPrediction).count.toDouble / recordCount
    println(s"data size=$recordCount")
    println(s"Test Error=$testErr")
  }
}
