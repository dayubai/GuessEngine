package com.dayu.lotto.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dayu.lotto.algorithm.JavaLabeledDocument;
import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.Division;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.OZLottoTicket;

@Service
public class OZLottoServiceImpl extends AbstractLottoService<OZLottoTicket, OZLottoResult> {

	@Autowired
	private LottoDAO lottoDAO;
	
	@Autowired
	private JavaSparkContext javaSparkContext;

	private static Logger log = LoggerFactory.getLogger(OZLottoServiceImpl.class);
	private static final int SAMPLE = 20;
	
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
					OZLottoResult ozLottoResult = new OZLottoResult();

					String[] columns = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

					// remove quotes
					for (int i =0; i < columns.length ; i++)
					{
						columns[i] = columns[i].replaceAll("\"", "");
					}


					if (columns.length == 18)
					{
						Date drawDate = df.parse(columns[1]);

						ozLottoResult.setDrawNumber(Integer.parseInt(columns[0]));
						ozLottoResult.setDrawDate(drawDate);
						List<Integer> winningNumbers = new ArrayList<Integer>();

						winningNumbers.add(Integer.parseInt(columns[2]));
						winningNumbers.add(Integer.parseInt(columns[3]));
						winningNumbers.add(Integer.parseInt(columns[4]));
						winningNumbers.add(Integer.parseInt(columns[5]));
						winningNumbers.add(Integer.parseInt(columns[6]));
						winningNumbers.add(Integer.parseInt(columns[7]));
						winningNumbers.add(Integer.parseInt(columns[8]));

						Collections.sort(winningNumbers);
						ozLottoResult.setWinningNumbers(winningNumbers);

						List<Integer> supplementaryNumbers = new ArrayList<Integer>();

						supplementaryNumbers.add(Integer.parseInt(columns[9]));
						supplementaryNumbers.add(Integer.parseInt(columns[10]));

						Collections.sort(supplementaryNumbers);
						ozLottoResult.setSupplementaryNumbers(supplementaryNumbers);

						List<Division> divisions = new ArrayList<Division>();
						divisions.add(new Division(1, BigDecimal.valueOf(Double.parseDouble(columns[11].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(2, BigDecimal.valueOf(Double.parseDouble(columns[12].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(3, BigDecimal.valueOf(Double.parseDouble(columns[13].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(4, BigDecimal.valueOf(Double.parseDouble(columns[14].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(5, BigDecimal.valueOf(Double.parseDouble(columns[15].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(6, BigDecimal.valueOf(Double.parseDouble(columns[16].replaceAll("[^\\d.]+", "")))));
						divisions.add(new Division(7, BigDecimal.valueOf(Double.parseDouble(columns[17].replaceAll("[^\\d.]+", "")))));
						Collections.sort(divisions);
						ozLottoResult.setDivisions(divisions);

						lottoDAO.save(ozLottoResult);
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

	public int numberToPick() {
		return 7;
	}

	public int pool() {
		return 45;
	}

	public String draw(WeightedSelector selector, int draw, int games) {
		List<OZLottoResult> results = findLast(SAMPLE);
		CrossValidatorModel cvModel = getCrossValidatorModel();

		// all numbers collected from results
		List<Integer> allDraws = new ArrayList<Integer>();
		for (OZLottoResult result : results)
		{
			allDraws.addAll(result.getWinningNumbers());
			
			allDraws.addAll(result.getSupplementaryNumbers());
		}


		List<OZLottoTicket.Result> ticketResults = new ArrayList<OZLottoTicket.Result>();

		// build model and predict
		for (int i = 0; i < games; i++)
		{
			OZLottoTicket.Result ticketResult = new OZLottoTicket().newResult();

			Map<Integer, Integer> model = selector.buildWeightingModel(allDraws);


			int totalWeight = 0;
			for (int key: model.keySet()) {
				totalWeight += model.get(key);
			}

			ticketResult.setNumbers(selector.select(numberToPick(), model, totalWeight, cvModel));

			ticketResults.add(ticketResult);
		}

		OZLottoTicket ticket = new OZLottoTicket();
		ticket.setDraw(draw);
		ticket.setResults(ticketResults);

		return lottoDAO.save(ticket);		
	}

	public void checkTicket(LottoTicket lottoTicket) {
		OZLottoTicket ozLottoTicket = (OZLottoTicket) lottoTicket;

		OZLottoResult lottoResult = findResultByDraw(ozLottoTicket.getDraw());

		if (lottoResult != null)
		{
			BigDecimal prize = BigDecimal.ZERO;
			for (OZLottoTicket.Result result : ozLottoTicket.getResults())
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
				if (winNumbers == 7)
				{
					log.info("1st prize: " + result.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(0).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(0).getAmount());
				}
				else if (winNumbers == 6)
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
				else if (winNumbers == 5)
				{
					if (supNumbers > 0)
					{
						log.info("4th prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(3).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(3).getAmount());
					}
					else
					{
						log.info("5th prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(4).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(4).getAmount());
					}
				}
				else if (winNumbers == 4)
				{
					log.info("6th prize: " + result.getNumbers().toString());
					log.info("Prize win $" + lottoResult.getDivisions().get(5).getAmount());
					prize = prize.add(lottoResult.getDivisions().get(5).getAmount());
				}
				else if (winNumbers == 3)
				{
					if (supNumbers > 0)
					{
						log.info("7th prize: " + result.getNumbers().toString());
						log.info("Prize win $" + lottoResult.getDivisions().get(6).getAmount());
						prize = prize.add(lottoResult.getDivisions().get(6).getAmount());
					}
				}
			}
			lottoDAO.updateTicketPrize(ozLottoTicket.getId(), prize, OZLottoTicket.class);
		}
	}

	public List<OZLottoTicket> findTicketsByDraw(int draw) {
		return lottoDAO.findTicketsByDraw(draw, OZLottoTicket.class);
	}

	public List<OZLottoTicket> findAllTickets() {
		return lottoDAO.findAllTickets(OZLottoTicket.class);
	}

	public List<OZLottoResult> findLast(int limit) {
		return lottoDAO.findLastResults(limit, OZLottoResult.class);
	}

	public OZLottoResult findResultByDraw(int draw) {
		return lottoDAO.findResultByDraw(draw, OZLottoResult.class);
	}

	public int supplementaries() {
		return 2;
	}

	public OZLottoTicket findByTicketId(String id) {
		return lottoDAO.findTicketById(id, OZLottoTicket.class);
	}

	@Override
	public List<JavaLabeledDocument> buildTrainingData(int max) {

		List<JavaLabeledDocument> numbers = new ArrayList<JavaLabeledDocument>();
		long id = 1l;
		for (OZLottoResult result: findLast(max))
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
				.appName("getOZCrossValidatorModel")
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

	@Override
	public void generateNumberPredictions(String drawNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<OZLottoResult> findLastResultsFromDraw(int draw, int limit) {
		// TODO Auto-generated method stub
		return null;
	}


}
