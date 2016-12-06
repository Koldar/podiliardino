package com.massimobono.podiliardino.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class TeamHandlingController {

	@FXML
	private TableView<Team> teamTable;
	@FXML
	private TableColumn<Team, String> teamNameColumn;
	@FXML
	private TableColumn<Team, String> teamMembersColumn;

	@FXML
	private Label teamNameLabel;
	@FXML
	private Label teamDateLabel;
	@FXML
	private Label firstTeamMemberLabel;
	@FXML
	private Label secondTeamMemberLabel;

	@FXML
	private Button addTeam;
	@FXML
	private Button editTeam;
	@FXML
	private Button deleteTeam;

	private Main mainApp;

	public TeamHandlingController() {
	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		this.teamNameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
		this.teamMembersColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(
						cellData.getValue().getPlayers().stream()
						.map(p -> p.getName().get())
						.collect(Collectors.joining(", "))));
		
		// Listen for selection changes and show the person details when changed.
		this.teamTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectPlayer);

	}

	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.teamTable.setItems(this.mainApp.getDAO().getTeamList());
	}

	@FXML
	private void handleAddTeam() {
		try {
			Optional<Team> t = this.mainApp.showCustomDialog(
					"TeamEditDialog", 
					"New Team", 
					(TeamEditDialogController c, Stage s) -> {
						try {
							c.setup(s, new Team(), this.mainApp.getDAO().getAllPlayers());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TeamEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTeam() : null);}
					);
			if (t.isPresent()) {
				//we have added a new team. We copy the players from the newly generated team, we flush its list, we add the team inside the DAO
				//and then we readd the players inside it. Why do we do that? because the dao start listening for changes in the players list on
				//after addTeam. If we didn't do this procedure the DAO would never know about the new players
				Collection<Player> players = new ArrayList<>(t.get().getPlayers());
				t.get().getPlayers().clear();
				this.mainApp.getDAO().addTeam(t.get());
				for (Player p : players) {
					t.get().add(p);
				}
				this.teamTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleEditTeam() {
		try {
			if (this.teamTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Optional<Team> t = this.mainApp.showCustomDialog(
					"TeamEditDialog", 
					"Edit Team", 
					(TeamEditDialogController c, Stage s) -> {
						try {
							c.setup(s, this.teamTable.getSelectionModel().getSelectedItem(), this.mainApp.getDAO().getAllPlayers());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TeamEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTeam() : null);}
					);
			if (t.isPresent()) {
				//we have added a new player. We can add it to the DAO
				Team t1 = this.mainApp.getDAO().update(t.get());
				this.teamTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleDeleteTeam() {
		try {
			if (this.teamTable.getSelectionModel().getSelectedItem() == null) {
				//the user has selected nothing
				return;
			}
			Team t =this.teamTable.getSelectionModel().getSelectedItem();
			if (!Utils.waitUserReplyForConfirmationDialog(String.format("Do you really want to delete team \"%s\"?",t.getName().get()), "Removing the team wil lalso remove every tournament the team has ever partecipate in in, all its days and all its matches. Are you sure?")){
				return;
			}
			this.mainApp.getDAO().remove(t);
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void handleUserSelectPlayer(ObservableValue<? extends Team> observableValue, Team oldValue, Team newValue) {
		try {
			if (newValue == null) {
				//we delete the last item of the list
				this.teamNameLabel.setText("");
				this.teamDateLabel.setText("");
				this.firstTeamMemberLabel.setText("");
				this.secondTeamMemberLabel.setText("");
			} else {
				this.teamNameLabel.setText(newValue.getName().get());
				this.teamDateLabel.setText(Utils.getStandardDateFrom(newValue.getDate().get()));
				this.firstTeamMemberLabel.setText(newValue.getPlayers().size() > 0 ? newValue.getPlayers().get(0).getName().get() : "");
				this.secondTeamMemberLabel.setText(newValue.getPlayers().size() > 1 ? newValue.getPlayers().get(1).getName().get() : "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}


}
