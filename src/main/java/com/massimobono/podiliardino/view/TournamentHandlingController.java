package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.util.Optional;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.dao.DAO;
import com.massimobono.podiliardino.dao.DAOException;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TournamentHandlingController {

	@FXML
	private TableView<Tournament> tournamentTable;
	@FXML
	private TableColumn<Tournament, String> tournamentNameColumn;
	@FXML
	private TableColumn<Tournament, String> tournamentInfoColumn;
	@FXML
	private ListView<Team> availableTeams;
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
	
	/**
	 * A list of {@link Team} that can partecipate in the selected tournament.
	 * This list has to be different that the one in the {@link DAO}. However, to ensure synchronization, listeners are made
	 */
	private ObservableList<Team> availableTeamsList;

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		this.tournamentNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		this.tournamentInfoColumn.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.toString(cellData.getValue().getPartecipations().size())));

		// Listen for selection changes and show the person details when changed.
		this.tournamentTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
		
		this.availableTeams.setCellFactory(
	             new Callback<ListView<Team>, javafx.scene.control.ListCell<Team>>() {
	                 @Override
	                 public ListCell<Team> call(ListView<Team> listView) {
	                     return new TeamListCell(TournamentHandlingController.this.tournamentTable, TournamentHandlingController.this.mainApp.getDAO());
	                 }
	             });
	}

	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.tournamentTable.setItems(this.mainApp.getDAO().getTournamentList());
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
				this.tournamentTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleEditTournament() {
		try {
			if (this.tournamentTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Optional<Tournament> t = this.mainApp.showCustomDialog(
					"TournamentEditDialog", 
					"Edit Tournmanet", 
					(TournamentEditDialogController c, Stage s) -> {
						try {
							c.setup(s, this.tournamentTable.getSelectionModel().getSelectedItem());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TournamentEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTournament() : null);}
					);
			if (t.isPresent()) {
				this.mainApp.getDAO().update(t.get());
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleRemoveTournament() {
		try {
			if (this.tournamentTable.getSelectionModel().getSelectedItem() == null) {
				//the user has selected nothing
				return;
			}
			Tournament t =this.tournamentTable.getSelectionModel().getSelectedItem();
			this.mainApp.getDAO().remove(t);
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			if (newValue == null) {
				//if we delete the last item of the list
				this.tournamentNameLabel.setText("");
				this.tournamentStartDateLabel.setText("");
				this.tournamentEndDateLabel.setText("");
			}else {
				Optional<LocalDate> endDate = newValue.getEndDate().get();
				this.tournamentNameLabel.setText(newValue.getName().get());
				this.tournamentStartDateLabel.setText(Utils.getStandardDateFrom(newValue.getStartDate().get()));
				this.tournamentEndDateLabel.setText(endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
				
				//we need to populate the participation teams
				this.availableTeamsList = FXCollections.observableArrayList(this.mainApp.getDAO().getTeamList());
				this.availableTeamsList.addListener((ListChangeListener.Change<? extends Team> e) -> {
					//if someone adds or removes some team, we will be notified
					this.availableTeamsList.addAll(e.getAddedSubList());
					this.availableTeamsList.removeAll(e.getRemoved());
				});
				this.availableTeams.setItems(this.availableTeamsList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	/**
	 * Computes the list of {@link Team} that can partecipate in the tournament
	 * 
	 * Rules are:
	 * <ol>
	 * 	<li>If a team chooses to partecipate in the tournament, then all the other teams that share even one of the member of the joinining team
	 * 		are excluded</li>
	 * </ol>
	 * 
	 * @param tournament the {@link Tournament} involved
	 * @return an {@link ObservableList} of {@link Team} that may partecipate. This list can mutate depending on the partecipating teams
	 * @throws DAOException if something bad happens
	 */
}
