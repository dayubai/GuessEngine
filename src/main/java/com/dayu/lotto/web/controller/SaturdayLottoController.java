package com.dayu.lotto.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.service.SaturdayLottoService;

@Controller
public class SaturdayLottoController {
	@Autowired
	private SaturdayLottoService saturdayLottoService;
	
	@RequestMapping(value="/saturdayLotto", method=RequestMethod.GET)
	public ModelAndView saturdayLotto()
    {
    	ModelAndView modelAndView = new ModelAndView("saturdayLotto");	
    	modelAndView.addObject("tickets", saturdayLottoService.findAllTickets());
    	modelAndView.addObject("result", saturdayLottoService.findLast(1).get(0));
    	return modelAndView;
    }
	
	@RequestMapping(value="/saturdayLotto/ticket/{ticketId}", method=RequestMethod.GET)
	public ModelAndView saturdayLottoTicket(@PathVariable String ticketId)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/saturdayLottoTicket");	
    	modelAndView.addObject("ticket", saturdayLottoService.findByTicketId(ticketId));
    	return modelAndView;
	}
	
	@RequestMapping(value="/saturdayLotto/ticket/new", method=RequestMethod.POST)
	public ModelAndView saturdayLottoTicket(@RequestParam("draws") String draws, @RequestParam("games") String games)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/saturdayLottoTicket");	
		String ticketId = saturdayLottoService.draw(new WeightedSelector(), Integer.parseInt(draws), Integer.parseInt(games));
    	modelAndView.addObject("ticket", saturdayLottoService.findByTicketId(ticketId));
    	return modelAndView;
	}
}
