package com.naughtycodes.lab.options.app.services;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@Service
public class FetchOptionsDataService<T, V, K> {
	
	@Autowired
	AppUtils appUtils;
			
	public FetchOptionsDataService() {
		
	}
	
	public String getAllData(String parserKey, String expiryDate, String strikePrice) throws InterruptedException, ExecutionException, IOException {
		HashMap optionData = new HashMap<String, T>();
		FetchOptionsDataService fetch = new FetchOptionsDataService();
		NseOptionSymbols symbols = new NseOptionSymbols();
		Field[] fields = symbols.getClass().getDeclaredFields();
		for(String f : NseOptionSymbols.symbols){
			optionData.put(
					f,
					new JSONObject(fetch.getOptionDataFromNSE(
							fetch.constructUrl(parserKey, f, expiryDate, strikePrice),
							""
							))
					);
		}
		
		appUtils.writeOutAsFile
        (
        		appUtils.getFileName(expiryDate.substring(2,5),Integer.valueOf(expiryDate.substring(5,9))), 
        		new JSONObject(optionData).toString(), 
        		"json"
        );
		JSONObject jsonOut = new JSONObject(optionData);
		
		return jsonOut.toString();
	}
	
	public String constructUrl(String parserKey, String symbol, String expiry, String strikePrice){
		
		String url = "";
		
        switch(parserKey)
        {
            case "ByExpiry":
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+symbol+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
        		url = url+"date="+expiry;
        		System.out.println(url);
            	return url;
            case "ByPrice":
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionDates.jsp?companySymbol=";
        		url = url+symbol+"&series=EQ&indexSymbol=NIFTY&instrument=OPTSTK&";
        		url = url+"strike="+strikePrice;
            	return url;
            default:
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+symbol+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
        		url = url+"date="+expiry;
        		return url;
        }
		
	}
	
	public String getOptionDataFromNSE(String url, String parserKey) throws InterruptedException, ExecutionException{
		String htmlOut= "";
		
        // create a client
        var client = HttpClient.newHttpClient();

        // create a request
        var request = HttpRequest.newBuilder(
            URI.create(url))
            .header("accept", "text/html,application/xhtml+xml")
            .build();

        // use the client to send the request
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
      
        // This blocks until the request is complete
        var response = responseFuture.get();

        // the response:
        htmlOut = response.body();
        
        switch(parserKey)
        {
            case "ByExpiry":
            	return appUtils.parseHtmlData(htmlOut);
            case "ByPrice":
            	return appUtils.parseHtmlData(htmlOut);
            default:
            	return appUtils.parseHtmlData(htmlOut);
        }
		
	}
	
	public String getAsyncAllOptionDataFromNSE(String parserKey, String expiryDate, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException{
		
		ConcurrentHashMap<String, JSONObject> optionData = new ConcurrentHashMap<>();
		
        // create a client
        var client = HttpClient.newHttpClient();
		List<CompletableFuture> ls = new ArrayList<>();
		
		for(String f : NseOptionSymbols.symbols){
			
			var url = this.constructUrl(parserKey, f, expiryDate, "");
			
	        // create a request
	        var request = HttpRequest.newBuilder(
	            URI.create(url))
	            .header("accept", "text/html,application/xhtml+xml")
	            .build();
	        
	        // use the client to send the request
	        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
	        
	        // This blocks until the request is complete
	        ls.add(responseFuture);
	        
	        var response = responseFuture.thenAccept(httpResponse -> {
	        	 var htmlOut = httpResponse.body();
	        	 System.out.println(f+" ==> completed");
	        	 optionData.put( f,new JSONObject(appUtils.parseHtmlData(htmlOut)) );
	        });
	        
		}
		
		CompletableFuture<Void> totalFuture = CompletableFuture.allOf(ls.toArray(new CompletableFuture[ls.size()]));
		totalFuture.thenAccept(s -> {
			appUtils.writeOutAsFile
		        (
		        		appUtils.getFileName(expiryDate.substring(2,5),Integer.valueOf(expiryDate.substring(5,9))), 
		        		new JSONObject(optionData).toString(), 
		        		"json"
		        );
			dfr.setResult(new JSONObject(optionData).toString());
		});

		return new JSONObject(optionData).toString();
		
	}
	
}