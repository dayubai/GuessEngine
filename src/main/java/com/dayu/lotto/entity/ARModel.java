package com.dayu.lotto.entity;

public class ARModel {

	private Boolean label;
	private Boolean[] trainingSet;
	
	public Boolean getLabel() {
		return label;
	}
	public void setLabel(Boolean label) {
		this.label = label;
	}
	public Boolean[] getTrainingSet() {
		return trainingSet;
	}
	public void setTrainingSet(Boolean[] trainingSet) {
		this.trainingSet = trainingSet;
	}
	
	@Override
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append("Label: " + label + ", [");
		
		for (Boolean b: trainingSet)
		{
			sb.append(b + ",");
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	
}
