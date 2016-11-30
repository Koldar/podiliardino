package com.massimobono.podiliardino.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Partecipation{

	private final BooleanProperty bye;
	private final ObjectProperty<Tournament> tournament;
	private final ObjectProperty<Team> team;
	
	public Partecipation(boolean bye, Tournament tournament, Team team) {
		this.bye = new SimpleBooleanProperty(bye);
		this.tournament = new SimpleObjectProperty<>(tournament);
		this.team = new SimpleObjectProperty<>(team);
	}

	/**
	 * @return the bye
	 */
	public BooleanProperty getBye() {
		return bye;
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
		return String.format("<%s team_name=%s tournament_name=%s byte=%s>", 
				this.getClass().getSimpleName(), 
				this.team.get().getName().get(), 
				this.tournament.get().getName().get(), 
				this.bye.get()?"yes":"no");
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
