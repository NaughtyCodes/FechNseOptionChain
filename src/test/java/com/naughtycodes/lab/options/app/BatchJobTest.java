package com.naughtycodes.lab.options.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class BatchJobTest {

	public static void main(String[] args) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String asOn = simpleDateFormat.format(new Date());

	}
	
	public Date getLastThursday(int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month + 1, 1);
		cal.add(Calendar.DAY_OF_MONTH, -((cal.get(Calendar.DAY_OF_WEEK) + 2) % 7));
		if (cal.get(Calendar.MONTH) != month)
			cal.add(Calendar.DAY_OF_MONTH, -7);
		return cal.getTime();
	}

}
