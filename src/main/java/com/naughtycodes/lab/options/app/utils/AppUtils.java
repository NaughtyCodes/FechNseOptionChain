package com.naughtycodes.lab.options.app.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naughtycodes.lab.options.app.LabOptionsApplication;
import com.naughtycodes.lab.options.app.models.AppProperties;
import com.naughtycodes.lab.options.app.models.NseOptionSymbols;
import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;

@Service
public class AppUtils<T, K, V> {
	
	private static final Logger LOGGER=LoggerFactory.getLogger(AppUtils.class);
	
	@Autowired private FetchOptionsDataService fetchOptionsDataService;
	@Autowired private AppProperties appProperties;
	
	private String rsiData = null;
	private String stPriceData = null;

	public String parseHtmlGetOptionsChain(String html, String rsiData, String stPriceData) {
		
		Document doc = Jsoup.parse(html);
		Elements tables = doc.getElementsByTag("table");
		
		String[] searchKey = {"Expiry Date", "As on", "Quote", "Open Interest", "Strike Price"};
		
		HashMap options = new HashMap<String, T>();
		
		for(int tableNo = 0; tableNo <= tables.size(); tableNo++) {
			//Options chain meta information table
			if(tableNo == 4) {
				for(Element tr : tables.get(tableNo).getElementsByTag("tr")) {
					for (Element td : tr.getElementsByTag("td")) {
						if(td.text().contains(searchKey[0])) {
							var symbol = td.text().split(searchKey[0])[0].trim();
							options.put("symbol", symbol);
							options.put("lotSize",NseOptionSymbols.lotSize.get(symbol));
							options.put("expiryDate", td.text().split(searchKey[0])[1].trim());
						} else if(td.text().contains(searchKey[1])) {
							options.put("asOn", td.text().split(searchKey[1])[1].trim());
						} else if(td.text().contains(searchKey[4])) {
							options.put("strikPrice", td.text().split(searchKey[4])[1].trim());
						}						
					}
				}
								
				if(rsiData != null) {
					options.put("rsi", new JSONObject(rsiData));
				}
				
				if(stPriceData != null) {
					options.put("stPrice", new JSONObject(stPriceData));
				}
			//Options chain heading information table
			} else if(tableNo == 5) {
				List optionChain = new ArrayList<HashMap<K, V>>();
				List fields = new ArrayList<String>();
				for (Element tr : tables.get(tableNo).getElementsByTag("tr")) {
					if(tr.text().contains(searchKey[3])) {
						for (Element td : tr.getElementsByTag("td")) {
							String key = "";
							if(td.text().equalsIgnoreCase("Strike Price") || td.text().equalsIgnoreCase("Expiry Date")) {
								key = this.toCamelCase(td.text());
							} else {
								key = td.text().replace(" ","");
							}
														
							fields.add(key);
						}
				   //Options chain information table	
					} else if(tr.text().contains(searchKey[2])) {
						HashMap o = new HashMap<K, V>();
						for(int i=0; i < tr.getElementsByTag("td").size(); i++) {
							Element td = tr.getElementsByTag("td").get(i);
							String moneyAt = td.attr("class").replace("chcontenttext", "").replace("cht2","");
							o.put(moneyAt+fields.get(i), td.text());
							if(td.text() == fields.get(i)) {
								o.put(moneyAt+fields.get(i), td.text());
							}
						}
						optionChain.add(o);
					} 
				}
				options.put("optionChain", optionChain);
			}
		}
		
		JSONObject jsonOut = new JSONObject(options);
		
		//System.out.println(jsonOut.toString());
		return jsonOut.toString();
	
	}

	public String parseHtmlGetRsi(String htmlOut) {
        
        Document doc = Jsoup.parse(htmlOut);
		Elements tables = doc.getElementsByTag("table");
		HashMap rsi = new HashMap<String, String>();
		
		for(int tableNo = 0; tableNo <= tables.size(); tableNo++) {
			if(tableNo == 3) {
				for(int i=0; i<=1; i++) {
					String d = tables.get(tableNo).getElementsByTag("tr").get(i).text();
					rsi.put("date", d.split(" ")[0]);
					rsi.put("perios", d.split(" ")[1]);
					rsi.put("value", d.split(" ")[2]);
				}
			}
		}
		
		JSONObject jsonOut = new JSONObject(rsi);
		return jsonOut.toString();
	}
	
	public String parseHtmlGetStPrice(String htmlOut) {
		HashMap<String, String> stPrice = new HashMap<String, String>();
		Document doc = Jsoup.parse(htmlOut);
		String stPriceData = doc.getElementById("responseDiv").text();
		JSONObject jsonOut = new JSONObject(stPriceData);
		stPrice.put("lastPrice", jsonOut.getJSONArray("data").getJSONObject(0).get("lastPrice").toString());
		stPrice.put("companyName", jsonOut.getJSONArray("data").getJSONObject(0).get("companyName").toString());
		
		return new JSONObject(stPrice).toString();
	}
	
	public String getLastThursday(int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month + 1, 1);
		cal.add(Calendar.DAY_OF_MONTH, -((cal.get(Calendar.DAY_OF_WEEK) + 2) % 7));
		if (cal.get(Calendar.MONTH) != month)
			cal.add(Calendar.DAY_OF_MONTH, -7);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		return simpleDateFormat.format(cal.getTime()).toUpperCase();
		
	}

	public String getFileName(String mon, String year) {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String asOn 	= simpleDateFormat.format(new Date());
		String expiry 	= this.getLastThursday(mon, year);
		
		return (asOn+"_"+expiry).toUpperCase();
		
	}
    
	public String toCamelCase(String str) {
    	var key = str.replace(" ","");
		char[] c = key.toCharArray();
		String l = String.valueOf(c[0]).toLowerCase();
		key = Character.isUpperCase(c[c.length-1]) ? key : key.replaceFirst(l.toUpperCase(), l);
    	return key;
    }
    
    public void writeOutAsFile(String fn, String data, String ext) {
		try {
			
			if(fn == "LastUpdatedData") {
				File file = new File("..\\NseOptionsChainData\\"+fn+"."+ext);
				if(!file.exists() ? false : file.delete()) {
					LOGGER.info(fn+"."+ext + "File has been deleted successfully");
				} else {
					LOGGER.info(fn+"."+ext + "File not exist and not been deleted.");
				}
			} 
			
			FileWriter fw = new FileWriter("..\\NseOptionsChainData\\"+fn+"."+ext, false);			
			fw.write(data);
			fw.close();
			LOGGER.info(fn+"."+ext + "File has been written successfully");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.info(fn+"."+ext + "File has not been written successfully");
			e.printStackTrace();
		} 
    }
    
    public String getLastThursday(String month, String year) {
    	
		HashMap<String, Integer> MONTH = new HashMap<String, Integer>();
		MONTH.put("JAN", 0);
		MONTH.put("FEB", 1);
		MONTH.put("MAR", 2);
		MONTH.put("APR", 3);
		MONTH.put("MAY", 4);
		MONTH.put("JUN", 5);
		MONTH.put("JUL", 6);
		MONTH.put("AUG", 7);
		MONTH.put("SEP", 8);
		MONTH.put("OCT", 9);
		MONTH.put("NOV", 10);
		MONTH.put("DEC", 11);
		
		final String parseKey = "ByExpiry";
		int m = MONTH.get(month.toUpperCase());
		int y = 0;
		
		if(year == "") {
			SimpleDateFormat yyyy = new SimpleDateFormat("yyyy");
			yyyy.setTimeZone(TimeZone.getTimeZone("IST"));
			y = Integer.valueOf(yyyy.format(new Date()));	
		} else {
			y = Integer.valueOf(year);
		}
		
		return this.getLastThursday(m, y).toString().toUpperCase();
    	
    }
    
    public String parseHtmlGetOptionsChain(String html) {
    	return this.parseHtmlGetOptionsChain(html, rsiData, stPriceData);
    }
    
    public String[] missingList(Map<String, JSONObject> optionData) {
    	ArrayList<String> missingList = new ArrayList<String>();
    	for(String f : NseOptionSymbols.symbols) {
    		if(!optionData.containsKey(f)) {
    			LOGGER.info("Missing Symbols : "+f);
    			missingList.add(f);
    		} 
    	}
    	String[] symbolList = Arrays.copyOf(missingList.toArray(), missingList.toArray().length, String[].class);
    	return symbolList;
    }
    
	public void writeFileOut(String expiryDate, ConcurrentHashMap<String, JSONObject> finalCollectedData) {
		writeOutAsFile(
				getFileName(expiryDate.substring(2, 5), 
				expiryDate.substring(5, 9)),
				new JSONObject(finalCollectedData).toString(), "json"
		);
	}
    
    public String stringEncoder(String s) {
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(s.getBytes());
    }
    
    public String stringDecoder(String s) {
        Base64.Decoder dec = Base64.getDecoder();
        return new String(dec.decode(s));
    }
    
    public Integer getMonthAsInteger(String mon) {
		HashMap<String, Integer> MONTH = new HashMap<String, Integer>();
    	MONTH.put("JAN", 0);
    	MONTH.put("FEB", 1);
    	MONTH.put("MAR", 2);
    	MONTH.put("APR", 3);
    	MONTH.put("MAY", 4);
    	MONTH.put("JUN", 5);
    	MONTH.put("JUL", 6);
    	MONTH.put("AUG", 7);
    	MONTH.put("SEP", 8);
    	MONTH.put("OCT", 9);
    	MONTH.put("NOV", 10);
    	MONTH.put("DEC", 11);
    	return MONTH.get(mon);
    }

}
