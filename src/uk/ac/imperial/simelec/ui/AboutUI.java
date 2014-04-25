package uk.ac.imperial.simelec.ui;

import javafx.scene.Scene;

import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AboutUI extends Application {

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		Label label = new Label(WORDS);
		label.setWrapText(true);
		
		StackPane layout = new StackPane();
		layout.setMaxWidth(200);
		layout.setStyle("-fx-padding: 10;");
		layout.getChildren().setAll(label);

		stage.setTitle("About");
		stage.setScene(new Scene(layout));
		stage.show();
	}

	// creates a triangle.
	private static final String WORDS = "SimElec 0.1.1\n\n"
			+ "Original version by Ian Richardson and Murray Thomson, Loughborough University\n\n"
			+ "Java implementation by James Keirstead, Imperial College London\n";
	
}