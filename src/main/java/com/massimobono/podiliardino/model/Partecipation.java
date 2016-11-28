package com.massimobono.podiliardino.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Partecipation {

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
	
	
}
