package com.dayu.lotto.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LottoController {
	
	@RequestMapping("/")
	public ModelAndView home()
    {
    	ModelAndView modelAndView = new ModelAndView("home");	
    	
    	return modelAndView;
    }
	
}
