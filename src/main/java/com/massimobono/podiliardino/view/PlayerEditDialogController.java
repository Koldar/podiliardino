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
import com.massimobono.podiliardino.util.I18N;
import com.massimobono.podiliardino.util.Utils;

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
		this.okButton.setDefaultButton(true);
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.playerInvolved.nameProperty().set(this.nameTextField.getText());
			this.playerInvolved.surnameProperty().set(this.surnameTextField.getText());
			this.playerInvolved.setBirthdayFromStandardString(this.birthdayTextField.getText().length() == 0 ? null : this.birthdayTextField.getText());
			this.playerInvolved.phoneProperty().set(Optional.ofNullable(this.phoneTextField.getText().length() == 0 ? null : this.phoneTextField.getText()));
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
			strs.add(I18N.get().getString("name_must_be_non_empty_and_have_only_alphabetic_characters"));
		}
		
		if (!this.surnameTextField.getText().matches(SURNAME_REGEX)){
			strs.add(I18N.get().getString("surname_must_be_non_empty_and_have_only_alphabetic_characters"));
		}
		
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.birthdayTextField.getText());
		} catch (DateTimeParseException e) {
			this.birthdayTextField.setText(Utils.EMPTY_DATE);
		}
		
		if (!this.phoneTextField.getText().matches(PHONE_REGEX)) {
			this.phoneTextField.setText(Utils.EMPTY_PHONE);
		}
		
		if (!strs.isEmpty()) {
			Utils.createDefaultErrorAlert(
					I18N.get().getString("error_in_input_data"), 
					String.join("\n", strs));
		}
		
		return strs.isEmpty();
	}
	
	/**
	 * Set the player this view needs to handle.
	 * 
	 * It also set the textfields presents inside this view with the data of the player
	 * @param player the player to change
	 */
	public void setPlayer(Player player) {
		this.playerInvolved = player;
		
		Optional<String>  birthday = player.getBirthdayAsStandardString();
		this.nameTextField.setText(player.nameProperty().get());
		this.surnameTextField.setText(player.surnameProperty().get());
		this.birthdayTextField.setText(birthday.isPresent() ? birthday.get() : Utils.EMPTY_DATE);
		this.phoneTextField.setText(player.phoneProperty().get().isPresent() ? player.phoneProperty().get().get() : Utils.EMPTY_PHONE);
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
	public Player getPlayer() {
		return this.playerInvolved;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Stage dialog) {
		this.dialog = dialog;
	}
	
	
}
