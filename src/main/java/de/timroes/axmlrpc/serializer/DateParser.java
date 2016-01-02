package de.timroes.axmlrpc.serializer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

class DateParser {
	private final String toParse;

	private int year;
	private int month = 1;
	private int day;
	private int hour;
	private int minute;
	private int second;
	private int milliseconds;
	private TimeZone timezone = TimeZone.getTimeZone("UTC");

	public DateParser(String toParse){
		this.toParse = toParse;
	}

	public Date parse(){
		int indexOfT = toParse.indexOf('T');
		if ( indexOfT == -1 ){
			parseDate(toParse);
		} else {
			parseDate(toParse.substring(0, indexOfT));
			parseHourAndTimezone(toParse.substring(indexOfT + 1));
		}

		return buildCalendar().getTime();
	}

	private void parseDate(String dateStr){
		//Limitations:
		// * Only support Calendar dates for now (ie: no Week dates and no Ordinal dates)
		// * Don't support years with reduced precision (ie: less than 4 digits)
		// * No check for corrupted inputs
		String basicFormatDate = dateStr.replaceAll("-", "");
		year = Integer.parseInt(basicFormatDate.substring(0, 4));

		if ( basicFormatDate.length() > 4 ){
			month = Integer.parseInt(basicFormatDate.substring(4, 6));
		}

		if ( basicFormatDate.length() > 6 ){
			day = Integer.parseInt(basicFormatDate.substring(6, 8));
		}
	}

	private void parseHourAndTimezone(String hourStr){
		String basicFormatHour = hourStr.replaceAll(":", "");
		int indexOfZ = basicFormatHour.indexOf('Z');
		if ( indexOfZ != -1 ){
			parseHour(basicFormatHour.substring(0, indexOfZ));
		} else {
			int indexOfSign = getIndexOfSign(basicFormatHour);
			if ( indexOfSign == -1 ){
				parseHour(basicFormatHour);
			} else {
				parseHour(basicFormatHour.substring(0, indexOfSign));
				parseTimeZone(basicFormatHour.substring(indexOfSign));
			}
		}
	}

	private static int getIndexOfSign(String str){
		int index = str.indexOf('+');
		return index != -1 ? index : str.indexOf('-');
	}

	private void parseHour(String basicFormatHour){
		int indexOfDot = basicFormatHour.indexOf('.');
		double fractionalPart = 0;
		if ( indexOfDot != -1 ){
			fractionalPart = Double.parseDouble("0" + basicFormatHour.substring(indexOfDot));
			basicFormatHour = basicFormatHour.substring(0, indexOfDot);
		}

		if ( basicFormatHour.length() >= 2 ){
			hour = Integer.parseInt(basicFormatHour.substring(0, 2));
		}

		if ( basicFormatHour.length() > 2 ){
			minute = Integer.parseInt(basicFormatHour.substring(2, 4));
		} else {
			fractionalPart *= 60;
		}

		if ( basicFormatHour.length() > 4 ){
			second = Integer.parseInt(basicFormatHour.substring(4, 6));
		} else {
			fractionalPart *= 60;
		}

		milliseconds = (int) (fractionalPart * 1000);
	}

	private void parseTimeZone(String tzStr){
		timezone = TimeZone.getTimeZone("GMT" + tzStr);
	}

	private Calendar buildCalendar(){
		GregorianCalendar result = new GregorianCalendar(timezone);
		result.set(Calendar.YEAR, year);
		result.set(Calendar.MONTH, month - 1);
		result.set( Calendar.DAY_OF_MONTH, day);
		result.set( Calendar.HOUR_OF_DAY, hour);
		result.set( Calendar.MINUTE, minute);
		result.set( Calendar.SECOND, second);
		result.set( Calendar.MILLISECOND, milliseconds);

		return result;
	}
}
