package com.dayu.lotto.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.CrossValidator;
import org.apache.spark.ml.tuning.CrossValidatorModel;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dayu.lotto.algorithm.JavaDocument;
import com.dayu.lotto.algorithm.JavaLabeledDocument;
import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.ARModel;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.LottoNumberPrediction;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.SaturdayLottoPrediction;
import com.dayu.lotto.entity.SaturdayLottoPrediction.SinglePredictionObject;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.SaturdayLottoService;

@Service
public class SaturdayLottoServiceImpl extends AbstractLottoService<SaturdayLottoResult> implements SaturdayLottoService {

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

	@Override
	public List<SaturdayLottoResult> findLast(int limit) {
		List<SaturdayLottoResult> result = lottoDAO.findLastResults(limit, SaturdayLottoResult.class);
		Collections.sort(result);
		
		return result;
	}

	public int numberToPick() {
		return 6;
	}

	public int pool() {
		return 45;
	}

	public String draw(WeightedSelector selector, int draw, int games) {
		
		CrossValidatorModel cvModel = getCrossValidatorModel();
		
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

			ticketResult.setNumbers(selector.select(numberToPick(), model, totalWeight, cvModel));
			
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
		List<JavaLabeledDocument> numbers = new ArrayList<JavaLabeledDocument>();
		long id = 1l;
		for (SaturdayLottoResult result: findLast(max))
		{
			String winText = "";
			String nonWinText = "";
			
			// non winning number
			for (int i = 1; i <= pool(); i++)
			{
				if ( result.getWinningNumbers().contains(i))
					winText += i + " ";
				else
					nonWinText += i + " ";
			}
			
			numbers.add(new JavaLabeledDocument(id++, winText, 1.0));
			numbers.add(new JavaLabeledDocument(id++, nonWinText, 0.0));
		}
		
		return numbers;
	}

	public CrossValidatorModel getCrossValidatorModel()
	{
		SparkSession spark = SparkSession
				.builder()
				.appName("getSatCrossValidatorModel")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(buildTrainingData(SAMPLE), JavaLabeledDocument.class);

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
		return cv.fit(training);
	}

	/*@Override
	public void generateNumberPredictions() {
		
		SparkSession spark = SparkSession
				.builder()
				.appName("getSatCrossValidatorModel")
				.getOrCreate();

		// $example on$
		// Prepare training documents, which are labeled.
		Dataset<Row> training = spark.createDataFrame(buildTrainingData(SAMPLE), JavaLabeledDocument.class);

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
		CrossValidatorModel model = cv.fit(training);
		
		processPrdiction(spark, model);
		
		spark.stop();
	}*/
	
	/*private void processPrdiction(SparkSession spark, CrossValidatorModel model)
	{
		// Drop SaturdayLottoPrediction collection
		lottoDAO.dropDatabase(SaturdayLottoPrediction.class);
		
		List<SinglePredictionObject> predictionObjects = new ArrayList<SinglePredictionObject>();
		List<JavaDocument> jDocs = new ArrayList<JavaDocument>();
		
		List<Integer> l1 = new ArrayList<Integer>();
		List<Integer> l2 = new ArrayList<Integer>();
		List<Integer> l3 = new ArrayList<Integer>();
		List<Integer> l4 = new ArrayList<Integer>();
		List<Integer> l5 = new ArrayList<Integer>();
		List<Integer> l6 = new ArrayList<Integer>();
		
		long id =1;
		
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
		for (int i1 = 0; i1 < 40; i1++)
		{
			int n1 = l1.get(i1);
			
			// remove n1 from other list
			l2.remove(Integer.valueOf(n1));
			l3.remove(Integer.valueOf(n1));
			l4.remove(Integer.valueOf(n1));
			l5.remove(Integer.valueOf(n1));
			l6.remove(Integer.valueOf(n1));
			
			
			temp.add(n1);
			
			for (int i2 = 0; i2<l2.size(); i2++)
			{
				int n2 = l2.get(i2);
				if (n2 > n1 && !temp.contains(n2))
				{
					temp.add(n2);
					
					for (int i3 =0; i3<l3.size(); i3++)
					{
						int n3 = l3.get(i3);
						if (n3 > n2 && !temp.contains(n3))
						{
							temp.add(n3);
							for (int i4=0;i4<l4.size();i4++)
							{
								int n4 = l4.get(i4);
								if (n4 > n3 && !temp.contains(n4))
								{
									temp.add(n4);
									for (int i5=0; i5<l5.size();i5++)
									{
										int n5 = l5.get(i5);
										if (n5 > n4 && !temp.contains(n5))
										{
											temp.add(n5);
											for (int i6=0; i6<l6.size();i6++)
											{
												int n6 = l6.get(i6);
												if (n6 > n5 && !temp.contains(n6))
												{
													temp.add(n6);

													jDocs.add(new JavaDocument(id, temp.get(0) + " " + temp.get(1) + " " + temp.get(2) + " " + temp.get(3) + " " + temp.get(4) + " " + temp.get(5)));
													// Prepare test documents, which are unlabeled.
													Dataset<Row> test = spark.createDataFrame(jDocs, JavaDocument.class);

													// Make predictions on test documents. cvModel uses the best model found (lrModel).
													Dataset<Row> predictions = model.transform(test);
													for (Row r : predictions.select("id", "text", "probability", "prediction").collectAsList()) {
														
														SaturdayLottoPrediction obj = new SaturdayLottoPrediction();
														SinglePredictionObject singlePredictionObject = obj.newSinglePredictionObject();
														
														singlePredictionObject.setNumbers(r.getString(1));
														singlePredictionObject.setPrediction(r.getDouble(3));
														singlePredictionObject.setProbability(r.get(2));
												
														
														obj.setPredictionObjects(predictionObjects);
														lottoDAO.saveOrUpdateNumberPrediction(obj);
														
													}
												
													
													id++;
													
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
	}*/
	
	
	@Override
    public void generateNumberPredictions(String drawNumber)
    {
        // build model	
    	super.buildForrestRandomModel();
    	
    	// DB Object
    	SaturdayLottoPrediction obj = new SaturdayLottoPrediction();
    	obj.setDrawNumber(drawNumber);
    	List<SinglePredictionObject> predictionObjects = new ArrayList<SinglePredictionObject>();
    	
    	
    	/**
		 **  ML Model
		 */
		//Build RandomForrest Model
		SparkSession spark = SparkSession
				.builder()
				.appName("SaturdayLottoRandomForestClassifierExample")
				.getOrCreate();

		
		
		for (int trainingNumber = 1; trainingNumber <=pool(); trainingNumber++) 
		{


			// Create some vector data; also works for sparse vectors
			List<Row> rows = new ArrayList<Row>();
			for (ARModel arModel : super.frTrainingData.get(trainingNumber))
			{	
				rows.add(RowFactory.create(arModel.getLabel(), Vectors.dense(ArrayUtils.toPrimitive( arModel.getTrainingSet()))));
			}

			List<StructField> fields = new ArrayList<>();
			fields.add(DataTypes.createStructField("label", DataTypes.DoubleType, false));
			fields.add(DataTypes.createStructField("features", new VectorUDT(), false));

			StructType schema = DataTypes.createStructType(fields);

			Dataset<Row> data = spark.createDataFrame(rows, schema);

			Dataset<Row> test = spark.createDataFrame(Arrays.asList(RowFactory.create(super.frTestData.get(trainingNumber).getLabel(), Vectors.dense(ArrayUtils.toPrimitive( super.frTestData.get(trainingNumber).getTrainingSet())))),schema);

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

			// Make predictions.
			Dataset<Row> predictions = model.transform(test);

			Row row = predictions.select("predictedLabel", "label","probability", "features").first();
			
			// write predictions to database
			
			SinglePredictionObject singlePredictionObject = obj.newSinglePredictionObject();
			
			singlePredictionObject.setNumber(String.valueOf(trainingNumber));
			singlePredictionObject.setPrediction(row.getDouble(1));
			singlePredictionObject.setProbability(row.getString(3));
	
			predictionObjects.add(singlePredictionObject);
			
			
/*			Row row = predictions.select("predictedLabel", "label","probability", "features").first();
 
			pw.println(trainingNumber + ", " + row.get(0) + " " + row.get(2));
			pw.flush();*/

		}
		spark.stop();
		
		obj.setPredictionObjects(predictionObjects);
		lottoDAO.saveOrUpdateNumberPrediction(obj);
    }
}
