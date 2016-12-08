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

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
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
	
	//derived properties
	private final ReadOnlyIntegerWrapper numberOfPartecipants;
	
	
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
		
		this.numberOfPartecipants = new ReadOnlyIntegerWrapper();
		this.numberOfPartecipants.bind(Bindings.createIntegerBinding(this::getNumberOfPartecipants, this.partecipations));
	}
	
	public Tournament() {
		this(0, "", LocalDate.now(), null, new ArrayList<>(), new ArrayList<>());
	}
	
	/**
	 * 
	 * @return true if the partecipants of the tournament are odd in number, false otherwise
	 * @see #hasEvenPartecipants()
	 */
	public boolean hasOddPartecipants() {
		return (this.partecipations.size() % 2) > 0;
	}
	
	/**
	 * 
	 * @return true if the partecipants of the tournament are odd in number, false otherwise
	 * @see #hasOddPartecipants()
	 */
	public boolean hasEvenPartecipants() {
		return (this.partecipations.size() % 2) == 0;
	}

	
	/**
	 * 
	 * @param team1 the team involved
	 * @param team2 the team involved
	 * @param maximumPreviousMatchAllowed non negative number of previous fights between 2 team allowed. forexample if 2 teams fought eachother 2 times,
	 * 	calling this method with either 0 or 1 will return true but calling this method with 2 or an higher alue will return false
	 * @return True if the 2 teams have a match/ has already fought eachother in this day, false otherwise
	 */
	public boolean hasAMatchAgainst(Team team1, Team team2, int maximumPreviousMatchAllowed) {
		int count = 0;
		for (Day d : this.getDays()) {
			if (d.hasAMatchAgainst(team1, team2)){
				count++;
				if (count > maximumPreviousMatchAllowed) {
					return true;
				}
			}
		}
		return false;
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
	 * Removes all the partecipations betwee tournament and teams
	 */
	public void removeAllPartecipations() {
		while (!this.partecipations.isEmpty()){
			Partecipation p = this.partecipations.get(0);
			this.remove(p);
		}
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
	
	public void removeAllDays() {
		while (!this.days.isEmpty()) {
			Day d = this.days.get(0);
		}
	}
	
	/**
	 * 
	 * @return the number of players that will be attending to the tournament
	 */
	public int getNumberOfPartecipants() {
		return this.getPartecipations().parallelStream().mapToInt(p -> p.getTeam().get().getPlayers().size()).sum();
	}
	
	public ReadOnlyIntegerProperty numberOfPartecipantsProperty() {
		return this.numberOfPartecipants.getReadOnlyProperty();
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
