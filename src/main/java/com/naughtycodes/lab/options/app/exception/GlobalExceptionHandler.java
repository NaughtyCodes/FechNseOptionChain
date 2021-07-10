package com.naughtycodes.lab.options.app.exception;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

	@ExceptionHandler(value={AsyncRequestTimeoutException.class})
	public ResponseEntity<String> AsyncRequestTimeoutExceptionHandler(Exception e, WebRequest request){
		LOGGER.info("Exception Message ==> "+e.getMessage());
		LOGGER.info("Exception Class ==> "+e.getClass().getName());
		
		String responseText = "An error occured in the request, please try later.";
		
		if(e instanceof AsyncRequestTimeoutException){
			for(String s : NseOptionSymbols.symbols) {
				if(!fetchOptionsDataService.finalCollectedData.containsKey(s)) {
					LOGGER.info("Missing Symbols after rsi failed : "+s);
				} 
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
