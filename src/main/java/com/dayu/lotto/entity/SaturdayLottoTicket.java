package com.dayu.lotto.entity;

import java.math.BigDecimal;
import java.util.List;


public class SaturdayLottoTicket extends LottoTicket {
	private List<Result> results;

	public List<Result> getResults() {
		return results;
	}



	public void setResults(List<Result> results) {
		this.results = results;
	}

	public Result newResult()
	{
		return new Result();
	}

	public class Result
	{
		private List<Integer> numbers;
		private BigDecimal prize;

		public List<Integer> getNumbers() {
			return numbers;
		}
		public void setNumbers(List<Integer> numbers) {
			this.numbers = numbers;
		}
		public BigDecimal getPrize() {
			return prize;
		}
		public void setPrize(BigDecimal prize) {
			this.prize = prize;
		}

	}
}
