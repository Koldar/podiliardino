package com.massimobono.podiliardino;
	
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.massimobono.podiliardino.extensibles.dao.DAO;
import com.massimobono.podiliardino.extensibles.dao.DAOException;
import com.massimobono.podiliardino.extensibles.dao.SQLiteDAOImpl;
import com.massimobono.podiliardino.util.ExceptionAlert;
import com.massimobono.podiliardino.util.Utils;
import com.massimobono.podiliardino.view.DayHandlingController;
import com.massimobono.podiliardino.view.PlayerHandlingController;
import com.massimobono.podiliardino.view.TeamHandlingController;
import com.massimobono.podiliardino.view.TournamentHandlingController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;


public class Main extends Application {
	
	private static Logger LOG = LogManager.getLogger(Main.class.getName());
	
	private Stage primaryStage;
	private BorderPane rootScene;
	
	private DAO dao;
	
	public static void main(String[] args) throws IOException {
		launch(args);
	}
	
	
	public Main() throws DAOException {
		this.dao = new SQLiteDAOImpl(new File("data.db"), true);
		//ensure to have a dummy team with 2 dummy players
		Utils.addDummyTeam(this.dao);
		//this.dao.clearAll();
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Podiliardino");
		
		try {
			this.loadAndShowRootLayout();
			this.addMainSceneToStage("PlayerHandling", "Player Management", (PlayerHandlingController c) -> {
				try {
					c.setup(this);
				} catch (Exception e) {
					e.printStackTrace();
					ExceptionAlert.showAndWait(e);
				}
			});
			this.addMainSceneToStage("TeamHandling", "Team Management", (TeamHandlingController c) -> {
				try {
					c.setup(this);
				} catch (Exception e) {
					e.printStackTrace();
					ExceptionAlert.showAndWait(e);
				}
			});
			this.addMainSceneToStage("TournamentHandling", "Tournament Management", (TournamentHandlingController c) -> {
				try {
					c.setup(this);
				} catch (Exception e) {
					e.printStackTrace();
					ExceptionAlert.showAndWait(e);
				}
			});
			this.addMainSceneToStage("DayHandling", "Day Management", (DayHandlingController c) -> {
				try {
					c.setup(this);
				} catch (Exception e) {
					e.printStackTrace();
					ExceptionAlert.showAndWait(e);
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
			ExceptionAlert.showAndWait(e1);
		}
		
		//fetch data from the DAO. Otherwie the DAO won't fetch anything at the beginning
		try {
			LOG.info("Fetching data from DAO...");
			this.dao.getAllPlayers();
			this.dao.getAllTeams();
			this.dao.getAllTournaments();
			LOG.info("DONE");
		} catch (DAOException e) {
			e.printStackTrace();
			ExceptionAlert.showAndWait(e);
		}
		
	}
	
	@Override
	public void stop() throws Exception {
		this.dao.tearDown();
		super.stop();
	}
	
	
	private void loadAndShowRootLayout() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"));
		this.rootScene = loader.load();
		this.rootScene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
		this.primaryStage.setScene(new Scene(this.rootScene));
		this.primaryStage.show();
	}
	
	/**
	 * Add a new tab inside the {@link TabPane} in {@link #rootScene}. Use it to swap scene in your application  
	 * 
	 * @param fxmlFile the FXML file inside "view" package, with or without ".fxml" extension
	 * @param tabName name of the scene attached to the main stage 
	 * @param initializeController a consumer that initialize the controller associated to the "fxml" view. If it's null, no  controller will be <b>loaded</b> at all;
	 * @throws FileNotFoundException if we couldn't fetch the file you requested
	 * @throws IOException if something goes very wrong
	 */
	private <CONTROLLER> void addMainSceneToStage(String fxmlFile, String tabName, Consumer<CONTROLLER> initializeController) throws IOException {
		
		if (!fxmlFile.endsWith(".fxml")) {
			fxmlFile +=".fxml";
		}
		FXMLLoader loader = new FXMLLoader();
		String baseUrl = String.format("view/%s", fxmlFile);
		Optional<URL> resourceURL = Optional.of(this.getClass().getResource(baseUrl));
		if (!resourceURL.isPresent()) {
			throw new FileNotFoundException(String.format("Couldn't find %s", baseUrl));
		} else {
			loader.setLocation(resourceURL.get());
			Tab tab = new Tab(tabName);
			tab.setContent(loader.load());
			((TabPane)this.rootScene.getCenter()).getTabs().add(tab);
		}
		
		if (initializeController != null) {
			CONTROLLER c = loader.getController();
			initializeController.accept(c);
		}
	}
	
	/**
	 * like {@link #setMainTo(String, Consumer)}, but the controller isn't loaded at all
	 * @param fxmlFile
	 * @param tabName
	 * @throws IOException
	 */
	private <CONTROLLER> void setMainTo(String fxmlFile, String tabName) throws IOException {
		this.addMainSceneToStage(fxmlFile, tabName, null);
	}
	
	/**
	 * Show a dialog and waits until the user closes the dialog
	 * 
	 * The dialog is a <b>modal</b> one.
	 * 
	 * <b>This function blocks the program</b>
	 * 
	 * @param fxmlFile the file to fetch the content of the modal from. You may add ".fxml" or not to this parameter
	 * @param dialogTitle the title of the dialog itself
	 * @param initializeController a method telling you what you need to do to initialize the controller. In input you have the controller of the FXML file and the dialog created.
	 * @param returnValueFunction a function called after the user closed the dialog that computes the return value of this function itself
	 * @return the value computed by returnValueFunction
	 * @throws FileNotFoundException if we couldn't fetch the fxml file inside "view" package.
	 * @throws IOException if something goes very wrong 
	 * 
	 */
	public <CONTROLLER, OUTPUT> OUTPUT showCustomDialog(String fxmlFile, String dialogTitle, BiConsumer<CONTROLLER, Stage> initializeController, Function<CONTROLLER, OUTPUT> returnValueFunction) throws IOException {
		if (!fxmlFile.endsWith(".fxml")) {
			fxmlFile +=".fxml";
		}
		FXMLLoader loader = new FXMLLoader();
		String baseUrl = String.format("view/%s", fxmlFile);
		Optional<URL> resourceURL = Optional.of(this.getClass().getResource(baseUrl));
		if (!resourceURL.isPresent()) {
			throw new FileNotFoundException(String.format("Couldn't find %s", baseUrl));
		} else {
			loader.setLocation(resourceURL.get());
			Pane page = loader.load();
			
			// Create the dialog Stage.
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle(dialogTitle);
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initOwner(primaryStage);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        // Initialize the controller of the dialog
	        CONTROLLER c = loader.getController();
	        initializeController.accept(c, dialogStage);
	        // Show the dialog and wait until the user closes it
	        dialogStage.showAndWait();

	        return returnValueFunction.apply(c);
		}
	}
	
	public DAO getDAO() {
		return this.dao;
	}
	
	public Stage getPrimaryStage() {
		return this.primaryStage;
	}
	
}
