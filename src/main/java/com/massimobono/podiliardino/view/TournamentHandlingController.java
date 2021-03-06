package com.massimobono.podiliardino.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.extensibles.dao.DAO;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.I18N;
import com.massimobono.podiliardino.util.Utils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

public class TournamentHandlingController {

	private static final Logger LOG = LogManager.getLogger(TournamentHandlingController.class.getName());

	@FXML
	private TableView<Tournament> tournamentTable;
	@FXML
	private TableColumn<Tournament, String> tournamentNameColumn;
	@FXML
	private TableColumn<Tournament, Integer> tournamentInfoColumn;
	@FXML
	private ListView<Team> availableTeamsList;
	@FXML
	private Label tournamentNameLabel;
	@FXML
	private Label tournamentStartDateLabel;
	@FXML
	private Label tournamentEndDateLabel;
	@FXML
	private Label minimumDaysRequiredLabel;
	@FXML
	private Button addTournament;
	@FXML
	private Button editTournament;
	@FXML
	private Button removeTournament;

	private Main mainApp;
	/**
	 * Contains only the team that you need to display on screen
	 */
	private ObservableList<Team> teamsToDisplay;
	/**
	 * A reference of {@link DAO#getTeamList()}. Used to simplicity
	 */
	private ObservableList<Team> availableTeams;
	/**
	 * An observable list that is listened by a listener object {@link #tournamentParticipationsListener} 
	 */
	private ObservableList<Partecipation> tournamentPartcipations;
	/**
	 * The listener that will notify the {@link #availableTeams} if a new participation has been created
	 * or if a partecipation has been dismissed
	 */
	private ListChangeListener<Partecipation> tournamentParticipationsListener;
	/**
	 * The listener listening to changes inside {@link #availableTeams}. Since {@link #teamsToDisplay} is a totally different list than {@link #availableTeams},
	 * if someone adds a new team, {@link #teamsToDisplay} is not updated. Thanks to this listener, the update is carried till {@link #teamsToDisplay}
	 */
	private ListChangeListener<Team> availableTeamsListener;

	public TournamentHandlingController() {
		this.tournamentPartcipations = FXCollections.observableArrayList();
		this.teamsToDisplay = FXCollections.observableArrayList();
		this.tournamentParticipationsListener = null;
		this.availableTeamsListener = new ListChangeListener<Team>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Team> c) {
				while (c.next()) {
					for (Team t : c.getAddedSubList()) {
						teamsToDisplay.add(t);
					}
					for (Team t : c.getRemoved()) {
						teamsToDisplay.remove(t);
					}
				}
			}
		};
	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
		this.tournamentNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		this.tournamentInfoColumn.setCellValueFactory(cellData -> {
			return cellData.getValue().numberOfPartecipantsProperty().asObject();
		});

		// Listen for selection changes and show the person details when changed.
		this.tournamentTable.getSelectionModel().selectedItemProperty().addListener(this::handleUserSelectTournament);
		
		this.availableTeamsList.setItems(this.teamsToDisplay);

		this.availableTeamsList.setCellFactory(
				new Callback<ListView<Team>, javafx.scene.control.ListCell<Team>>() {
					@Override
					public ListCell<Team> call(ListView<Team> listView) {
						return new ListCell<Team>() {
							@Override
							public void updateItem(Team team, boolean empty) {
								super.updateItem(team,empty);

								if(empty || team == null) {
									setText(null);
									setGraphic(null);
								} else  {
									CheckableListCellViewController data = new CheckableListCellViewController();
									final Tournament tournament = tournamentTable.getSelectionModel().getSelectedItem();
									if (tournament == null) {
										setText(null);
										setGraphic(null);
										return;
									}
									//adds the listener
									data.getCheckBox().setOnAction(e -> {
										Partecipation partecipation = null;
										if (data.getCheckBox().isSelected()) {
											partecipation = new Partecipation(tournament, team);
											tournament.partecipationsProperty().add(partecipation);
											team.partecipationsProperty().add(partecipation);
										} else {
											Optional<Partecipation> op = tournament.partecipationsProperty()
													.parallelStream()
													.filter(p -> {return p.getTeam().get().getId() == team.getId();}).findFirst();
											op.ifPresent( p -> {
												tournament.partecipationsProperty().remove(p);	
												team.partecipationsProperty().remove(p);
											});

										}
									});
									data.getNameLabel().setText(team.nameProperty().get());
									data.getCheckBox().setSelected(tournament.partecipationsProperty()
											.parallelStream()
											.filter(p -> p.getTeam().get() == team)
											.findFirst().isPresent());
									setGraphic(data.getPane());
								}
							}
						};
					}
				});
	}

	public void setup(Main mainApp) throws DAOException {
		this.mainApp = mainApp;

		this.availableTeams = this.mainApp.getDAO().getTeamList().filtered(t -> !t.isSpecial());
		//we first remove the listener in order to avoid listener pollution
		this.availableTeams.removeListener(this.availableTeamsListener);
		this.availableTeams.addListener(this.availableTeamsListener);
		
		
		this.tournamentTable.setItems(this.mainApp.getDAO().getTournamentList());
	}

	@FXML
	private void handleAddTournament() {
		try {
			Optional<Tournament> t = this.mainApp.showCustomDialog(
					"TournamentEditDialog", 
					String.format(I18N.get("new_object"), I18N.get("tournament")), 
					(TournamentEditDialogController c, Stage s) -> {
						try {
							c.setup(s, new Tournament());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TournamentEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTournament() : null);}
					);
			if (t.isPresent()) {
				//we have added a new player. We can add it to the DAO
				this.mainApp.getDAO().add(t.get());
				this.tournamentTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleEditTournament() {
		try {
			if (this.tournamentTable.getSelectionModel().getSelectedItem() == null) {
				return;
			}
			Optional<Tournament> t = this.mainApp.showCustomDialog(
					"TournamentEditDialog", 
					String.format(I18N.get("update_object"), I18N.get("tournament")), 
					(TournamentEditDialogController c, Stage s) -> {
						try {
							c.setup(s, this.tournamentTable.getSelectionModel().getSelectedItem());
						} catch (Exception e){
							e.printStackTrace();
							ExceptionAlert.showAndWait(e);
						}
					},
					(TournamentEditDialogController c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getTournament() : null);}
					);
			if (t.isPresent()) {
				this.mainApp.getDAO().update(t.get());
				this.tournamentTable.getSelectionModel().clearSelection();
			}

		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	@FXML
	private void handleRemoveTournament() {
		try {
			if (this.tournamentTable.getSelectionModel().getSelectedItem() == null) {
				Utils.createDefaultErrorAlert(
						String.format(I18N.get("cant_remove"), I18N.get("tournament")),
						I18N.get("in_order_to_remove_a_tournament_you_need_to_select_one_first"));
				return;
			}
			Tournament t =this.tournamentTable.getSelectionModel().getSelectedItem();
			if (!Utils.waitUserReplyForConfirmationDialog(
					String.format(I18N.get("are_you_sure_yo_delete_tournament"), t.getName()), 
					I18N.get("removing_a_tournament_will_delete_all_its_days_and_the_matches_of_that_tournament_within_the_database_are_you_sure"))){
				return;
			}
			this.mainApp.getDAO().remove(t);
			this.tournamentTable.getSelectionModel().clearSelection();
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}

	private void handleUserSelectTournament(ObservableValue<? extends Tournament> observableValue, Tournament oldValue, Tournament newValue) {
		try {
			if (newValue == null) {
				//if we delete the last item of the list
				this.tournamentNameLabel.setText("");
				this.tournamentStartDateLabel.setText("");
				this.tournamentEndDateLabel.setText("");
				this.minimumDaysRequiredLabel.setText("");
			}else {
				Optional<LocalDate> endDate = newValue.endDateProperty().get();
				this.tournamentNameLabel.setText(newValue.nameProperty().get());
				this.tournamentStartDateLabel.setText(Utils.getStandardDateFrom(newValue.startDateProperty().get()));
				this.tournamentEndDateLabel.setText(endDate.isPresent() ? Utils.getStandardDateFrom(endDate.get()) : Utils.EMPTY_DATE);
				this.minimumDaysRequiredLabel.setText(Integer.toString(this.computeMinimumDaysRequired(newValue.partecipationsProperty())));
				
				this.teamsToDisplay.clear();
				this.teamsToDisplay.addAll(this.availableTeams);
				//we remove the previous listener (if existent). We do thins to avoid polluting the listener field of the tournamentPartecipation
				if (this.tournamentParticipationsListener != null) {
					this.tournamentPartcipations.removeListener(this.tournamentParticipationsListener);
				}
				this.tournamentParticipationsListener = new ListChangeListener<Partecipation>() {
					@Override
					public void onChanged(ListChangeListener.Change<? extends Partecipation> c) {
						minimumDaysRequiredLabel.setText(Integer.toString(computeMinimumDaysRequired(TournamentHandlingController.this.tournamentPartcipations)));
						updateTeamsToDisplayFromPartecipationChange(c);
					}
				};
				this.tournamentPartcipations = this.tournamentTable.getSelectionModel().getSelectedItem().partecipationsProperty();
				this.tournamentPartcipations.addListener(this.tournamentParticipationsListener);
				this.updateTeamsToDisplayFromPartecipationChange(this.tournamentPartcipations, new ArrayList<>());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
	}
	
	private void updateTeamsToDisplayFromPartecipationChange(Collection<? extends Partecipation> added, Collection<? extends Partecipation> removed) {
		final Tournament tournament = tournamentTable.getSelectionModel().getSelectedItem();
		
		LOG.debug("User has added {}", String.join(", ", added.stream().map(p -> p.getTeam().get().toString()).collect(Collectors.toList())));
		LOG.debug("User has removed {}", String.join(", ", removed.stream().map(p -> p.getTeam().get().toString()).collect(Collectors.toList())));
		//new partecipations: remove from the display all the team conflicting with the new added 
		Collection<Team> toRemove = new HashSet<>();
		for (Partecipation newP : added) {
			toRemove.addAll(this.availableTeams
			.stream()
			.filter(t -> !t.equals(newP.getTeam().get()))
			.filter(t -> !t.isPartecipatingIn(tournament))
			.filter(t -> t.containsAnyPlayerOfTeam(newP.getTeam().get()))
			.collect(Collectors.toSet()));
		}
		
		//removed partecipation: add all the teams not in display that were conflicting with the team removed
		Collection<Team> toAdd = new HashSet<>();
		for (Partecipation removedP : removed) {
			toAdd.addAll(this.availableTeams
					.parallelStream()
					.filter(t -> !t.isPartecipatingIn(tournament))
					.filter(t -> t.containsAnyPlayerOfTeam(removedP.getTeam().get()))
					.collect(Collectors.toSet()));
		}
		
		LOG.debug("teams to add: {}", toAdd);
		LOG.debug("teams to remove: {}", toRemove);
		LOG.debug("teams display before removing: {}", Arrays.toString(this.teamsToDisplay.toArray()));
		this.teamsToDisplay.removeAll(toRemove);
		LOG.debug("teams display after removing before adding: {}", Arrays.toString(this.teamsToDisplay.toArray()));
		this.teamsToDisplay.addAll(toAdd.stream().filter(t -> !this.teamsToDisplay.contains(t)).collect(Collectors.toList()));
		LOG.debug("teams display after adding: {}", Arrays.toString(this.teamsToDisplay.toArray()));
	}
	
	private void updateTeamsToDisplayFromPartecipationChange(ListChangeListener.Change<? extends Partecipation> e) {
		while (e.next()) {
			this.updateTeamsToDisplayFromPartecipationChange(e.getAddedSubList(), e.getRemoved());
		}
	}

	/**
	 * 
	 * 
	 * @param partecipations the partecipations of a tournament 
	 * @return the minimum number of days required in order to create teams ranking
	 */
	private int computeMinimumDaysRequired(Collection<Partecipation> partecipations) {
		if (partecipations.size() < 2) {
			return 0;
		}
		return (int) Math.ceil(Math.log(partecipations.size())/Math.log(2));
	}
}
