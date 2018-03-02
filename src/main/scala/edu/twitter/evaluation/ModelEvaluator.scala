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


    val EvaluationFields(happyCorrect, happyTotal, sadCorrect, sadTotal) =
      evaluation.aggregate(EvaluationFields(0, 0, 0, 0))(
        (evaluationFields, p) => (evaluationFields, p) match {
          case (e, (1, 1)) => evaluationFields.copy(happyCorrect = evaluationFields.happyCorrect + 1, happyTotal = evaluationFields.happyTotal + 1)
          case (e, (1, 0)) => evaluationFields.copy(happyCorrect = evaluationFields.happyCorrect + 1, happyTotal = evaluationFields.happyTotal + 1)
          case (e, (0, 1)) => evaluationFields.copy(happyCorrect = evaluationFields.happyCorrect + 1, happyTotal = evaluationFields.happyTotal + 1)
          case (e, (0, 0)) => evaluationFields.copy(happyCorrect = evaluationFields.happyCorrect + 1, happyTotal = evaluationFields.happyTotal + 1)
        },
        (e1, e2) => e1 + e2
      )

    println("sad messages= " + sadTotal + " happy messages: " + happyTotal)
    println("happy % correct: " + happyCorrect.toDouble / happyTotal)
    println("sad % correct: " + sadCorrect.toDouble / sadTotal)

    val recordCount = evaluation.count()
    var testErr = evaluation.filter(r => r._1 != r._2).count.toDouble / recordCount
    println("data size=" + recordCount)
    println("Test Error " + testErr)
  }

}
