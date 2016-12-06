package com.massimobono.podiliardino.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import com.massimobono.podiliardino.extensibles.dao.DAO;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class Utils {

	public static final String STANDARD_DATE_PATTERN = "dd-MM-yyyy";
	public static final String  EMPTY_PHONE = "";
	public static final String EMPTY_DATE = "";
	public static final int DEFAULT_POINTS_EARNED_FROM_WINNING = 3;
	public static final int DEFAULT_POINTS_EARNED_FROM_LOSING = 0;
	
	
	private static final Player DUMMYPLAYER1 = new Player(0, "dummy1", "", LocalDate.now(), "", new ArrayList<>());
	private static final Player DUMMYPLAYER2 = new Player(0, "dummy2", "", LocalDate.now(), "", new ArrayList<>());
	public static final Team DUMMYTEAM = new Team(0, "", LocalDate.now(), Arrays.asList(), new ArrayList<>(), new ArrayList<>());
	
	/**
	 * You might want to produce fake matches where a team goes against noone and get a default victory.
	 * 
	 * Call this function and then a "dummy" team will be available to you
	 * 
	 * @param dao
	 * @throws DAOException
	 */
	public static void addDummyTeam(DAO dao) throws DAOException {
		if (!dao.getPlayerThat(p -> p.getName().get().equals(DUMMYPLAYER1.getName().get())).isPresent()) {
			dao.add(DUMMYPLAYER1);
		} else {
			DUMMYPLAYER1.setId(dao.getPlayerThat(p -> p.getName().get().equals(DUMMYPLAYER1.getName().get())).get().getId());			
		}
		if (!dao.getPlayerThat(p -> p.getName().get().equals(DUMMYPLAYER2.getName().get())).isPresent()) {
			dao.add(DUMMYPLAYER2);
		} else {
			DUMMYPLAYER2.setId(dao.getPlayerThat(p -> p.getName().get().equals(DUMMYPLAYER2.getName().get())).get().getId());			
		}
		
		if (!dao.getTeamThat(t -> t.getName().get().equals(DUMMYTEAM.getName().get())).isPresent()) {
			dao.addTeam(DUMMYTEAM);
			DUMMYTEAM.add(DUMMYPLAYER1);
			DUMMYTEAM.add(DUMMYPLAYER2);
		} else {
			DUMMYTEAM.setId(dao.getTeamThat(t -> t.getName().get().equals(DUMMYTEAM.getName().get())).get().getId());
		}
		
	}
	
	public static Team getDummyTeam(DAO dao) throws DAOException {
		Optional<Team> retVal = dao.getTeamThat(t -> t.getName().get().equals(DUMMYTEAM.getName().get()));
		if (!retVal.isPresent()) {
			throw new DAOException("Dummy team not present. Are you sure you've called at least once addDummyTeam?");
		}
		return retVal.get();
	}
	
	/**
	 * 
	 * @param standardDate
	 * @return the converted {@link LocalDate}, according to {@link #STANDARD_DATE_PATTERN} pattern
	 */
	public static LocalDate getDateFrom(String standardDate) {
		return DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN).parse(standardDate, LocalDate::from);
	}
	
	public static String getStandardDateFrom(LocalDate date) {
		return date.format(DateTimeFormatter.ofPattern(Utils.STANDARD_DATE_PATTERN));
	}
	
	public static Alert createDefaultErrorAlert(String header, String body) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Dialog");
		alert.setHeaderText(header);
		alert.setContentText(body);
		alert.showAndWait();
		return alert;
	}
	
	/**
	 * Shows a confirmation dialog to the user
	 * @param header
	 * @param body
	 * @return true if the user has pressed "OK", false otherwise
	 */
	public static boolean waitUserReplyForConfirmationDialog(String header, String body) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText(header);
		alert.setContentText(body);

		Optional<ButtonType> result = alert.showAndWait();
		return result.get() == ButtonType.OK;
	}
	
	public static Alert createInformationAlert(String header, String body) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Information");
		alert.setHeaderText(header);
		alert.setContentText(body);
		alert.showAndWait();
		return alert;
	}
}
