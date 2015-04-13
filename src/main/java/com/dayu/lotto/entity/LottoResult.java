package com.dayu.lotto.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;

public abstract class LottoResult {
	private int drawNumber;
    private Date drawDate;
    private List<Integer> winningNumbers;
    private List<Division> divisions;
    
    @Id
	public int getDrawNumber() {
		return drawNumber;
	}
	public void setDrawNumber(int drawNumber) {
		this.drawNumber = drawNumber;
	}
	public Date getDrawDate() {
		return drawDate;
	}
	public void setDrawDate(Date drawDate) {
		this.drawDate = drawDate;
	}
	public List<Integer> getWinningNumbers() {
		return winningNumbers;
	}
	public void setWinningNumbers(List<Integer> winningNumbers) {
		this.winningNumbers = winningNumbers;
	}
	public List<Division> getDivisions() {
		return divisions;
	}
	public void setDivisions(List<Division> divisions) {
		this.divisions = divisions;
	}
}
