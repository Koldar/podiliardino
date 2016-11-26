package com.massimobono.podiliardino;
	
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.massimobono.podiliardino.view.PlayerHandlingController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;


public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane rootScene;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public Main() {
		
	}
	
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Podiliardino");
		
		try {
			this.loadAndShowRootLayout();
			this.setMainTo("PlayerHandling", (PlayerHandlingController c) -> {
				c.setMainApp(this);
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	private void loadAndShowRootLayout() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"));
		this.rootScene = (BorderPane)loader.load();
		this.rootScene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
		this.primaryStage.setScene(new Scene(this.rootScene));
		this.primaryStage.show();
	}
	
	/**
	 * Set the scene inside the {@link #rootScene}. Use it to swap scene in your application  
	 * 
	 * @param fxmlFile the FXML file inside "view" package, with or without ".fxml" extension
	 * @param initializeController a consumer that initialize the controller associated to the "fxml" view. If it's null, no  controller will be <b>loaded</b> at all;
	 * @throws FileNotFoundException if we couldn't fetch the file you requested
	 * @throws IOException if something goes very wrong
	 */
	private <CONTROLLER> void setMainTo(String fxmlFile, Consumer<CONTROLLER> initializeController) throws IOException {
		
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
			this.rootScene.setCenter(loader.load());
		}
		
		if (initializeController != null) {
			CONTROLLER c = loader.getController();
			initializeController.accept(c);
		}
	}
	
	/**
	 * like {@link #setMainTo(String, Consumer)}, but the controller isn't loaded at all
	 * @param fxmlFile
	 * @throws IOException
	 */
	private <CONTROLLER> void setMainTo(String fxmlFile) throws IOException {
		this.setMainTo(fxmlFile, null);
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
	
}
