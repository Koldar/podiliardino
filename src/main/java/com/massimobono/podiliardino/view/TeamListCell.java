package com.massimobono.podiliardino.view;

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
            	if (data.getCheckBox().isSelected()) {
            		this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().add(new Partecipation(
            				false,
            				this.tournamentTable.getSelectionModel().getSelectedItem(),
            				team));
            	}
            });
            data.getNameLabel().setText(team.getName().get());
			data.getCheckBox().setSelected(this.tournamentTable.getSelectionModel().getSelectedItem().getPartecipations().contains(team));
            setGraphic(data.getPane());
        }
    }
}