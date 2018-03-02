package edu.twitter.evaluation

import edu.twitter.model.{GradientBoostingModel, TweetsLoader}
import org.apache.spark.SparkContext
import org.apache.spark.mllib.feature.HashingTF

case class EvaluationFields(happyCorrect: Int, happyTotal: Int, sadCorrect: Int, sadTotal: Int) {

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

class ModelEvaluator(sc: SparkContext) {
  def evaluate(model: GradientBoostingModel#GenericModel): Unit = {
    val tweetsLoader = new TweetsLoader(sc)
    val hashingTF = new HashingTF(2000)

    val evaluation = for {
      row <- tweetsLoader.getTweetsDataSet()
      actualLabel = row.getAs[Double]("label")
      tokens = row.getAs[String]("msg").split(" ").toSeq
      features = hashingTF.transform(tokens)
      modelPrediction = model(features)
    } yield (actualLabel, modelPrediction)


    val e = evaluation.aggregate(EvaluationFields(0, 0, 0, 0))(
      (e, p) => p match {
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
    var testErr = evaluation.filter(r => r._1 != r._2).count.toDouble / recordCount
    println("data size=" + recordCount)
    println("Test Error " + testErr)
  }

}
