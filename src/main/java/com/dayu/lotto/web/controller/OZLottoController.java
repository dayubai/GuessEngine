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
import com.dayu.lotto.entity.OZLottoPrediction;
import com.dayu.lotto.entity.OZLottoResult;
import com.dayu.lotto.entity.OZLottoTicket;
import com.dayu.lotto.service.LottoService;

@Controller
public class OZLottoController {
	@Autowired
	private LottoService<OZLottoTicket, OZLottoResult, OZLottoPrediction> ozLottoService;
	
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
	
	@RequestMapping(value="/ozLotto/uploadResult", method=RequestMethod.POST)
	public ModelAndView ozLottoUploadResult(@RequestParam("file") MultipartFile file)
	{
		try {
			ozLottoService.uploadResults(file.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return ozLotto();
	}
	
	@RequestMapping(value="/ozLotto/rfpredict/run", method=RequestMethod.POST)
	public ModelAndView ozLottoRfPredict(@RequestParam("draw") String draw)
	{
		ozLottoService.generateNumberPredictions(draw);
		return ozLottoRfPredictDetailView(draw);
	}
	
	@RequestMapping(value="/ozLotto/rfpredict/draw/{draw}", method=RequestMethod.GET)
	public ModelAndView ozLottoRfPredictDetailView(@PathVariable("draw") String draw)
	{
		ModelAndView modelAndView = new ModelAndView("rfpredict/detail/ozLottoPredictionDetail");
		modelAndView.addObject("prediction", ozLottoService.findForestRandomPredictionByDraw(Integer.parseInt(draw)));
    	return modelAndView;
	}
	
	@RequestMapping(value="/ozLotto/rfpredict", method=RequestMethod.GET)
	public ModelAndView ozLottoRfPredictView()
	{
		ModelAndView modelAndView = new ModelAndView("rfpredict/ozLottoPrediction");
		modelAndView.addObject("predictions", ozLottoService.findAllForestRandomPredictions());
    	return modelAndView;
	}
}
