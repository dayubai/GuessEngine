package com.dayu.lotto.entity;

import java.util.List;

import com.dayu.lotto.entity.SaturdayLottoTicket.Result;

public class SaturdayLottoPrediction extends LottoNumberPrediction {
    private List<SinglePredictionObject> predictionObjects;

	@Override
	public String getLottoName() {
		return "SatturdayLotto";
	}


	public List<SinglePredictionObject> getPredictionObjects() {
		return predictionObjects;
	}


	public void setPredictionObjects(List<SinglePredictionObject> predictionObjects) {
		this.predictionObjects = predictionObjects;
	}

	public SinglePredictionObject newSinglePredictionObject()
	{
		return new SinglePredictionObject();
	}

	public class SinglePredictionObject 
    {
    	private String numbers;
    	private Object probability;
    	private double prediction;
		public String getNumbers() {
			return numbers;
		}
		public void setNumbers(String numbers) {
			this.numbers = numbers;
		}
		public Object getProbability() {
			return probability;
		}
		public void setProbability(Object probability) {
			this.probability = probability;
		}
		public double getPrediction() {
			return prediction;
		}
		public void setPrediction(double prediction) {
			this.prediction = prediction;
		}    	   	
    }
}
