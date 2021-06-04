package com.naughtycodes.lab.options.app.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naughtycodes.lab.options.app.services.FetchOptionsDataService;

@Service
public class AppUtils<T, K, V> {
	
	@Autowired
	private FetchOptionsDataService fetchOptionsDataService; 

	public String parseHtmlData(String html) {
		
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
							options.put("symbol", td.text().split(searchKey[0])[0].trim());
							options.put("expiryDate", td.text().split(searchKey[0])[1].trim());
						} else if(td.text().contains(searchKey[1])) {
							options.put("asOn", td.text().split(searchKey[1])[1].trim());
						} else if(td.text().contains(searchKey[4])) {
							options.put("strikPrice", td.text().split(searchKey[4])[1].trim());
						}
					}
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
		
		return jsonOut.toString();
	
	}

	public Date getLastThursday(int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month + 1, 1);
		cal.add(Calendar.DAY_OF_MONTH, -((cal.get(Calendar.DAY_OF_WEEK) + 2) % 7));
		if (cal.get(Calendar.MONTH) != month)
			cal.add(Calendar.DAY_OF_MONTH, -7);
		return cal.getTime();
	}

	public String getFileName(String mon, int year) {
		
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
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String asOn 	= simpleDateFormat.format(new Date());
		String expiry 	= simpleDateFormat.format(this.getLastThursday(MONTH.get(mon.toUpperCase()), year));
		
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
    	System.out.println(data);
		try {
			FileWriter fw = new FileWriter(fn+"."+ext);
			fw.write(data);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    
}
