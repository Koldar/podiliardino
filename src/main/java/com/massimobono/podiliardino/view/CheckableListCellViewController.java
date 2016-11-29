package com.massimobono.podiliardino.view;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class CheckableListCellViewController
{
	@FXML
	private AnchorPane pane;
    @FXML
    private Label nameLabel;
    @FXML
    private CheckBox checkBox;

    public CheckableListCellViewController()
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TeamListCell.fxml"));
        fxmlLoader.setController(this);
        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
   

	/**
	 * @return the pane
	 */
	public AnchorPane getPane() {
		return pane;
	}

	/**
	 * @return the nameLabel
	 */
	public Label getNameLabel() {
		return nameLabel;
	}

	/**
	 * @return the checkBox
	 */
	public CheckBox getCheckBox() {
		return checkBox;
	}
    
    

}
