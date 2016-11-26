package com.massimobono.podiliardino;
	
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
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
			this.setMainTo("PlayerHandling");
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
	
	private void setMainTo(String fxmlFile) throws IOException {
		
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
		
		
	}
	
}
