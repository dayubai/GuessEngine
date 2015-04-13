package com.dayu.lotto.service;

import java.util.List;

import com.dayu.lotto.entity.PowerBallResult;
import com.dayu.lotto.entity.PowerBallTicket;

public interface PowerBallService extends LottoService {
	
	int powerballPool();
	
	List<PowerBallTicket> findTicketsByDraw(int draw);
	
	List<PowerBallTicket> findAllTickets();
	
	List<PowerBallResult> findLast(int limit);
	
	PowerBallResult findResultByDraw(int draw);
}
