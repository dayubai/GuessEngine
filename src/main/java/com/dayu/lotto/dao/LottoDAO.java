package com.dayu.lotto.dao;

import java.math.BigDecimal;
import java.util.List;

import com.dayu.lotto.entity.LottoNumberPrediction;
import com.dayu.lotto.entity.LottoResult;
import com.dayu.lotto.entity.LottoTicket;

public interface LottoDAO {
	<T extends LottoResult> void save(T lottoResult);
	
	<T extends LottoTicket> String save(T lottoTicket);
	
	<T extends LottoResult> List<T> findLastResults(int limit, Class<T> entityClass);
	
	<T extends LottoResult> T findResultByDraw(int draw, Class<T> entityClass);
	
	<T extends LottoTicket> List<T> findTicketsByDraw(int draw, Class<T> entityClass);
	
	<T extends LottoTicket> void updateTicketPrize(String ticketId, BigDecimal prize, Class<T> entityClass);
	
	<T extends LottoTicket> List<T> findAllTickets(Class<T> entityClass);
	
	<T extends LottoTicket> T findTicketById(String id, Class<T> entityClass);
	
	<T extends LottoNumberPrediction> String saveOrUpdateNumberPrediction(T lottoNumberPrediction);
	
	<T extends Object> void dropDatabase(Class<T> c);
	
	<T extends LottoResult> List<T> findLastResultsFromDraw(int draw, int limit, Class<T> entityClass);
	
	<T extends LottoNumberPrediction> List<T> findAllForestRandomPrediction(Class<T> entityClass);
	
	<T extends LottoNumberPrediction> T findAllForestRandomPredictionByDraw(String draw, Class<T> entityClass);
}
