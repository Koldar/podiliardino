package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.util.ObservableDistinctList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Day implements Indexable {
	
	private static final Logger LOG = LogManager.getLogger(Day.class);

	//OWN ATTRIBUTES
	
	private long id;
	private final IntegerProperty number;
	private final ObjectProperty<LocalDate> day;
	
	//RELATIONSHIPS
	
	private final ObjectProperty<Tournament> tournament;
	private final ObservableDistinctList<Match> matches;
	
	public Day(long id, int number, LocalDate day, Tournament tournament, Collection<Match> matches) {
		super();
		this.id = id;
		this.number = new SimpleIntegerProperty(number);
		this.day = new SimpleObjectProperty<>(day);
		this.tournament = new SimpleObjectProperty<>(tournament);
		this.matches = new ObservableDistinctList<>(FXCollections.observableArrayList(matches));
	}
	
	public Day() {
		this(0,1,LocalDate.now(), null, new ArrayList<>());
	}
	
	/**
	 * @return the matches
	 */
	public ObservableList<Match> getMatches() {
		return matches;
	}

	/**
	 * @return the number
	 */
	public IntegerProperty getNumber() {
		return number;
	}

	/**
	 * @return the day
	 */
	public ObjectProperty<LocalDate> getDate() {
		return day;
	}

	/**
	 * @return the tournament
	 */
	public ObjectProperty<Tournament> getTournament() {
		return tournament;
	}


	@Override
	public void setId(long id) {
		this.id = id;
	}
	@Override
	public long getId() {
		return this.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Day other = (Day) obj;
		if (id != other.id)
			return false;
		return true;
	}

	
	
	
	
	
}
