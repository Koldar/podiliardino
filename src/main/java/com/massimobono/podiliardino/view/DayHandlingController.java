package com.massimobono.podiliardino.view;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private static final Logger LOG = LogManager.getLogger(DayHandlingController.class);

	@FXML
	private TableView<Tournament> tournamentTableView;
	@FXML
	private TableColumn<Tournament, String> tournamentTableColumn;
	@FXML
	private TableView<Day> dayTableView;
	@FXML
	private TableColumn<Day, Integer> dayTableColumn;
	
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
		this.dayTableView.setItems(this.daysToDisplay);
	}
	
	@FXML
	private void initialize() {
		this.tournamentTableColumn.setCellValueFactory(celldata -> celldata.getValue().getName());
		this.dayTableColumn.setCellValueFactory(celldata -> celldata.getValue().getNumber().asObject());
		
		this.tournamentTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
		this.dayTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectDay);
	}
	
	@FXML
	private void handleAddDay() {
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't add a new day", "In order to create a new day, a tournament needs to be selected in the current frame");
				return;
			}
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					"New Day", 
					(DayEditDialogController c, Stage s) -> {
						Day newDay = new Day();
						newDay.getTournament().set(this.tournamentTableView.getSelectionModel().getSelectedItem());
						newDay.getNumber().set(newDay.getTournament().get().getDays().size()+1);
						c.setup(s,newDay); 
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
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't edit a new day", "In order to edit a day, a tournament needs to be selected in the current frame");
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't edit a new day", "In order to edit a day, a day needs to be selected in the current frame");
				return;
			}
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					"Edit Day", 
					(DayEditDialogController c, Stage s) -> {
						c.setup(s,this.dayTableView.getSelectionModel().getSelectedItem());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getDay() : null);}
					);
			if (d.isPresent()) {
				//we have added a new player. We can add it to the DAO
				this.mainApp.getDAO().update(d.get());
				this.dayTableView.getSelectionModel().clearSelection();
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleRemoveDay() {
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't remove a day", "In order to remove a day, a tournament needs to be selected in the current frame");
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't remove a day", "In order to remove a day, a day needs to be selected in the current frame");
				return;
			}
			Day d =this.dayTableView.getSelectionModel().getSelectedItem();
			this.mainApp.getDAO().remove(d);
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			this.daysToDisplay.clear();
			if (newValue == null) {
				//we delete the last item of the list
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
