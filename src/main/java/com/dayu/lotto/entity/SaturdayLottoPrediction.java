package com.dayu.lotto.entity;

import java.util.List;

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
    	private String number;
    	private String probability;
    	private double prediction;
		public String getNumber() {
			return number;
		}
		public void setNumber(String number) {
			this.number = number;
		}
		public String getProbability() {
			return probability;
		}
		public void setProbability(String probability) {
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
