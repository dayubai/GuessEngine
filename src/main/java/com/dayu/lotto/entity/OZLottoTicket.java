package com.dayu.lotto.entity;

import java.math.BigDecimal;
import java.util.List;


public class OZLottoTicket extends LottoTicket {
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
		private Double prediction;

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
		public Double getPrediction() {
			return prediction;
		}
		public void setPrediction(Double prediction) {
			this.prediction = prediction;
		}

	}
}
