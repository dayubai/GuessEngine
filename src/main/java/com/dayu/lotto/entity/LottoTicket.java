package com.dayu.lotto.entity;

import java.math.BigDecimal;
import java.util.List;

public abstract class LottoTicket {
	private String id;
    private int draw;
    
    private BigDecimal prize;
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}



	public int getDraw() {
		return draw;
	}



	public void setDraw(int draw) {
		this.draw = draw;
	}



	



	public BigDecimal getPrize() {
		return prize;
	}



	public void setPrize(BigDecimal prize) {
		this.prize = prize;
	}

   
}
