package com.dayu.lotto.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.spark.ml.tuning.CrossValidatorModel;

import com.dayu.lotto.algorithm.spark.SparkGuessPredictor;

public class WeightedSelector {

	public Map<Integer,Integer> buildWeightingModel(List<Integer> allDraws) {

		Map<Integer,Integer> weithingOptions = new TreeMap<Integer,Integer>();

		for (Integer number : allDraws)
		{
			if (weithingOptions.containsKey(number))
			{
				int w = weithingOptions.get(number) + 1;
				weithingOptions.put(number, w);
			}
			else
			{
				weithingOptions.put(number, 1);
			}
		}

		return weithingOptions;

	}

	public List<Integer> select(int numberToSelect, Map<Integer,Integer> weightOptions, int totalWeight, CrossValidatorModel model) {

		List<Integer> predict = new ArrayList<Integer>();
		
		String text = "";
		for (int i = 0; i < numberToSelect; i++)
		{
			int number = rand(weightOptions, totalWeight);
			predict.add(number);
			
			text += number + " ";
			
			totalWeight -= weightOptions.get(number);
			weightOptions.remove(number);
		}
		
		
		if (new SparkGuessPredictor().predict(model, new JavaDocument(1, text)) != 1L)
			return select(numberToSelect, weightOptions, totalWeight, model);
		else 
			return predict;

	}
	
	public List<Integer> select(int numberToSelect, Map<Integer,Integer> weightOptions, int totalWeight) {

		List<Integer> predict = new ArrayList<Integer>();
		
		for (int i = 0; i < numberToSelect; i++)
		{
			int number = rand(weightOptions, totalWeight);
			predict.add(number);
			
			totalWeight -= weightOptions.get(number);
			weightOptions.remove(number);
		}
			
		return predict;

	}
	
	private int rand(Map<Integer,Integer> weightOptions, int totalWeight) {
		
		int i = 100;
		while (i > 0)
		{
			new Random().nextInt(totalWeight);
			i--;
		}
		
		//select a random value between 0 and our total
		int random = new Random().nextInt(totalWeight);

		//loop through our weightings until we arrive at the correct one
		int current = 0;
		for (int key : weightOptions.keySet()) {
			current += weightOptions.get(key);
			if (random < current)
			{
				return key;
			}
		}
		assert(false);
		return -1;
	}
}
