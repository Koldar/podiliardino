package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.massimobono.podiliardino.util.ObservableDistinctList;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Team implements Indexable {

	//OWNED ATTRIBUTES
	
	private final LongProperty id;
	private final StringProperty name;
	private final ObjectProperty<LocalDate> date;
	
	//RELATIONSHIPS
	
	private final ObservableDistinctList<Player> players;
	private final ObservableDistinctList<Partecipation> partecipations;
	private final ObservableDistinctList<Match> matches;
	
	public Team(long id, String name, LocalDate date, Collection<Player> players, Collection<Partecipation> partecipations, Collection<Match> matches){
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.date = new SimpleObjectProperty<>(date);
		this.players = new ObservableDistinctList<>(FXCollections.observableArrayList(players));
		this.partecipations = new ObservableDistinctList<>(FXCollections.observableArrayList(partecipations));
		this.matches = new ObservableDistinctList<>(FXCollections.observableArrayList(matches));
	}
	
	public Team() {
		this(0, "", LocalDate.now(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}
	
	public String toString() {
		return String.format("<%s name=%s players=%s>", this.getClass().getSimpleName(), this.name.get(), String.join(", ", this.players.stream().map(p -> p.getName().get()).collect(Collectors.toList())));
	}
	
	/**
	 * 
	 * @param t
	 * @return the number of points scored by the team in the whole tournament 
	 */
	public int getPointsScoredIn(Tournament t) {
		return this.getMatches()
				.parallelStream()
				.filter(m -> m.getDay().get().getTournament().get() == t)
				.filter(m -> m.getStatus().get() == MatchStatus.DONE)
				.filter(m -> m.getWinner() == this)
				.mapToInt(m -> m.getPointsEarnedByWinning().get())
				.sum();
	}
	
	/**
	 * For example if the team has performed only one match that ended in 3-2, this function will return 3;
	 * 
	 * @param t
	 * @return the number of goals this team scored in the whole tournament
	 */
	public int getNumberOfGoalsScored(Tournament t) {
		return this.getMatches()
				.parallelStream()
				.filter(m -> m.getDay().get().getTournament().get() == t)
				.filter(m -> m.getStatus().get() == MatchStatus.DONE)
				.mapToInt(m -> m.getNumberOfGoalsOfTeam(this))
				.sum();
	}
	
	/**
	 * For example if the team has performed only one match that ended in 3-2, this function will return 2; 
	 * 
	 * @param t
	 * @return the number of goals this team received in the whole tournament
	 */
	public int getNumberOfGoalsReceived(Tournament t) {
		return this.getMatches()
				.parallelStream()
				.filter(m -> m.getDay().get().getTournament().get() == t)
				.filter(m -> m.getStatus().get() == MatchStatus.DONE)
				.mapToInt(m -> m.getNumberOfGoalssOfOtherTeam(this))
				.sum();
	}
	
	/**
	 * 
	 * @param t
	 * @return the number of goals all your opponents have scored
	 */
	public int getNumberOfGoalsYourOpponentsScored(Tournament t) {
		return this.getMatches()
				.parallelStream()
				.filter(m -> m.getDay().get().getTournament().get() == t)
				.filter(m -> m.getStatus().get() == MatchStatus.DONE)
				.mapToInt(m -> m.getOtherTeam(this).getNumberOfGoalsScored(t))
				.sum();
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
	 * @return the matches
	 */
	public ObservableList<Match> getMatches() {
		return matches;
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
		Team other = (Team) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	
	
}
