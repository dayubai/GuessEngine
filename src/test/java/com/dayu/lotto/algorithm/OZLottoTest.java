package com.dayu.lotto.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.dayu.lotto.TestAppConfig;
import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.OZLottoTicket;
import com.dayu.lotto.service.OZLottoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class OZLottoTest {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private OZLottoService ozLottoService;
	
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
	public void testCheckTicket() {		
		
		for (OZLottoTicket powerBallTicket : ozLottoService.findTicketsByDraw(1071))
		{
			ozLottoService.checkTicket(powerBallTicket);
		}
	}
}
