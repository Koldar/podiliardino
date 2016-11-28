package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Team {

	private final LongProperty id;
	private final StringProperty name;
	/**
	 * The date where the members founded the team
	 */
	private final ObjectProperty<LocalDate> date;
	private final ObservableList<Player> players;
	
	public Team(long id, String name, LocalDate date, Collection<Player> players){
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.date = new SimpleObjectProperty<>(date);
		this.players = FXCollections.observableArrayList(players);
	}
	
	public Team() {
		this(0, "", LocalDate.now(), new ArrayList<>());
	}

	/**
	 * @return the name
	 */
	public StringProperty getName() {
		return name;
	}

	/**
	 * @return the date
	 */
	public ObjectProperty<LocalDate> getDate() {
		return date;
	}

	/**
	 * @return the players
	 */
	public ObservableList<Player> getPlayers() {
		return players;
	}
	
	public long getId() {
		return this.id.get();
	}
	
	public void setId(long id) {
		this.id.set(id);
	}
}
