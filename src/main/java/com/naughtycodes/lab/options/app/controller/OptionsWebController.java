package com.naughtycodes.lab.options.app.controller;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.websocket.server.PathParam;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.LabOptionsApplication;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@RestController
@RequestMapping(value = "/opt")
public class OptionsWebController {
	
	@Autowired
	private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	
	@Autowired
	private AppUtils appUtils;
	
	private static final Logger LOGGER=LoggerFactory.getLogger(OptionsWebController.class);

	@GetMapping(value = "/by/expiry/{symbol}/{mon}")
	public String fetchByExpiry(
				@PathVariable ("symbol") String symbol, 
				@PathVariable("mon") String mon
			) throws InterruptedException, ExecutionException {
		
		final String parseKey = "ByExpiry";
		String date = appUtils.getLastThursday(mon, "");
		String url = fetchOptionsDataService.constructUrl(parseKey, symbol, date, "");
		return fetchOptionsDataService.getOptionDataFromNSE(url, parseKey);
	}
	
	@GetMapping(value = "/by/price/{symbol}/{price}")
	public String fetchByPrice(
				@PathVariable("symbol") String symbol,
				@PathVariable("price") String price
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByPrice";
		String url = fetchOptionsDataService.constructUrl(parseKey, symbol, "", price);
		return fetchOptionsDataService.getOptionDataFromNSE(url, parseKey);
	}
	
	@GetMapping(value = "/by/expiry/sync/all/{date}")
	public String fetchAllByExpiry(
				@PathVariable("date") String date
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		return fetchOptionsDataService.getAllData(parseKey, date, "");
	}
	
	@GetMapping(value = "/by/expiry/all/{mon}")
	public DeferredResult<String> fetchAsyncAllByExpiry(
			@RequestParam boolean gitFlag, @PathVariable("mon") String mon
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		String date = appUtils.getLastThursday(mon, "");
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, gitFlag, dfr);
		
		return dfr;
	}
	
	@GetMapping(value = "/by/expiry/all/{mon}/{year}")
	public DeferredResult<String> fetchAsyncAllByExpiry(
			@RequestParam boolean gitFlag, @PathVariable("mon") String mon, @PathVariable("year") String year 
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		
		String date = appUtils.getLastThursday(mon, year);
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, gitFlag, dfr);
		
		return dfr;
	}
	
}
