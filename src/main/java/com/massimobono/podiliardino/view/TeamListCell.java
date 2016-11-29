package com.massimobono.podiliardino.view;

import java.util.Optional;

import com.massimobono.podiliardino.dao.DAO;
import com.massimobono.podiliardino.dao.DAOException;
import com.massimobono.podiliardino.model.Partecipation;
import com.massimobono.podiliardino.model.Team;
import com.massimobono.podiliardino.model.Tournament;

import javafx.scene.control.ListCell;
import javafx.scene.control.TableView;

public class TeamListCell extends ListCell<Team>
{
	private TableView<Tournament> tournamentTable;
	private DAO dao;
	
	public TeamListCell(TableView<Tournament> tournamentTable, DAO dao) {
		this.tournamentTable = tournamentTable;
		this.dao = dao;
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
            		partecipation = new Partecipation(false, tournament, team);
            		tournament.getPartecipations().add(partecipation);
            		team.getPartecipations().add(partecipation);
            	} else {
            		Optional<Partecipation> op = tournament.getPartecipations()
            		.stream()
            		.parallel()
            		.filter(p -> {return p.getTeam().get().getId() == team.getId();}).findFirst();
            		op.ifPresent( p -> {
            			this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().remove(p);	
            			team.getPartecipations().remove(p);
            		});
            	}
            });
            data.getNameLabel().setText(team.getName().get());
			data.getCheckBox().setSelected(this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().parallelStream().filter(p -> p.getTeam().get() == team).findFirst().isPresent());
            setGraphic(data.getPane());
        }
    }
}