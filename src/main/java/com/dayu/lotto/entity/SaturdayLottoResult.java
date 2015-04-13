package com.dayu.lotto.entity;

import java.util.List;

public class SaturdayLottoResult extends LottoResult {
	
    private List<Integer> supplementaryNumbers;

	public List<Integer> getSupplementaryNumbers() {
		return supplementaryNumbers;
	}

	public void setSupplementaryNumbers(List<Integer> supplementaryNumbers) {
		this.supplementaryNumbers = supplementaryNumbers;
	}
}
