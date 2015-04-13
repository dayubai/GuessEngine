package com.dayu.lotto.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.service.PowerBallService;

@Controller
public class PowerballController {
	@Autowired
	private PowerBallService powerBallService;
	
	@RequestMapping(value="/powerball", method=RequestMethod.GET)
	public ModelAndView powerball()
    {
    	ModelAndView modelAndView = new ModelAndView("powerball");	
    	modelAndView.addObject("tickets", powerBallService.findAllTickets());
    	modelAndView.addObject("result", powerBallService.findLast(1).get(0));
    	
    	return modelAndView;
    }
	
	@RequestMapping(value="/powerball/ticket/{ticketId}", method=RequestMethod.GET)
	public ModelAndView saturdayLottoTicket(@PathVariable String ticketId)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/powerballTicket");	
    	modelAndView.addObject("ticket", powerBallService.findByTicketId(ticketId));
    	return modelAndView;
	}
	
	@RequestMapping(value="/powerball/ticket/new", method=RequestMethod.POST)
	public ModelAndView saturdayLottoTicket(@RequestParam("draws") String draws, @RequestParam("games") String games)
	{
		ModelAndView modelAndView = new ModelAndView("ticket/powerballTicket");	
		String ticketId = powerBallService.draw(new WeightedSelector(), Integer.parseInt(draws), Integer.parseInt(games));
    	modelAndView.addObject("ticket", powerBallService.findByTicketId(ticketId));
    	return modelAndView;
	}
}
