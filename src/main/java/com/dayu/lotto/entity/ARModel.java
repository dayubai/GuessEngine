package com.dayu.lotto.entity;

public class ARModel {

	private double label;
	
	private Double[] trainingSet;
	
	public double getLabel() {
		return label;
	}
	public void setLabel(double label) {
		this.label = label;
	}
	public Double[] getTrainingSet() {
		return trainingSet;
	}
	public void setTrainingSet(Double[] trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("Label: " + label + ", [");
		
		for (double b: trainingSet)
		{
			sb.append(b + ",");
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	
}
