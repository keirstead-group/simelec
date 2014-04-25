package uk.ac.imperial.simelec.ui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

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
		Label label = new Label(getAboutMessage());
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
	private String getAboutMessage() {
		String template = "SimElec %s\n\n"
				+ "Original version by Ian Richardson and Murray Thomson, Loughborough University\n\n"
				+ "Java implementation by James Keirstead, Imperial College London\n";
		
		String version = getVersion();
		return String.format(template, version);
	}
		
	
	 public String getVersion() {
	    	InputStream is = this.getClass().getResourceAsStream("/version.txt");
	    	try {
	    		BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    		String line = br.readLine();
	    		br.close();
	    		return line;
	    	} catch (Exception e) {
	    		return null;
	    	}
	    	
	    }
}