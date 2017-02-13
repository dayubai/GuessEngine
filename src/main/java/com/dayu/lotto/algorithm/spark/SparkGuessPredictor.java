package com.dayu.lotto.algorithm.spark;

import java.util.Arrays;

import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import com.dayu.lotto.algorithm.GuessPredictor;
import com.dayu.lotto.algorithm.JavaDocument;

public class SparkGuessPredictor implements GuessPredictor <CrossValidatorModel> {

	public double predict(CrossValidatorModel model, JavaDocument testData) {
		SparkSession spark = SparkSession
				.builder()
				.appName("SaveOZLottoModelSelectionViaCrossValidation")
				.getOrCreate();
		
		Dataset<Row> test = spark.createDataFrame(Arrays.asList(testData), JavaDocument.class);

		// Make predictions on test documents. cvModel uses the best model found (lrModel).
		Dataset<Row> predictions = model.transform(test);
		
		return Double.valueOf(predictions.select("prediction").first().get(0).toString());
	}
	

}
