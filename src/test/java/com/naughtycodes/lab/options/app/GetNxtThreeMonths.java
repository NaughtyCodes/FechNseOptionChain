package com.naughtycodes.lab.options.app;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class GetNxtThreeMonths {
	
	public static void main(String args[]) {
		
		Date today = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		
		SimpleDateFormat mon = new SimpleDateFormat("MMM");
		mon.setTimeZone(TimeZone.getTimeZone("IST"));
		
		String[] m = new String[3];
		m[0] = mon.format(today).toUpperCase();
		calendar.add(Calendar.MONTH, 1);
		m[1] = mon.format(calendar.getTime());
		calendar.add(Calendar.MONTH, 2);
		m[2] = mon.format(calendar.getTime());
		
		for(String s : m) {
			System.out.println(s);
		}
		
		
		
	}

}
