package com.dayu.lotto.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import com.dayu.lotto.entity.ARModel;
import com.dayu.lotto.entity.LottoResult;
import com.dayu.lotto.service.LottoService;

public abstract class AbstractLottoService<T extends LottoResult> implements LottoService
{
	private static final int FR_HISTORY_FEATURES = 5;
    private static final int FR_HISTORY_SAMPLE = 50;
    
    protected Map<Integer, List<ARModel>> frTrainingData = new TreeMap<Integer, List<ARModel>>();
    protected Map<Integer, ARModel> frTestData = new TreeMap<Integer,ARModel>();
    
    public abstract List<T> findLast(int limit);
    
	protected void buildForrestRandomModel()
	{		
		// initial
		Map<Integer, Queue<Double>> resultMap = new TreeMap<Integer, Queue<Double>>();
		for (int i = 1; i <= pool(); i++)
		{
			frTrainingData.put(i, new ArrayList<ARModel>());

			Queue<Double> q = new ArrayBlockingQueue<Double>(FR_HISTORY_FEATURES);
			resultMap.put(i, q);
		}

		for (T result : findLast(FR_HISTORY_SAMPLE))
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
}
