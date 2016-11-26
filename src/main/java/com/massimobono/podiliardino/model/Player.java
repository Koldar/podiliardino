package com.massimobono.podiliardino.model;

import java.time.LocalDateTime;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {

	private final LongProperty id;
	private final StringProperty name;
	private final StringProperty surname;
	private final ObjectProperty<LocalDateTime> birthday;
	private final StringProperty phone;
	
	public Player(long id, String name, String surname, LocalDateTime birthday, String phone) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.surname = new SimpleStringProperty(surname);
		this.birthday = new SimpleObjectProperty<LocalDateTime>(birthday);
		this.phone = new SimpleStringProperty(phone);
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
	public LocalDateTime getBirthday() {
		return birthday.get();
	}

	/**
	 * @param birthday the birthday to set
	 */
	public void setBirthday(LocalDateTime birthday) {
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
	
	
	
	
}
