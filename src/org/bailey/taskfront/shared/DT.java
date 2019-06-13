package org.bailey.taskfront.shared;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class DT {

	@SuppressWarnings("deprecation")
	public static final double timeZoneOffset = new Date().getTimezoneOffset() * 60 * 1000;
	public static final double dayLength = 1000 * 60 * 60 * 24;
	public static final double weekLength = dayLength * 7;
	public static final double yearLength = 365.24219 * dayLength;
	public static final double ancientPast = -1000 * yearLength; // TODO should not use this!!
	public static final double farFuture = 1000 * yearLength; // TODO should not use this!!
	private static double NOW = getTime();
	public static double todayStart = getDayStart(NOW);
	public static double tomorrowStart = getDayStart(NOW + dayLength);
	public static double yesterdayStart = getDayStart(NOW - dayLength);
	public static double getNow(){return NOW;}
	public static double refreshNow(){
		NOW = getTime();
		todayStart = getDayStart(NOW);
		tomorrowStart = getDayStart(NOW + dayLength);
		yesterdayStart = getDayStart(NOW - dayLength);
		return NOW;
	}
	public static double getTime(){return new Date().getTime();}

	public static double getDayStart(double time){
		return ((int)((time - timeZoneOffset - dayStartsAt) / dayLength)) * dayLength + dayStartsAt + timeZoneOffset;
	}
	public static double getDayMiddle(double time){
		return getDayStart(time) + .5 * dayLength;
	}
	public static double getDayMiddle(){return getDayMiddle(NOW);}
	public static double dayStartsAt = 4 * 1000 * 60 * 60; // Work day changes over at 4AM
	
	public static boolean isYesterday(double time){return (time >= yesterdayStart) && (time < todayStart);}
	public static boolean isToday(double time){return (time >= todayStart) && (time < tomorrowStart);}
	public static boolean isTomorrow(double time){return (time >= tomorrowStart) && (time < tomorrowStart + dayLength);}
	public static boolean isThisWeek(double time){return (time >= todayStart) && (time < todayStart + weekLength);}
	public static boolean isThisMonth(double time){return (time >= todayStart) && (time < todayStart + 30 * dayLength);}
	public static boolean isSixMonths(double time){return (time >= todayStart) && (time < todayStart + yearLength / 2);}
	public static String getTimeText(double time) {
		if(isYesterday(time))return "Yesterday";
		if(isToday(time)) return "Today";
		if(isTomorrow(time)) return "Tomorrow";
		if(isThisWeek(time)) return "This week";
		if(isThisMonth(time)) return "This month";
		if(isSixMonths(time)) return "Six months";
		if(time > todayStart) return "Far future";
		else return "Past";
	}
	
	@SuppressWarnings("deprecation")
	public static int dayOfWeek(double time){
		return (new Date((long)time)).getDay();
	}
	public static DateTimeFormat dayOfMonthFormat = DateTimeFormat.getFormat("d");
	@SuppressWarnings("deprecation")
	public static int dayOfMonth(double time){
		//return Integer.parseInt(dayOfMonthFormat.format(new Date((long) time)));
		// alternatively:
		return (new Date((long)time)).getDate();
	}
	@SuppressWarnings("deprecation")
	public static int month(double time){
		return (new Date((long)time)).getMonth();
	}
}
