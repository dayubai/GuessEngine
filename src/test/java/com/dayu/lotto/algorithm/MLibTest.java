package com.dayu.lotto.algorithm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.NaiveBayes;
import org.apache.spark.ml.classification.NaiveBayesModel;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.ml.tuning.TrainValidationSplit;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;
import org.apache.spark.mllib.fpm.AssociationRules;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.PrefixSpan;
import org.apache.spark.mllib.fpm.PrefixSpanModel;
import org.apache.spark.mllib.stat.KernelDensity;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;

import com.dayu.lotto.TestAppConfig;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppConfig.class})
public class MLibTest implements Serializable {

	private static JavaSparkContext sparkCtx;
	@Before
	public void init() throws IllegalArgumentException, IOException {
		/*System.setProperty("hadoop.home.dir", getClass().getResource("/hadoop").getPath());
		//ctxtBuilder = new ContextBuilder(tempFolder);
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("junit");
		sparkCtx = new JavaSparkContext(conf);  */
		/*SparkConf conf = new SparkConf().setAppName("App_Name")
    .setMaster("spark://localhost:18080").set("spark.ui.port","18080"); */  
	}

	@Test
	public void testJavaEstimatorTransformerParamExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaEstimatorTransformerParamExample")
				.getOrCreate();

		// $example on$
		// Prepare training data.
		List<Row> dataTraining = Arrays.asList(
				RowFactory.create(1.0, Vectors.dense(0.0, 1.1, 0.1)),
				RowFactory.create(0.0, Vectors.dense(2.0, 1.0, -1.0)),
				RowFactory.create(0.0, Vectors.dense(2.0, 1.3, 1.0)),
				RowFactory.create(1.0, Vectors.dense(0.0, 1.2, -0.5))
				);
		StructType schema = new StructType(new StructField[]{
				new StructField("label", DataTypes.DoubleType, false, Metadata.empty()),
				new StructField("features", new VectorUDT(), false, Metadata.empty())
		});
		Dataset<Row> training = spark.createDataFrame(dataTraining, schema);

		// Create a LogisticRegression instance. This instance is an Estimator.
		LogisticRegression lr = new LogisticRegression();
		// Print out the parameters, documentation, and any default values.
		System.out.println("LogisticRegression parameters:\n" + lr.explainParams() + "\n");

		// We may set parameters using setter methods.
		lr.setMaxIter(10).setRegParam(0.01);

		// Learn a LogisticRegression model. This uses the parameters stored in lr.
		LogisticRegressionModel model1 = lr.fit(training);
		// Since model1 is a Model (i.e., a Transformer produced by an Estimator),
		// we can view the parameters it used during fit().
		// This prints the parameter (name: value) pairs, where names are unique IDs for this
		// LogisticRegression instance.
		System.out.println("Model 1 was fit using parameters: " + model1.parent().extractParamMap());

		// We may alternatively specify parameters using a ParamMap.
		ParamMap paramMap = new ParamMap()
		.put(lr.maxIter().w(20))  // Specify 1 Param.
		.put(lr.maxIter(), 30)  // This overwrites the original maxIter.
		.put(lr.regParam().w(0.1), lr.threshold().w(0.55));  // Specify multiple Params.

		// One can also combine ParamMaps.
		ParamMap paramMap2 = new ParamMap()
		.put(lr.probabilityCol().w("myProbability"));  // Change output column name
		ParamMap paramMapCombined = paramMap.$plus$plus(paramMap2);

		// Now learn a new model using the paramMapCombined parameters.
		// paramMapCombined overrides all parameters set earlier via lr.set* methods.
		LogisticRegressionModel model2 = lr.fit(training, paramMapCombined);
		System.out.println("Model 2 was fit using parameters: " + model2.parent().extractParamMap());

		// Prepare test documents.
		List<Row> dataTest = Arrays.asList(
				RowFactory.create(1.0, Vectors.dense(-1.0, 1.5, 1.3)),
				RowFactory.create(0.0, Vectors.dense(3.0, 2.0, -0.1)),
				RowFactory.create(1.0, Vectors.dense(0.0, 2.2, -1.5))
				);
		Dataset<Row> test = spark.createDataFrame(dataTest, schema);

		// Make predictions on test documents using the Transformer.transform() method.
		// LogisticRegression.transform will only use the 'features' column.
		// Note that model2.transform() outputs a 'myProbability' column instead of the usual
		// 'probability' column since we renamed the lr.probabilityCol parameter previously.
		Dataset<Row> results = model2.transform(test);
		Dataset<Row> rows = results.select("features", "label", "myProbability", "prediction");
		for (Row r: rows.collectAsList()) {
			System.out.println("(" + r.get(0) + ", " + r.get(1) + ") -> prob=" + r.get(2)
					+ ", prediction=" + r.get(3));
		}
		// $example off$

		spark.stop();
	}

	@Test
	public void testJavaPipelineExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaPipelineExample")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(Arrays.asList(
				new JavaLabeledDocument(0L, "a b c d e spark", 1.0),
				new JavaLabeledDocument(1L, "b d", 0.0),
				new JavaLabeledDocument(2L, "spark f g h", 1.0),
				new JavaLabeledDocument(3L, "hadoop mapreduce", 0.0)
				), JavaLabeledDocument.class);

		// Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
		Tokenizer tokenizer = new Tokenizer()
		.setInputCol("text")
		.setOutputCol("words");
		HashingTF hashingTF = new HashingTF()
		.setNumFeatures(1000)
		.setInputCol(tokenizer.getOutputCol())
		.setOutputCol("features");
		LogisticRegression lr = new LogisticRegression()
		.setMaxIter(10)
		.setRegParam(0.001);
		Pipeline pipeline = new Pipeline()
		.setStages(new PipelineStage[] {tokenizer, hashingTF, lr});

		// Fit the pipeline to training documents.
		PipelineModel model = pipeline.fit(training);

		// Prepare test documents, which are unlabeled.
		Dataset<Row> test = spark.createDataFrame(Arrays.asList(
				new JavaDocument(4L, "spark i j k"),
				new JavaDocument(5L, "l m n"),
				new JavaDocument(6L, "spark hadoop spark"),
				new JavaDocument(7L, "apache hadoop")
				), JavaDocument.class);

		// Make predictions on test documents.
		Dataset<Row> predictions = model.transform(test);
		for (Row r : predictions.select("id", "text", "probability", "prediction").collectAsList()) {
			System.out.println("(" + r.get(0) + ", " + r.get(1) + ") --> prob=" + r.get(2)
					+ ", prediction=" + r.get(3));
		}
		// $example off$

		spark.stop();
	}

	@Test
	public void testJavaALSExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaALSExample")
				.getOrCreate();

		// $example on$
		JavaRDD<Rating> ratingsRDD = spark
				.read().textFile(getClass().getResource("/data/sample_movielens_ratings.txt").getPath()).javaRDD()
				.map(new Function<String, Rating>() {
					public Rating call(String str) {
						return Rating.parseRating(str);
					}
				});
		Dataset<Row> ratings = spark.createDataFrame(ratingsRDD, Rating.class);
		Dataset<Row>[] splits = ratings.randomSplit(new double[]{0.8, 0.2});
		Dataset<Row> training = splits[0];
		Dataset<Row> test = splits[1];

		// Build the recommendation model using ALS on the training data
		ALS als = new ALS()
		.setMaxIter(5)
		.setRegParam(0.01)
		.setUserCol("userId")
		.setItemCol("movieId")
		.setRatingCol("rating");
		ALSModel model = als.fit(training);

		// Evaluate the model by computing the RMSE on the test data
		Dataset<Row> predictions = model.transform(test);

		RegressionEvaluator evaluator = new RegressionEvaluator()
		.setMetricName("rmse")
		.setLabelCol("rating")
		.setPredictionCol("prediction");
		Double rmse = evaluator.evaluate(predictions);
		System.out.println("Root-mean-square error = " + rmse);
		// $example off$
		spark.stop();
	}

	// $example on$
	@SuppressWarnings("serial")
	public static class Rating implements Serializable {
		private int userId;
		private int movieId;
		private float rating;
		private long timestamp;

		public Rating() {}

		public Rating(int userId, int movieId, float rating, long timestamp) {
			this.userId = userId;
			this.movieId = movieId;
			this.rating = rating;
			this.timestamp = timestamp;
		}

		public int getUserId() {
			return userId;
		}

		public int getMovieId() {
			return movieId;
		}

		public float getRating() {
			return rating;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public static Rating parseRating(String str) {
			String[] fields = str.split("::");
			if (fields.length != 4) {
				throw new IllegalArgumentException("Each line must contain 4 fields");
			}
			int userId = Integer.parseInt(fields[0]);
			int movieId = Integer.parseInt(fields[1]);
			float rating = Float.parseFloat(fields[2]);
			long timestamp = Long.parseLong(fields[3]);
			return new Rating(userId, movieId, rating, timestamp);
		}
	}
	// $example off$

	@Test
	public void testJavaModelSelectionViaCrossValidationExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaModelSelectionViaCrossValidationExample")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(Arrays.asList(
				new JavaLabeledDocument(0L, "a b c d e spark", 1.0),
				new JavaLabeledDocument(1L, "b d", 0.0),
				new JavaLabeledDocument(2L,"spark f g h", 1.0),
				new JavaLabeledDocument(3L, "hadoop mapreduce", 0.0),
				new JavaLabeledDocument(4L, "b spark who", 1.0),
				new JavaLabeledDocument(5L, "g d a y", 0.0),
				new JavaLabeledDocument(6L, "spark fly", 1.0),
				new JavaLabeledDocument(7L, "was mapreduce", 0.0),
				new JavaLabeledDocument(8L, "e spark program", 1.0),
				new JavaLabeledDocument(9L, "a e c l", 0.0),
				new JavaLabeledDocument(10L, "spark compile", 1.0),
				new JavaLabeledDocument(11L, "hadoop software", 0.0)
				), JavaLabeledDocument.class);

		// Configure an ML pipeline, which consists of three stages: tokenizer, hashingTF, and lr.
		Tokenizer tokenizer = new Tokenizer()
		.setInputCol("text")
		.setOutputCol("words");
		HashingTF hashingTF = new HashingTF()
		.setNumFeatures(1000)
		.setInputCol(tokenizer.getOutputCol())
		.setOutputCol("features");
		LogisticRegression lr = new LogisticRegression()
		.setMaxIter(10)
		.setRegParam(0.01);
		Pipeline pipeline = new Pipeline()
		.setStages(new PipelineStage[] {tokenizer, hashingTF, lr});

		// We use a ParamGridBuilder to construct a grid of parameters to search over.
		// With 3 values for hashingTF.numFeatures and 2 values for lr.regParam,
		// this grid will have 3 x 2 = 6 parameter settings for CrossValidator to choose from.
		ParamMap[] paramGrid = new ParamGridBuilder()
		.addGrid(hashingTF.numFeatures(), new int[] {10, 100, 1000})
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
		.setEstimatorParamMaps(paramGrid).setNumFolds(2);  // Use 3+ in practice

		// Run cross-validation, and choose the best set of parameters.
		CrossValidatorModel cvModel = cv.fit(training);

		// Prepare test documents, which are unlabeled.
		Dataset<Row> test = spark.createDataFrame(Arrays.asList(
				new JavaDocument(4L, "spark i j k"),
				new JavaDocument(5L, "l m n"),
				new JavaDocument(6L, "mapreduce spark"),
				new JavaDocument(7L, "apache hadoop")
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
	public void testJavaModelSelectionViaTrainValidationSplitExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaModelSelectionViaTrainValidationSplitExample")
				.getOrCreate();

		// $example on$
		Dataset<Row> data = spark.read().format("libsvm")
				.load(getClass().getResource("/data/sample_linear_regression_data.txt").getPath());

		// Prepare training and test data.
		Dataset<Row>[] splits = data.randomSplit(new double[] {0.9, 0.1}, 12345);
		Dataset<Row> training = splits[0];
		Dataset<Row> test = splits[1];

		LinearRegression lr = new LinearRegression();

		// We use a ParamGridBuilder to construct a grid of parameters to search over.
		// TrainValidationSplit will try all combinations of values and determine best model using
		// the evaluator.
		ParamMap[] paramGrid = new ParamGridBuilder()
		.addGrid(lr.regParam(), new double[] {0.1, 0.01})
		.addGrid(lr.fitIntercept())
		.addGrid(lr.elasticNetParam(), new double[] {0.0, 0.5, 1.0})
		.build();

		// In this case the estimator is simply the linear regression.
		// A TrainValidationSplit requires an Estimator, a set of Estimator ParamMaps, and an Evaluator.
		TrainValidationSplit trainValidationSplit = new TrainValidationSplit()
		.setEstimator(lr)
		.setEvaluator(new RegressionEvaluator())
		.setEstimatorParamMaps(paramGrid)
		.setTrainRatio(0.8);  // 80% for training and the remaining 20% for validation

		// Run train validation split, and choose the best set of parameters.
		TrainValidationSplitModel model = trainValidationSplit.fit(training);

		// Make predictions on test data. model is the model with combination of parameters
		// that performed best.
		model.transform(test)
		.select("features", "label", "prediction")
		.show();
		// $example off$

		spark.stop();
	}

	@Test
	public void testJavaNaiveBayesExample()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("JavaNaiveBayesExample")
				.getOrCreate();

		// $example on$
		// Load training data
		Dataset<Row> dataFrame =
				spark.read().format("libsvm").load(getClass().getResource("/data/sample_libsvm_data.txt").getPath());
		// Split the data into train and test
		Dataset<Row>[] splits = dataFrame.randomSplit(new double[]{0.6, 0.4}, 1234L);
		Dataset<Row> train = splits[0];
		Dataset<Row> test = splits[1];

		// create the trainer and set its parameters
		NaiveBayes nb = new NaiveBayes();

		// train the model
		NaiveBayesModel model = nb.fit(train);

		// Select example rows to display.
		Dataset<Row> predictions = model.transform(test);
		predictions.show();

		// compute accuracy on the test set
		MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
		.setLabelCol("label")
		.setPredictionCol("prediction")
		.setMetricName("accuracy");
		double accuracy = evaluator.evaluate(predictions);
		System.out.println("Test set accuracy = " + accuracy);
		// $example off$

		spark.stop();
	}
	
	@Test
	public void testJavaKernelDensityEstimationExample()
	{
		//SparkConf conf = new SparkConf().setAppName("JavaKernelDensityEstimationExample");
	
	//    JavaSparkContext jsc = new JavaSparkContext(conf);
		SparkSession spark = SparkSession
		.builder()
		.appName("JavaKernelDensityEstimationExample")
		.getOrCreate();

	    // $example on$
	    // an RDD of sample data
	    JavaRDD<Double> data = sparkCtx.parallelize(
	      Arrays.asList(1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.0, 6.0, 7.0, 8.0, 9.0, 9.0));

	    // Construct the density estimator with the sample data
	    // and a standard deviation for the Gaussian kernels
	    KernelDensity kd = new KernelDensity().setSample(data).setBandwidth(3.0);

	    // Find density estimates for the given values
	    double[] densities = kd.estimate(new double[]{-1.0, 2.0, 5.0});

	    System.out.println(Arrays.toString(densities));
	    // $example off$

	    spark.stop();
	}
	
	@Test
	public void testJavaAssociationRulesExample()
	{
		//SparkConf conf = new SparkConf().setAppName("JavaKernelDensityEstimationExample");
	
	//    JavaSparkContext jsc = new JavaSparkContext(conf);
		SparkSession spark = SparkSession
		.builder()
		.appName("JavaAssociationRulesExample")
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
	public void testJavaPrefixSpanExample()
	{
		SparkSession spark = SparkSession
		.builder()
		.appName("JavaPrefixSpanExample")
		.getOrCreate();

		// $example on$
	    JavaRDD<List<List<Integer>>> sequences = sparkCtx.parallelize(Arrays.asList(
	      Arrays.asList(Arrays.asList(1, 2), Arrays.asList(3)),
	      Arrays.asList(Arrays.asList(1), Arrays.asList(3, 2), Arrays.asList(1, 2)),
	      Arrays.asList(Arrays.asList(1, 2), Arrays.asList(5)),
	      Arrays.asList(Arrays.asList(6))
	    ), 2);
	    PrefixSpan prefixSpan = new PrefixSpan()
	      .setMinSupport(0.5)
	      .setMaxPatternLength(5);
	    PrefixSpanModel<Integer> model = prefixSpan.run(sequences);
	    for (PrefixSpan.FreqSequence<Integer> freqSeq: model.freqSequences().toJavaRDD().collect()) {
	      System.out.println(freqSeq.javaSequence() + ", " + freqSeq.freq());
	    }
	    // $example off$

	    spark.stop();
	}
}
