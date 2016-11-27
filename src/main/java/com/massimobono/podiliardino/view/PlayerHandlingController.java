package com.massimobono.podiliardino.view;

import java.io.IOException;
import java.util.Optional;

import com.massimobono.podiliardino.Main;
import com.massimobono.podiliardino.dao.DAOException;
import com.massimobono.podiliardino.model.Player;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class PlayerHandlingController {

	@FXML
	private Button newPlayer;
	@FXML
	private Button editPlayer;
	@FXML
	private TableView<Player> playersTable;
	@FXML
	private TableColumn<Player, String> nameColumn;
	@FXML
	private TableColumn<Player, String> surnameColumn;

	/**
	 * Main application. Used to get all the data related to the software
	 */
	private Main mainApp;
	/**
	 * the list of players to show inside the table
	 */
	private ObservableList<Player> playerList;

	public PlayerHandlingController() {
		this.playerList = FXCollections.observableArrayList();
	}

	@FXML
	private void initialize() {
		// Initialize the person table with the two columns.
        this.nameColumn.setCellValueFactory(cellData -> cellData.getValue().getName());
        this.surnameColumn.setCellValueFactory(cellData -> cellData.getValue().getSurname());
	}
	
	public void setup(Main mainApp) throws DAOException {
		this.setMainApp(mainApp);
		this.playerList.addAll(this.mainApp.getDAO().getAllPlayers());
		this.playersTable.setItems(this.playerList);
	}

	@FXML
	public void handleNewPlayer() {
		try {
			Optional<Player> p = this.mainApp.showCustomDialog(
					"PlayerEditDialog", 
					"New Player", 
					(PlayerEditDialogController c, Stage s) -> {
						c.setDialog(s);
						c.setPlayer(new Player());
					},
					(c) -> {return Optional.ofNullable(c.isClickedOK() ? c.getPlayer() : null);}
					);
			if (p.isPresent()) {
				//we have added a new player. We can add it to the DAO
				Player p1 = this.mainApp.getDAO().addPlayer(p.get());
				this.playerList.add(p1);
			}
			
		} catch (IOException | DAOException e) {
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
