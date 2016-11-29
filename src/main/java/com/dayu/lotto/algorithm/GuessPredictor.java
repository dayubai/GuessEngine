package com.dayu.lotto.algorithm;

import org.apache.spark.ml.Model;

public interface GuessPredictor<M> {
	
	double predict(M model, JavaDocument testData);

}
