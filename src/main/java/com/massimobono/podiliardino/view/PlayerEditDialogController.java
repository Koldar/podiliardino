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

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PlayerEditDialogController {
	
	private static final String NAME_REGEX = "^[a-zA-Z]+$";
	private static final String SURNAME_REGEX = "^[a-zA-Z]+$";
	private static final String PHONE_REGEX = "^[0-9]{10}$";
	
	@FXML
	private TextField nameTextField;
	@FXML
	private TextField surnameTextField;
	@FXML
	private TextField birthdayTextField;
	@FXML
	private TextField phoneTextField;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	/**
	 * Player we're currently changing
	 */
	private Player playerInvolved;
	/**
	 * True if the user has pressed OK when he went out from this modal, false otherwise
	 */
	private boolean clickedOK;
	/**
	 * The dialog we're currently in
	 */
	private Stage dialog;
	
	public PlayerEditDialogController() {
	}
	
	@FXML
	private void initialize() {
		
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.playerInvolved.setName(this.nameTextField.getText());
			this.playerInvolved.setSurname(this.surnameTextField.getText());
			this.playerInvolved.setBirthdayFromStandardString(this.birthdayTextField.getText());
			this.playerInvolved.setPhone(this.phoneTextField.getText());
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
			strs.add("Name must be non empty and have only alphabetic characters");
		}
		
		if (!this.surnameTextField.getText().matches(SURNAME_REGEX)){
			strs.add("Surname must be non empty and have only alphabetic characters");
		}
		
		try {
			DateTimeFormatter.ofPattern(Player.BIRTHDAY_PATTERN).parse(this.birthdayTextField.getText());
		} catch (DateTimeParseException e) {
			strs.add("Cannot parse date. It has to be of format "+Player.BIRTHDAY_PATTERN);
		}
		
		if (!this.phoneTextField.getText().matches(PHONE_REGEX)) {
			strs.add("Phone must have 10 digits");
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
	
	public void setPlayer(Player player) {
		this.playerInvolved = player;
		
		this.nameTextField.setText(player.getName());
		this.surnameTextField.setText(player.getSurname());
		this.birthdayTextField.setText(player.getBirthdayAsStandardString());
		this.phoneTextField.setText(player.getPhone());
	}

	/**
	 * @return the clickedOK
	 */
	public boolean isClickedOK() {
		return clickedOK;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Stage dialog) {
		this.dialog = dialog;
	}
	
	
}
