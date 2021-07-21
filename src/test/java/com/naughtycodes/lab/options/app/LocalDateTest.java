package com.naughtycodes.lab.options.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class LocalDateTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LocalDateTest l = new LocalDateTest();
		System.out.println(l.getFileName("Jun", 2021));
		System.out.println(l.getFileName("JUN", 2021));
		String s = "29JUN2021";
		System.out.println(s.substring(2,5));
		System.out.println(s.substring(5,9));
		
		//Print next month
		Calendar calendar = Calendar.getInstance();         
		calendar.add(Calendar.MONTH, 1);
		System.out.println(calendar.getTime());
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
	
}
