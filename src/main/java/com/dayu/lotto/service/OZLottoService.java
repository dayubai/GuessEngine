package com.dayu.lotto.service;

import java.util.List;

import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.OZLottoTicket;

public interface OZLottoService extends LottoService {
	List<OZLottoTicket> findTicketsByDraw(int draw);

	List<OZLottoTicket> findAllTickets();

	List<OZLottoResult> findLast(int limit);

	OZLottoResult findResultByDraw(int draw);
}
