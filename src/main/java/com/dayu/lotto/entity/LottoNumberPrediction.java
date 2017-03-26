package com.dayu.lotto.entity;

import org.springframework.data.annotation.Id;

public abstract class LottoNumberPrediction {
	private String drawNumber;
	
	@Id
	public String getDrawNumber() {
		return drawNumber;
	}
	public void setDrawNumber(String drawNumber) {
		this.drawNumber = drawNumber;
	}
	
	protected abstract String getLottoName();
	
}
