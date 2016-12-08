package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Observer;
import java.util.Optional;

import com.massimobono.podiliardino.util.ObservableDistinctList;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Player implements Indexable{
	
	
	//owned properties
	private final LongProperty id;
	private final StringProperty name;
	private final StringProperty surname;
	private final ObjectProperty<Optional<LocalDate>> birthday;
	private final ObjectProperty<Optional<String>> phone;
	
	//relationships
	private final ObservableDistinctList<Team> teams;
	
	//derived properties
	private final ReadOnlyObjectWrapper<Optional<Integer>> age;
	
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
		
		this.teams = new ObservableDistinctList<>(FXCollections.observableArrayList(teams));
		
		this.age = new ReadOnlyObjectWrapper<>();
		this.age.bind(Bindings.createObjectBinding(this::getAge, this.birthday));
	}
	
	public Player() {
		this(0, "", "", LocalDate.now(), null, new ArrayList<>());
	}
	
	/**
	 * 
	 * @return true if the player is not a player with special meaning inside the application, false otherwise
	 */
	public boolean isSpecial() {
		return this.nameProperty().get().equals(Utils.DUMMYPLAYER1.nameProperty().get()) || this.nameProperty().get().equals(Utils.DUMMYPLAYER2.nameProperty().get());
	}
	
	/**
	 * Add a new relationship "compose" between player-team
	 * 
	 * @param t
	 */
	public void add(Team t) {
		this.teams.add(t);
		t.getPlayers().add(this);
	}
	
	/**
	 * Removes a relationship "compose" between player-team
	 * 
	 * @param t
	 */
	public void remove(Team t) {
		this.teams.remove(t);
		t.getPlayers().remove(this);
	}

	/**
	 * @return the name
	 */
	public StringProperty nameProperty() {
		return name;
	}

	/**
	 * @return the surname
	 */
	public StringProperty surnameProperty() {
		return surname;
	}

	/**
	 * @return the birthday
	 */
	public ObjectProperty<Optional<LocalDate>> birthdayProperty() {
		return birthday;
	}
	
	/**
	 * The {@value #STANDARD_DATE_PATTERN} represents the so-called "standardized date".
	 * This values is stored in the database as well
	 * 
	 * @return the birthday date, according to the {@link #STANDARD_DATE_PATTERN} pattern
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
			this.birthdayProperty().set(Optional.of(Utils.getDateFrom(birthday)));
		}
	}

	/**
	 * @return the phone
	 */
	public ObjectProperty<Optional<String>> phoneProperty() {
		return phone;
	}
	
	public ObservableList<Team> teamsProperty() {
		return this.teams;
	}
	
	@Override
	public long getId() {
		return this.id.get();
	}
	
	/**
	 * Do not use this unless you know what you're doing
	 * 
	 * @param id
	 */
	@Override
	public void setId(long id) {
		this.id.set(id);
	}
	
	/**
	 * @return the age of this player
	 */
	public Optional<Integer> getAge() {
		if (this.birthday.get().isPresent()) {
			return Optional.of(Period.between(this.birthdayProperty().get().get(), LocalDate.now()).getYears());
		}
		return Optional.empty();
	}
	
	public ReadOnlyObjectProperty<Optional<Integer>> ageProperty() {
		return this.age.getReadOnlyProperty();
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name.get();
	}

	/**
	 * @return the surname
	 */
	public final String getSurname() {
		return surname.get();
	}

	/**
	 * @return the birthday
	 */
	public final Optional<LocalDate> getBirthday() {
		return birthday.get();
	}

	/**
	 * @return the phone
	 */
	public final Optional<String> getPhone() {
		return phone.get();
	}
	
	
	
}
