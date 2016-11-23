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

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.mllib.fpm.AssociationRules;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
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
	
	private static JavaSparkContext sparkCtx;
	@Before
	public void init() throws IllegalArgumentException, IOException {
		System.setProperty("hadoop.home.dir", getClass().getResource("/hadoop").getPath());
		//ctxtBuilder = new ContextBuilder(tempFolder);
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("junit");
		sparkCtx = new JavaSparkContext(conf);  
		/*SparkConf conf = new SparkConf().setAppName("App_Name")
    .setMaster("spark://localhost:18080").set("spark.ui.port","18080"); */  
	}

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
	
	@Test
	public void testOZLottoModelSelectionViaCrossValidationExample()
	{
		
		SparkSession spark = SparkSession
				.builder()
				.appName("OZLottoModelSelectionViaCrossValidationExample")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(ozLottoService.listWinnngNumbers(20), JavaLabeledDocument.class);
				/*List<Row> dataTraining = Arrays.asList(
						RowFactory.create(1.0, Vectors.dense(0.0, 1.1, 0.1)),
						RowFactory.create(0.0, Vectors.dense(2.0, 1.0, -1.0)),
						RowFactory.create(0.0, Vectors.dense(2.0, 1.3, 1.0)),
						RowFactory.create(1.0, Vectors.dense(0.0, 1.2, -0.5))
						);
				StructType schema = new StructType(new StructField[]{
						new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
						new StructField("features", new VectorUDT(), false, Metadata.empty())
				});
				Dataset<Row> training = spark.createDataFrame(dataTraining, schema);*/

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
		.setNumFolds(2);  // Use 3+ in practice

		// Run cross-validation, and choose the best set of parameters.
		CrossValidatorModel cvModel = cv.fit(training);
		//cvModel.save(getClass().getResource("/model/").getPath().concat("OZLottoRegressionModel"));

		// Prepare test documents, which are unlabeled.
		Dataset<Row> test = spark.createDataFrame(Arrays.asList(
				new JavaDocument(1L, "3 4 5 9 10 25 39"),
				new JavaDocument(2L, "44 2 1 45 26 6 36"),
				new JavaDocument(3L, "4 2 1 5 6 7 3"),
				new JavaDocument(3L, "1 2 3 4 5 6 7"),
				new JavaDocument(4L, "20 8 37 393 165 0 101"),
				new JavaDocument(5L, "20 8 37 33 5 34 36"),
				new JavaDocument(6L, "18 22 20 35 16 26 7")
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
	
	@Test
	public void testOZAssociationRulesExample()
	{
		SparkSession spark = SparkSession
		.builder()
		.appName("OZAssociationRulesExample")
		.getOrCreate();

		// $example on$
	    JavaRDD<FPGrowth.FreqItemset<String>> freqItemsets = sparkCtx.parallelize(Arrays.asList(
	      new FreqItemset<String>(new String[] {"a"}, 15L),
	      new FreqItemset<String>(new String[] {"b"}, 35L),
	      new FreqItemset<String>(new String[] {"a", "b"}, 12L)
	    ));

	    AssociationRules arules = new AssociationRules()
	      .setMinConfidence(0.7);
	    JavaRDD<AssociationRules.Rule<String>> results = arules.run(freqItemsets);

	    for (AssociationRules.Rule<String> rule : results.collect()) {
	      System.out.println(
	        rule.javaAntecedent() + " => " + rule.javaConsequent() + ", " + rule.confidence());
	    }
	    // $example off$

	    spark.stop();
	}
	
	@Test
	public void testSaveOZLottoModelSelectionViaCrossValidation() throws IOException
	{
		
		SparkSession spark = SparkSession
				.builder()
				.appName("SaveOZLottoModelSelectionViaCrossValidation")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(ozLottoService.listWinnngNumbers(20), JavaLabeledDocument.class);

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
		.setNumFolds(2);  // Use 3+ in practice

		// Run cross-validation, and choose the best set of parameters.
		CrossValidatorModel cvModel = cv.fit(training);
		cvModel.save(getClass().getResource("/model/").getPath().concat("OZLottoRegressionModel"));

		// $example off$

		spark.stop();
	}
}
