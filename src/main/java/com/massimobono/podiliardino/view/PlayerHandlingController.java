package com.massimobono.podiliardino.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.I18N;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class PlayerHandlingController {

	@FXML
	private Button newPlayer;
	@FXML
	private Button editPlayer;
	@FXML
	private Button deletePlayer;

	@FXML
	private Label nameLabel;
	@FXML
	private Label surnameLabel;
	@FXML
	private Label birthdayLabel;
	@FXML
	private Label phoneLabel;

	@FXML
	private TableView<Player> playersTable;
	@FXML
	private TableColumn<Player, String> nameColumn;
	@FXML
	private TableColumn<Player, String> surnameColumn;

	/**
	 * Main application. Used to get all the data related to the software
	 */
	private Main mainApp;

	public PlayerHandlingController() {
	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		this.surnameColumn.setCellValueFactory(cellData -> cellData.getValue().surnameProperty());

		// Listen for selection changes and show the person details when changed.
		this.playersTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectPlayer);
	}

	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		this.playersTable.setItems(this.mainApp.getDAO().getPlayerList().filtered(p -> !p.isSpecial()));
	}

	@FXML
	public void handleNewPlayer() {
		try {
			Optional<Player> p = this.mainApp.showCustomDialog(
					"PlayerEditDialog", 
					String.format(I18N.get("new_object"), I18N.get("player")), 
					(PlayerEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setPlayer(new Player());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getPlayer() : null);}
					);
			if (p.isPresent()) {
				//we have added a new player. We can add it to the DAO
				Player p1 = this.mainApp.getDAO().add(p.get());
				this.playersTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	public void handleEditPlayer() {
		try {
			if (this.playersTable.getSelectionModel().getSelectedItem() == null) {
				//the user has selected nothing
				return;
			}
			Optional<Player> p = this.mainApp.showCustomDialog(
					"PlayerEditDialog", 
					String.format(I18N.get("update_object"), I18N.get("player")), 
					(PlayerEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setPlayer(this.playersTable.getSelectionModel().getSelectedItem());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getPlayer() : null);}
					);
			if (p.isPresent()) {
				//we have added a new player. We can add it to the DAO
				this.mainApp.getDAO().updatePlayer(p.get());
				this.playersTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleDeletePlayer() {
		try {
			if (this.playersTable.getSelectionModel().getSelectedItem() == null) {
				//the user has selected nothing
				Utils.createDefaultErrorAlert(
						String.format(I18N.get("cant_remove"), I18N.get("player")),
						I18N.get("in_order_to_remove_a_player_you_need_to_select_one"));
				return;
			}
			Player p = this.playersTable.getSelectionModel().getSelectedItem();
			if (!Utils.waitUserReplyForConfirmationDialog(
					String.format(
							I18N.get("do_you_really_want_to_remove_the_player"), p.getName()),
							I18N.get("if_you_remove_the_following_player_all_the_team_he_belongs_will_be_removed_as_well_this_will_also_remove_all_the_tournaments_they_partecipate_in_whe_all_the_related_day_and_related_matches_are_you_sure"))){
				return;
			}
			this.mainApp.getDAO().remove(p);
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	private void handleUserSelectPlayer(ObservableValue<? extends Player> observableValue, Player oldValue, Player newValue) {
		try {
			if (newValue == null) {
				//when we delete the last item of the list
				this.nameLabel.setText("");
				this.surnameLabel.setText("");
				this.birthdayLabel.setText("");
				this.phoneLabel.setText("");
			}else {
				Optional<String> birthday = newValue.getBirthdayAsStandardString();
				this.nameLabel.setText(newValue.nameProperty().get());
				this.surnameLabel.setText(newValue.surnameProperty().get());
				this.birthdayLabel.setText(birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
				this.phoneLabel.setText(newValue.phoneProperty().get().isPresent() ? newValue.phoneProperty().get().get() : Utils.EMPTY_PHONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}


}
