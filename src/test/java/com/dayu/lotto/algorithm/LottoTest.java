package com.dayu.lotto.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dayu.lotto.TestAppConfig;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.SaturdayLottoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})

public class LottoTest {
	@Autowired
	private SaturdayLottoService saturdayLottoService;
	
	@Autowired
	private LottoDAO  saturdayLottoDAO;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private static Logger log = LoggerFactory.getLogger(LottoTest.class);

	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testSaturdayLottoUploadResult()
	{		
		if (mongoTemplate.collectionExists(SaturdayLottoResult.class))
    		mongoTemplate.dropCollection(SaturdayLottoResult.class);
		
		try {
			URL resourceUrl = getClass().getResource("/results/LottoSaturday.csv");
			Path resourcePath = Paths.get(resourceUrl.toURI());
			File file = resourcePath.toFile();
			saturdayLottoService.uploadResults(new FileInputStream(file));
			
		} catch (Exception e) {
			throw new AssertionError(e);
		} 
	}
	
	@Test
	public void testWeightedSelector() {
		
		WeightedSelector selector = new WeightedSelector();
		
		int limit = 15;
		
		List<SaturdayLottoResult> results = saturdayLottoService.findLast(limit);
		List<Integer> allDraws = new ArrayList<Integer>();
		for (SaturdayLottoResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());
			
			allDraws.addAll(result.getSupplementaryNumbers());
		}
		
		assertEquals(limit * (saturdayLottoService.numberToPick() + saturdayLottoService.supplementaries()), allDraws.size());
		
		
		Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);
		
		int totalWeight = 0;
		for (int key: model.keySet()) {
			totalWeight += model.get(key);
			System.out.println("number: " + key + ", weight: " + model.get(key));
		}
		
		assertEquals(limit * (saturdayLottoService.numberToPick() + saturdayLottoService.supplementaries()), totalWeight);
		
		System.out.println(selector.select(saturdayLottoService.numberToPick(), model, totalWeight));
		
	}
	
	@Test
	public void testSaturdayLottoDraw() {		
		/*if (mongoTemplate.collectionExists(SaturdayLottoTicket.class))
    		mongoTemplate.dropCollection(SaturdayLottoTicket.class);*/
		
		saturdayLottoService.draw(new WeightedSelector(), 3459, 10);
		
		/*for (SaturdayLottoTicket saturdayLottoTicket : saturdayLottoService.findByDraw(3449))
		{
			saturdayLottoService.checkTicket(saturdayLottoTicket);
		}*/
	}
	
	@Test
	@Ignore
	public void testUpdatePrize()
	{
		saturdayLottoDAO.updateTicketPrize("53e972a6f2deae057bb1dc99", BigDecimal.TEN, SaturdayLottoTicket.class);
	}
	
	@Test
	public void testFindTopPrize()
	{
		/*SaturdayLottoResult lottoResult = saturdayLottoService.findResultByDraw(3449);*/
		
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
	}

}
