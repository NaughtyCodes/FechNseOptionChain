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
        		appUtils.getFileName(expiryDate.substring(2,5),expiryDate.substring(5,9)), 
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
            //expiry and strikeprice can be null	
            case "GetRsi":
            	url = "https://www.traderscockpit.com/?pageView=rsi-indicator-rsi-chart&type=rsi&symbol=";
            	url = url+symbol;
            	return url;
            default:
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+symbol+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
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
		System.out.println(queryParams.get("companySymbol").get(0));
		String rsi = this.getRsi(queryParams.get("companySymbol").get(0));

		switch(parserKey)
		{
		    case "ByExpiry":
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut, rsi);
		    case "ByPrice":
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut, rsi);
		    default:
		    	return appUtils.parseHtmlGetOptionsChain(htmlOut);
		}
        
		
	}
	
	public void getAsyncAllOptionDataFromNSE(String parserKey, String expiryDate, boolean gitFlag, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException{
		
		ConcurrentHashMap<String, JSONObject> optionData = new ConcurrentHashMap<>();
		
        // create a client
        var client = HttpClient.newHttpClient();
		List<CompletableFuture> ls = new ArrayList<>();
		List<CompletableFuture> rsiList = new ArrayList<>();
		
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
	        	LOGGER.info(f+" ==> completed");
	        	try {
	        		var risResponseFuture = HttpClient.newHttpClient().sendAsync(this.buildRequest(f), HttpResponse.BodyHandlers.ofString());
	        		rsiList.add(risResponseFuture);
	        		risResponseFuture.thenAccept(rsiResponse -> {
	        			optionData.put( f,
	        					new JSONObject(
	        							appUtils.parseHtmlGetOptionsChain(
	        									htmlOut,appUtils.parseHtmlGetRsi(
	        											rsiResponse.body()
	        									)
	        								)
	        							) 
	        						);
	        					});
				} catch (JSONException | IOException | InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}	        	
	        	
	        });
	        
		}
		
		CompletableFuture<Void> totalFuture = CompletableFuture.allOf(ls.toArray(new CompletableFuture[ls.size()]));
		totalFuture.thenAccept(s -> {
			CompletableFuture<Void> rsiTotalFuture = CompletableFuture.allOf(rsiList.toArray(new CompletableFuture[rsiList.size()]));
			rsiTotalFuture.thenAccept(a -> {
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
					
					dfr.setResult(new JSONObject(optionData).toString());
					
				} catch (IOException | GitAPIException e) {
					e.printStackTrace();
				}
				
			});
		});
		
	}

	public HttpRequest buildRequest(String symbol) throws InterruptedException, ExecutionException, IOException {
	 
		 String url = this.constructUrl("GetRsi", symbol, null, null);
	
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