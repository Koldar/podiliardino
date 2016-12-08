package com.massimobono.podiliardino.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Partecipation{
	private final ObjectProperty<Tournament> tournament;
	private final ObjectProperty<Team> team;
	
	public Partecipation( Tournament tournament, Team team) {
		this.tournament = new SimpleObjectProperty<>(tournament);
		this.team = new SimpleObjectProperty<>(team);
	}

	/**
	 * @return the tournament
	 */
	public ObjectProperty<Tournament> getTournament() {
		return tournament;
	}

	/**
	 * @return the team
	 */
	public ObjectProperty<Team> getTeam() {
		return team;
	}
	
	public String toString() {
		return String.format("<%s team_name=%s tournament_name=%s>", 
				this.getClass().getSimpleName(), 
				this.team.get().nameProperty().get(), 
				this.tournament.get().nameProperty().get());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((team == null) ? 0 : team.get().hashCode());
		result = prime * result + ((tournament == null) ? 0 : tournament.get().hashCode());
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
		Partecipation other = (Partecipation) obj;
		
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.get().equals(other.team.get()))
			return false;
		if (tournament == null) {
			if (other.tournament != null)
				return false;
		} else if (!tournament.get().equals(other.tournament.get()))
			return false;
		return true;
	}
	
	
	
	
	
	
}
