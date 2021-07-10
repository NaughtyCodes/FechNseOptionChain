package com.naughtycodes.lab.options.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import javax.websocket.server.PathParam;

import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.LabOptionsApplication;
import com.naughtycodes.lab.options.app.config.GitConfig;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

import io.micrometer.core.annotation.Timed;

@RestController
@RequestMapping(value = "/opt")
public class OptionsWebController {
	
	@Autowired
	private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	
	@Autowired GitConfig gitConfig;
	
	@Autowired
	private AppUtils appUtils;
	
	private static final Logger LOGGER=LoggerFactory.getLogger(OptionsWebController.class);

	@GetMapping(value = "/by/expiry/{symbol}/{mon}")
	public String fetchByExpiry(
				@PathVariable ("symbol") String symbol, 
				@PathVariable("mon") String mon
			) throws InterruptedException, ExecutionException, IOException {
		
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
	
	@GetMapping(value = "/by/expiry/all/{mon}")
	@Timed
	public DeferredResult<String> fetchAsyncAllByExpiry(
			@RequestParam(value = "gitFlag", required = false, defaultValue = "false") boolean gitFlag,
			@PathVariable("mon") String mon
			) throws InterruptedException, ExecutionException, IOException {
		
		final String parseKey = "ByExpiry";
		String date = appUtils.getLastThursday(mon, "");
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, gitFlag, dfr);
		
		return dfr;
	}
	
	@GetMapping(value = "/syncgit")
	public String syncGit() throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		gitConfig.pushToGit();
		return "updated to git";
	}
	
	@GetMapping(value = "/test/{mon}")
	@Timed
	public DeferredResult<String> fetchAsyncAllByExpiryTest(
			@RequestParam(value = "gitFlag", required = false, defaultValue = "false") boolean gitFlag, 
			@PathVariable("mon") String mon) {
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		try {
			final String parseKey = "ByExpiry";
			String date = appUtils.getLastThursday(mon, "");
			fetchOptionsDataService.getNseOptionsData(parseKey, date, null, gitFlag, dfr);
			return dfr;	
		} catch(Exception e) {
			LOGGER.info("TimeoutException =>>");
			return dfr;	
		}
		
	}
	
	@GetMapping(value = "/{mon}")
	@Timed
	public DeferredResult<String> fetchAllOptions(
			@RequestParam(value = "gitFlag", required = false, defaultValue = "false") boolean gitFlag, 
			@PathVariable("mon") String mon) {
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		try {
			String date = appUtils.getLastThursday(mon, "");
			fetchOptionsDataService.getAllOptions(date, dfr);
			return dfr;	
		} catch(Exception e) {
			LOGGER.info("TimeoutException =>>");
			return dfr;	
		}
		
	}
	
	@GetMapping(value = "/rsi")
	@Timed
	public DeferredResult<String> fetchAllOptions(
			@RequestParam(value = "gitFlag", required = false, defaultValue = "false") boolean gitFlag) {
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		try {
			fetchOptionsDataService.getAllCurrentRsi(dfr);
			return dfr;	
		} catch(Exception e) {
			LOGGER.info("TimeoutException =>>");
			return dfr;	
		}
		
	}
	
	@GetMapping(value = "/price")
	@Timed
	public DeferredResult<String> fetchAllPrice(
			@RequestParam(value = "gitFlag", required = false, defaultValue = "false") boolean gitFlag) {
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		try {
			fetchOptionsDataService.getAllCurrentPrice(dfr);
			return dfr;	
		} catch(Exception e) {
			LOGGER.info("TimeoutException =>>");
			return dfr;	
		}
		
	}
	
}
