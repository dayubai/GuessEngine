package com.dayu.lotto.algorithm;


public interface GuessPredictor<M> {
	
	double predict(M model, JavaDocument testData);

}
