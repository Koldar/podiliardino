package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.util.Optional;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class TournamentHandlingController {

	@FXML
	private TableView<Tournament> tournamentTable;
	@FXML
	private TableColumn<Tournament, String> tournamentNameColumn;
	@FXML
	private TableColumn<Tournament, String> tournamentInfoColumn;
	@FXML
	private ListView availableTeams;
	@FXML
	private Label tournamentNameLabel;
	@FXML
	private Label tournamentStartDateLabel;
	@FXML
	private Label tournamentEndDateLabel;
	@FXML
	private Button addTournament;
	@FXML
	private Button editTournament;
	@FXML
	private Button removeTournament;

	private Main mainApp;

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		this.tournamentNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		this.tournamentInfoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getPartecipations().size())));

		// Listen for selection changes and show the person details when changed.
		this.tournamentTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
	}

	public void setup(Main mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleAddTournament() {
		try {
			Optional<Tournament> t = this.mainApp.showCustomDialog(
					"TournamentEditDialog", 
					"New Tournmanet", 
					(TournamentEditDialogController c, Stage s) -> {
						try {
							c.setup(s, new Tournament());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TournamentEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTournament() : null);}
					);
			if (t.isPresent()) {
				//we have added a new player. We can add it to the DAO
				this.mainApp.getDAO().add(t.get());
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleEditTournament() {

	}

	@FXML
	private void handleRemoveTournament() {

	}

	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			Optional<LocalDate> endDate = newValue.getEndDate().get();
			this.tournamentNameLabel.setText(newValue.getName().get());
			this.tournamentStartDateLabel.setText(Utils.getStandardDateFrom(newValue.getStartDate().get()));
			this.tournamentEndDateLabel.setText(endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
}
