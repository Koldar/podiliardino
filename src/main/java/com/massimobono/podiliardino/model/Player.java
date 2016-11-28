package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;

import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Player {
	
	private final LongProperty id;
	private final StringProperty name;
	private final StringProperty surname;
	private final ObjectProperty<LocalDate> birthday;
	private final StringProperty phone;
	private final ObservableList<Team> teams;
	
	public Player(long id, String name, String surname, LocalDate birthday, String phone, Collection<Team> teams) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.surname = new SimpleStringProperty(surname);
		this.birthday = new SimpleObjectProperty<LocalDate>(birthday);
		this.phone = new SimpleStringProperty(phone);
		this.teams = FXCollections.observableArrayList(teams);
	}
	
	public Player() {
		this(0, "", "", LocalDate.now(), "", new ArrayList<>());
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
		return Utils.getStandardDateFrom(this.birthday.get());
	}
	
	public void setBirthdayFromStandardString(String birthday) throws DateTimeParseException {
		this.getBirthday().set(Utils.getDateFrom(birthday));
	}

	/**
	 * @return the phone
	 */
	public StringProperty getPhone() {
		return phone;
	}
	
	public ObservableList<Team> getTeamWithPlayer() {
		return this.teams;
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
	
	/**
	 * @return the age of this player
	 */
	public int getAge() {
		return Period.between(this.getBirthday().get(), LocalDate.now()).getYears();
	}
	
}
