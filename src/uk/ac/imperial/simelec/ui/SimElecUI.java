package uk.ac.imperial.simelec.ui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * User interface for SimElec
 * 
 * @author James Keirstead
 * 
 */
public class SimElecUI  extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		Parent root;
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));			
			root = (Parent) loader.load();
			MainForm form = (MainForm) loader.getController();
			form.setStage(primaryStage);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.setTitle("SimElec");
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	} 

}
