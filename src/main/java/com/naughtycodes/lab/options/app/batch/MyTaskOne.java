package com.naughtycodes.lab.options.app.batch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.controller.OptionsWebController;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

public class MyTaskOne implements Tasklet {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(MyTaskOne.class);
	@Autowired private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	@Autowired private AppUtils appUtils;
	 
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception
    {
    	LOGGER.info("Batch started to update nse options data to git repo");
        LOGGER.info("Git repo : https://github.com/hellomohanakrishnan/NseOptionsChainData");
 
		HashMap<String, Integer> MONTH = new HashMap<String, Integer>();
		MONTH.put("JAN", 0);
		MONTH.put("FEB", 1);
		MONTH.put("MAR", 2);
		MONTH.put("APR", 3);
		MONTH.put("MAY", 4);
		MONTH.put("JUN", 5);
		MONTH.put("JUL", 6);
		MONTH.put("AUG", 7);
		MONTH.put("SEP", 8);
		MONTH.put("OCT", 9);
		MONTH.put("NOV", 10);
		MONTH.put("DEC", 11);
		
		final String parseKey = "ByExpiry";
		SimpleDateFormat mon = new SimpleDateFormat("MMM");
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		mon.setTimeZone(TimeZone.getTimeZone("IST"));
		year.setTimeZone(TimeZone.getTimeZone("IST"));
		
		int m = MONTH.get(mon.format(new Date()));
		int y 	= Integer.valueOf(year.format(new Date()));
		String date = appUtils.getLastThursday(m, y).toString();
		
		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, dfr);
         
        LOGGER.info("Batch ended to update nse options data to git repo");
        return RepeatStatus.FINISHED;
    }   
}
