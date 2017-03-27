package com.dayu.lotto.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.RandomForestClassifier;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.feature.StringIndexerModel;
import org.apache.spark.ml.feature.VectorIndexer;
import org.apache.spark.ml.feature.VectorIndexerModel;
import org.apache.spark.ml.linalg.VectorUDT;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Component;

import com.dayu.lotto.entity.ARModel;
import com.dayu.lotto.entity.LottoNumberPrediction;
import com.dayu.lotto.entity.LottoNumberPrediction.SinglePredictionObject;
import com.dayu.lotto.entity.LottoResult;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.service.LottoService;

@Component
public abstract class AbstractLottoService<T extends LottoTicket, R extends LottoResult, P extends LottoNumberPrediction> implements LottoService<T, R, P>
{
	private static final int FR_HISTORY_FEATURES = 5;
    private static final int FR_HISTORY_SAMPLE = 50;
    
    protected Map<Integer, List<ARModel>> frTrainingData = new TreeMap<Integer, List<ARModel>>();
    protected Map<Integer, ARModel> frTestData = new TreeMap<Integer,ARModel>();
    
	protected void buildForrestRandomModel(int draw)
	{		
		// initial
		Map<Integer, Queue<Double>> resultMap = new TreeMap<Integer, Queue<Double>>();
		for (int i = 1; i <= pool(); i++)
		{
			frTrainingData.put(i, new ArrayList<ARModel>());

			Queue<Double> q = new ArrayBlockingQueue<Double>(FR_HISTORY_FEATURES);
			resultMap.put(i, q);
		}

		for (R result : findLastResultsFromDraw(draw, FR_HISTORY_SAMPLE))
		{
			List<Integer> winningNumbers = result.getWinningNumbers();

			for (int i=1; i <= pool(); i++)
			{
				if (resultMap.get(i).size() == FR_HISTORY_FEATURES)
				{
					//generate model
					ARModel arModel = new ARModel();
					arModel.setLabel(winningNumbers.contains(i) ? 1.0 : 0.0);
					Double[] arr = new Double[FR_HISTORY_FEATURES];
					resultMap.get(i).toArray(arr);

					arModel.setTrainingSet(arr);

					frTrainingData.get(i).add(arModel);

					//remove first
					resultMap.get(i).poll();
				}

				// insert new result
				resultMap.get(i).offer(winningNumbers.contains(i) ? 1.0 : 0.0);
			}

		}


		//test Data
		for (int key: frTrainingData.keySet())
		{
			ARModel testData = new ARModel();
			Double[] arr = new Double[FR_HISTORY_FEATURES];
			resultMap.get(key).toArray(arr);

			testData.setLabel(1);
			testData.setTrainingSet(arr);


			frTestData.put(key, testData);

		}
	}
	
	protected List<SinglePredictionObject> forrestRandomPredict(SparkSession spark, LottoNumberPrediction lottoNumberPrediction) {
			
		List<SinglePredictionObject> predictionObjects = new ArrayList<SinglePredictionObject>();
		for (int trainingNumber = 1; trainingNumber <=pool(); trainingNumber++) 
		{
			// Create some vector data; also works for sparse vectors
			List<Row> rows = new ArrayList<Row>();
			for (ARModel arModel : frTrainingData.get(trainingNumber))
			{	
				rows.add(RowFactory.create(arModel.getLabel(), Vectors.dense(ArrayUtils.toPrimitive( arModel.getTrainingSet()))));
			}

			List<StructField> fields = new ArrayList<>();
			fields.add(DataTypes.createStructField("label", DataTypes.DoubleType, false));
			fields.add(DataTypes.createStructField("features", new VectorUDT(), false));

			StructType schema = DataTypes.createStructType(fields);

			Dataset<Row> data = spark.createDataFrame(rows, schema);

			Dataset<Row> test = spark.createDataFrame(Arrays.asList(RowFactory.create(frTestData.get(trainingNumber).getLabel(), Vectors.dense(ArrayUtils.toPrimitive( frTestData.get(trainingNumber).getTrainingSet())))),schema);

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
			SinglePredictionObject singlePredictionObject = lottoNumberPrediction.newSinglePredictionObject();
			
			singlePredictionObject.setNumber(String.valueOf(trainingNumber));
			singlePredictionObject.setPrediction(Double.parseDouble(row.get(0).toString()));
			singlePredictionObject.setProbability(row.get(2).toString());
	
			predictionObjects.add(singlePredictionObject);
		}
		
		return predictionObjects;
	}
}
