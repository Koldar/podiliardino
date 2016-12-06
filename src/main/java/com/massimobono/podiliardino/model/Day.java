package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.util.ObservableDistinctList;
import com.massimobono.podiliardino.util.Utils;

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
	 * 
	 * @param team1 the team involved
	 * @param team2 the team involved
	 * @return True if the 2 teams have a match/ has already fought eachother in this day, false otherwise
	 */
	public boolean hasAMatchAgainst(Team team1, Team team2) {
		for (Match m : this.getMatches()) {
			if (!m.hasTeamFoughtInThisMatch(team1)) {
				continue;
			}
			if (!m.hasTeamFoughtInThisMatch(team2)) {
				continue;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return True if there are no matched to do in this day
	 */
	public boolean isDayCompleted() {
		return this.getNumberOfMatchesToDo() == 0;
	}
	
	/**
	 * Add a new relationship "divide" between tournament-day
	 * 
	 * @param t
	 */
	public void add(Tournament t) {
		this.tournament.set(t);
		t.getDays().add(this);
	}
	
	/**
	 * Removes a relationship "divide" between tournament-day
	 * 
	 * @param t
	 */
	public void remove(Tournament t) {
		t.getDays().remove(this);
	}
	
	public void add(Match m) {
		m.getDay().get().getMatches().add(m);
		m.getTeam1().get().getMatches().add(m);
		m.getTeam2().get().getMatches().add(m);
	}
	
	public void remove(Match m) {
		m.getDay().get().getMatches().remove(m);
		m.getTeam1().get().getMatches().remove(m);
		m.getTeam2().get().getMatches().remove(m);
	}
	
	/**
	 * Iteratively call {@link #remove(Match)} until no matches are inside it
	 */
	public void removeAllMatches() {
		while (!this.matches.isEmpty()) {
			this.remove(this.matches.get(0));
		}
	}
	
	/**
	 * We get all the matches in the day and then we retain only those with status {@link MatchStatus#TODO}
	 * 
	 * @return the number of mathces that we have left in order to conclude the day
	 */
	public int getNumberOfMatchesToDo() {
		return this.getMatches().parallelStream().filter(m -> m.getStatus().get() == MatchStatus.TODO).mapToInt(m -> 1).sum();
	}
	
	/**
	 * like {@link #getNumberOfMatchesDone(boolean)} but we exclude fake matches by default
	 * 
	 * @return the number of matches already terminated (fake matches are excluded by default)
	 */
	public int getNumberOfMatchesDone() {
		return this.getNumberOfMatchesDone(false);
	}
	
	/**
	 * We get all the matches in the day and then we retain only those with status {@link MatchStatus#DONE}
	 * 
	 * @param includeBye true if you want to consider "done" also the bye match. Technically they are match, but practically there are not
	 * @return the number of matches already terminated
	 */
	public int getNumberOfMatchesDone(final boolean includeBye) {
		return this.getMatches()
		.parallelStream()
		.filter(m -> m.getStatus().get() == MatchStatus.DONE)
		.filter(m -> !m.getLoser().equals(Utils.DUMMYTEAM) || includeBye) //implication
		.mapToInt(m -> 1)
		.sum();
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
