package com.massimobono.podiliardino.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.extensibles.Formatter;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.extensibles.dummymatch.AddDefaultVictoryDummyMatchHandler;
import com.massimobono.podiliardino.extensibles.dummymatch.DummyMatchHandler;
import com.massimobono.podiliardino.extensibles.matches.DistinctMatchesByeAwarePairComputer;
import com.massimobono.podiliardino.extensibles.matches.PairComputer;
import com.massimobono.podiliardino.extensibles.matches.SimpleCSVMatchesFormatter;
import com.massimobono.podiliardino.extensibles.matches.SubsequentPairComputer;
import com.massimobono.podiliardino.extensibles.ranking.CSVRankingFormatter;
import com.massimobono.podiliardino.extensibles.ranking.RankingComputer;
import com.massimobono.podiliardino.extensibles.ranking.SwissRankingManager;
import com.massimobono.podiliardino.model.Day;
import com.massimobono.podiliardino.model.Match;
import com.massimobono.podiliardino.model.MatchStatus;
import com.massimobono.podiliardino.model.Player;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.I18N;
import com.massimobono.podiliardino.util.ObservableDistinctList;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
	private Button exportRanking;
	@FXML
	private Button exportMatches;
	
	private Main mainApp;
	
	public DayHandlingController() {
	}
	
	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;
		
		this.tournamentTableView.setItems(this.mainApp.getDAO().getTournamentList());
	}
	
	@FXML
	private void initialize() {
		this.tournamentTableColumn.setCellValueFactory(celldata -> celldata.getValue().nameProperty());
		this.dayTableColumn.setCellValueFactory(celldata -> celldata.getValue().numberProperty().asObject());
		
		this.tournamentTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
		this.dayTableView.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectDay);
		
		//double click on the match will automatically call update match result
		this.matchesTableView.setRowFactory( tv -> {
		    TableRow<Match> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		        	this.matchesTableView.getSelectionModel().select(row.getItem());
		        	this.handleUpdateMatchResult();
		        }
		    });
		    return row ;
		});
		
		this.vsTableColumn.setCellValueFactory(celldata -> new SimpleStringProperty(String.format(
				"%s / %s", 
				celldata.getValue().getTeam1().get().nameProperty().get(),
				celldata.getValue().getTeam2().get().nameProperty().get()
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
				Utils.createDefaultErrorAlert(
						I18N.get().getString("cant_add_a_new_day"), 
						I18N.get().getString("in_order_to_create_a_new_day_a_tournament_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			Tournament t = this.tournamentTableView.getSelectionModel().getSelectedItem();
			for (Day d : t.daysProperty()) {
				if (!d.isDayCompleted()) {
					Utils.createDefaultErrorAlert(
							I18N.get().getString("cant_add_a_new_day"),
							I18N.get().getString("in_order_to_create_a_new_dat_all_previous_days_of_the_tournament_needs_to_be_completed_no_matches_are_left_to_run"));
					return;
				}
			}
			
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					I18N.get().getString("new_day"), 
					(DayEditDialogController c, Stage s) -> {
						Day newDay = new Day();
						newDay.numberProperty().set(this.tournamentTableView.getSelectionModel().getSelectedItem().daysProperty().size()+1);
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
				Utils.createDefaultErrorAlert(
						I18N.get().getString("cant_edit_a_new_day"), 
						I18N.get().getString("in_order_to_edit_a_day_a_yournament_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						I18N.get().getString("cant_edit_a_new_day"), 
						I18N.get().getString("in_order_to_edit_a_day_a_day_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			Optional<Day> d = this.mainApp.showCustomDialog(
					"DayEditDialog", 
					I18N.get().getString("edit_day"), 
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
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_remove"), I18N.get().getString("day")),
						I18N.get().getString("in_order_to_remove_a_day_a_tournament_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_remove"), I18N.get().getString("day")),
						I18N.get().getString("in_order_to_remove_a_day_a_day_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			Day d =this.dayTableView.getSelectionModel().getSelectedItem();
			
			if (d.matchesProperty().size() > 0) {
				if (!Utils.waitUserReplyForConfirmationDialog(
						String.format(I18N.get().getString("confirm_the_deletion_of_the_whole"), I18N.get().getString("day")), 
						I18N.get().getString("by_deleting_a_day_you_will_be_deleting_every_match_done_or_todo_in_that_day_as_well_are_you_sure"))) {
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
				this.dayNumberLabel.setText("");
				this.dayDateLabel.setText("");
				this.matchesToDoLabel.setText("");
				this.matchesDoneLabel.setText("");
				//we delete the last item of the list
				this.dayTableView.setItems(FXCollections.emptyObservableList());
			} else {
				this.dayTableView.setItems(newValue.daysProperty());
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
				this.dayNumberLabel.setText(Integer.toString(newValue.numberProperty().get()));
				this.dayDateLabel.setText(Utils.getStandardDateFrom(newValue.dateProperty().get()));
				this.matchesToDoLabel.setText(Integer.toString(newValue.getNumberOfMatchesToDo()));
				this.matchesDoneLabel.setText(Integer.toString(newValue.getNumberOfMatchesDone()));
				
				this.matchesTableView.setItems(newValue.matchesProperty());
				
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
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")), 
						I18N.get().getString("in_order_to_generate_matches_you_need_to_select_a_tournament"));
				return;
			}
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")),
						I18N.get().getString("in_order_to_generate_matches_you_need_to_select_a_day"));
				return;
			}
			Tournament tournament = this.tournamentTableView.getSelectionModel().getSelectedItem();
			Day day = this.dayTableView.getSelectionModel().getSelectedItem();
			
			if (tournament.getNumberOfTeams() < 2) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")),
						I18N.get().getString("in_order_to_generate_matches_you_need_to_select_a_tournbament_with_at_least_2_attending_teams"));
				return;
			}
			if (day.getNumberOfMatchesDone() > 0) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")),
						I18N.get().getString("in_order_to_generate_matches_you_need_to_select_a_day_with_no_matches_already_done_it_would_be_unfair_to_generate_matches_in_a_day_when_someone_has_already_played"));
				return;
			}
			
			RankingComputer<Team> rm = new SwissRankingManager();
			//we call the ranking immediately: 
			List<Team> ranks = rm.getDayRanking(day);
			PairComputer<Team> pairComputer = new DistinctMatchesByeAwarePairComputer<Team>();
			DummyMatchHandler dummyMatchHandler = new AddDefaultVictoryDummyMatchHandler();
			day.matchesProperty().clear();
			
			
			LOG.info("The ranking used to do computation is {}", ranks);
			for (Pair<Team,Team> pair : pairComputer.computePairs(day, ranks)) {
				if (pair.getValue() != null) {
					day.matchesProperty().add(new Match(
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
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_change"), I18N.get().getString("match")),
						I18N.get().getString("in_order_to_change_a_match_result_a_match_needs_to_be_selected_in_the_current_frame"));
				return;
			}
			Optional<Match> m = this.mainApp.showCustomDialog(
					"MatchResultEditDialog",
					String.format(I18N.get().getString("update_object"), I18N.get().getString("match_result")),
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
			LOG.catching(e);
		}
	}
	
	@FXML
	private void exportRanking() {
		
		try {
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("ranking")), 
						I18N.get().getString("in_order_to_generate_a_ranking_you_need_to_select_a_day"));
				return;
			}
			
			Day day = this.dayTableView.getSelectionModel().getSelectedItem();
			RankingComputer<Team> rm = new SwissRankingManager();
			List<Team> ranks = rm.getDayRanking(day);
			
			
			FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(String.format(I18N.get().getString("save_object"), I18N.get().getString("ranking")));
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV", "*.csv"));
            fileChooser.setInitialFileName("ranking.csv");
            File outFile = fileChooser.showSaveDialog(this.mainApp.getPrimaryStage());
            if (outFile != null) {
            	Formatter<List<Team>, File> rf = new CSVRankingFormatter(outFile.getAbsolutePath(), day);
    			rf.format(ranks);
    			
    			Utils.createInformationAlert(
    					String.format(I18N.get().getString("object_produced"), I18N.get().getString("ranking")),
    					String.format(
    					I18N.get().getString("the_ranking_of_the_day_of_tournament_has_been_produced_you_can_view_it_at"), 
    					day.numberProperty().get(),
    					day.tournamentProperty().get().nameProperty().get(),
    					outFile.getAbsolutePath()
    					));
            }
			
		} catch (Exception e) {
			Throwable cause = Utils.getBaseCause(e);
			if (cause.getClass() == FileNotFoundException.class) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("ranking")), 
						cause.getLocalizedMessage());
				return;
			}
			ExceptionAlert.showAndWait(e);
			e.printStackTrace();
			LOG.catching(e);
		}
		
	}
	
	@FXML
	private void exportMatches() {
		try {
			LOG.info("Export matches...");
			if (this.dayTableView.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")), 
						I18N.get().getString("in_order_to_export_the_matches_you_need_to_select_a_day"));
				return;
			}
			Day day = this.dayTableView.getSelectionModel().getSelectedItem();
			if (day.getNumberOfMatchesToDo() == 0){
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")),
						I18N.get().getString("in_order_to_export_matches_you_need_to_select_a_day_with_at_least_one_match_that_needs_to_be_done"));
				return;
			}
			
			FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(String.format(I18N.get().getString("save_object"), I18N.get().getString("matches")));
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.setInitialFileName("matches.csv");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV", "*.csv"));
            File outFile = fileChooser.showSaveDialog(this.mainApp.getPrimaryStage());
            if (outFile != null) {
    			Formatter<Day, File> matchesFormatter = new SimpleCSVMatchesFormatter(outFile.getAbsolutePath());
    			outFile = matchesFormatter.format(day);
    			
    			Utils.createInformationAlert(
    					String.format(I18N.get().getString("object_produced"), I18N.get().getString("matches")),
    					String.format(
    					I18N.get().getString("the_matches_needed_to_do_of_the_day_of_tournament_has_been_produced_you_can_view_it_at"), 
    					day.numberProperty().get(),
    					day.tournamentProperty().get().nameProperty().get(),
    					outFile.getAbsolutePath()
    					));
            }
			
			
		} catch (Exception e) {
			Throwable cause = Utils.getBaseCause(e);
			if (cause.getClass() == FileNotFoundException.class) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get().getString("cant_generate"), I18N.get().getString("matches")),
						cause.getLocalizedMessage());
				return;
			}
			ExceptionAlert.showAndWait(e);
			LOG.catching(e);
			e.printStackTrace();
		}
	}
}
