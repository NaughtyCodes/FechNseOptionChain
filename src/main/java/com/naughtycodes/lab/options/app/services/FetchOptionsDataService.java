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
import java.time.Duration;
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
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.util.UriComponentsBuilder;

import com.naughtycodes.lab.options.app.LabOptionsApplication;
import com.naughtycodes.lab.options.app.config.GitConfig;
import com.naughtycodes.lab.options.app.models.AppProperties;
import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.utils.AppUtils;

@Service
public class FetchOptionsDataService<T, V, K> {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(FetchOptionsDataService.class);
	private static ConcurrentHashMap<String, String> rsiData = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> optionsData = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, String> stocksData = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, JSONObject> finalCollectedData = new ConcurrentHashMap<>();

	@Autowired AppUtils appUtils;
	@Autowired GitConfig gitConfig;
	@Autowired private AppProperties appProperties;
			
	public FetchOptionsDataService() {
		
	}
	
	public String constructUrl(String parserKey, String symbol, String expiry, String strikePrice){
		
		String url = "";
		
        switch(parserKey)
        {
            case "ByExpiry":
            	url = appProperties.getGetNseOptionsByExpiryUrl();
            	url = url.replace("{symbol}", URLEncoder.encode(symbol));
            	url = url.replace("{expiry}", expiry);
            	return url;
            case "ByPrice":
            	url = appProperties.getGetNseOptionsByPriceUrl();
            	url = url.replace("{symbol}", URLEncoder.encode(symbol));
            	url = url.replace("{strikePrice}", strikePrice);
            	return url;
            //expiry and strikeprice can be null	
            case "GetRsi":
            	url = appProperties.getGetRsiUrl();
            	url = url.replace("{symbol}", URLEncoder.encode(symbol));
            	return url;
            //expiry and strikeprice can be null	
            case "GetStockPrice":
            	url = appProperties.getGetStockPriceUrl();
            	url = url.replace("{symbol}", URLEncoder.encode(symbol));
            	return url;
            default:
            	url = appProperties.getGetNseOptionsByExpiryUrl();
            	url = url.replace("{symbol}", URLEncoder.encode(symbol));
            	url = url.replace("{expiry}", expiry);
        		return url;
        }
		
	}
	
	public String getOptionDataFromNSE(String url, String parserKey) throws InterruptedException, ExecutionException, IOException {
		
		String htmlOut= "";
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
            URI.create(url))
            .header("accept", "text/html,application/xhtml+xml")
            .build();
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        var response = responseFuture.get();
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
	
	public String getRsi(String symbol) throws InterruptedException, ExecutionException, IOException {
		 
		 String url = this.constructUrl("GetRsi", symbol, null, null);
		 var client = HttpClient.newHttpClient();
	     var request = HttpRequest.newBuilder(
	         URI.create(url))
	         .header("accept", "text/html,application/xhtml+xml")
	         .build();

	     var responseFuture = client.send(request, HttpResponse.BodyHandlers.ofString());
	     var response = responseFuture.body();
	     
		 return appUtils.parseHtmlGetRsi(response);
	 }	

	public CompletableFuture<HttpResponse<String>> requestHandler(String parserKey, String symbol, String expiryDate, String strikePrice) throws InterruptedException, ExecutionException, IOException {
		try {
			String url = this.constructUrl(parserKey, symbol, expiryDate, strikePrice);
			//LOGGER.info(url);
		    var request = HttpRequest.newBuilder(URI.create(url))
		    		//.timeout(Duration.ofSeconds(20))
			        .header("accept", "text/html,application/xhtml+xml")
			        .build();
			return HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString());
		} catch(AsyncRequestTimeoutException e ) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage()+" :: "+symbol);
			return null;
		} catch (Exception e) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage()+" :: "+symbol);
			return null;
		}
	}
	
	public void getNseOptionsData(String expiryDate, String[] symbolList, boolean gitFlag, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException  {
		
		List<CompletableFuture> optionsTotalFuture = new ArrayList<>();
		List<CompletableFuture> stPriceTotalFuture = new ArrayList<>();
		List<CompletableFuture> rsiTotalFuture = new ArrayList<>();
		boolean isUploadGit = gitFlag;
		
		String [] symbols = (symbolList == null) ? NseOptionSymbols.symbols : symbolList;
		
		for(String symbol : symbols) {
			
			var rsiFuture = this.requestHandler("GetRsi", symbol, null, null);
			rsiTotalFuture.add(rsiFuture);
			rsiFuture.thenAccept(rsiResponse -> {
				//LOGGER.info("rsi get body completed ----> "+symbol);
				rsiData.put(symbol, rsiResponse.body());
				
				try {
					var stPriceFuture = this.requestHandler("GetStockPrice", symbol, null, null);
					stPriceTotalFuture.add(stPriceFuture);				
					stPriceFuture.thenAccept(stPriceResponse -> {
						//LOGGER.info("stPrise get body completed ----> "+symbol);
						stocksData.put(symbol, stPriceResponse.body());

						try {
							var optionFuture = this.requestHandler("ByExpiry", symbol, expiryDate, null);
							optionsTotalFuture.add(optionFuture);					
							optionFuture.thenAccept(optionResponse -> {
								//LOGGER.info("Optionsdata get body completed ----> "+symbol);
								optionsData.put(symbol, optionResponse.body());

							}).thenRun(()->{
								//LOGGER.info("completed ----> "+symbol);
									
								finalCollectedData.put( symbol, new JSONObject(
								appUtils.parseHtmlGetOptionsChain(optionsData.get(symbol),
									appUtils.parseHtmlGetRsi(rsiData.get(symbol)),
									appUtils.parseHtmlGetStPrice(stocksData.get(symbol)))
								));
									
							});
						} catch (InterruptedException | ExecutionException | IOException e) {
							LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
							this.handleException(e);
						}	
					});
				} catch (InterruptedException | ExecutionException | IOException e) {
					LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
					this.handleException(e);
				}
			});
				
    	}
		

		try {
			CompletableFuture.allOf(rsiTotalFuture.toArray(new CompletableFuture[rsiTotalFuture.size()])).thenRun(() -> {
				LOGGER.info("Completed Rsi...!");
				CompletableFuture.allOf(stPriceTotalFuture.toArray(new CompletableFuture[stPriceTotalFuture.size()])).thenRun(() -> {
					LOGGER.info("Completed Stock Price...!");
					if(CompletableFuture.allOf(optionsTotalFuture.toArray(new CompletableFuture[optionsTotalFuture.size()])).thenRun(() -> {
						LOGGER.info("Completed Options...!");
					}).thenRun(()->{
						try {
							Thread.sleep(20000); //10s
							LOGGER.info("Done...!");	
							appUtils.missingList(finalCollectedData);
							
							dfr.setResult(new JSONObject(finalCollectedData).toString());
							
							if(isUploadGit) {
								this.gitCommitAndWriteFile(expiryDate);
							}
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}).isCompletedExceptionally()) {
						appUtils.missingList(finalCollectedData);
						dfr.setResult(new JSONObject(finalCollectedData).toString());
					}
				});
			});
		} catch (Exception e) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
			this.handleException(e);
		}

	}
	
	public void getAllCurrentRsi(DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException {
		List<CompletableFuture> rsiTotalFuture = new ArrayList<>();
		for(String symbol : NseOptionSymbols.symbols) {
			var rsiFuture = this.requestHandler("GetRsi", symbol, null, null);
			rsiTotalFuture.add(rsiFuture);
			rsiFuture.thenAccept(rsiResponse -> {
				LOGGER.info("rsi get body completed ----> "+symbol);
				rsiData.put(symbol, rsiResponse.body());
				finalCollectedData.put(symbol, new JSONObject(appUtils.parseHtmlGetRsi(rsiData.get(symbol))));
			});
		}
		
		try {
			CompletableFuture.allOf(rsiTotalFuture.toArray(new CompletableFuture[rsiTotalFuture.size()])).thenAccept(e -> {
				LOGGER.info("Completed Rsi...!");
			}).thenRun(()->{
				LOGGER.info("Done...!");
				dfr.setResult(new JSONObject(finalCollectedData).toString());
			});
		} catch (Exception e) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
			appUtils.missingList(finalCollectedData);
			this.handleException(e);
		}

	}
	
	public void getAllCurrentPrice(DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException {
		List<CompletableFuture> stPriceTotalFuture = new ArrayList<>();
		for(String symbol : NseOptionSymbols.symbols) {
			var stPriceFuture = this.requestHandler("GetStockPrice", symbol, null, null);
			stPriceTotalFuture.add(stPriceFuture);
			stPriceFuture.thenAccept(stPriceResponse -> {
				LOGGER.info("stPrice get body completed ----> "+symbol);
				finalCollectedData.put(symbol, new JSONObject(appUtils.parseHtmlGetStPrice(stPriceResponse.body())));
			});
		}
		
		try {
			CompletableFuture.allOf(stPriceTotalFuture.toArray(new CompletableFuture[stPriceTotalFuture.size()])).thenAccept(e -> {
				LOGGER.info("Completed Stock Price...!");
			}).thenRun(()->{
				LOGGER.info("Done...!");
				dfr.setResult(new JSONObject(finalCollectedData).toString());
			});
		} catch (Exception e) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
			appUtils.missingList(finalCollectedData);
			this.handleException(e);
		}

	}
	
	public void getAllOptions(String expiryMonth, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException {
		List<CompletableFuture> optionsTotalFuture = new ArrayList<>();
		for(String symbol : NseOptionSymbols.symbols) {
			var optionFuture = this.requestHandler("ByExpiry", symbol, expiryMonth, null);
			optionsTotalFuture.add(optionFuture);
			optionFuture.thenAccept(optionResponse -> {
				LOGGER.info("option chain get body completed ----> "+symbol);
				finalCollectedData.put(symbol, new JSONObject(appUtils.parseHtmlGetStPrice(optionResponse.body())));
			});
		}
		
		try {
			CompletableFuture.allOf(optionsTotalFuture.toArray(new CompletableFuture[optionsTotalFuture.size()])).thenAccept(e -> {
				LOGGER.info("Completed Option Chain Data ...!");
			}).thenRun(()->{
				LOGGER.info("Done...!");
				dfr.setResult(new JSONObject(finalCollectedData).toString());
			});
		} catch (Exception e) {
			LOGGER.info(e.getClass().getName()+" :: "+e.getMessage());
			appUtils.missingList(finalCollectedData);
			this.handleException(e);
		}

	}
		
	public void handleException(Exception e) {
		LOGGER.info(e.getMessage());
		for(String s : NseOptionSymbols.symbols) {
			if(!optionsData.containsKey(s)) {
				LOGGER.info("Missing Symbols after rsi failed : "+s);
			} 
		}
	}

	public void gitCommitAndWriteFile(String expiryDate) {
		String fn = expiryDate.substring(2,5)+"_UpdatedData";
		String ext = "json";
		try {
			appUtils.writeOutAsFile(fn, new JSONObject(finalCollectedData).toString(), ext);
			gitConfig.pushToGit();
			for(String f : NseOptionSymbols.symbols) {
				if(!finalCollectedData.containsKey(f)) {
					LOGGER.info("Missing Symbols : "+f);
				} 
			}
		} catch (IOException | GitAPIException e) {
			LOGGER.info(fn+"."+ext + "File has not been written successfully");
			e.printStackTrace();
		}
	}

	//TODO
	public void fetchMissingSymbols() {
		
	}
}