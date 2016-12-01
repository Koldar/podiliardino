package com.massimobono.podiliardino.view;

import java.util.Optional;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.dao.DAOException;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class DayHandlingController {

	@FXML
	private TableView<Tournament> tournamentTableView;
	@FXML
	private TableColumn<Tournament, String> tournamentTableColumn;
	@FXML
	private TableView<Day> dayTableView;
	@FXML
	private TableColumn<Day, String> dayTableColumn;
	
	@FXML
	private Button addDay;
	@FXML
	private Button editDay;
	@FXML
	private Button deleteDay;
	
	private Main mainApp;
	/**
	 * A list representing all the day to display inside dayTableView
	 */
	private ObservableList<Day> daysToDisplay;
	
	public DayHandlingController() {
		this.daysToDisplay = FXCollections.observableArrayList();
	}
	
	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.tournamentTableView.setItems(this.mainApp.getDAO().getTournamentList());
	}
	
	@FXML
	private void initialize() {
		this.tournamentTableColumn.setCellValueFactory(celldata -> celldata.getValue().getName());
	}
	
	@FXML
	private void handleAddDay() {
		try {
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					"New Day", 
					(DayEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setDay(new Day());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getDay() : null);}
					);
			if (d.isPresent()) {
				//we have added a new player. We can add it to the DAO
				Day p1 = this.mainApp.getDAO().add(d.get());
				this.dayTableView.getSelectionModel().clearSelection();
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleEditDay() {
		
	}
	
	@FXML
	private void handleRemoveDay() {
		
	}
	
	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			if (newValue == null) {
				//we delete the last item of the list
				this.daysToDisplay.clear();
			} else {
				this.daysToDisplay.addAll(newValue.getDays());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void handleUserSelectDay(ObservableValue<? extends Day> observableValue, Day oldValue, Day newValue) {
		try {
			if (newValue == null) {
				//we delete the last item of the list
				
			} else {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
}
