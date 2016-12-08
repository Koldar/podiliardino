package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.zip.DataFormatException;

import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.Utils;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TournamentEditDialogController {
	
	private static final String NAME_REGEX = "^[a-zA-Z\\d ]+$";
	
	@FXML
	private TextField nameTextField;
	@FXML
	private TextField startDateTextField;
	@FXML
	private TextField endDateTextField;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	/**
	 * {@link Tournament} we're currently changing
	 */
	private Tournament tournamentInvolved;
	/**
	 * True if the user has pressed OK when he went out from this modal, false otherwise
	 */
	private boolean clickedOK;
	/**
	 * The dialog we're currently in
	 */
	private Stage dialog;
	
	public TournamentEditDialogController() {
	}
	
	public void setup(Stage dialog, Tournament tournamentHandle) {
		this.dialog = dialog;
		this.setTournament(tournamentHandle);
	}
	
	@FXML
	private void initialize() {
		this.okButton.setDefaultButton(true);
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.tournamentInvolved.nameProperty().set(this.nameTextField.getText());
			this.tournamentInvolved.startDateProperty().set(Utils.getDateFrom(this.startDateTextField.getText()));
			this.tournamentInvolved.endDateProperty().set(Optional.ofNullable(!this.endDateTextField.getText().equalsIgnoreCase(Utils.EMPTY_DATE) ? Utils.getDateFrom(this.endDateTextField.getText()) : null));
			this.clickedOK = true;
			this.dialog.close();
		}
	}
	
	/**
	 * Checks values inside {@link #nameTextField}, {@link #startDateTextField}, {@link #endDateTextField}
	 * 
	 * If any check fail, the function return false. For every malformed value, the function will notify the user with an error message
	 * @return
	 */
	private boolean checkValues() {
		Collection<String>strs = new ArrayList<>();
		if (!this.nameTextField.getText().matches(NAME_REGEX)) {
			strs.add("Name must be non empty and have only alphanumeric characters");
		}
		
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.startDateTextField.getText());
		} catch (DateTimeParseException e) {
			strs.add("Tournament start date have to be set. It must follow the pattern "+Utils.STANDARD_DATE_PATTERN);
		}
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.endDateTextField.getText());
		} catch (DateTimeParseException e) {
			this.endDateTextField.setText(Utils.EMPTY_DATE);
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
	 * Set the tournament this view needs to handle.
	 * 
	 * It also set the textfields presents inside this view with the data of the tournament
	 * @param tournament the {@link Tournament} to change
	 */
	private void setTournament(Tournament tournament) {
		this.tournamentInvolved = tournament;
		
		this.nameTextField.setText(tournament.nameProperty().get());
		this.startDateTextField.setText(Utils.getStandardDateFrom(tournament.startDateProperty().get()));
		this.endDateTextField.setText(tournament.endDateProperty().get().isPresent() ? Utils.getStandardDateFrom(tournament.endDateProperty().get().get()) : Utils.EMPTY_DATE);
	}

	/**
	 * @return the clickedOK
	 */
	public boolean isClickedOK() {
		return clickedOK;
	}
	
	/**
	 * 
	 * @return the player we have handled
	 */
	public Tournament getTournament() {
		return this.tournamentInvolved;
	}

	
	
}
