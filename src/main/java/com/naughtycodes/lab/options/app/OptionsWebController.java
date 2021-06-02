package com.naughtycodes.lab.options.app;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping(value = "/opt")
public class OptionsWebController {
	
	@Autowired	
	FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;

	@GetMapping(value = "/by/expiry/{symbol}/{date}")
	public String fetchByExpiry(
				@PathVariable ("symbol") String symbol, 
				@PathVariable("date") String date
			) throws InterruptedException, ExecutionException {
		
		final String parseKey = "ByExpiry";
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
	
	@GetMapping(value = "/by/expiry/all/{date}")
	public String fetchAllByExpiry(
				@PathVariable("date") String date
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		return fetchOptionsDataService.getAllData(parseKey, date, "");
	}
	
	@GetMapping(value = "/by/async/all/{date}")
	public DeferredResult<String> fetchAsyncAllByExpiry(
				@PathVariable("date") String date
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, dfr);
		
		return dfr;
	}
	
}
