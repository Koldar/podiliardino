package com.massimobono.podiliardino.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Utils {

	public static final String STANDARD_DATE_PATTERN = "dd-MM-yyyy";
	public static final String  EMPTY_PHONE = "";
	public static final String EMPTY_DATE = "";
	public static final int DEFAULT_POINTS_EARNED_FROM_WINNING = 3;
	public static final int DEFAULT_POINTS_EARNED_FROM_LOSING = 0;
	
	/**
	 * 
	 * @param standardDate
	 * @return the converted {@link LocalDate}, according to {@link #STANDARD_DATE_PATTERN} pattern
	 */
	public static LocalDate getDateFrom(String standardDate) {
		return DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(standardDate, LocalDate::from);
	}
	
	public static String getStandardDateFrom(LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN));
	}
	
	public static Alert createDefaultErrorAlert(String header, String body) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(body);
		alert.showAndWait();
		return alert;
	}
}
