package com.massimobono.podiliardino.view;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.extensibles.dummymatch.AddDefaultVictoryDummyMatchHandler;
import com.massimobono.podiliardino.extensibles.dummymatch.DummyMatchHandler;
import com.massimobono.podiliardino.extensibles.matcher.PairComputer;
import com.massimobono.podiliardino.extensibles.matcher.SubsequentPairComputer;
import com.massimobono.podiliardino.extensibles.ranking.CSVRankingFormatter;
import com.massimobono.podiliardino.extensibles.ranking.Formatter;
import com.massimobono.podiliardino.extensibles.ranking.RankingComputer;
import com.massimobono.podiliardino.extensibles.ranking.SwissRankingManager;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.ObservableDistinctList;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Pair;

public class DayHandlingController {
	
	private static final Logger LOG = LogManager.getLogger(DayHandlingController.class);

	@FXML
	private TableView<Tournament> tournamentTableView;
	@FXML
	private TableColumn<Tournament, String> tournamentTableColumn;
	@FXML
	private TableView<Day> dayTableView;
	@FXML
	private TableColumn<Day, Integer> dayTableColumn;
	
	@FXML
	private Button addDay;
	@FXML
	private Button editDay;
	@FXML
	private Button deleteDay;
	
	@FXML
	private Label dayNumberLabel;
	@FXML
	private Label dayDateLabel;
	@FXML
	private Label matchesToDoLabel;
	@FXML
	private Label matchesDoneLabel;
	@FXML
	private TableView<Match> matchesTableView;
	@FXML
	private TableColumn<Match, String> vsTableColumn;
	@FXML
	private TableColumn<Match, String> goalsColumn;
	@FXML
	private TableColumn<Match, String> totalGoalsDifferenceColumn;
	@FXML
	private TableColumn<Match, String> totalOpponentsGoalColumn;
	@FXML
	private TableColumn<Match, String> statusColumn;
	
	
	@FXML
	private Button generateMatch;
	@FXML
	private Button updateMatchResult;
	@FXML
	private Button printRanking;
	
	private Main mainApp;
	
	public DayHandlingController() {
	}
	
	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.tournamentTableView.setItems(this.mainApp.getDAO().getTournamentList());
	}
	
	@FXML
	private void initialize() {
		this.tournamentTableColumn.setCellValueFactory(celldata -> celldata.getValue().getName());
		this.dayTableColumn.setCellValueFactory(celldata -> celldata.getValue().getNumber().asObject());
		
		this.tournamentTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
		this.dayTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectDay);
		
		this.vsTableColumn.setCellValueFactory(celldata -> new SimpleStringProperty(String.format(
				"%s / %s", 
				celldata.getValue().getTeam1().get().getName().get(),
				celldata.getValue().getTeam2().get().getName().get()
		)));
		this.vsTableColumn.setSortable(false);
		this.goalsColumn.setCellValueFactory(celldata -> new SimpleStringProperty(String.format(
				"%d / %d",
				celldata.getValue().getTeam1Goals().get(),
				celldata.getValue().getTeam2Goals().get()
		)));
		this.goalsColumn.setSortable(false);
		this.totalGoalsDifferenceColumn.setCellValueFactory(celldata -> new SimpleStringProperty(String.format(
				"%d / %d",
				celldata.getValue().getTeam1().get().getNumberOfGoalsScored(tournamentTableView.getSelectionModel().getSelectedItem()) -
				celldata.getValue().getTeam1().get().getNumberOfGoalsReceived(tournamentTableView.getSelectionModel().getSelectedItem()),
				celldata.getValue().getTeam2().get().getNumberOfGoalsScored(tournamentTableView.getSelectionModel().getSelectedItem()) -
				celldata.getValue().getTeam2().get().getNumberOfGoalsReceived(tournamentTableView.getSelectionModel().getSelectedItem())
		)));
		this.totalGoalsDifferenceColumn.setSortable(false);
		this.totalOpponentsGoalColumn.setCellValueFactory(celldata -> new SimpleStringProperty(String.format(
				"%d / %d", 
				celldata.getValue().getTeam1().get().getNumberOfGoalsYourOpponentsScored(tournamentTableView.getSelectionModel().getSelectedItem()),
				celldata.getValue().getTeam2().get().getNumberOfGoalsYourOpponentsScored(tournamentTableView.getSelectionModel().getSelectedItem())
		)));
		this.totalOpponentsGoalColumn.setSortable(false);
		
		this.statusColumn.setCellValueFactory(celldata -> new SimpleStringProperty(celldata.getValue().getStatus().get().toString()));
		this.statusColumn.setSortable(false);
	}
	
	@FXML
	private void handleAddDay() {
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't add a new day", "In order to create a new day, a tournament needs to be selected in the current frame");
				return;
			}
			Tournament t = this.tournamentTableView.getSelectionModel().getSelectedItem();
			for (Day d : t.getDays()) {
				if (!d.isDayCompleted()) {
					Utils.createDefaultErrorAlert("Can't add a new day", "In order to create a new day, all previous days of the tournament needs to be completed (no matches are left to run)");
					return;
				}
			}
			
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					"New Day", 
					(DayEditDialogController c, Stage s) -> {
						Day newDay = new Day();
						newDay.getNumber().set(this.tournamentTableView.getSelectionModel().getSelectedItem().getDays().size()+1);
						c.setup(s,newDay); 
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getDay() : null);}
					);
			if (d.isPresent()) {
				//we have added a new day. We copy the tournament from the newly generated team, we flush it, we add the tournament inside the DAO
				//and then we readd the tournament inside it. Why do we do that? because the dao start listening for changes in the tournament object on
				//after addTDay. If we didn't do this procedure the DAO would never know about the new tournament
				Day day = this.mainApp.getDAO().add(d.get());
				day.add(this.tournamentTableView.getSelectionModel().getSelectedItem());
				
				this.dayTableView.getSelectionModel().clearSelection();
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleEditDay() {
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't edit a new day", "In order to edit a day, a tournament needs to be selected in the current frame");
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't edit a new day", "In order to edit a day, a day needs to be selected in the current frame");
				return;
			}
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					"Edit Day", 
					(DayEditDialogController c, Stage s) -> {
						c.setup(s,this.dayTableView.getSelectionModel().getSelectedItem());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getDay() : null);}
					);
			if (d.isPresent()) {
				//we have added a new player. We can add it to the DAO
				this.mainApp.getDAO().update(d.get());
				this.dayTableView.getSelectionModel().clearSelection();
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleRemoveDay() {
		try {
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't remove a day", "In order to remove a day, a tournament needs to be selected in the current frame");
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't remove a day", "In order to remove a day, a day needs to be selected in the current frame");
				return;
			}
			Day d =this.dayTableView.getSelectionModel().getSelectedItem();
			
			if (d.getMatches().size() > 0) {
				if (!Utils.waitUserReplyForConfirmationDialog("Confirm the deletion of the whole day", "By deleting a day, you will be deleting every match (done or todo) in that day as well. Are you sure?")) {
					return;
				}
			}
			
			this.mainApp.getDAO().remove(d);
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			if (newValue == null) {
				//we delete the last item of the list
				this.dayTableView.setItems(FXCollections.emptyObservableList());
			} else {
				this.dayTableView.setItems(newValue.getDays());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void handleUserSelectDay(ObservableValue<? extends Day> observableValue, Day oldValue, Day newValue) {
		try {
			if (newValue == null) {
				//we delete the last item of the list
				this.dayNumberLabel.setText("");
				this.dayDateLabel.setText("");
				this.matchesToDoLabel.setText("");
				this.matchesDoneLabel.setText("");
				
				this.matchesTableView.setItems(FXCollections.emptyObservableList());
			} else {
				this.dayNumberLabel.setText(Integer.toString(newValue.getNumber().get()));
				this.dayDateLabel.setText(Utils.getStandardDateFrom(newValue.getDate().get()));
				this.matchesToDoLabel.setText(Integer.toString(newValue.getNumberOfMatchesToDo()));
				this.matchesDoneLabel.setText(Integer.toString(newValue.getNumberOfMatchesDone()));
				
				this.matchesTableView.setItems(newValue.getMatches());
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	/**
	 * Given a selected day with no matches results yet, the function will create some matches.
	 * Some teams may have no matches in a day. In this case a bye is called
	 */
	@FXML
	private void handleGenerateMatches() {
		try {
			LOG.debug("Starting generate matches");
			if (this.tournamentTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't generate matches", "In order to generate matches you need to select a tournament");
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't generate matches", "In order to generate matches you need to select a day");
				return;
			}
			Tournament tournament = this.tournamentTableView.getSelectionModel().getSelectedItem();
			Day day = this.dayTableView.getSelectionModel().getSelectedItem();
			
			if (tournament.getNumberOfTeams() < 2) {
				Utils.createDefaultErrorAlert("Can't generate matches", "In order to generate matches you need to select a tournament with at least 2 attending teams!");
				return;
			}
			if (day.getNumberOfMatchesDone() > 0) {
				Utils.createDefaultErrorAlert("Can't generate matches", "In order to generate matches you need to select a day with no mathces already done. It would be unfair to generate matches in a day when someone has already played!");
				return;
			}
			
			RankingComputer<Team> rm = new SwissRankingManager();
			//we call the ranking immediately: 
			List<Team> ranks = rm.getDayRanking(day);
			PairComputer<Team> pairComputer = new SubsequentPairComputer<>();
			DummyMatchHandler dummyMatchHandler = new AddDefaultVictoryDummyMatchHandler();
			day.getMatches().clear();
			for (Pair<Team,Team> pair : pairComputer.computePairs(ranks)) {
				if (pair.getValue() != null) {
					day.getMatches().add(new Match(
							pair.getKey(), 
							pair.getValue(),
							day, 
							Utils.DEFAULT_POINTS_EARNED_FROM_WINNING,
							Utils.DEFAULT_POINTS_EARNED_FROM_LOSING, 
							0, 
							0,
							MatchStatus.TODO));
				} else {
					//the second pair is empty. We need to perform an action to establish what will happen to the unpaired team
					dummyMatchHandler.handleUnPairedTeam(day, pair.getKey());
				}
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
		
	}
	
	@FXML
	private void handleUpdateMatchResult() {
		try {
			if (this.matchesTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't change a match result", "In order to change a match result, a match needs to be selected in the current frame");
				return;
			}
			Optional<Match> m = this.mainApp.showCustomDialog(
					"MatchResultEditDialog",
					"Update Match",
					(MatchResultEditDialogController c, Stage s) -> {
						c.setup(s, this.matchesTableView.getSelectionModel().getSelectedItem()); 
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getMatch() : null);}
					);
			if (m.isPresent()) {
				m.get().getDay().get().remove(m.get());
				m.get().getDay().get().add(m.get());
				this.matchesTableView.getSelectionModel().clearSelection();
			}
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
	}
	
	@FXML
	private void printRanking() {
		
		try {
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert("Can't generate ranking", "In order to generate a ranking you need to select a day");
				return;
			}
			
			Day day = this.dayTableView.getSelectionModel().getSelectedItem();
			RankingComputer<Team> rm = new SwissRankingManager();
			List<Team> ranks = rm.getDayRanking(day);
			File outfile = new File("ranking.csv");
			Formatter<List<Team>, File> rf = new CSVRankingFormatter(outfile.getAbsolutePath(), day);
			rf.format(ranks);
			
			Alert alert = Utils.createDefaultErrorAlert("Ranking produced", String.format(
					"The ranking of the day %d of tournament %s has been produced. You can view it at %s", 
					day.getNumber().get(),
					day.getTournament().get().getName().get(),
					outfile.getAbsolutePath()
					));
			alert.showAndWait();
		} catch (Exception e) {
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
		}
		
	}
}
