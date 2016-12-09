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

public class DayEditDialogController {
	
	@FXML
	private TextField dateTextField;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	/**
	 * {@link Day} we're currently changing
	 */
	private Day dayInvolved;
	/**
	 * True if the user has pressed OK when he went out from this modal, false otherwise
	 */
	private boolean clickedOK;
	/**
	 * The dialog we're currently in
	 */
	private Stage dialog;
	
	public DayEditDialogController() {
	}
	
	@FXML
	private void initialize() {
		this.okButton.setDefaultButton(true);
	}
	
	private ListCell<Tournament> getDefaultButtonCell() {
		return new ListCell<Tournament>() {
			@Override
	        protected void updateItem(Tournament t, boolean bln) {
	            super.updateItem(t, bln); 
	            if (bln) {
	                setText("");
	            } else {
	                setText(String.format("%s", t.nameProperty().get()));
	            }
	        }
		};
	}
	
	private Callback<ListView<Tournament>, ListCell<Tournament>> getDefaultCellFactory() {
		return new Callback<ListView<Tournament>,ListCell<Tournament>>(){
            @Override
            public ListCell<Tournament> call(ListView<Tournament> l){
                return new ListCell<Tournament>(){
                    @Override
                    protected void updateItem(Tournament item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(String.format("%s", item.nameProperty().get()));
                        }
                    }
                } ;
            }
        };
	}
	
	public void setup(Stage dialog, Day dayToHandle) {
		this.setDialog(dialog);
		this.setDay(dayToHandle);
	}
	
	@FXML
	private void handleCancel() {
		this.dialog.close();
	}
	
	@FXML
	private void handleOK() {
		if (this.checkValues()) {
			this.dayInvolved.dateProperty().set(Utils.getDateFrom(this.dateTextField.getText()));
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
		
		try {
			DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(this.dateTextField.getText());
		} catch (DateTimeParseException e) {
			strs.add(String.format(I18N.get().getString("cannot_parse_date_it_has_to_be_of_format"), Utils.STANDARD_DATE_PATTERN));
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
	 * @param team the team to change
	 */
	public void setDay(Day day) {
		this.dayInvolved = day;
		
		this.dateTextField.setText(Utils.getStandardDateFrom(day.dateProperty().get()));
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
	public Day getDay() {
		return this.dayInvolved;
	}

	/**
	 * @param dialog the dialog to set
	 */
	public void setDialog(Stage dialog) {
		this.dialog = dialog;
	}
	
	
}
