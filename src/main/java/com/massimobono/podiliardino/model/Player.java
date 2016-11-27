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
	public StringProperty getName() {
		return name;
	}

	/**
	 * @return the surname
	 */
	public StringProperty getSurname() {
		return surname;
	}

	/**
	 * @return the birthday
	 */
	public ObjectProperty<LocalDate> getBirthday() {
		return birthday;
	}
	
	/**
	 * The {@value #BIRTHDAY_PATTERN} represents the so-called "standardized date".
	 * This values is stored in the database as well
	 * 
	 * @return the birthday date, according to the {@link #BIRTHDAY_PATTERN} pattern
	 */
	public String getBirthdayAsStandardString() {
		return this.getBirthday().get().format(DateTimeFormatter.ofPattern(BIRTHDAY_PATTERN));
	}
	
	public void setBirthdayFromStandardString(String birthday) throws DateTimeParseException {
		this.getBirthday().set((DateTimeFormatter.ofPattern(Player.BIRTHDAY_PATTERN).parse(birthday, LocalDate::from)));
	}
	
	public static LocalDate getBirthdayDateFromStandardString(String standardDate) {
		return DateTimeFormatter.ofPattern(Player.BIRTHDAY_PATTERN).parse(standardDate, LocalDate::from);
	}

	/**
	 * @return the phone
	 */
	public StringProperty getPhone() {
		return phone;
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
