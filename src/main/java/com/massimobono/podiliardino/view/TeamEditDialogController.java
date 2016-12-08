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
	}
	
	private ListCell<Player> getDefaultButtonCell() {
		return new ListCell<Player>() {
			@Override
	        protected void updateItem(Player t, boolean bln) {
	            super.updateItem(t, bln); 
	            if (bln) {
	                setText("");
	            } else {
	                setText(String.format("%s %s (age: %s)", t.nameProperty().get(), t.surnameProperty().get(), t.getAge().isPresent() ? t.getAge().get() : "unknown"));
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
			this.teamInvolved.getName().set(this.nameTextField.getText());
			this.teamInvolved.getDate().set(Utils.getDateFrom(this.dateTextField.getText()));
			this.teamInvolved.getPlayers().clear();
			this.teamInvolved.getPlayers().add(this.firstTeamMemberChoiceBox.getValue());
			this.teamInvolved.getPlayers().add(this.secondTeamMemberChoiceBox.getValue());
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
			strs.add("Name must be non empty and have only alphanumeric characters or spaces");
		}
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.dateTextField.getText());
		} catch (DateTimeParseException e) {
			strs.add("Cannot parse date. It has to be of format "+Utils.STANDARD_DATE_PATTERN);
		}
		
		if (this.firstTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == null) {
			strs.add("The first member of the team is unselected. Please select one member of the team.");
		}
		if (this.secondTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == null) {
			strs.add("The second member of the team is unselected. Please select one member of the team.");
		}
		if (this.firstTeamMemberChoiceBox.getSelectionModel().getSelectedItem() == this.secondTeamMemberChoiceBox.getSelectionModel().getSelectedItem()) {
			strs.add("A team cannot be formed by a single person (even if he's Superman).");
		}
		
		if (!strs.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Error In input data");
			alert.setContentText(String.join("\n", strs));
			alert.showAndWait();
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
		
		this.nameTextField.setText(team.getName().get());
		if (team.getPlayers().size() >= 2) { 
			this.firstTeamMemberChoiceBox.setValue(team.getPlayers().get(0));
			this.secondTeamMemberChoiceBox.setValue(team.getPlayers().get(1));
		}
		this.dateTextField.setText(Utils.getStandardDateFrom(team.getDate().get()));
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
