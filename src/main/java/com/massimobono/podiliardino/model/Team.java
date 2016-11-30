package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Team implements Indexable {

	private final LongProperty id;
	private final StringProperty name;
	/**
	 * The date where the members founded the team
	 */
	private final ObjectProperty<LocalDate> date;
	
	/**
	 * A list of all the players inside this team
	 */
	private final ObservableList<Player> players;
	/**
	 * A list of all the tournaments this team has partecipated
	 */
	private final ObservableList<Partecipation> partecipations;
	
	public Team(long id, String name, LocalDate date, Collection<Player> players, Collection<Partecipation> partecipations){
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.date = new SimpleObjectProperty<>(date);
		this.players = FXCollections.observableArrayList(players);
		this.partecipations = FXCollections.observableArrayList(partecipations);
	}
	
	public Team() {
		this(0, "", LocalDate.now(), new ArrayList<>(), new ArrayList<>());
	}
	
	public String toString() {
		return String.format("<%s name=%s players=%s>", this.getClass().getSimpleName(), this.name.get(), String.join(", ", this.players.stream().map(p -> p.getName().get()).collect(Collectors.toList())));
	}
	
	/**
	 * 
	 * @param team another team to compare against
	 * @return true if the 2 teams share at least one teammates. false otherwise 
	 */
	public boolean containsAnyPlayerOfTeam(Team team) {
		for (Player p : this.players) {
			if (team.getPlayers().contains(p)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * like {@link #containsAnyPlayerOfTeam(Team)} but returns the list of shared teamates
	 * @param team the other team to compare against
	 * @return the overlaps between the membership of the 2 teams
	 */
	public Collection<Player> getSharedTeammates(Team team) {
		Collection<Player> retVal = new ArrayList<>();
		for (Player p: this.players) {
			if (team.getPlayers().contains(p)) {
				retVal.add(p);
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 * @return a list of tournaments this team is partcepating/have partecipated in the past.
	 */
	public Collection<Tournament> getAllPartecipatingTournaments() {
		return this.getPartecipations().parallelStream().map(p -> p.getTournament().get()).collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @param t the tournament involved
	 * @return True if the team is partecipating/partecipated in the given tournament, false otherwise
	 */
	public boolean isPartecipatingIn(Tournament t) {
		return this.getPartecipations().parallelStream().filter(p -> p.getTournament().get() == t).count() > 0;
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
	
	/**
	 * @return the partecipations
	 */
	public ObservableList<Partecipation> getPartecipations() {
		return partecipations;
	}

	@Override
	public long getId() {
		return this.id.get();
	}
	
	@Override
	public void setId(long id) {
		this.id.set(id);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int)this.getId();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Team other = (Team) obj;
		return this.getId() == other.getId();
	}
	
	
}
