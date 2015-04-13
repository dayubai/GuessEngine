package com.dayu.lotto.service;

import java.io.InputStream;

import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.entity.LottoTicket;

public interface LottoService  {
    
	void uploadResults(InputStream is) throws Exception;
	
	int numberToPick();
	
	int supplementaries();
    
    int pool();
    
    String draw(WeightedSelector selector, int draw, int games);
    
    void checkTicket(LottoTicket lottoTicket);
    
    LottoTicket findByTicketId(String id);
}
