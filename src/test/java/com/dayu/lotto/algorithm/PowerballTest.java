package com.dayu.lotto.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dayu.lotto.TestAppConfig;
import com.dayu.lotto.entity.PowerBallResult;
import com.dayu.lotto.entity.PowerBallTicket;
import com.dayu.lotto.service.LottoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class PowerballTest {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private LottoService<PowerBallTicket, PowerBallResult> powerBallService;
	
	private static Logger log = LoggerFactory.getLogger(PowerballTest.class);
	
	@Test
	public void testPowerBallUploadResult()
	{		
		if (mongoTemplate.collectionExists(PowerBallResult.class))
    		mongoTemplate.dropCollection(PowerBallResult.class);
		
		try {
			URL resourceUrl = getClass().getResource("/results/Powerball.csv");
			Path resourcePath = Paths.get(resourceUrl.toURI());
			File file = resourcePath.toFile();
			powerBallService.uploadResults(new FileInputStream(file));
			
		} catch (Exception e) {
			throw new AssertionError(e);
		} 
	}
	
	@Test
	public void testWeightedSelector() {
		
		WeightedSelector selector = new WeightedSelector();
		
		int limit = 45;
		
		List<PowerBallResult> results = powerBallService.findLast(limit);
		List<Integer> allDraws = new ArrayList<Integer>();
		for (PowerBallResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());
		}
		
		assertEquals(limit * powerBallService.numberToPick(), allDraws.size());
		
		
		Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);
		
		int totalWeight = 0;
		for (int key: model.keySet()) {
			totalWeight += model.get(key);
			System.out.println("number: " + key + ", weight: " + model.get(key));
		}
		
		assertEquals(limit * powerBallService.numberToPick(), totalWeight);
		
		System.out.println(selector.select(powerBallService.numberToPick(), model, totalWeight));
		
	}
	
	@Test
	public void testPowerBallLottoDraw() {		
		
		powerBallService.draw(new WeightedSelector(), 953, 12);
		
	}
	
	@Test
	public void testCheckTicket() {		
		
		for (PowerBallTicket powerBallTicket : powerBallService.findTicketsByDraw(953))
		{
			powerBallService.checkTicket(powerBallTicket);
		}
	}
	
	/*@Test
	public void testFindTopPrize()
	{
		SaturdayLottoResult lottoResult = saturdayLottoService.findResultByDraw(3449);
		
		int drawToTest = 3455;
		SaturdayLottoResult lottoResult = new SaturdayLottoResult();
		lottoResult.setDrawDate(new Date());
		lottoResult.setDrawNumber(3455);
		lottoResult.setWinningNumbers(Arrays.asList(new Integer[] {24,37,44,14,2,40}));
		lottoResult.setSupplementaryNumbers(Arrays.asList(new Integer[] {33,18}));
		lottoResult.setDivisions(Arrays.asList(new Division[] {new Division(1, BigDecimal.valueOf(1000000))}));
		int i = 0;
		
		
		while(true)
		{
			if (mongoTemplate.collectionExists(SaturdayLottoTicket.class))
				mongoTemplate.dropCollection(SaturdayLottoTicket.class);

			saturdayLottoService.draw(new WeightedSelector(), drawToTest, 50);
		
			i += 50;
			
			for (SaturdayLottoTicket saturdayLottoTicket : saturdayLottoService.findTicketsByDraw(drawToTest))
			{
				for (SaturdayLottoTicket.Result result : saturdayLottoTicket.getResults())
				{
					int winNumbers = 0;
					for (int n : result.getNumbers())
					{
						if (lottoResult.getWinningNumbers().contains(n))
							winNumbers++;
					}
					
					if (winNumbers == 5)
					{
						log.info("1st prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(0).getAmount());
						return;
					}
				}				
			}
			
			log.info(String.valueOf(i));
				
		}
	}*/
}
