package com.naughtycodes.lab.options.app.batch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;


public class TaskFetchOptionChain implements Tasklet {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(TaskFetchOptionChain.class);
	public FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	public AppUtils appUtils;
	
	private boolean gitFlag;
	public String executeFor = "CURRENT_MONTH";
	
	public TaskFetchOptionChain(FetchOptionsDataService fetchOptionsDataService,AppUtils appUtils, String executeFor, boolean gitFlag) {
		this.appUtils = appUtils;
		this.fetchOptionsDataService = fetchOptionsDataService;
		this.executeFor = executeFor;
		this.gitFlag = gitFlag;
	}
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception
    {
    	LOGGER.info("Batch started to update nse options data to git repo");
        LOGGER.info("Git repo : https://github.com/hellomohanakrishnan/NseOptionsChainData");

        String month = "";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		Calendar calendar = Calendar.getInstance();
		DeferredResult<String> dfr;
		
        switch(executeFor) {
        case "CURRENT_MONTH":
        	dfr = new DeferredResult<String>((long) 300000);
        	month = simpleDateFormat.format(new Date());
        	month = appUtils.getLastThursday(month, "");
        	fetchOptionsDataService.isBatchMode = true;
        	fetchOptionsDataService.getNseOptionsData(month, NseOptionSymbols.symbols, this.gitFlag, dfr);
        	dfr.onCompletion(()->{
        		LOGGER.info("Batch job has completed for "+executeFor);
        	});
        	Thread.sleep(200000); 
        	break;
        case "NEXT_MONTH":
        	dfr = new DeferredResult<String>((long) 300000);
    		calendar.add(Calendar.MONTH, 1);
    		month = simpleDateFormat.format(calendar.getTime());
    		month = appUtils.getLastThursday(month, "");
    		fetchOptionsDataService.isBatchMode = true;
        	fetchOptionsDataService.getNseOptionsData(month, NseOptionSymbols.symbols, this.gitFlag, dfr);
        	dfr.onCompletion(()->{
        		LOGGER.info("Batch job has completed for "+executeFor);
        	});
        	Thread.sleep(200000); 
        	break;
        case "LAST_MONTH":
        	dfr = new DeferredResult<String>((long) 300000);
    		calendar.add(Calendar.MONTH, 2);
    		month = simpleDateFormat.format(calendar.getTime());
    		month = appUtils.getLastThursday(month, "");
    		fetchOptionsDataService.isBatchMode = true;
        	fetchOptionsDataService.getNseOptionsData(month, NseOptionSymbols.symbols, this.gitFlag, dfr);
        	dfr.onCompletion(()->{
        		LOGGER.info("Batch job has completed for "+executeFor);
        	});
        	Thread.sleep(200000); 
        	break;
        }

		
//        new BatchJobTest().testBeat();
        
        LOGGER.info("Batch ended to update nse options data to git repo");
        return RepeatStatus.FINISHED;
    }   
}
