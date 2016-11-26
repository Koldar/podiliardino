package com.massimobono.podiliardino;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	private Stage primaryStage;
	private Scene rootScene;
	
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
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	
	private void loadAndShowRootLayout() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"));
		this.rootScene = new Scene(loader.load());
		this.rootScene.getStylesheets().add(this.getClass().getResource("application.css").toExternalForm());
		this.primaryStage.setScene(this.rootScene);
		this.primaryStage.show();
	}
	
}
