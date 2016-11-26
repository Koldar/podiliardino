package com.massimobono.podiliardino.view;

import java.io.IOException;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.model.Player;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class PlayerHandlingController {

	@FXML
	private Button newPlayer;
	@FXML
	private Button editPlayer;

	private Main mainApp;

	public PlayerHandlingController() {
	}

	@FXML
	public void initialize() {

	}

	@FXML
	public void handleNewPlayer() {
		try {
			this.mainApp.showCustomDialog(
					"PlayerEditDialog", 
					"New Player", 
					(PlayerEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setPlayer(new Player());
					},
					(c) -> {return c.isClickedOK();}
					);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	public void handleEditPlayer() {
		try {
			this.mainApp.showCustomDialog(
					"PlayerEditDialog", 
					"Edit Player", 
					(PlayerEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setPlayer(new Player());
					},
					(c) -> {return c.isClickedOK();}
					);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param mainApp the mainApp to set
	 */
	public void setMainApp(Main mainApp) {
		this.mainApp = mainApp;
	}


}
