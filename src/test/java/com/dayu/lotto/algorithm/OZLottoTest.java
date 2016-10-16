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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dayu.lotto.TestAppConfig;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.OZLottoTicket;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.OZLottoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class OZLottoTest {
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private OZLottoService ozLottoService;

	private static Logger log = LoggerFactory.getLogger(OZLottoTest.class);

	@Test
	public void testOZUploadResult()
	{		
		if (mongoTemplate.collectionExists(OZLottoResult.class))
			mongoTemplate.dropCollection(OZLottoResult.class);

		try {
			URL resourceUrl = getClass().getResource("/results/OzLotto.csv");
			Path resourcePath = Paths.get(resourceUrl.toURI());
			File file = resourcePath.toFile();
			ozLottoService.uploadResults(new FileInputStream(file));

		} catch (Exception e) {
			throw new AssertionError(e);
		} 
	}

	@Test
	public void testWeightedSelector() {

		WeightedSelector selector = new WeightedSelector();

		int limit = 45;

		List<OZLottoResult> results = ozLottoService.findLast(limit);
		List<Integer> allDraws = new ArrayList<Integer>();
		for (OZLottoResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());

			allDraws.addAll(result.getSupplementaryNumbers());
		}

		assertEquals(limit * (ozLottoService.numberToPick() + ozLottoService.supplementaries()), allDraws.size());


		Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);

		int totalWeight = 0;
		for (int key: model.keySet()) {
			totalWeight += model.get(key);
			System.out.println("number: " + key + ", weight: " + model.get(key));
		}

		assertEquals(limit * (ozLottoService.numberToPick() + ozLottoService.supplementaries()), totalWeight);

		System.out.println(selector.select(ozLottoService.numberToPick(), model, totalWeight));

	}

	@Test
	public void testOZLottoDraw() {		

		ozLottoService.draw(new WeightedSelector(), 1071, 100000);

	}

	@Test
	public void testFindTopPrize()
	{
		int drawToTest = 1173;
		OZLottoResult lottoResult = new OZLottoResult();
		lottoResult.setDrawDate(new Date());
		lottoResult.setDrawNumber(1173);
		lottoResult.setWinningNumbers(Arrays.asList(new Integer[] {26,13,33,2,18,11,41}));
		lottoResult.setSupplementaryNumbers(Arrays.asList(new Integer[] {34,6}));
		lottoResult.setDivisions(Arrays.asList(new Division[] {new Division(1, BigDecimal.valueOf(1000000))}));
		int i = 0;


		while(true)
		{
			if (mongoTemplate.collectionExists(SaturdayLottoTicket.class))
				mongoTemplate.dropCollection(SaturdayLottoTicket.class);

			ozLottoService.draw(new WeightedSelector(), drawToTest, 50);

			i += 50;

			for (OZLottoTicket ozLottoTicket : ozLottoService.findTicketsByDraw(drawToTest))
			{
				for (OZLottoTicket.Result result : ozLottoTicket.getResults())
				{
					int winNumbers = 0;
					for (int n : result.getNumbers())
					{
						if (lottoResult.getWinningNumbers().contains(n))
							winNumbers++;
					}

					if (winNumbers == 6)
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

	@Test
	public void testCheckTicket() {		

		for (OZLottoTicket powerBallTicket : ozLottoService.findTicketsByDraw(1071))
		{
			ozLottoService.checkTicket(powerBallTicket);
		}
	}
}
