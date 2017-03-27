package com.dayu.lotto.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dayu.lotto.algorithm.JavaLabeledDocument;
import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.entity.PowerBallPrediction;
import com.dayu.lotto.entity.PowerBallResult;
import com.dayu.lotto.entity.PowerBallTicket;

@Service
public class PowerBallServiceImpl extends AbstractLottoService<PowerBallTicket, PowerBallResult, PowerBallPrediction> {

	@Autowired
	private LottoDAO lottoDAO;
	
	private static Logger log = LoggerFactory.getLogger(PowerBallServiceImpl.class);
    private static final int SAMPLE = 15;
	
	public void uploadResults(InputStream is) throws Exception {
		BufferedReader reader = null;
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			reader = new BufferedReader (new InputStreamReader(is));
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null)
			{
				if (lineNumber > 878)
				{
					PowerBallResult powerBallResult = new PowerBallResult();

					String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

					// remove quotes
					for (int i =0; i < columns.length ; i++)
					{
						columns[i] = columns[i].replaceAll("\"", "");
					}

					if (columns.length == 17)
					{
						Date drawDate = df.parse(columns[1]);

						powerBallResult.setDrawNumber(Integer.parseInt(columns[0]));
						powerBallResult.setDrawDate(drawDate);
						List<Integer> winningNumbers = new ArrayList<Integer>();

						winningNumbers.add(Integer.parseInt(columns[2]));
						winningNumbers.add(Integer.parseInt(columns[3]));
						winningNumbers.add(Integer.parseInt(columns[4]));
						winningNumbers.add(Integer.parseInt(columns[5]));
						winningNumbers.add(Integer.parseInt(columns[6]));
						winningNumbers.add(Integer.parseInt(columns[7]));

						Collections.sort(winningNumbers);
						powerBallResult.setWinningNumbers(winningNumbers);

						powerBallResult.setPowerball(Integer.parseInt(columns[8]));

						List<Division> divisions = new ArrayList<Division>();
						divisions.add(new Division(1, BigDecimal.valueOf(Double.parseDouble(columns[9].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(2, BigDecimal.valueOf(Double.parseDouble(columns[10].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(3, BigDecimal.valueOf(Double.parseDouble(columns[11].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(4, BigDecimal.valueOf(Double.parseDouble(columns[12].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(5, BigDecimal.valueOf(Double.parseDouble(columns[13].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(6, BigDecimal.valueOf(Double.parseDouble(columns[14].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(7, BigDecimal.valueOf(Double.parseDouble(columns[15].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(8, BigDecimal.valueOf(Double.parseDouble(columns[16].replaceAll("[^\\d.]+", "")))));
						Collections.sort(divisions);
						powerBallResult.setDivisions(divisions);

						lottoDAO.save(powerBallResult);
					}
					else
					{
						log.error(" line " + lineNumber + " is illigal with length " + columns.length);
					}
				}
				lineNumber++;
			}
		}
		finally
		{
			reader.close();
			is.close();
		}
	}

	public int numberToPick() {
		return 6;
	}

	public int pool() {
		return 40;
	}

	public int powerballPool() {
		return 20;
	}

	public List<PowerBallResult> findLast(int limit) {
		return lottoDAO.findLastResults(limit, PowerBallResult.class);
	}

	public String draw(WeightedSelector selector, int draw, int games) {
		List<PowerBallResult> results = findLast(SAMPLE);

		// all numbers collected from results
		List<Integer> allDraws = new ArrayList<Integer>();
		List<Integer> allPowerBalls = new ArrayList<Integer>();
		for (PowerBallResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());
			
			allPowerBalls.add(result.getPowerball());
		}


		List<PowerBallTicket.Result> ticketResults = new ArrayList<PowerBallTicket.Result>();

		// build model and predict
		for (int i = 0; i < games; i++)
		{
			PowerBallTicket.Result ticketResult = new PowerBallTicket().newResult();
			
			Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);
			
			Map<Integer, Integer> powerBallModel = selector.buildWeightingModel(allPowerBalls);


			int totalWeight = 0;
			int totalPowerballWeight = 0;
			
			for (int key: model.keySet()) {
				totalWeight += model.get(key);
			}
			
			for (int key: powerBallModel.keySet()) {
				totalPowerballWeight += powerBallModel.get(key);
			}

			ticketResult.setNumbers(selector.select(numberToPick(), model, totalWeight));
			ticketResult.setPowerball(selector.select(1, powerBallModel, totalPowerballWeight).get(0));
			
			ticketResults.add(ticketResult);
		}

		PowerBallTicket ticket = new PowerBallTicket();
		ticket.setDraw(draw);
		ticket.setResults(ticketResults);

		return lottoDAO.save(ticket);
	}

	public List<PowerBallTicket> findTicketsByDraw(int draw) {
		return lottoDAO.findTicketsByDraw(draw, PowerBallTicket.class);
	}

	public void checkTicket(LottoTicket lottoTicket) {
		PowerBallTicket powerBallTicket = (PowerBallTicket) lottoTicket;
		
		PowerBallResult lottoResult = findResultByDraw(powerBallTicket.getDraw());

		if (lottoResult != null)
		{
			BigDecimal prize = BigDecimal.ZERO;
			for (PowerBallTicket.Result ticketResult : powerBallTicket.getResults())
			{
				int winNumbers = 0;
				boolean powerball = ticketResult.getPowerball() == lottoResult.getPowerball();
				
				for (int n : ticketResult.getNumbers())
				{
					if (lottoResult.getWinningNumbers().contains(n))
						winNumbers++;
				}
				if (winNumbers == 6 && powerball)
				{
					log.info("1st prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(0).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(0).getAmount());
				}
				else if (winNumbers == 6)
				{
					log.info("2nd prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(1).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(1).getAmount());
				}
				else if (winNumbers == 5 && powerball)
				{
					log.info("3rd prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(2).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(2).getAmount());
				}
				else if (winNumbers == 5)
				{
					log.info("4th prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(3).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(3).getAmount());
				}
				else if (winNumbers == 4 && powerball)
				{
					log.info("5th prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(4).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(4).getAmount());
				}
				else if (winNumbers == 3 && powerball)
				{
					log.info("6th prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(5).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(5).getAmount());
				}
				else if (winNumbers == 4)
				{
					log.info("7th prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(6).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(6).getAmount());
				}
				else if (winNumbers == 2 && powerball)
				{
					log.info("8th prize: " + ticketResult.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(7).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(7).getAmount());
				}
			}
			lottoDAO.updateTicketPrize(powerBallTicket.getId(), prize, PowerBallTicket.class);
		}
	}

	public PowerBallResult findResultByDraw(int draw) {
		return lottoDAO.findResultByDraw(draw, PowerBallResult.class);
	}

	public List<PowerBallTicket> findAllTickets() {
		return lottoDAO.findAllTickets(PowerBallTicket.class);
	}

	public int supplementaries() {
		return 0;
	}

	public PowerBallTicket findByTicketId(String id) {
		return lottoDAO.findTicketById(id, PowerBallTicket.class);
	}

	@Override
	public List<JavaLabeledDocument> buildTrainingData(int max) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void generateNumberPredictions(String drawNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<PowerBallResult> findLastResultsFromDraw(int draw, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PowerBallPrediction> findAllForestRandomPredictions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PowerBallPrediction findForestRandomPredictionByDraw(int draw) {
		// TODO Auto-generated method stub
		return null;
	}


	

}
