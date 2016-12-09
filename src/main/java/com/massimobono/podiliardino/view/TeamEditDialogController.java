package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.util.I18N;
import com.massimobono.podiliardino.util.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class TeamEditDialogController {
	
	private static final String NAME_REGEX = "^[a-zA-Z0-9 ]+$";
	
	@FXML
	private TextField nameTextField;
	@FXML
	private TextField dateTextField;
	@FXML
	private ComboBox<Player> firstTeamMemberChoiceBox;
	@FXML
	private ComboBox<Player> secondTeamMemberChoiceBox;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	/**
	 * {@link Team} we're currently changing
	 */
	private Team teamInvolved;
	/**
	 * True if the user has pressed OK when he went out from this modal, false otherwise
	 */
	private boolean clickedOK;
	/**
	 * The dialog we're currently in
	 */
	private Stage dialog;
	
	public TeamEditDialogController() {
	}
	
	@FXML
	private void initialize() {
		this.firstTeamMemberChoiceBox.setCellFactory(this.getDefaultCellFactory());
		this.firstTeamMemberChoiceBox.setButtonCell(this.getDefaultButtonCell());
		
		this.secondTeamMemberChoiceBox.setCellFactory(this.getDefaultCellFactory());
		this.secondTeamMemberChoiceBox.setButtonCell(this.getDefaultButtonCell());
		
		this.okButton.setDefaultButton(true);
	}
	
	private ListCell<Player> getDefaultButtonCell() {
		return new ListCell<Player>() {
			@Override
	        protected void updateItem(Player t, boolean bln) {
	            super.updateItem(t, bln); 
	            if (bln) {
	                setText("");
	            } else {
	                setText(String.format("%s %s (age: %s)", t.getName(), t.getSurname(), t.getAge().isPresent() ? t.getAge().get() : "unknown"));
	            }
	        }
		};
	}
	
	private Callback<ListView<Player>, ListCell<Player>> getDefaultCellFactory() {
		return new Callback<ListView<Player>,ListCell<Player>>(){
            @Override
            public ListCell<Player> call(ListView<Player> l){
                return new ListCell<Player>(){
                    @Override
                    protected void updateItem(Player item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(String.format("%s %s (age: %s)", item.nameProperty().get(), item.surnameProperty().get(), item.getAge().isPresent() ? item.getAge().get() : "unknown"));
                        }
                    }
                } ;
            }
        };
	}
	
	public void setup(Stage dialog, Team teamToHandle, Collection<Player> players) {
		this.setDialog(dialog);
		this.setAvailablePlayers(players);
		this.setTeam(teamToHandle);
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.teamInvolved.nameProperty().set(this.nameTextField.getText());
			this.teamInvolved.dateProperty().set(Utils.getDateFrom(this.dateTextField.getText()));
			this.teamInvolved.playersProperty().clear();
			this.teamInvolved.playersProperty().add(this.firstTeamMemberChoiceBox.getValue());
			this.teamInvolved.playersProperty().add(this.secondTeamMemberChoiceBox.getValue());
			this.clickedOK = true;
			this.dialog.close();
		}
	}
	
	/**
	 * Checks values inside {@link #nameTextField}, {@link #surnameTextField}, {@link #birthdayTextField}, {@link #phoneTextField}
	 * 
	 * If any check fail, the function return false. For every malformed value, the function will notify the user with an error message
	 * @return
	 */
	private boolean checkValues() {
		Collection<String>strs = new ArrayList<>();
		if (!this.nameTextField.getText().matches(NAME_REGEX)) {
			strs.add(I18N.get("name_must_be_non_empty_and_have_only_alphanumeric_characters_or_spaces"));
		}
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.dateTextField.getText());
		} catch (DateTimeParseException e) {
			strs.add(String.format(I18N.get("cannot_parse_date_it_has_to_be_of_format"), Utils.STANDARD_DATE_PATTERN));
		}
		
		if (this.firstTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == null) {
			strs.add(I18N.get("the_first_member_of_the_team_is_unselected_please_select_one_member_of_the_team"));
		}
		if (this.secondTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == null) {
			strs.add(I18N.get("the_second_member_of_the_team_is_unselected_please_select_one_member_of_the_team"));
		}
		if (this.firstTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == this.secondTeamMemberChoiceBox.getSelectionModel().getSelectedItem()) {
			strs.add(I18N.get("a_team_canot_be_formed_by_a_single_person"));
		}
		
		if (!strs.isEmpty()) {
			Utils.createDefaultErrorAlert(
					I18N.get("error_in_input_data"), 
					String.join("\n", strs));
		}
		
		return strs.isEmpty();
	}
	
	/**
	 * Set the {@link Team} this view needs to handle.
	 * 
	 * It also set the textfields presents inside this view with the data of the player
	 * @param team the team to change
	 */
	public void setTeam(Team team) {
		this.teamInvolved = team;
		
		this.nameTextField.setText(team.nameProperty().get());
		if (team.playersProperty().size() >= 2) { 
			this.firstTeamMemberChoiceBox.setValue(team.playersProperty().get(0));
			this.secondTeamMemberChoiceBox.setValue(team.playersProperty().get(1));
		}
		this.dateTextField.setText(Utils.getStandardDateFrom(team.dateProperty().get()));
	}

	/**
	 * @return the clickedOK
	 */
	public boolean isClickedOK() {
		return clickedOK;
	}
	
	/**
	 * 
	 * @return the team we have handled
	 */
	public Team getTeam() {
		return this.teamInvolved;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Stage dialog) {
		this.dialog = dialog;
	}
	
	public void setAvailablePlayers(Collection<Player> players) {
		ObservableList<Player> ol = FXCollections.observableArrayList(players);
		this.firstTeamMemberChoiceBox.setItems(ol);
		this.secondTeamMemberChoiceBox.setItems(ol);
	}
	
	
}
