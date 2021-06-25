package com.naughtycodes.lab.options.app.services;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;

import com.naughtycodes.lab.options.app.LabOptionsApplication;
import com.naughtycodes.lab.options.app.config.GitConfig;
import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@Service
public class FetchOptionsDataService<T, V, K> {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FetchOptionsDataService.class);
	
	@Autowired AppUtils appUtils;
	@Autowired GitConfig gitConfig;
			
	public FetchOptionsDataService() {
		
	}
	
	
	public String constructUrl(String parserKey, String symbol, String expiry, String strikePrice){
		
		String url = "";
		
        switch(parserKey)
        {
            case "ByExpiry":
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+URLEncoder.encode(symbol)+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
        		url = url+"date="+expiry;
        		//LOGGER.info(url);
            	return url;
            case "ByPrice":
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionDates.jsp?companySymbol=";
        		url = url+URLEncoder.encode(symbol)+"&series=EQ&indexSymbol=NIFTY&instrument=OPTSTK&";
        		url = url+"strike="+strikePrice;
            	return url;
            //expiry and strikeprice can be null	
            case "GetRsi":
            	url = "https://www.traderscockpit.com/?pageView=rsi-indicator-rsi-chart&type=rsi&symbol=";
            	url = url+URLEncoder.encode(symbol);
            	return url;
            //expiry and strikeprice can be null	
            case "GetStockPrice":
            	url = "https://www1.nseindia.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=";
            	url = url+URLEncoder.encode(symbol);
            	return url;
            default:
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+URLEncoder.encode(symbol)+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
        		url = url+"date="+expiry;
        		return url;
        }
		
	}
	
	public String getOptionDataFromNSE(String url, String parserKey) throws InterruptedException, ExecutionException, IOException {
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
        
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString(url).build().getQueryParams();
		LOGGER.info(queryParams.get("companySymbol").get(0));
		String rsi = this.getRsi(queryParams.get("companySymbol").get(0));

		switch(parserKey)
		{
		    case "ByExpiry":
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut, rsi, null);
		    case "ByPrice":
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut, rsi, null);
		    default:
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut);
		}
        
		
	}
	
	public void getAsyncAllOptionDataFromNSE(String parserKey, String expiryDate, boolean gitFlag, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException{
		
		ConcurrentHashMap<String, JSONObject> optionData = new ConcurrentHashMap<>();
		
		List<CompletableFuture> ls = new ArrayList<>();
		List<CompletableFuture> rsiList = new ArrayList<>();
		List<CompletableFuture> stPriceList = new ArrayList<>();
		
		for(String f : NseOptionSymbols.symbols){
			
			var url = this.constructUrl(parserKey, f, expiryDate, "");
			
	        // create a request
	        var request = HttpRequest.newBuilder(
	            URI.create(url))
	            .header("accept", "text/html,application/xhtml+xml")
	            .build();
	        
			var rsiResponseFuture = HttpClient.newHttpClient().sendAsync(this.buildRequest("GetRsi",f), HttpResponse.BodyHandlers.ofString());
			rsiList.add(rsiResponseFuture);
			rsiResponseFuture.thenAccept(rsiResponse -> {
	        	//LOGGER.info(f+"::OC ==> completed");
	        	try {
	        		var stPriceResponseFuture = HttpClient.newHttpClient().sendAsync(this.buildRequest("GetStockPrice",f), HttpResponse.BodyHandlers.ofString());
	        		stPriceList.add(stPriceResponseFuture);
	        		stPriceResponseFuture.thenAccept(stPriceResponse -> {
	        			//LOGGER.info(f+"::STPR ==> completed");
						try {
							var responseFuture = HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
					        ls.add(responseFuture);
					        responseFuture.thenAccept(opChainResponse -> {
			        			optionData.put( f, new JSONObject(
			        									appUtils.parseHtmlGetOptionsChain(opChainResponse.body(),
			        									appUtils.parseHtmlGetRsi(rsiResponse.body()),
			        									appUtils.parseHtmlGetStPrice(stPriceResponse.body())
			        								)
			        							) 
			        						);
			        			LOGGER.info(f+":: ==> completed");
			        		});
			        		
						} catch (Exception e) {
							LOGGER.info("GetRsi has failed : Timeout");
							for(String n : NseOptionSymbols.symbols) {
								if(!optionData.containsKey(f)) {
									LOGGER.info("Missing Symbols after rsi failed : "+n);
								} 
							}
							e.printStackTrace();
						}
	        			
	        		});
				} catch (JSONException | IOException | InterruptedException | ExecutionException e) {
					LOGGER.info("GetRsi has failed : Timeout");
					for(String n : NseOptionSymbols.symbols) {
						if(!optionData.containsKey(f)) {
							LOGGER.info("Missing Symbols after rsi failed : "+n);
						} 
					}
					e.printStackTrace();
				}	        	
	        	
	        });
	        
		}
		
		CompletableFuture<Void> rsiTotalFuture = CompletableFuture.allOf(rsiList.toArray(new CompletableFuture[rsiList.size()]));
		rsiTotalFuture.thenAccept(p -> {
			//LOGGER.info("completed optional chain calls");
			
			CompletableFuture<Void> stPriceTotalFuture = CompletableFuture.allOf(stPriceList.toArray(new CompletableFuture[stPriceList.size()]));
			stPriceTotalFuture.thenAccept(a -> {
				//LOGGER.info("completed stock price calls");
				
				CompletableFuture<Void> totalFuture = CompletableFuture.allOf(ls.toArray(new CompletableFuture[ls.size()]));		
				totalFuture.thenAccept(s -> {				
					//LOGGER.info("completed RSI calls");
					
					try {
						if(gitFlag) {
							appUtils.writeOutAsFile
						        (
						        		appUtils.getFileName(expiryDate.substring(2,5),expiryDate.substring(5,9)), 
						        		new JSONObject(optionData).toString(), 
						        		"json"
						        );
							
							appUtils.writeOutAsFile
						        (
						        		"LastUpdatedData", 
						        		new JSONObject(optionData).toString(), 
						        		"json"
						        );
							
							gitConfig.pushToGit();
						}
						
						for(String f : NseOptionSymbols.symbols) {
							if(!optionData.containsKey(f)) {
								LOGGER.info("Missing Symbols : "+f);
							} 
						}
						
						dfr.setResult(new JSONObject(optionData).toString());
						
					} catch (IOException | GitAPIException e) {
						e.printStackTrace();
					}
				});	
				
			});
		});
		
	}

	public HttpRequest buildRequest(String key, String symbol) throws InterruptedException, ExecutionException, IOException {
	 
		 String url = this.constructUrl(key, symbol, null, null);
	
	     // create a request
	     var request = HttpRequest.newBuilder(
	         URI.create(url))
	         .header("accept", "text/html,application/xhtml+xml")
	         .build();
	
	     return request;
	
	}

	public String getRsi(String symbol) throws InterruptedException, ExecutionException, IOException {
		 
		 String url = this.constructUrl("GetRsi", symbol, null, null);
		 
		 var client = HttpClient.newHttpClient();

	     // create a request
	     var request = HttpRequest.newBuilder(
	         URI.create(url))
	         .header("accept", "text/html,application/xhtml+xml")
	         .build();

	     // use the client to send the request
	     var responseFuture = client.send(request, HttpResponse.BodyHandlers.ofString());
	   
	     // This blocks until the request is complete
	     var response = responseFuture.body();
	
	     // the response:
	     String htmlOut = response;
	     
		 return appUtils.parseHtmlGetRsi(htmlOut);
	 }

}