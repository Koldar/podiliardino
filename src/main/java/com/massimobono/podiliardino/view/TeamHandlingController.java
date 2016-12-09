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
import com.massimobono.podiliardino.util.I18N;
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
		this.teamNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		this.teamMembersColumn.setCellValueFactory(
				cellData -> new SimpleStringProperty(
						cellData.getValue().playersProperty().stream()
						.map(p -> p.nameProperty().get())
						.collect(Collectors.joining(", "))));
		
		// Listen for selection changes and show the person details when changed.
		this.teamTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectPlayer);

	}

	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.teamTable.setItems(this.mainApp.getDAO().getTeamList().filtered(t -> !t.isSpecial()));
	}

	@FXML
	private void handleAddTeam() {
		try {
			Optional<Team> t = this.mainApp.showCustomDialog(
					"TeamEditDialog", 
					String.format(I18N.get().getString("new_object"), I18N.get().getString("team")),
					(TeamEditDialogController c, Stage s) -> {
						try {
							c.setup(s, new Team(), this.mainApp.getDAO().getAllPlayersThat(p -> !p.isSpecial()));
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
				Collection<Player> players = new ArrayList<>(t.get().playersProperty());
				t.get().playersProperty().clear();
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
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_update"), I18N.get().getString("team")),
						I18N.get().getString("in_order_to_update_a_team_you_need_to_select_one")
						);
				return;
			}
			Optional<Team> t = this.mainApp.showCustomDialog(
					"TeamEditDialog", 
					String.format(I18N.get().getString("update_object"), I18N.get().getString("team")), 
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
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_remove"), I18N.get().getString("team")), 
						I18N.get().getString("in_order_to_remove_a_team_you_need_to_select_one"));
				//the user has selected nothing
				return;
			}
			Team t =this.teamTable.getSelectionModel().getSelectedItem();
			if (!Utils.waitUserReplyForConfirmationDialog(
					String.format(I18N.get().getString("do_you_really_want_to_delete_team"),t.getName()),
					I18N.get().getString("removing_the_team_will_also_remove_every_tournament_the_team_has_ever_partecipate_in_it_all_its_day_and_all_its_matches_are_you_sure"))){
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
				this.teamNameLabel.setText(newValue.nameProperty().get());
				this.teamDateLabel.setText(Utils.getStandardDateFrom(newValue.dateProperty().get()));
				this.firstTeamMemberLabel.setText(newValue.playersProperty().size() > 0 ? newValue.playersProperty().get(0).nameProperty().get() : "");
				this.secondTeamMemberLabel.setText(newValue.playersProperty().size() > 1 ? newValue.playersProperty().get(1).nameProperty().get() : "");
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}


}
