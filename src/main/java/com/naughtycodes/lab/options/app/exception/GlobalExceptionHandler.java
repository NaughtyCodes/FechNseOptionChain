package com.naughtycodes.lab.options.app.exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@Autowired 
	private FetchOptionsDataService<?, ?, ?> fetchOptionsDataService;
	@Autowired
	private AppUtils appUtils;

	@ExceptionHandler(value={AsyncRequestTimeoutException.class})
	public ResponseEntity<String> AsyncRequestTimeoutExceptionHandler(Exception e, WebRequest request){
		LOGGER.info("Exception Message ==> "+e.getMessage());
		LOGGER.info("Exception Class ==> "+e.getClass().getName());
		
		String responseText = "An error occured in the request, please try later.";
		
		if(e instanceof AsyncRequestTimeoutException){
			String expiryDate = appUtils.getLastThursday(((ServletWebRequest)request).getRequest().getRequestURI().split("/")[3], "");
			boolean gitFlag   = ((ServletWebRequest) request).getRequest().getParameter("gitFlag").equalsIgnoreCase("true");
			
			var missingList = appUtils.missingList(fetchOptionsDataService.finalCollectedData);
//TODO			
//			if(missingList.length > 0 && fetchOptionsDataService.version == 1) {
//				try {
//					fetchOptionsDataService.getNseOptionsData(expiryDate, missingList, gitFlag, null);					
//				} catch (InterruptedException | ExecutionException | IOException e1) {
//					LOGGER.info("Exception Message ==> "+e.getMessage());
//					LOGGER.info("Exception Class ==> "+e.getClass().getName());
//					return new ResponseEntity<String>(responseText, HttpStatus.OK);
//				}
//			} 
			
			if(gitFlag) {
				fetchOptionsDataService.gitCommitAndWriteFile(expiryDate);	
			}
			
			responseText = new JSONObject(fetchOptionsDataService.finalCollectedData).toString();
			return new ResponseEntity<String>(responseText, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>(responseText, HttpStatus.OK);
		}
		
	}
	
	@ExceptionHandler(value={Exception.class})
	public ResponseEntity<String> handleException(Exception e, WebRequest request){
		LOGGER.info("Exception Message ==> "+e.getMessage());
		LOGGER.info("Exception Class ==> "+e.getClass().getName());
		String responseText = "An error occured in the request, please try later.";
		return new ResponseEntity<String>(responseText, HttpStatus.OK);
	}
  
}
