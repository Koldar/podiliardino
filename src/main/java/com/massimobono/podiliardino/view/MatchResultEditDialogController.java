package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
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

public class MatchResultEditDialogController {
	
	@FXML
	private TextField team1GoalsTextField;
	@FXML
	private TextField team2GoalsTextField;
	@FXML
	private TextField pointsEarnedFromWinningTextField;
	@FXML
	private TextField pointsEarnedFromLosingTextField;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	/**
	 * {@link Match} we're currently changing
	 */
	private Match matchInvolved;
	/**
	 * True if the user has pressed OK when he went out from this modal, false otherwise
	 */
	private boolean clickedOK;
	/**
	 * The dialog we're currently in
	 */
	private Stage dialog;
	
	public MatchResultEditDialogController() {
	}
	
	@FXML
	private void initialize() {
		this.okButton.setDefaultButton(true);
	}
	
	public void setup(Stage dialog, Match matchToHandle) {
		this.setDialog(dialog);
		this.setMatch(matchToHandle);
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.matchInvolved.getTeam1Goals().set(Integer.parseInt(this.team1GoalsTextField.getText()));
			this.matchInvolved.getTeam2Goals().set(Integer.parseInt(this.team2GoalsTextField.getText()));
			this.matchInvolved.getPointsEarnedByWinning().set(Integer.parseInt(this.pointsEarnedFromWinningTextField.getText()));
			this.matchInvolved.getPointsEarnedByLosing().set(Integer.parseInt(this.pointsEarnedFromLosingTextField.getText()));
			this.matchInvolved.getStatus().set(MatchStatus.DONE);
			this.clickedOK = true;
			this.dialog.close();
		}
	}
	
	/**
	 * Checks values inside {@link #team1GoalsTextField}, {@link #team2GoalsTextField}, {@link #pointsEarnedFromWinningTextField}, {@link #pointsEarnedFromLosingTextField}
	 * 
	 * If any check fail, the function return false. For every malformed value, the function will notify the user with an error message
	 * @return
	 */
	private boolean checkValues() {
		Collection<String>strs = new ArrayList<>();
		
		try {
			Integer.parseInt(this.team1GoalsTextField.getText());
		} catch (NumberFormatException e) {
			strs.add(I18N.get().getString("team_1_goals_cant_be_converted_into_a_number"));
		}
		
		try {
			Integer.parseInt(this.team2GoalsTextField.getText());
		} catch (NumberFormatException e) {
			strs.add(I18N.get().getString("team_2_goals_cant_be_converted_into_a_number"));
		}
		
		try {
			Integer.parseInt(this.pointsEarnedFromWinningTextField.getText());
		} catch (NumberFormatException e) {
			strs.add(I18N.get().getString("points_earned_from_winning_cant_be_converted_into_a_number"));
		}
		
		try {
			Integer.parseInt(this.pointsEarnedFromLosingTextField.getText());
		} catch (NumberFormatException e) {
			strs.add(I18N.get().getString("points_earned_from_losing_cant_be_converted_into_a_number"));
		}
		
		if (!strs.isEmpty()) {
			Utils.createDefaultErrorAlert(
					I18N.get().getString("error_in_input_data"), 
					String.join("\n", strs));
		}
		
		return strs.isEmpty();
	}
	
	/**
	 * Set the {@link Day} this view needs to handle.
	 * 
	 * @param match the match to change
	 */
	public void setMatch(Match match) {
		this.matchInvolved = match;
		
		this.team1GoalsTextField.setText(Integer.toString(match.getTeam1Goals().get()));
		this.team2GoalsTextField.setText(Integer.toString(match.getTeam2Goals().get()));
		this.pointsEarnedFromWinningTextField.setText(Integer.toString(match.getPointsEarnedByWinning().get()));
		this.pointsEarnedFromLosingTextField.setText(Integer.toString(match.getPointsEarnedByLosing().get()));
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
	public Match getMatch() {
		return this.matchInvolved;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Stage dialog) {
		this.dialog = dialog;
	}
	
	
}
