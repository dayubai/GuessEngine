package com.dayu.lotto.mongodb;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.dayu.lotto.dao.LottoDAO;
import com.dayu.lotto.entity.LottoNumberPrediction;
import com.dayu.lotto.entity.LottoResult;
import com.dayu.lotto.entity.LottoTicket;

@Component
public class LottoDB implements LottoDAO {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public <T extends LottoResult> void save(T lottoResult) {
		mongoTemplate.save(lottoResult);
	}

	public <T extends LottoTicket> String save(T lottoTicket) {
		mongoTemplate.save(lottoTicket);
		return lottoTicket.getId();
	}

	public <T extends LottoResult> List<T> findLastResults(int limit,
			Class<T> entityClass) {
		return mongoTemplate.find(new Query().with(new Sort(Sort.Direction.DESC, "_id")).limit(limit), entityClass);
	}

	public <T extends LottoResult> T findResultByDraw(int draw,
			Class<T> entityClass) {
		return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(draw)), entityClass);
	}

	public <T extends LottoTicket> List<T> findTicketsByDraw(int draw,
			Class<T> entityClass) {
		return mongoTemplate.find(Query.query(Criteria.where("draw").is(draw)), entityClass);
	}

	public <T extends LottoTicket> void updateTicketPrize(String ticketId,
			BigDecimal prize, Class<T> entityClass) {
		mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(ticketId)), new Update().set("prize", prize), entityClass);
	}

	public <T extends LottoTicket> List<T> findAllTickets(Class<T> entityClass) {
		return mongoTemplate.find(new Query().with(new Sort(Sort.Direction.DESC, "_id")), entityClass);
	}

	public <T extends LottoTicket> T findTicketById(String id, Class<T> entityClass) {
		return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(id)), entityClass);
	}
	
	public <T extends LottoNumberPrediction> String saveOrUpdateNumberPrediction(T lottoNumberPrediction) {
		mongoTemplate.save(lottoNumberPrediction);
		return lottoNumberPrediction.getDrawNumber();
	}
	
	public <T extends Object> void dropDatabase(Class<T> c)
	{
		if (mongoTemplate.collectionExists(c))
    		mongoTemplate.dropCollection(c);
	}

	@Override
	public <T extends LottoResult> List<T> findLastResultsFromDraw(int draw,
			int limit, Class<T> entityClass) {
		return mongoTemplate.find(Query.query(Criteria.where("_id").lt(draw)).with(new Sort(Sort.Direction.DESC, "_id")).limit(limit), entityClass);
	}

	@Override
	public <T extends LottoNumberPrediction> List<T> findAllForestRandomPrediction(Class<T> entityClass) {
		return mongoTemplate.find(new Query().with(new Sort(Sort.Direction.DESC, "_id")), entityClass);
	}

	@Override
	public <T extends LottoNumberPrediction> T findAllForestRandomPredictionByDraw(String draw, Class<T> entityClass) {
		return mongoTemplate.findOne(Query.query(Criteria.where("_id").is(draw)), entityClass);
	}

	

}
