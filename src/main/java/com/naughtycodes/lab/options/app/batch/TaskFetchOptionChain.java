package com.naughtycodes.lab.options.app.batch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

public class TaskFetchOptionChain implements Tasklet {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(TaskFetchOptionChain.class);
	@Autowired private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	@Autowired private AppUtils appUtils;
	 
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception
    {
    	LOGGER.info("Batch started to update nse options data to git repo");
        LOGGER.info("Git repo : https://github.com/hellomohanakrishnan/NseOptionsChainData");

//        final String parseKey = "ByExpiry";
//		SimpleDateFormat mon = new SimpleDateFormat("MMM");
//		SimpleDateFormat year = new SimpleDateFormat("yyyy");
//		mon.setTimeZone(TimeZone.getTimeZone("IST"));
//		year.setTimeZone(TimeZone.getTimeZone("IST"));
//		
//		String date = appUtils.getLastThursday(mon.format(new Date()).toString(), year.format(new Date()).toString()).toString();
//		
//		DeferredResult<String> dfr = new DeferredResult<String>((long) 300000);
//		fetchOptionsDataService.getAsyncAllOptionDataFromNSE(parseKey, date, true, dfr);
         
        LOGGER.info("Batch ended to update nse options data to git repo");
        return RepeatStatus.FINISHED;
    }   
}
