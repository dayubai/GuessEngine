package com.dayu.lotto.service;

import java.io.InputStream;
import java.util.List;

import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;

import com.dayu.lotto.algorithm.JavaLabeledDocument;
import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.entity.LottoNumberPrediction;
import com.dayu.lotto.entity.LottoTicket;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;

public interface LottoService<T, R>  {
    
	void uploadResults(InputStream is) throws Exception;
	
	int numberToPick();
	
	int supplementaries();
    
    int pool();
    
    String draw(WeightedSelector selector, int draw, int games);
    
    void checkTicket(LottoTicket lottoTicket);
    
    LottoTicket findByTicketId(String id);
    
    List<JavaLabeledDocument> buildTrainingData(int max);
    
    void generateNumberPredictions (String drawNumber);
    
    
    // new method
    List<T> findTicketsByDraw(int draw);
    
    List<T> findAllTickets();
    
    List<R> findLast(int limit);
    
    R findResultByDraw(int draw);
    
    List<R> findLastResultsFromDraw(int draw,int limit);
    
}
