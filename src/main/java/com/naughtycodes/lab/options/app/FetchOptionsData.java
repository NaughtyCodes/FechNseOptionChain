package com.naughtycodes.lab.options.app;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FetchOptionsData<T, V, K> {
	
	FetchOptionsData(){}
	
	public String getData() throws InterruptedException, ExecutionException {
		HashMap optionData = new HashMap<String, T>();
		FetchOptionsData fetch = new FetchOptionsData();
		NseOptionSymbols symbols = new NseOptionSymbols();
		Field[] fields = symbols.getClass().getDeclaredFields();
		for(String f : NseOptionSymbols.symbols){
			optionData.put(
					f,
					new JSONObject(fetch.getOptionDataFromNSE(
							fetch.constructUrl("ByExpiry", f, "29JUL2021", ""),
							""
							))
					);
		}
		
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
            	return url;
            case "ByPrice":
        		url = "https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=";
        		url = url+symbol+"&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&";
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
	
	public String parseHtmlData(String html) {
		
		Document doc = Jsoup.parse(html);
		Elements tables = doc.getElementsByTag("table");
		
		String[] searchKey = {"Expiry Date", "As on", "Quote", "Open Interest"};
		
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
						}
					}
				}
			} else if(tableNo == 5) {
				List optionChain = new ArrayList<HashMap<K, V>>();
				List fields = new ArrayList<String>();
				for (Element tr : tables.get(tableNo).getElementsByTag("tr")) {
					if(tr.text().contains(searchKey[3])) {
						for (Element td : tr.getElementsByTag("td")) {
							String key = td.text().replace(" ","");
							char[] c = key.toCharArray();
							String l = String.valueOf(c[0]).toLowerCase();
							key = Character.isUpperCase(c[c.length-1]) ? key : key.replaceFirst(l.toUpperCase(), l);
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
