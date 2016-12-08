package com.massimobono.podiliardino.view;

import java.util.Optional;

import com.massimobono.podiliardino.extensibles.dao.DAO;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;

import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableView;

public class TeamListCell extends ListCell<Team>
{
	private TableView<Tournament> tournamentTable;
	private DAO dao;
	private ObservableList<Team> availableTeams;
	
	public TeamListCell(TableView<Tournament> tournamentTable, DAO dao, ObservableList<Team> availableTeams) {
		this.tournamentTable = tournamentTable;
		this.dao = dao;
		this.availableTeams = availableTeams;
	}
	
	
	
    @Override
    public void updateItem(Team team, boolean empty)
    {
        super.updateItem(team,empty);
        
        if(empty || team == null) {
        	setText(null);
        	setGraphic(null);
        } else  {
            CheckableListCellViewController data = new CheckableListCellViewController();
            //adds the listener
            data.getCheckBox().setOnAction(e -> {
            	Tournament tournament = this.tournamentTable.getSelectionModel().getSelectedItem(); 
            	Partecipation partecipation = null;
            	if (data.getCheckBox().isSelected()) {
            		partecipation = new Partecipation(tournament, team);
            		tournament.getPartecipations().add(partecipation);
            		team.partecipationsProperty().add(partecipation);
            		this.availableTeams.removeIf(t -> t.playersProperty().contains(team.playersProperty().get(0)) || t.playersProperty().contains(team.playersProperty().get(1)));
            	} else {
            		Optional<Partecipation> op = tournament.getPartecipations()
            		.stream()
            		.parallel()
            		.filter(p -> {return p.getTeam().get().getId() == team.getId();}).findFirst();
            		op.ifPresent( p -> {
            			this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().remove(p);	
            			team.partecipationsProperty().remove(p);
            		});
            		
            	}
            });
            data.getNameLabel().setText(team.nameProperty().get());
			data.getCheckBox().setSelected(this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().parallelStream().filter(p -> p.getTeam().get() == team).findFirst().isPresent());
            setGraphic(data.getPane());
        }
    }
}