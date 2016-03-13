package com.dayu.lotto.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.service.OZLottoService;

@Controller
public class OZLottoController {
	@Autowired
	private OZLottoService ozLottoService;
	
	@RequestMapping(value="/ozLotto", method=RequestMethod.GET)
	public ModelAndView ozLotto()
    {
    	ModelAndView modelAndView = new ModelAndView("ozLotto");	
    	modelAndView.addObject("tickets", ozLottoService.findAllTickets());
    	modelAndView.addObject("result", ozLottoService.findLast(1).get(0));
    	return modelAndView;
    }
	
	@RequestMapping(value="/ozLotto/ticket/{ticketId}", method=RequestMethod.GET)
	public ModelAndView saturdayLottoTicket(@PathVariable String ticketId)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/ozLottoTicket");	
    	modelAndView.addObject("ticket", ozLottoService.findByTicketId(ticketId));
    	return modelAndView;
	}
	
	@RequestMapping(value="/ozLotto/ticket/new", method=RequestMethod.POST)
	public ModelAndView saturdayLottoTicket(@RequestParam("draws") String draws, @RequestParam("games") String games)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/ozLottoTicket");	
		String ticketId = ozLottoService.draw(new WeightedSelector(), Integer.parseInt(draws), Integer.parseInt(games));
    	modelAndView.addObject("ticket", ozLottoService.findByTicketId(ticketId));
    	return modelAndView;
	}
}
