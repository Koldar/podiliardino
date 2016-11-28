package com.massimobono.podiliardino.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Utils {

	public static final String BIRTHDAY_PATTERN = "dd-MM-yyyy";
	
	public static LocalDate getDateFrom(String standardDate) {
		return DateTimeFormatter.ofPattern(Utils.BIRTHDAY_PATTERN).parse(standardDate, LocalDate::from);
	}
	
	public static String getStandardDateFrom(LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern(Utils.BIRTHDAY_PATTERN));
	}
}
