package com.dayu.lotto.web.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.dayu.lotto.algorithm.WeightedSelector;
import com.dayu.lotto.entity.SaturdayLottoPrediction;
import com.dayu.lotto.entity.SaturdayLottoResult;
import com.dayu.lotto.entity.SaturdayLottoTicket;
import com.dayu.lotto.service.LottoService;

@Controller
public class SaturdayLottoController {
	@Autowired
	private LottoService<SaturdayLottoTicket, SaturdayLottoResult, SaturdayLottoPrediction> saturdayLottoService;
	
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
	
	@RequestMapping(value="/saturdayLotto/uploadResult", method=RequestMethod.POST)
	public ModelAndView saturdayLottoUploadResult(@RequestParam("file") MultipartFile file)
	{
		try {
			saturdayLottoService.uploadResults(file.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return saturdayLotto();
	}
	
	@RequestMapping(value="/saturdayLotto/rfpredict/run", method=RequestMethod.POST)
	public ModelAndView saturdayLottoRfPredict(@RequestParam("draw") String draw)
	{
		saturdayLottoService.generateNumberPredictions(draw);
		return saturdayLottoRfPredictDetailView(draw);
	}
	
	@RequestMapping(value="/saturdayLotto/rfpredict/draw/{draw}", method=RequestMethod.GET)
	public ModelAndView saturdayLottoRfPredictDetailView(@PathVariable("draw") String draw)
	{
		ModelAndView modelAndView = new ModelAndView("rfpredict/detail/saturdayLottoPredictionDetail");
		modelAndView.addObject("prediction", saturdayLottoService.findForestRandomPredictionByDraw(Integer.parseInt(draw)));
    	return modelAndView;
	}
	
	@RequestMapping(value="/saturdayLotto/rfpredict", method=RequestMethod.GET)
	public ModelAndView saturdayLottoRfPredictView()
	{
		ModelAndView modelAndView = new ModelAndView("rfpredict/saturdayLottoPrediction");
		modelAndView.addObject("predictions", saturdayLottoService.findAllForestRandomPredictions());
    	return modelAndView;
	}
}
