package com.dayu.lotto.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
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
	
	private static JavaSparkContext sparkCtx;
	
	@Before
	public void init() throws IllegalArgumentException, IOException {
		//System.setProperty("hadoop.home.dir", getClass().getResource("/hadoop").getPath());
		//ctxtBuilder = new ContextBuilder(tempFolder);
		/*SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("junit");
		sparkCtx = new JavaSparkContext(conf);  
		SparkConf conf = new SparkConf().setAppName("App_Name")
    .setMaster("spark://localhost:18080").set("spark.ui.port","18080");   */
	}

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
	
	@Test
	public void testLottoModelSelectionViaCrossValidationExample()
	{
		
		SparkSession spark = SparkSession
				.builder()
				.appName("SaturdayLottoModelSelectionViaCrossValidationExample")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(saturdayLottoService.buildTrainingData(15), JavaLabeledDocument.class);

		// Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
		Tokenizer tokenizer = new Tokenizer()
		.setInputCol("text")
		.setOutputCol("words");
		HashingTF hashingTF = new HashingTF()
		.setNumFeatures(1000)
		.setInputCol("words")
		.setOutputCol("features");
		LogisticRegression lr = new LogisticRegression()
		.setMaxIter(3)
		.setRegParam(0.01);
		
		Pipeline pipeline = new Pipeline()
		.setStages(new PipelineStage[] {tokenizer, hashingTF, lr});

		// We use a ParamGridBuilder to construct a grid of parameters to search over.
		// With 3 values for hashingTF.numFeatures and 2 values for lr.regParam,
		// this grid will have 3 x 2 = 6 parameter settings for CrossValidator to choose from.
		ParamMap[] paramGrid = new ParamGridBuilder()
		.addGrid(hashingTF.numFeatures(), new int[] {100, 1000})
		.addGrid(lr.regParam(), new double[] {0.1, 0.01})
		.build();

		// We now treat the Pipeline as an Estimator, wrapping it in a CrossValidator instance.
		// This will allow us to jointly choose parameters for all Pipeline stages.
		// A CrossValidator requires an Estimator, a set of Estimator ParamMaps, and an Evaluator.
		// Note that the evaluator here is a BinaryClassificationEvaluator and its default metric
		// is areaUnderROC.
		CrossValidator cv = new CrossValidator()
		.setEstimator(pipeline)
		.setEvaluator(new BinaryClassificationEvaluator())
		.setEstimatorParamMaps(paramGrid)
		.setNumFolds(5);  // Use 3+ in practice

		// Run cross-validation, and choose the best set of parameters.
		CrossValidatorModel cvModel = cv.fit(training);
		//cvModel.save(getClass().getResource("/model/").getPath().concat("OZLottoRegressionModel"));

		// Prepare test documents, which are unlabeled.
		Dataset<Row> test = spark.createDataFrame(Arrays.asList(
				new JavaDocument(1L, "3 4 5 9 10 25"),
				new JavaDocument(2L, "44 2 1 45 26 6"),
				new JavaDocument(3L, "4 2 1 5 6 7"),
				new JavaDocument(3L, "1 2 7 4 5 6"),
				new JavaDocument(4L, "20 8 37 393 165 101"),
				new JavaDocument(5L, "20 8 37 33 5 34"),
				new JavaDocument(6L, "18 22 20 35 16 26")
				), JavaDocument.class);

		// Make predictions on test documents. cvModel uses the best model found (lrModel).
		Dataset<Row> predictions = cvModel.transform(test);
		for (Row r : predictions.select("id", "text", "probability", "prediction").collectAsList()) {
			System.out.println("(" + r.get(0) + ", " + r.get(1) + ") --> prob=" + r.get(2)
					+ ", prediction=" + r.get(3));
		}
		// $example off$

		spark.stop();
	}

}
