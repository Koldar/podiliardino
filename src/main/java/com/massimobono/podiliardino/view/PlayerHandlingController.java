package com.massimobono.podiliardino.view;

import com.massimobono.podiliardino.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PlayerHandlingController {

	@FXML
	private Button newPlayer;
	
	private Main mainApp;
	
	public PlayerHandlingController() {
	}
	
	@FXML
	public void initialize() {
		
	}
	
	@FXML
	public void handleNewPlayer() {
		System.out.println("hello world!");
	}
}
