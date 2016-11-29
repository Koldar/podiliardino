package com.massimobono.podiliardino.view;

import com.massimobono.podiliardino.model.Team;

import javafx.scene.control.ListCell;

public class TeamListCell extends ListCell<Team>
{
	
	
    @Override
    public void updateItem(Team team, boolean empty)
    {
        super.updateItem(team,empty);
        
        if(empty || team == null) {
        	setText(null);
        	setGraphic(null);
        } else  {
            CheckableListCellViewController data = new CheckableListCellViewController();
            data.getNameLabel().setText(team.getName().get());
            data.getCheckBox().setSelected(false);
            setGraphic(data.getPane());
        }
    }
}