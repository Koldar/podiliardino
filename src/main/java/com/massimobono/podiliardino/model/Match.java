package com.massimobono.podiliardino.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Match {

	private final ObjectProperty<Team> team1;
	private final ObjectProperty<Team> team2;
	private final ObjectProperty<Day> day;
	private final IntegerProperty pointsEarnedByWinning;
	private final IntegerProperty pointsEarnedByLosing;
	private final IntegerProperty team1Goals;
	private final IntegerProperty team2Goals;
	private final ObjectProperty<MatchStatus> status;
	
	
	public Match(Team team1, Team team2, Day day, int pointsEarnedByWinning, int pointsEarnedByLosing, int team1Goals, int team2Goals, MatchStatus status) {
		super();
		this.team1 = new SimpleObjectProperty<>(team1);
		this.team2 = new SimpleObjectProperty<>(team2);
		this.day = new SimpleObjectProperty<>(day);
		this.team1Goals = new SimpleIntegerProperty(team1Goals);
		this.team2Goals = new SimpleIntegerProperty(team2Goals);
		this.pointsEarnedByWinning = new SimpleIntegerProperty(pointsEarnedByWinning);
		this.pointsEarnedByLosing = new SimpleIntegerProperty(pointsEarnedByLosing);
		this.status = new SimpleObjectProperty<>(status);
	}
	
	/**
	 * 
	 * @param t the team involved
	 * @return true if the team has fought inside this match, false otherwise
	 */
	public boolean hasTeamFoughtInThisMatch(Team t) throws UnsupportedOperationException{
		return (this.team1.get().equals(t))||(this.team2.get().equals(t));
	}
	
	/**
	 * 
	 * @param t
	 * @return the number of goals scored by the team in the parameter
	 * @throws UnsupportedOperationException if you try to call this function with a team not involved in the match
	 * @throws UnsupportedOperationException if you tried to call this method on a match with status {@link MatchStatus#TODO}
	 */
	public int getNumberOfGoalsOfTeam(Team t) throws UnsupportedOperationException{
		if (this.status.get() == MatchStatus.TODO) {
			throw new UnsupportedOperationException();
		}
		if (this.team1.get() == t) {
			return this.team1Goals.get();
		}
		if (this.team2.get() == t) {
			return this.team2Goals.get();
		}
		throw new UnsupportedOperationException(String.format("team %s not found in the match %s", t, this));
	}
	
	/**
	 * 
	 * @param yourTeam your team
	 * @return the goals the other team scored against you
	 * @throws UnsupportedOperationException if you try to call this function with a team not involved in the match
	 * @throws UnsupportedOperationException if you tried to call this method on a match with status {@link MatchStatus#TODO}
	 */
	public int getNumberOfGoalssOfOtherTeam(Team yourTeam) throws UnsupportedOperationException {
		Team other = this.getOtherTeam(yourTeam);
		return this.getNumberOfGoalsOfTeam(other);
	}
	
	/**
	 * 
	 * @param t
	 * @return the team you have fought in the match
	 * @throws UnsupportedOperationException if you try to call this function with a team not involved in the match
	 */
	public Team getOtherTeam(Team t) throws UnsupportedOperationException {
		if (this.team1.get() == t) {
			return this.team2.get();
		}
		if (this.team2.get() == t) {
			return this.team1.get();
		}
		throw new UnsupportedOperationException(String.format("team %s is not involved in the match %s", t, this));
	}
	
	/**
	 * 
	 * @return the winner of this match
	 * @throws UnsupportedOperationException if you're trying to get the winner from a match that needs to be done yet
	 */
	public Team getWinner() throws UnsupportedOperationException{
		if (this.status.get() == MatchStatus.TODO) {
			throw new UnsupportedOperationException();
		}
		return this.team1Goals.get() > this.team2Goals.get() ? this.team1.get() : this.team2.get();
	}
	
	/**
	 * 
	 * @return the loser of the match
	 * @throws UnsupportedOperationException if you're trying to get the loser from a match that needs to be done yet
	 */
	public Team getLoser() throws UnsupportedOperationException{
		if (this.status.get() == MatchStatus.TODO) {
			throw new UnsupportedOperationException();
		}
		return this.team1Goals.get() < this.team2Goals.get() ? this.team1.get() : this.team2.get();
	}


	/**
	 * @return the team1
	 */
	public ObjectProperty<Team> getTeam1() {
		return team1;
	}


	/**
	 * @return the team2
	 */
	public ObjectProperty<Team> getTeam2() {
		return team2;
	}


	/**
	 * @return the day
	 */
	public ObjectProperty<Day> getDay() {
		return day;
	}


	/**
	 * @return the team1Goals
	 */
	public IntegerProperty getTeam1Goals() {
		return team1Goals;
	}


	/**
	 * @return the team2Goals
	 */
	public IntegerProperty getTeam2Goals() {
		return team2Goals;
	}


	/**
	 * @return the pointsEarnedByWinning
	 */
	public IntegerProperty getPointsEarnedByWinning() {
		return pointsEarnedByWinning;
	}


	/**
	 * @return the pointsEarnedByLosing
	 */
	public IntegerProperty getPointsEarnedByLosing() {
		return pointsEarnedByLosing;
	}


	/**
	 * @return the status
	 */
	public ObjectProperty<MatchStatus> getStatus() {
		return status;
	}
	
	public String toString() {
		return String.format("<%s status=%s team1=%s team2=%s tournament=%s day=%d team1Goals=%d team2Goals=%d>",
				this.getClass().getName(),
				this.getStatus().get(),
				this.getTeam1().get().nameProperty().get(),
				this.getTeam2().get().nameProperty().get(),
				this.getDay().get().tournamentProperty().get().nameProperty().get(),
				this.getDay().get().numberProperty().get(),
				this.getTeam1Goals().get(),
				this.getTeam2Goals().get());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day.get() == null) ? 0 : day.get().hashCode());
		result = prime * result + ((team1.get() == null) ? 0 : team1.get().hashCode());
		result = prime * result + ((team2.get() == null) ? 0 : team2.get().hashCode());
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
		Match other = (Match) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.get().equals(other.day.get()))
			return false;
		if (team1 == null) {
			if (other.team1 != null)
				return false;
		} else if (!team1.get().equals(other.team1.get()))
			return false;
		if (team2 == null) {
			if (other.team2 != null)
				return false;
		} else if (!team2.get().equals(other.team2.get()))
			return false;
		return true;
	}
	
	
	
	
}
