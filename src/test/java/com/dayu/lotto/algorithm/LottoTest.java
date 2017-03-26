package com.dayu.lotto.algorithm;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.RandomForestClassificationModel;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.regression.RandomForestRegressionModel;
import org.apache.spark.ml.regression.RandomForestRegressor;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.api.java.function.Function;
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

import scala.Tuple2;

import com.dayu.lotto.TestAppConfig;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.ARModel;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.LottoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})

public class LottoTest {
	@Autowired
	private LottoService<SaturdayLottoTicket, SaturdayLottoResult> saturdayLottoService;

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
				new JavaDocument(6L, "22 16 34 36 38 4")
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
	public void testWriteAllNumber() throws IOException
	{
		String filename = getClass().getResource("/results").getPath() + "/AllLottoNumber.csv";

		FileWriter fw = new FileWriter(filename, true);
		PrintWriter pw = new PrintWriter(fw);


		List<Integer> l1 = new ArrayList<Integer>();
		List<Integer> l2 = new ArrayList<Integer>();
		List<Integer> l3 = new ArrayList<Integer>();
		List<Integer> l4 = new ArrayList<Integer>();
		List<Integer> l5 = new ArrayList<Integer>();
		List<Integer> l6 = new ArrayList<Integer>();



		for (int i = 1; i <= 45; i++)
		{
			l1.add(i);
			l2.add(i);
			l3.add(i);
			l4.add(i);
			l5.add(i);
			l6.add(i);
		}

		List<Integer> temp = new ArrayList<Integer>(6);
		for (int i1 = 0; i1 < 45; i1++)
		{
			int n1 = l1.get(i1);
			temp.add(n1);

			for (int i2 = 0; i2<45; i2++)
			{
				if (!temp.contains(l2.get(i2)))
				{
					temp.add(l2.get(i2));

					for (int i3 =0; i3<45; i3++)
					{
						if (!temp.contains(l3.get(i3)))
						{
							temp.add(l3.get(i3));
							for (int i4=0;i4<45;i4++)
							{
								if (!temp.contains(l4.get(i4)))
								{
									temp.add(l4.get(i4));
									for (int i5=0; i5<45;i5++)
									{
										if (!temp.contains(i5))
										{
											temp.add(l5.get(i5));
											for (int i6=0; i6<45;i6++)
											{
												if (!temp.contains(l6.get(i6)))
												{
													temp.add(l6.get(i6));
													// print temp
													pw.println(temp.get(0) + " " + temp.get(1) + " " + temp.get(2) + " " + temp.get(3) + " " + temp.get(4) + " " + temp.get(5));
													pw.flush();

													temp.remove(5);
												}
											}
											temp.remove(4);
										}
									}
									temp.remove(3);
								}
							}
							temp.remove(2);
						}
					}
					temp.remove(1);
				}
			}
			temp.remove(0);
		}

		pw.close();
	}

	/*private void addNumberToTemp(List<Integer> numbers, List <Integer> temp, PrintWriter pw)
	{
		for (int i = 0; i < numbers.size(); i++)
		{
			if (!temp.contains(numbers.get(i)))
			{
				temp.add(numbers.get(i));

				if (temp.size() <6)
				{
					addNumberToTemp(numbers, temp, pw);
				}
				else
				{
					for (Integer n : temp)
					{
						pw.print(n);
						pw.print(" ");
					}
					pw.println();
				}
			}




			List<Integer> temp = new ArrayList<Integer>(6);
			int n1 = numbers.get(i);
			temp.add(n1);

			for (int i2 = 0; i2<45; i2++)
			{
				if (!temp.contains(l2.get(i2)))
				{
					temp.add(l2.get(i2));

					for (int i3 =0; i3<45; i3++)
				}
			}
		}

	}*/

	@Test
	public void testBuildARModelToDB() {
		saturdayLottoService.generateNumberPredictions("3715");
	}
	
	@Test
	public void testBuildARModel() throws IOException
	{

		String filename = getClass().getResource("/results").getPath() + "/ARModel.csv";
		
		FileWriter fw = new FileWriter(filename, true);
		PrintWriter pw = new PrintWriter(fw);
		
		
		int historyCapacity = 5;
		List<SaturdayLottoResult> results = saturdayLottoService.findLastResultsFromDraw(3715, 50);
		Map<Integer, List<ARModel>> arModels = new TreeMap<Integer, List<ARModel>>();

		Map<Integer, Queue<Double>> resultMap = new TreeMap<Integer, Queue<Double>>();
		for (int i=1; i<=45;i++)
		{
			arModels.put(i, new ArrayList<ARModel>());

			Queue<Double> q = new ArrayBlockingQueue<Double>(historyCapacity);
			resultMap.put(i, q);
		}



		for (SaturdayLottoResult result : results)
		{
			List<Integer> winningNumbers = result.getWinningNumbers();

			for (int i=1;i<=45;i++)
			{
				if (resultMap.get(i).size() == historyCapacity)
				{
					//generate model
					ARModel arModel = new ARModel();
					arModel.setLabel(winningNumbers.contains(i)?1.0:0.0);
					Double[] arr = new Double[historyCapacity];
					resultMap.get(i).toArray(arr);

					arModel.setTrainingSet(arr);

					arModels.get(i).add(arModel);

					//remove first
					resultMap.get(i).poll();
				}

				// insert new result
				resultMap.get(i).offer(winningNumbers.contains(i)?1.0:0.0);
			}

		}

		Map<Integer, ARModel> testDataMap=new TreeMap<Integer,ARModel>();

		//print
		for (int key: arModels.keySet())
		{
			ARModel testData = new ARModel();
			Double[] arr = new Double[historyCapacity];
			resultMap.get(key).toArray(arr);

			testData.setLabel(1);
			testData.setTrainingSet(arr);


			testDataMap.put(key, testData);

			
				System.out.println(key + ": " + testDataMap.get(key).toString());
		

		}


		/**
		 **  ML Model
		 */
		//Build RandomForrest Model
		SparkSession spark = SparkSession
				.builder()
				.appName("RandomForestClassifierExample")
				.getOrCreate();

		for (int trainingNumber = 1; trainingNumber <= 45; trainingNumber++) 
		{



			// Create some vector data; also works for sparse vectors
			List<Row> rows = new ArrayList<Row>();
			for (ARModel arModel : arModels.get(trainingNumber))
			{	
				rows.add(RowFactory.create(arModel.getLabel(), Vectors.dense(ArrayUtils.toPrimitive( arModel.getTrainingSet()))));
			}

			List<StructField> fields = new ArrayList<>();
			fields.add(DataTypes.createStructField("label", DataTypes.DoubleType, false));
			fields.add(DataTypes.createStructField("features", new VectorUDT(), false));

			StructType schema = DataTypes.createStructType(fields);

			Dataset<Row> data = spark.createDataFrame(rows, schema);

			Dataset<Row> test = spark.createDataFrame(Arrays.asList(RowFactory.create(testDataMap.get(trainingNumber).getLabel(), Vectors.dense(ArrayUtils.toPrimitive( testDataMap.get(trainingNumber).getTrainingSet())))),schema);

			// Index labels, adding metadata to the label column.
			// Fit on whole dataset to include all labels in index.
			StringIndexerModel labelIndexer = new StringIndexer()
			.setInputCol("label")
			.setOutputCol("indexedLabel")
			.fit(data);

			// Automatically identify categorical features, and index them.
			// Set maxCategories so features with > 4 distinct values are treated as continuous.

			VectorIndexerModel featureIndexer = new VectorIndexer()
			.setInputCol("features")
			.setOutputCol("indexedFeatures")
			.setMaxCategories(3)
			.fit(data);

			// Train a RandomForest model.
			RandomForestClassifier rf = new RandomForestClassifier()
			.setLabelCol("indexedLabel")
			.setFeaturesCol("indexedFeatures");

			// Convert indexed labels back to original labels.
			IndexToString labelConverter = new IndexToString()
			.setInputCol("prediction")
			.setOutputCol("predictedLabel")
			.setLabels(labelIndexer.labels());

			// Chain indexers and forest in a Pipeline
			Pipeline pipeline = new Pipeline()
			.setStages(new PipelineStage[] {labelIndexer, featureIndexer, rf, labelConverter});

			// Train model. This also runs the indexers.
			PipelineModel model = pipeline.fit(data);

			/*RandomForestClassificationModel rfModel = (RandomForestClassificationModel)(model.stages()[2]);
		System.out.println("Learned classification forest model:\n" + rfModel.toDebugString());*/


			// Make predictions.
			Dataset<Row> predictions = model.transform(test);

			// Select example rows to display.
			Row row = predictions.select("predictedLabel", "label","probability", "features").first();
 
			pw.println(trainingNumber + ", " + row.get(0) + " " + row.get(2));
			pw.flush();

		}
		spark.stop();
	}

	@Test
	public void testLottoModelDecisionTreeClassificationExample(){
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaRandomForestClassifierExample")
				.getOrCreate();

		// $example on$
		// Load and parse the data file, converting it to a DataFrame.
		Dataset<Row> data = spark.read().format("libsvm").load(getClass().getResource("/data/sample_libsvm_data.txt").getPath());

		data.select("features").show(20);
		// Index labels, adding metadata to the label column.
		// Fit on whole dataset to include all labels in index.
		StringIndexerModel labelIndexer = new StringIndexer()
		.setInputCol("label")
		.setOutputCol("indexedLabel")
		.fit(data);
		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as continuous.
		VectorIndexerModel featureIndexer = new VectorIndexer()
		.setInputCol("features")
		.setOutputCol("indexedFeatures")
		.setMaxCategories(4)
		.fit(data);

		// Split the data into training and test sets (30% held out for testing)
		Dataset<Row>[] splits = data.randomSplit(new double[] {0.7, 0.3});
		Dataset<Row> trainingData = splits[0];
		Dataset<Row> testData = splits[1];

		// Train a RandomForest model.
		RandomForestClassifier rf = new RandomForestClassifier()
		.setLabelCol("indexedLabel")
		.setFeaturesCol("indexedFeatures");

		// Convert indexed labels back to original labels.
		IndexToString labelConverter = new IndexToString()
		.setInputCol("prediction")
		.setOutputCol("predictedLabel")
		.setLabels(labelIndexer.labels());

		// Chain indexers and forest in a Pipeline
		Pipeline pipeline = new Pipeline()
		.setStages(new PipelineStage[] {labelIndexer, featureIndexer, rf, labelConverter});

		// Train model. This also runs the indexers.
		PipelineModel model = pipeline.fit(trainingData);

		// Make predictions.
		Dataset<Row> predictions = model.transform(testData);

		// Select example rows to display.
		predictions.select("predictedLabel", "label", "features").show(5);

		// Select (prediction, true label) and compute test error
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
		.setLabelCol("indexedLabel")
		.setPredictionCol("prediction")
		.setMetricName("accuracy");
		double accuracy = evaluator.evaluate(predictions);
		System.out.println("Test Error = " + (1.0 - accuracy));

		RandomForestClassificationModel rfModel = (RandomForestClassificationModel)(model.stages()[2]);
		System.out.println("Learned classification forest model:\n" + rfModel.toDebugString());
		// $example off

		spark.stop();
	}

	@Test
	public void testLottoModelDecisionTreeRegressionExample(){
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaRandomForestRegressorExample")
				.getOrCreate();

		// $example on$
		// Load and parse the data file, converting it to a DataFrame.
		Dataset<Row> data = spark.read().format("libsvm").load(getClass().getResource("/data/sample_libsvm_data.txt").getPath());

		// Automatically identify categorical features, and index them.
		// Set maxCategories so features with > 4 distinct values are treated as continuous.
		VectorIndexerModel featureIndexer = new VectorIndexer()
		.setInputCol("features")
		.setOutputCol("indexedFeatures")
		.setMaxCategories(4)
		.fit(data);

		// Split the data into training and test sets (30% held out for testing)
		Dataset<Row>[] splits = data.randomSplit(new double[] {0.7, 0.3});
		Dataset<Row> trainingData = splits[0];
		Dataset<Row> testData = splits[1];

		// Train a RandomForest model.
		RandomForestRegressor rf = new RandomForestRegressor()
		.setLabelCol("label")
		.setFeaturesCol("indexedFeatures");

		// Chain indexer and forest in a Pipeline
		Pipeline pipeline = new Pipeline()
		.setStages(new PipelineStage[] {featureIndexer, rf});

		// Train model. This also runs the indexer.
		PipelineModel model = pipeline.fit(trainingData);

		// Make predictions.
		Dataset<Row> predictions = model.transform(testData);

		// Select example rows to display.
		predictions.select("prediction", "label", "features").show(5);

		// Select (prediction, true label) and compute test error
		RegressionEvaluator evaluator = new RegressionEvaluator()
		.setLabelCol("label")
		.setPredictionCol("prediction")
		.setMetricName("rmse");
		double rmse = evaluator.evaluate(predictions);
		System.out.println("Root Mean Squared Error (RMSE) on test data = " + rmse);

		RandomForestRegressionModel rfModel = (RandomForestRegressionModel)(model.stages()[1]);
		System.out.println("Learned regression forest model:\n" + rfModel.toDebugString());
		// $example off$

		spark.stop();
	}
}
