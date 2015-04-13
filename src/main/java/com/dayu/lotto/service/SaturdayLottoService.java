package com.dayu.lotto.service;

import java.util.List;

import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;

public interface SaturdayLottoService extends LottoService {
    
    List<SaturdayLottoTicket> findTicketsByDraw(int draw);
    
    List<SaturdayLottoTicket> findAllTickets();
    
    List<SaturdayLottoResult> findLast(int limit);
    
    SaturdayLottoResult findResultByDraw(int draw);
}
