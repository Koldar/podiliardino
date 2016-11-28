package com.massimobono.podiliardino.model;

import java.time.LocalDate;
import java.util.Optional;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Tournament {

	private final LongProperty id;
	private final StringProperty name;
	private final ObjectProperty<LocalDate> startDate;
	private final ObjectProperty<Optional<LocalDate>> endDate;
	
	private final ObservableList<Partecipation> partecipations;
	
	
	/**
	 * 
	 * @param id
	 * @param name
	 * @param startDate
	 * @param endDate can be null. Must be after startDate
	 */
	public Tournament(long id, String name, LocalDate startDate, LocalDate endDate) {
		super();
		this.id = new SimpleLongProperty(id);
		this.name = new SimpleStringProperty(name);
		this.startDate = new SimpleObjectProperty<>(startDate);
		this.endDate = new SimpleObjectProperty<>(Optional.ofNullable(endDate));
		this.partecipations = FXCollections.observableArrayList();
	}


	/**
	 * @return the id
	 */
	public LongProperty getId() {
		return id;
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
	
	
}
