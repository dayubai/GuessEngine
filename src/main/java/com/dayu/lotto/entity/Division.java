package com.dayu.lotto.entity;

import java.math.BigDecimal;

public class Division implements Comparable<Division>{
    private int prize;
    private BigDecimal amount;
    
	public Division() {
		super();
	}
	public Division(int prize, BigDecimal amount) {
		super();
		this.prize = prize;
		this.amount = amount;
	}
	public int getPrize() {
		return prize;
	}
	public void setPrize(int prize) {
		this.prize = prize;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public int compareTo(Division o) {
		return prize - o.getPrize();
	}
    
}
