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
import com.dayu.lotto.entity.PowerBallResult;
import com.dayu.lotto.entity.PowerBallTicket;
import com.dayu.lotto.service.PowerBallService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class PowerballTest {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private PowerBallService powerBallService;
	
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
}
