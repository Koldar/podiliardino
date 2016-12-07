package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.massimobono.podiliardino.util.ObservableDistinctList;
import com.massimobono.podiliardino.util.Utils;

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
	 * @return true if this team represents some special team for the application
	 */
	public boolean isSpecial() {
		return this.equals(Utils.DUMMYTEAM);
	}
	
	/**
	 * Check if the current team has used the bye in the tournament
	 * @param t the tournament to check
	 * @param checkDaysWithNoMatches true if you want to take into account days with no matches. False otherwise.
	 * Usually you want to not take into account those days because they are temporary
	 * @return the number of bye of this team
	 */
	public int checkByeNumber(Tournament t, boolean checkDaysWithNoMatches) {
		int retVal = 0;
		for (Day d : t.getDays()) {
			if (!checkDaysWithNoMatches && (d.getMatches().size() == 0)) {
				continue;
			}
			if (!this.hasTeamFoughtInDay(d, false)) {
				retVal++;
			}
		}
		return retVal;
	}
	
	/**
	 * Check if the current team has fought at least once in a given tournament day
	 * @param d the day to check
	 * @param includeBye true if you want to include the "byes" as "match fought", false otherwise
	 * @return true if the team has fought at least once in that day, false otherwise
	 */
	public boolean hasTeamFoughtInDay(Day d, boolean includeBye) {
		return this.getMatches()
		.parallelStream()
		.filter(m -> m.getDay().get() == d) //consider only a particular day
		.filter(m -> m.getStatus().get() == MatchStatus.DONE)
		.filter(m -> m.hasTeamFoughtInThisMatch(this))
		.filter(m -> includeBye || !m.hasTeamFoughtInThisMatch(Utils.DUMMYTEAM))
		.count() > 0;
		
	}
	
	/**
	 * Add a new relationship "compose" between player-team
	 * 
	 * @param p the player to add througout all the model entities involved in the relationship
	 */
	public void add(Player p) {
		p.add(this);
	}
	
	/**
	 * Removes new relationship "compose" between player-team
	 * 
	 * @param p the player to remove througout all the model entities involved in the relationship
	 */
	public void remove(Player p) {
		p.remove(this);
	}
	
	/**
	 * Removes all the relationships bettewn this team and players
	 */
	public void removeAllPlayers(){
		while (!this.players.isEmpty()){
			Player p = this.players.get(0);
			this.remove(p);
		}
	}
	
	/**
	 * Add a new relationship "partecipate" between tournament-team
	 * 
	 * @param p the partecipation to add througout all the model entities involved in the relationship
	 */
	public void add(Partecipation p) {
		this.partecipations.add(p);
		p.getTournament().get().getPartecipations().add(p);
	}
	
	/**
	 * Removes relationship "partecipate" between tournament-team
	 * 
	 * @param p the partecipation to remove througout all the model entities involved in the relationship
	 */
	public void remove(Partecipation p) {
		this.partecipations.remove(p);
		p.getTournament().get().getPartecipations().remove(p);
	}
	
	/**
	 * Removes all the relationships of team-match-team-in-day of this particular team
	 */
	public void removeAllMatches() {
		while (!this.matches.isEmpty()) {
			Match m = this.matches.get(0);
			this.remove(m);
		}
	}
	
	/**
	 * Add a new relationship "match" between day-team
	 * 
	 * @param m the match to add througout all the model entities involved in the relationship
	 * @throws UnsupportedOperationException if the team haven't fought in this match
	 */
	public void add(Match m) throws UnsupportedOperationException{
		if (!m.hasTeamFoughtInThisMatch(this)){
			throw new UnsupportedOperationException();
		}
		m.getTeam1().get().getMatches().add(m);
		m.getTeam2().get().getMatches().add(m);
		m.getDay().get().getMatches().add(m);
	}
	
	/**
	 * Removes relationship "match" between day-team
	 * 
	 * @param m the match to remove througout all the model entities involved in the relationship
	 * @throws UnsupportedOperationException if the team haven't fought in this match
	 */
	public void remove(Match m) {
		if (!m.hasTeamFoughtInThisMatch(this)){
			throw new UnsupportedOperationException();
		}
		m.getTeam1().get().getMatches().remove(m);
		m.getTeam2().get().getMatches().remove(m);
		m.getDay().get().getMatches().remove(m);
	}
	
	/**
	 * 
	 * @param t the tournament involved
	 * @return the sum of the number of points all your opponents in the given tournament scored in their matches 
	 */
	public int getPointsYourOpponentsScored(Tournament t) {
		return this.getMatches()
				.parallelStream()
				.filter(m -> m.getDay().get().getTournament().get() == t)
				.filter(m -> m.getStatus().get() == MatchStatus.DONE)
				.mapToInt(m -> m.getOtherTeam(this).getPointsScoredIn(t))
				.sum();
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
		return (int)id.get();
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
		} else if (id.get() !=other.id.get())
			return false;
		return true;
	}


	
	
}
