package com.dayu.lotto.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.dayu.lotto.service.OZLottoService;
import com.dayu.lotto.service.PowerBallService;
import com.dayu.lotto.service.SaturdayLottoService;

@Controller
public class LottoController {
	
	
	@Autowired
	private OZLottoService ozLottoService;
	
	@RequestMapping("/")
	public ModelAndView home()
    {
    	ModelAndView modelAndView = new ModelAndView("home");	
    	
    	return modelAndView;
    }
	
	
	
	
	
	@RequestMapping("/ozLotto")
	public ModelAndView ozLotto()
    {
    	ModelAndView modelAndView = new ModelAndView("ozLotto");	
    	modelAndView.addObject("tickets", ozLottoService.findAllTickets());
    	return modelAndView;
    }
}
