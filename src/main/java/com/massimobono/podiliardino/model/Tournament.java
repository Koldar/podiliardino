package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.util.ObservableDistinctList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Tournament implements Indexable {
	
	private static final Logger LOG = LogManager.getLogger(Tournament.class);

	//concept specific attributes
	private final LongProperty id;
	private final StringProperty name;
	private final ObjectProperty<LocalDate> startDate;
	private final ObjectProperty<Optional<LocalDate>> endDate;
	
	//relationships with other concepts
	private final ObservableDistinctList<Partecipation> partecipations;
	private final ObservableDistinctList<Day> days;
	
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param startDate
	 * @param endDate can be null. Must be after startDate
	 */
	public Tournament(long id, String name, LocalDate startDate, LocalDate endDate, Collection<Partecipation> partecipations, Collection<Day> days) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.startDate = new SimpleObjectProperty<>(startDate);
		this.endDate = new SimpleObjectProperty<>(Optional.ofNullable(endDate));
		this.partecipations = new ObservableDistinctList<>(FXCollections.observableArrayList(partecipations));
		this.days = new ObservableDistinctList<>(FXCollections.observableArrayList(days));
	}
	
	public Tournament() {
		this(0, "", LocalDate.now(), null, new ArrayList<>(), new ArrayList<>());
	}

	
	/**
	 * Add a new relationship "partecipate" between tournament-team
	 * 
	 * @param p
	 */
	public void add(Partecipation p) {
		p.getTeam().get().add(p);
	}
	
	/**
	 * Removes relationship "partecipate" between tournament-team
	 * 
	 * @param p
	 */
	public void remove(Partecipation p) {
		p.getTeam().get().remove(p);
	}
	
	/**
	 * Add a new relationship "divide" between tournament-day
	 * 
	 * @param d
	 */
	public void add(Day d) {
		this.days.add(d);
		d.getTournament().set(this);
	}
	
	/**
	 * 
	 * @return the number of players that will be attending to the tournament
	 */
	public int getNumberOfPartecipants() {
		return this.getPartecipations().parallelStream().mapToInt(p -> p.getTeam().get().getPlayers().size()).sum();
	}
	
	/**
	 * 
	 * @return the number of teams that will be attending to the tournament
	 */
	public int getNumberOfTeams() {
		return this.getPartecipations().size();
	}

	/**
	 * @return the days
	 */
	public ObservableList<Day> getDays() {
		return days;
	}

	/**
	 * 
	 * @return a list of teams partecipating in the current tournament
	 */
	public Collection<Team> getPartecipatingTeams() {
		return this.getPartecipations().parallelStream().map(p -> p.getTeam().get()).collect(Collectors.toSet());
	}
	
	/**
	 * 
	 * @return all the players partecipating in the current tournament
	 */
	public Collection<Player> getPartecipatingPlayers() {
		Set<Player> retVal = new HashSet<>();
		for (Team t : this.getPartecipatingTeams()) {
			retVal.addAll(t.getPlayers());
		}
		return retVal;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id.get();
	}
	
	public void setId(long id) {
		this.id.set(id);
	}


	/**
	 * @return the name
	 */
	public StringProperty getName() {
		return name;
	}


	/**
	 * @return the startDate
	 */
	public ObjectProperty<LocalDate> getStartDate() {
		return startDate;
	}


	/**
	 * @return the endDate
	 */
	public ObjectProperty<Optional<LocalDate>> getEndDate() {
		return endDate;
	}


	/**
	 * @return the partecipations
	 */
	public ObservableList<Partecipation> getPartecipations() {
		return partecipations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Tournament other = (Tournament) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
	
	
}
