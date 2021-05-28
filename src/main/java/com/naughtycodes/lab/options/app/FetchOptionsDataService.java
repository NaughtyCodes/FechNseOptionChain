package com.naughtycodes.lab.options.app;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Service
public class FetchOptionsDataService<T, V, K> {
	
	FetchOptionsDataService(){}
	
	public String getAllData(String parserKey, String expiryDate, String strikePrice) throws InterruptedException, ExecutionException, IOException {
		HashMap optionData = new HashMap<String, T>();
		FetchOptionsDataService fetch = new FetchOptionsDataService();
		NseOptionSymbols symbols = new NseOptionSymbols();
		Field[] fields = symbols.getClass().getDeclaredFields();
		FileWriter fw=new FileWriter("NseOptionChainData.json"); 
		for(String f : NseOptionSymbols.symbols){
			optionData.put(
					f,
					new JSONObject(fetch.getOptionDataFromNSE(
							fetch.constructUrl(parserKey, f, expiryDate, strikePrice),
							""
							))
					);
		}
		
		fw.write(new JSONObject(optionData).toString());  
		fw.close();
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
            	return this.parseHtmlData(htmlOut);
            case "ByPrice":
            	return this.parseHtmlData(htmlOut);
            default:
            	return this.parseHtmlData(htmlOut);
        }
		
	}
	
	public String getAsyncAllOptionDataFromNSE(String parserKey, String expiryDate, DeferredResult<String> dfr) throws InterruptedException, ExecutionException, IOException{
		
		ConcurrentHashMap<String, JSONObject> optionData = new ConcurrentHashMap<>();
		
        // create a client
        var client = HttpClient.newHttpClient();

		FileWriter fw=new FileWriter("NseOptionChainData.json"); 
		
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
	        	 optionData.put( f,new JSONObject(this.parseHtmlData(htmlOut)) );
	        });
	        
		}
		
		CompletableFuture<Void> totalFuture = CompletableFuture.allOf(ls.toArray(new CompletableFuture[ls.size()]));
		totalFuture.thenAccept(s -> {
			dfr.setResult(new JSONObject(optionData).toString());
		});
		
		fw.write(new JSONObject(optionData).toString());  
		fw.close();
		
		return new JSONObject(optionData).toString();
		
	}
	
	public String parseHtmlData(String html) {
		
		Document doc = Jsoup.parse(html);
		Elements tables = doc.getElementsByTag("table");
		
		String[] searchKey = {"Expiry Date", "As on", "Quote", "Open Interest", "Strike Price"};
		
		HashMap options = new HashMap<String, T>();
		
		for(int tableNo = 0; tableNo <= tables.size(); tableNo++) {
			if(tableNo == 4) {
				for(Element tr : tables.get(tableNo).getElementsByTag("tr")) {
					for (Element td : tr.getElementsByTag("td")) {
						if(td.text().contains(searchKey[0])) {
							options.put("symbol", td.text().split(searchKey[0])[0].trim());
							options.put("expiryDate", td.text().split(searchKey[0])[1].trim());
						} else if(td.text().contains(searchKey[1])) {
							options.put("asOn", td.text().split(searchKey[1])[1].trim());
						} else if(td.text().contains(searchKey[4])) {
							options.put("strikPrice", td.text().split(searchKey[4])[1].trim());
						}
					}
				}
			} else if(tableNo == 5) {
				List optionChain = new ArrayList<HashMap<K, V>>();
				List fields = new ArrayList<String>();
				for (Element tr : tables.get(tableNo).getElementsByTag("tr")) {
					if(tr.text().contains(searchKey[3])) {
						String callOrPut = "call";
						for (Element td : tr.getElementsByTag("td")) {
							
							String key = "";
							if(td.text().equalsIgnoreCase("Strike Price") || td.text().equalsIgnoreCase("Expiry Date")) {
								callOrPut = "put";
								key = td.text().replace(" ","");
							} else {
								key = callOrPut+td.text().replace(" ","");
							}
							
							//char[] c = key.toCharArray();
							//String l = String.valueOf(c[0]).toLowerCase();
							//key = Character.isUpperCase(c[c.length-1]) ? key : key.replaceFirst(l.toUpperCase(), l);
							
							fields.add(key);
						}
						
					} else if(tr.text().contains(searchKey[2])) {
						HashMap o = new HashMap<K, V>();
						for(int i=0; i < tr.getElementsByTag("td").size(); i++) {
							Element td = tr.getElementsByTag("td").get(i);
							o.put(fields.get(i), td.text());
							if(td.text() == fields.get(i)) {
								o.put(fields.get(i), td.text());
							}
						}
						optionChain.add(o);
					} 
				}
				options.put("optionChain", optionChain);
			}
		}
		
		JSONObject jsonOut = new JSONObject(options);
		
		return jsonOut.toString();
	}

}
