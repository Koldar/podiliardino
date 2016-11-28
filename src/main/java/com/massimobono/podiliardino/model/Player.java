package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
	private final ObjectProperty<Optional<LocalDate>> birthday;
	private final ObjectProperty<Optional<String>> phone;
	private final ObservableList<Team> teams;
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param surname
	 * @param birthday can be null
	 * @param phone can be null
	 * @param teams
	 */
	public Player(long id, String name, String surname, LocalDate birthday, String phone, Collection<Team> teams) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.surname = new SimpleStringProperty(surname);
		this.birthday = new SimpleObjectProperty<Optional<LocalDate>>(Optional.ofNullable(birthday));
		this.phone = new SimpleObjectProperty<Optional<String>>(Optional.ofNullable(phone));
		this.teams = FXCollections.observableArrayList(teams);
	}
	
	public Player() {
		this(0, "", "", LocalDate.now(), null, new ArrayList<>());
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
	public ObjectProperty<Optional<LocalDate>> getBirthday() {
		return birthday;
	}
	
	/**
	 * The {@value #BIRTHDAY_PATTERN} represents the so-called "standardized date".
	 * This values is stored in the database as well
	 * 
	 * @return the birthday date, according to the {@link #BIRTHDAY_PATTERN} pattern
	 */
	public Optional<String> getBirthdayAsStandardString() {
		return Optional.ofNullable(this.birthday.get().isPresent() ? Utils.getStandardDateFrom(this.birthday.get().get()) : null);
	}
	
	/**
	 * 
	 * @param birthday can be null
	 * @throws DateTimeParseException
	 */
	public void setBirthdayFromStandardString(String birthday) throws DateTimeParseException {
		if (birthday == null) {
			this.birthday.set(Optional.empty());
		} else {
			this.getBirthday().set(Optional.of(Utils.getDateFrom(birthday)));
		}
	}

	/**
	 * @return the phone
	 */
	public ObjectProperty<Optional<String>> getPhone() {
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
	public Optional<Integer> getAge() {
		if (this.birthday.get().isPresent()) {
			return Optional.of(Period.between(this.getBirthday().get().get(), LocalDate.now()).getYears());
		}
		return Optional.empty();
	}
	
}
