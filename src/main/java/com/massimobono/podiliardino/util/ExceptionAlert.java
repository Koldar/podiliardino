package com.massimobono.podiliardino.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExceptionAlert extends Alert {

	private static final String TITLE = "Exception Dialog";
	private static final String HEADER = "An unhandled exception has occured. Please send an email to the developer pasting the text down here.";
	private static final String STACKTRACE_HEADER = "The exception stacktrace was:";
	
	public ExceptionAlert(Exception ex) {
		super(AlertType.ERROR);
		this.setTitle(TITLE);
		this.setHeaderText(HEADER);
		this.setContentText(ex.getMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label(STACKTRACE_HEADER);

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		this.getDialogPane().setExpandableContent(expContent);
	}
	
	public static void showAndWait(Exception ex) {
		Alert a = new ExceptionAlert(ex);
		a.showAndWait();
	}
}
