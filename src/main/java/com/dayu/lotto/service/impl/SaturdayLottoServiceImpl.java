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

import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dayu.lotto.algorithm.JavaLabeledDocument;
import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.SaturdayLottoService;

@Service
public class SaturdayLottoServiceImpl implements SaturdayLottoService {

	private static Logger log = LoggerFactory.getLogger(SaturdayLottoServiceImpl.class);
    private static final int SAMPLE = 15;
	
	@Autowired
	private LottoDAO lottoDAO;

	public void uploadResults(InputStream is) throws Exception {
		BufferedReader reader = null;
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		try {
			reader = new BufferedReader (new InputStreamReader(is));
			String line;
			int lineNumber = 1;
			while ((line = reader.readLine()) != null)
			{
				if (lineNumber > 1)
				{
					SaturdayLottoResult saturdayLottoResult = new SaturdayLottoResult();

					String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

					// remove quotes
					for (int i =0; i < columns.length ; i++)
					{
						columns[i] = columns[i].replaceAll("\"", "");
					}

					//Draw Number,Draw Date (yyyymmdd),Winning Number 1,2,3,4,5,6,Supplementary 1,2,Division 1,2,3,4,5,6
					if (columns.length == 10)
					{
						Date drawDate = df.parse(columns[1]);

						saturdayLottoResult.setDrawNumber(Integer.parseInt(columns[0]));
						saturdayLottoResult.setDrawDate(drawDate);
						List<Integer> winningNumbers = new ArrayList<Integer>();

						winningNumbers.add(Integer.parseInt(columns[2]));
						winningNumbers.add(Integer.parseInt(columns[3]));
						winningNumbers.add(Integer.parseInt(columns[4]));
						winningNumbers.add(Integer.parseInt(columns[5]));
						winningNumbers.add(Integer.parseInt(columns[6]));
						winningNumbers.add(Integer.parseInt(columns[7]));

						Collections.sort(winningNumbers);
						saturdayLottoResult.setWinningNumbers(winningNumbers);

						List<Integer> supplementaryNumbers = new ArrayList<Integer>();

						supplementaryNumbers.add(Integer.parseInt(columns[8]));

						if (!columns[9].equals("-"))
							supplementaryNumbers.add(Integer.parseInt(columns[9]));


						Collections.sort(supplementaryNumbers);
						saturdayLottoResult.setSupplementaryNumbers(supplementaryNumbers);

						lottoDAO.save(saturdayLottoResult);
					}					
					else if (columns.length == 15)
					{
						Date drawDate = df.parse(columns[1]);

						saturdayLottoResult.setDrawNumber(Integer.parseInt(columns[0]));
						saturdayLottoResult.setDrawDate(drawDate);
						List<Integer> winningNumbers = new ArrayList<Integer>();

						winningNumbers.add(Integer.parseInt(columns[2]));
						winningNumbers.add(Integer.parseInt(columns[3]));
						winningNumbers.add(Integer.parseInt(columns[4]));
						winningNumbers.add(Integer.parseInt(columns[5]));
						winningNumbers.add(Integer.parseInt(columns[6]));
						winningNumbers.add(Integer.parseInt(columns[7]));

						Collections.sort(winningNumbers);
						saturdayLottoResult.setWinningNumbers(winningNumbers);

						List<Integer> supplementaryNumbers = new ArrayList<Integer>();

						supplementaryNumbers.add(Integer.parseInt(columns[8]));
						supplementaryNumbers.add(Integer.parseInt(columns[9]));

						Collections.sort(supplementaryNumbers);
						saturdayLottoResult.setSupplementaryNumbers(supplementaryNumbers);

						List<Division> divisions = new ArrayList<Division>();
						divisions.add(new Division(1, BigDecimal.valueOf(Double.parseDouble(columns[10].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(2, BigDecimal.valueOf(Double.parseDouble(columns[11].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(3, BigDecimal.valueOf(Double.parseDouble(columns[12].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(4, BigDecimal.valueOf(Double.parseDouble(columns[13].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(5, BigDecimal.valueOf(Double.parseDouble(columns[14].replaceAll("[^\\d.]+", "")))));
						Collections.sort(divisions);
						saturdayLottoResult.setDivisions(divisions);
						
						lottoDAO.save(saturdayLottoResult);
					}
					else if (columns.length == 16)
					{
						Date drawDate = df.parse(columns[1]);

						saturdayLottoResult.setDrawNumber(Integer.parseInt(columns[0]));
						saturdayLottoResult.setDrawDate(drawDate);
						List<Integer> winningNumbers = new ArrayList<Integer>();

						winningNumbers.add(Integer.parseInt(columns[2]));
						winningNumbers.add(Integer.parseInt(columns[3]));
						winningNumbers.add(Integer.parseInt(columns[4]));
						winningNumbers.add(Integer.parseInt(columns[5]));
						winningNumbers.add(Integer.parseInt(columns[6]));
						winningNumbers.add(Integer.parseInt(columns[7]));

						Collections.sort(winningNumbers);
						saturdayLottoResult.setWinningNumbers(winningNumbers);

						List<Integer> supplementaryNumbers = new ArrayList<Integer>();

						supplementaryNumbers.add(Integer.parseInt(columns[8]));
						supplementaryNumbers.add(Integer.parseInt(columns[9]));

						Collections.sort(supplementaryNumbers);
						saturdayLottoResult.setSupplementaryNumbers(supplementaryNumbers);

						List<Division> divisions = new ArrayList<Division>();
						divisions.add(new Division(1, BigDecimal.valueOf(Double.parseDouble(columns[10].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(2, BigDecimal.valueOf(Double.parseDouble(columns[11].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(3, BigDecimal.valueOf(Double.parseDouble(columns[12].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(4, BigDecimal.valueOf(Double.parseDouble(columns[13].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(5, BigDecimal.valueOf(Double.parseDouble(columns[14].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(6, BigDecimal.valueOf(Double.parseDouble(columns[15].replaceAll("[^\\d.]+", "")))));
						Collections.sort(divisions);
						saturdayLottoResult.setDivisions(divisions);
						
						lottoDAO.save(saturdayLottoResult);
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

	public List<SaturdayLottoResult> findLast(int limit) {
		return lottoDAO.findLastResults(limit, SaturdayLottoResult.class);
	}

	public int numberToPick() {
		return 6;
	}

	public int pool() {
		return 45;
	}

	public String draw(WeightedSelector selector, int draw, int games) {
		
		List<SaturdayLottoResult> results = findLast(SAMPLE);

		// all numbers collected from results
		List<Integer> allDraws = new ArrayList<Integer>();
		for (SaturdayLottoResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());
			
			allDraws.addAll(result.getSupplementaryNumbers());
		}


		List<SaturdayLottoTicket.Result> ticketResults = new ArrayList<SaturdayLottoTicket.Result>();

		// build model and predict
		for (int i = 0; i < games; i++)
		{
			SaturdayLottoTicket.Result ticketResult = new SaturdayLottoTicket().newResult();
			
			Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);


			int totalWeight = 0;
			for (int key: model.keySet()) {
				totalWeight += model.get(key);
			}

			ticketResult.setNumbers(selector.select(numberToPick(), model, totalWeight));
			
			ticketResults.add(ticketResult);
		}

		SaturdayLottoTicket ticket = new SaturdayLottoTicket();
		ticket.setDraw(draw);
		ticket.setResults(ticketResults);

		return lottoDAO.save(ticket);
	}

	public List<SaturdayLottoTicket> findTicketsByDraw(int draw) {
		return lottoDAO.findTicketsByDraw(draw, SaturdayLottoTicket.class);
	}

	public void checkTicket(LottoTicket lottoTicket) {

		SaturdayLottoTicket saturdayLottoTicket = (SaturdayLottoTicket) lottoTicket;
		
		SaturdayLottoResult lottoResult = findResultByDraw(saturdayLottoTicket.getDraw());

		if (lottoResult != null)
		{
			BigDecimal prize = BigDecimal.ZERO;
			for (SaturdayLottoTicket.Result result : saturdayLottoTicket.getResults())
			{
				int winNumbers = 0;
				int supNumbers = 0;
				
				for (int n : result.getNumbers())
				{
					if (lottoResult.getWinningNumbers().contains(n))
						winNumbers++;
					else if (lottoResult.getSupplementaryNumbers().contains(n))
						supNumbers++;
				}
				if (winNumbers == 6)
				{
					log.info("1st prize: " + result.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(0).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(0).getAmount());
				}
				else if (winNumbers == 5)
				{
					if (supNumbers > 0)
					{
						log.info("2nd prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(1).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(1).getAmount());
					}
					else
					{
						log.info("3rd prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(2).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(2).getAmount());
					}
				}
				else if (winNumbers == 4)
				{
					log.info("4th prize: " + result.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(3).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(3).getAmount());
				}
				else if (winNumbers == 3)
				{
					if (supNumbers > 0)
					{
						log.info("5th prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(4).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(4).getAmount());
					}
				}
				else if (winNumbers == 1 || winNumbers == 2)
				{
					if(supNumbers == 2)
					{
						log.info("6th prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(5).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(5).getAmount());
					}
				}
			}
			lottoDAO.updateTicketPrize(saturdayLottoTicket.getId(), prize, SaturdayLottoTicket.class);
		}
	}

	public SaturdayLottoResult findResultByDraw(int draw) {
		return lottoDAO.findResultByDraw(draw, SaturdayLottoResult.class);
	}

	public List<SaturdayLottoTicket> findAllTickets() {
		return lottoDAO.findAllTickets(SaturdayLottoTicket.class);
	}

	public int supplementaries() {
		return 2;
	}

	public SaturdayLottoTicket findByTicketId(String id) {
		return lottoDAO.findTicketById(id, SaturdayLottoTicket.class);
	}

	@Override
	public List<JavaLabeledDocument> buildTrainingData(int max) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
