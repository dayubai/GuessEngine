package com.dayu.lotto.entity;

public abstract class LottoNumberPrediction {
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	protected abstract String getLottoName();
	
}
