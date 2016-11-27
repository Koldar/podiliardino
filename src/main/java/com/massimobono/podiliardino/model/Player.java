package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {
	
	public static final String BIRTHDAY_PATTERN = "dd-MM-yyyy";
	
	private final LongProperty id;
	private final StringProperty name;
	private final StringProperty surname;
	private final ObjectProperty<LocalDate> birthday;
	private final StringProperty phone;
	
	public Player(long id, String name, String surname, LocalDate birthday, String phone) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.surname = new SimpleStringProperty(surname);
		this.birthday = new SimpleObjectProperty<LocalDate>(birthday);
		this.phone = new SimpleStringProperty(phone);
	}
	
	public Player() {
		this(0, "", "", LocalDate.now(), "");
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name.get();
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name.set(name);
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname.get();
	}

	/**
	 * @param surname the surname to set
	 */
	public void setSurname(String surname) {
		this.surname.set(surname);
	}

	/**
	 * @return the birthday
	 */
	public LocalDate getBirthday() {
		return birthday.get();
	}
	
	/**
	 * The {@value #BIRTHDAY_PATTERN} represents the so-called "standardized date".
	 * This values is stored in the database as well
	 * 
	 * @return the birthday date, according to the {@link #BIRTHDAY_PATTERN} pattern
	 */
	public String getBirthdayAsStandardString() {
		return this.getBirthday().format(DateTimeFormatter.ofPattern(BIRTHDAY_PATTERN));
	}
	
	public void setBirthdayFromStandardString(String birthday) throws DateTimeParseException {
		this.setBirthday(DateTimeFormatter.ofPattern(Player.BIRTHDAY_PATTERN).parse(birthday, LocalDate::from));
	}
	
	public static LocalDate getBirthdayDateFromStandardString(String standardDate) {
		return DateTimeFormatter.ofPattern(Player.BIRTHDAY_PATTERN).parse(standardDate, LocalDate::from);
	}

	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(LocalDate birthday) {
		this.birthday.set(birthday);
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone.get();
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone.set(phone);
	}
	
	public long getId() {
		return this.id.get();
	}
	
	/**
	 * Do not use this unless you know what you're doing
	 * 
	 * @param id
	 */
	public void setId(long id) {
		this.id.set(id);
	}
	
}
